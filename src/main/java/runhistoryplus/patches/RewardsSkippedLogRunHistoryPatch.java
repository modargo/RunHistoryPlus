package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.relics.Sozu;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.*;
import runhistoryplus.savables.RewardsSkippedLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RewardsSkippedLogRunHistoryPatch {
    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class RewardsSkippedLogRunDataField {
        @SpireRawPatch
        public static void addRewardsSkipped(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = "public java.util.List rewards_skipped;";

            CtField field = CtField.make(fieldSource, runData);

            runData.addField(field);
        }
    }

    @SpirePatch(
            clz = AbstractDungeon.class,
            method = "generateSeeds"
    )
    public static class GenerateSeedsPatch {
        @SpirePostfixPatch
        public static void initializeRewardsSkipped() {
            RewardsSkippedLog.rewardsSkippedLog = new ArrayList<>();
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "rewards_skipped", RewardsSkippedLog.rewardsSkippedLog);
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = "refreshData")
    public static class RewardsSkippedRefreshDataPatch {
        @SuppressWarnings("unchecked")
        @SpireInsertPatch(locator = Locator.class, localvars = { "data" })
        public static void rewardSkippedRefreshData(RunHistoryScreen __instance, RunData data) throws NoSuchFieldException, IllegalAccessException {
            Field field = data.getClass().getField("rewards_skipped");
            List<LinkedTreeMap<String, Object>> rewards_skipped = (List<LinkedTreeMap<String, Object>>)field.get(data);
            if (rewards_skipped == null) {
                return;
            }
            List<RewardsSkippedLog> l = new ArrayList<>();
            for (LinkedTreeMap<String, Object> ltm : rewards_skipped) {
                RewardsSkippedLog log = new RewardsSkippedLog();
                log.floor = ((Double)ltm.get("floor")).intValue();
                log.relics = (List<String>)ltm.get("relics");
                log.potions = (List<String>)ltm.get("potions");
                l.add(log);
            }
            field.set(data, l);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(Gson.class, "fromJson");
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(RunData.class, "timestamp");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class RewardsSkippedField {
        public static final SpireField<RewardsSkippedLog> rewardsSkipped = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddRewardsSkippedDataPatch {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "floor" })
        public static void addRewardsSkippedData(RunHistoryPath __instance, RunData newData, RunPathElement element, int floor) throws NoSuchFieldException, IllegalAccessException {
            Field field = newData.getClass().getField("rewards_skipped");
            List rewards_skipped = (List)field.get(newData);
            if (rewards_skipped != null) {
                List<RewardsSkippedLog> rewardsSkippedThisFloor = (List<RewardsSkippedLog>)rewards_skipped.stream()
                        .filter(rs -> rs instanceof RewardsSkippedLog)
                        .filter(rs -> ((RewardsSkippedLog)rs).floor == floor)
                        .collect(Collectors.toList());
                if (!rewardsSkippedThisFloor.isEmpty()) {
                    addRewardsSkipped(element, rewardsSkippedThisFloor.get(0));
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.NewExprMatcher(RunPathElement.class);
                Matcher finalMatcher = new Matcher.MethodCallMatcher(List.class, "add");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddBossRelicsSkippedDataPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "floor", "bossRelicChoiceIndex" })
        public static void addRewardsSkippedData(RunHistoryPath __instance, RunData newData, RunPathElement element, int floor, int bossRelicChoiceIndex) {
            List<String> bossRelicsSkipped = newData.boss_relics.get(bossRelicChoiceIndex).not_picked;
            if (bossRelicsSkipped != null) {
                RewardsSkippedLog log = new RewardsSkippedLog();
                log.floor = floor;
                log.relics = bossRelicsSkipped;
                log.potions = new ArrayList<>();
                addRewardsSkipped(element, log);
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(RunData.class, "boss_relics");
                Matcher finalMatcher = new Matcher.MethodCallMatcher(RunPathElement.class, "addRelicObtainStats");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    private static void addRewardsSkipped(RunPathElement element, RewardsSkippedLog log) {
        RewardsSkippedLog existing = RewardsSkippedField.rewardsSkipped.get(element);
        RewardsSkippedLog newLog;
        if (existing == null) {
            newLog = log;
        }
        else {
            newLog = new RewardsSkippedLog();
            newLog.floor = log.floor;
            newLog.relics = Stream.concat(existing.relics.stream(), log.relics.stream()).collect(Collectors.toList());
            newLog.potions = Stream.concat(existing.potions.stream(), log.potions.stream()).collect(Collectors.toList());
        }
        RewardsSkippedField.rewardsSkipped.set(element, newLog);
    }

    @SpirePatch(clz = RewardItem.class, method = "claimReward")
    public static class RewardsSkippedSozuAddLogging {
        @SpirePrefixPatch
        public static void rewardsSkippedSozuAddLogging(RewardItem __instance) {
            if (__instance.type == RewardItem.RewardType.POTION && AbstractDungeon.player.hasRelic(Sozu.ID)) {
                RewardsSkippedLog log = new RewardsSkippedLog();
                log.floor = AbstractDungeon.floorNum;
                log.relics = new ArrayList<>();
                log.potions = new ArrayList<>();
                log.potions.add(__instance.potion.ID);
                addRewardsSkippedLog(log);
            }
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class RewardsSkippedAddLogging {
        @SpirePrefixPatch
        public static void rewardsSkippedAddLogging(AbstractDungeon __instance, SaveFile saveFile) {
            boolean isLoadingSave = CardCrawlGame.loadingSave && saveFile != null;
            if (!isLoadingSave && AbstractDungeon.currMapNode != null && AbstractDungeon.currMapNode.room != null) {
                if (RewardsSkippedLog.rewardsSkippedLog != null) {
                    RewardsSkippedLog log = new RewardsSkippedLog();
                    log.floor = AbstractDungeon.floorNum;
                    log.relics = new ArrayList<>();
                    log.potions = new ArrayList<>();
                    for (RewardItem r : AbstractDungeon.combatRewardScreen.rewards) {
                        if (!r.ignoreReward) {
                            if (r.type == RewardItem.RewardType.RELIC) {
                                log.relics.add(r.relic.relicId);
                            }
                            if (r.type == RewardItem.RewardType.POTION) {
                                log.potions.add(r.potion.ID);
                            }
                        }
                    }
                    if (!log.relics.isEmpty() || !log.potions.isEmpty()) {
                        addRewardsSkippedLog(log);
                    }
                }
            }
        }
    }

    public static void addRewardsSkippedLog(RewardsSkippedLog log) {
        if (RewardsSkippedLog.rewardsSkippedLog != null) {
            List<RewardsSkippedLog> sameFloorLogs = RewardsSkippedLog.rewardsSkippedLog.stream().filter(l -> l.floor == log.floor).collect(Collectors.toList());
            if (sameFloorLogs.size() > 0) {
                RewardsSkippedLog existingLog = sameFloorLogs.get(0);
                existingLog.relics.addAll(log.relics);
                existingLog.potions.addAll(log.potions);
            }
            else {
                RewardsSkippedLog.rewardsSkippedLog.add(log);
            }
        }
    }
}
