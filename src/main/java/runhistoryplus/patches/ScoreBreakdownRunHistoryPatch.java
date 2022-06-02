package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.audio.MusicMaster;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.screens.DeathScreen;
import com.megacrit.cardcrawl.screens.GameOverScreen;
import com.megacrit.cardcrawl.screens.GameOverStat;
import com.megacrit.cardcrawl.screens.VictoryScreen;
import com.megacrit.cardcrawl.screens.runHistory.ModIcons;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ScoreBreakdownRunHistoryPatch {
    private static ArrayList<String> scoreBreakdown;
    private static String scoreHeading;

    private static void createScoreBreakdown(ArrayList<GameOverStat> stats) {
        scoreBreakdown = new ArrayList<String>();
        for (GameOverStat stat : stats) {
            if (stat.label == null) {
                break;
            }
            scoreBreakdown.add(stat.label + ": " + stat.value);
        }
    }


    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class ScoreBreakdownField {
        @SpireRawPatch
        public static void addScoreBreakdownField(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());
            String fieldSource = "public java.util.List score_breakdown;";
            CtField field = CtField.make(fieldSource, runData);
            runData.addField(field);
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = "createGameOverStats")
    public static class DeathScreenPatch {
        @SpirePostfixPatch
        public static void logScoreBreakdown(DeathScreen _instance, ArrayList<GameOverStat> ___stats) {
            createScoreBreakdown(___stats);
        }
    }

    @SpirePatch(clz = DeathScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class InsertEarlyStatCalcDeathPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void insertEarlyStatCalcDeath(DeathScreen __instance) {
            // both methods are called twice now, but this shouldn't cause any problems
            ReflectionHacks.privateMethod(GameOverScreen.class, "calculateUnlockProgress").invoke(__instance);
            ReflectionHacks.privateMethod(DeathScreen.class, "createGameOverStats").invoke(__instance);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(MusicMaster.class, "playTempBgmInstantly");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), matcher);
            }
        }
    }

    @SpirePatch(clz = VictoryScreen.class, method = "createGameOverStats")
    public static class VictoryScreenPatch {
        @SpirePostfixPatch
        public static void logScoreBreakdown(VictoryScreen _instance, ArrayList<GameOverStat> ___stats) {
            createScoreBreakdown(___stats);
        }
    }

    @SpirePatch(clz = VictoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class InsertEarlyStatCalcVictoryPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void insertEarlyVictoryStatCalcVictory(VictoryScreen __instance) {
            // both methods are called twice now, but this shouldn't cause any problems
            ReflectionHacks.privateMethod(GameOverScreen.class, "calculateUnlockProgress").invoke(__instance);
            ReflectionHacks.privateMethod(DeathScreen.class, "createGameOverStats").invoke(__instance);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(VictoryScreen.class, "submitVictoryMetrics");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<Matcher>(), matcher);
            }
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "score_breakdown", scoreBreakdown);
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = SpirePatch.CLASS)
    public static class ScoreHitboxField {
        public static final SpireField<Hitbox> scoreHitbox = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class InitializeScoreHitbox {
        @SpirePostfixPatch
        public static void initializeScoreHitbox(RunHistoryScreen __instance) {
            ScoreHitboxField.scoreHitbox.set(__instance, new Hitbox(0, 0));
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = "renderRunHistoryScreen")
    public static class DisplayScoreBreakdown {
        @SpireInsertPatch(locator = Locator.class, localvars = {"header1x", "yOffset", "scoreLineXOffset", "scoreText"})
        public static void displayScoreBreakdown(RunHistoryScreen __instance, SpriteBatch sb, float header1x, @ByRef float[] yOffset, float scoreLineXOffset, String scoreText) {
            scoreHeading = scoreText;
            Hitbox scoreHitbox = ScoreHitboxField.scoreHitbox.get(__instance);
            float width = scoreLineXOffset - header1x;
            scoreHitbox.resize(width, 40.0F);
            scoreHitbox.move(header1x + width / 2.0F, yOffset[0]);
            scoreHitbox.render(sb);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(ModIcons.class, "hasMods");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = "update")
    public static class DisplayScoreBreakdownTooltip {
        @SpirePostfixPatch
        public static void displayScoreBreakdownTooltip(RunHistoryScreen __instance) throws NoSuchFieldException, IllegalAccessException {
            Hitbox hb = ScoreHitboxField.scoreHitbox.get(__instance);
            hb.update();
            String text = getTooltipText(__instance);
            if (hb.hovered && text != null) {
                CardCrawlGame.cursor.changeType(GameCursor.CursorType.INSPECT);
                float tipX = hb.x;
                float tipY = hb.y - 40.0F * Settings.scale;
                TipHelper.renderGenericTip(tipX, tipY, scoreHeading, text);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static String getTooltipText(RunHistoryScreen screen) throws NoSuchFieldException, IllegalAccessException {
        RunData runData = ReflectionHacks.getPrivate(screen, RunHistoryScreen.class, "viewedRun");
        if (runData == null) {
            return null;
        }
        Field scoreBreakdownField = runData.getClass().getField("score_breakdown");
        List<String> scoreBreakdown = (List<String>)scoreBreakdownField.get(runData);
        if (scoreBreakdown == null || scoreBreakdown.isEmpty()) {
            return null;
        }
        return String.join(" NL ", scoreBreakdown);
    }
}
