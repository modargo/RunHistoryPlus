package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.beyond.Falling;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.EventStats;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import runhistoryplus.savables.FallingOptionsLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FallingOptionsRunHistoryPatch {
    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class FallingOptionsLogRunDataField {
        @SpireRawPatch
        public static void addFallingOptionsLog(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = "public java.util.List falling_options_log;";

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
        public static void initializeFallingOptionsLog() {
            FallingOptionsLog.fallingOptionsLog = null;
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "falling_options_log", FallingOptionsLog.fallingOptionsLog);
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class FallingOptionsLogField {
        public static final SpireField<List<String>> fallingOptionsLog = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddFallingOptionsLogDataPatch {
        @SuppressWarnings("unchecked")
        @SpireInsertPatch(locator = Locator.class, localvars = { "element" })
        public static void addFallingOptionsLogData(RunHistoryPath __instance, RunData newData, RunPathElement element) throws NoSuchFieldException, IllegalAccessException {
            EventStats eventStats = ReflectionHacks.getPrivate(element, RunPathElement.class, "eventStats");
            if (eventStats != null && eventStats.event_name.equals(Falling.ID)) {
                Field field = newData.getClass().getField("falling_options_log");
                List<String> fallingOptionsLog = (List<String>)field.get(newData);
                if (fallingOptionsLog != null) {
                    FallingOptionsLogField.fallingOptionsLog.set(element, fallingOptionsLog);
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

    @SpirePatch(clz = Falling.class, method = "buttonEffect")
    public static class FallingOptionsAddLogging {
        public static class FallingOptionsAddLoggingExprEditor extends ExprEditor {
            @Override
            public void edit(MethodCall methodCall) throws CannotCompileException {
                if (methodCall.getClassName().equals(Falling.class.getName()) && methodCall.getMethodName().equals("logMetricCardRemoval")) {
                    methodCall.replace(String.format("{ %1$s.logFallingOptions(this, $3); $proceed($$); }", FallingOptionsAddLogging.class.getName()));
                }
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor fallingOptionsAddLoggingPatch() {
            return new FallingOptionsAddLoggingExprEditor();
        }

        public static void logFallingOptions(Falling fallingEvent, AbstractCard pickedCard) {
            AbstractCard c1 = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "attackCard");
            AbstractCard c2 = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "skillCard");
            AbstractCard c3 = ReflectionHacks.getPrivate(fallingEvent, Falling.class, "powerCard");

            List<String> otherOptions = new ArrayList<>();
            if (c1 != null && c1 != pickedCard) {
                otherOptions.add(c1.getMetricID());
            }
            if (c2 != null && c2 != pickedCard) {
                otherOptions.add(c2.getMetricID());
            }
            if (c3 != null && c3 != pickedCard) {
                otherOptions.add(c3.getMetricID());
            }

            FallingOptionsLog.fallingOptionsLog = otherOptions;
        }
    }
}
