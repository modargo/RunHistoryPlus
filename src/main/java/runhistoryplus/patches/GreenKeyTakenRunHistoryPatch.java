package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.*;
import runhistoryplus.savables.GreenKeyTakenLog;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class GreenKeyTakenRunHistoryPatch {
    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class GreenKeyTakenLogRunDataField {
        @SpireRawPatch
        public static void addGreenKeyTakenLog(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = String.format("public %1$s green_key_taken_log;", Integer.class.getName());

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
        public static void initializeGreenKeyTakenLog() {
            GreenKeyTakenLog.greenKeyTakenLog = null;
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "green_key_taken_log", GreenKeyTakenLog.greenKeyTakenLog);
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class GreenKeyTakenLogField {
        public static final SpireField<Boolean> greenKeyTaken = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddGreenKeyTakenDataPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "floor" })
        public static void addGreenKeyTakenData(RunHistoryPath __instance, RunData newData, RunPathElement element, int floor) throws NoSuchFieldException, IllegalAccessException {
            Field field = newData.getClass().getField("green_key_taken_log");
            Integer greenKeyTakenLog = (Integer)field.get(newData);
            GreenKeyTakenLogField.greenKeyTaken.set(element, greenKeyTakenLog != null && greenKeyTakenLog == floor);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.NewExprMatcher(RunPathElement.class);
                Matcher finalMatcher = new Matcher.MethodCallMatcher(List.class, "add");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = RewardItem.class, method = "claimReward")
    public static class GreenKeyTakenAddLogging {
        @SpirePostfixPatch
        public static void greenKeyTakenAddLogging(RewardItem __instance) {
            if (__instance.type == RewardItem.RewardType.EMERALD_KEY) {
                GreenKeyTakenLog.greenKeyTakenLog = AbstractDungeon.floorNum;
            }
        }
    }
}
