package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.*;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class BlueKeyRelicSkippedRunHistoryPatch {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:BlueKeyRelicSkipped").TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_OBTAIN_TYPE_RELIC = TOOLTIP_TEXT[23];;

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class BlueKeyRelicSkippedLogRunDataField {
        @SpireRawPatch
        public static void addBlueKeyRelicSkippedLog(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = "public runhistoryplus.patches.BlueKeyRelicSkippedLog blue_key_relic_skipped_log;";

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
        public static void initializeBlueKeyRelicSkippedLog() {
            BlueKeyRelicSkippedLog.blueKeyRelicSkippedLog = null;
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "blue_key_relic_skipped_log", BlueKeyRelicSkippedLog.blueKeyRelicSkippedLog);
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class BlueKeyRelicSkippedLogField {
        public static final SpireField<BlueKeyRelicSkippedLog> blueKeyRelicSkippedLog = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddBlueKeyRelicSkippedDataPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "floor" })
        public static void addBlueKeyRelicSkippedData(RunHistoryPath __instance, RunData newData, RunPathElement element, int floor) throws NoSuchFieldException, IllegalAccessException {
            Field field = newData.getClass().getField("blue_key_relic_skipped_log");
            BlueKeyRelicSkippedLog blueKeyRelicSkippedLog = (BlueKeyRelicSkippedLog)field.get(newData);
            if (blueKeyRelicSkippedLog != null && blueKeyRelicSkippedLog.floor == floor) {
                BlueKeyRelicSkippedLogField.blueKeyRelicSkippedLog.set(element, blueKeyRelicSkippedLog);
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

    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class DisplayBlueKeyRelicSkippedDataPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void displayBlueKeyRelicSkippedData(RunPathElement __instance, StringBuilder sb) {
            BlueKeyRelicSkippedLog blueKeyRelicSkippedLog = BlueKeyRelicSkippedLogField.blueKeyRelicSkippedLog.get(__instance);
            if (blueKeyRelicSkippedLog != null) {
                if (sb.length() > 0) {
                    sb.append(" NL ");
                }
                sb.append(TEXT[0]);
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_RELIC).append(RelicLibrary.getRelic(blueKeyRelicSkippedLog.relicID).name);
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(RunPathElement.class, "shopPurges");
                Matcher finalMatcher = new Matcher.MethodCallMatcher(StringBuilder.class, "length");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = RewardItem.class, method = "claimReward")
    public static class BlueKeyRelicSkippedAddLogging {
        @SpirePostfixPatch
        public static void blueKeyRelicSkippedAddLogging(RewardItem __instance) {
            if (__instance.type == RewardItem.RewardType.SAPPHIRE_KEY) {
                BlueKeyRelicSkippedLog log = new BlueKeyRelicSkippedLog();
                log.floor = AbstractDungeon.floorNum;
                log.relicID = __instance.relicLink.relic.relicId;
                BlueKeyRelicSkippedLog.blueKeyRelicSkippedLog = log;
            }
        }
    }
}
