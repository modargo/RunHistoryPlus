package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.actions.watcher.LessonLearnedAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import runhistoryplus.savables.LessonLearnedLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LessonLearnedRunHistoryPatch {
    private static final Logger logger = LogManager.getLogger(LessonLearnedRunHistoryPatch.class.getName());

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class LessonLearnedPerFloorField {
        @SpireRawPatch
        public static void addLessonLearned(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = "public java.util.List lesson_learned_per_floor;";

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
        public static void initializeLessonLearnedPerFloor() {
            LessonLearnedLog.lesson_learned_per_floor = new ArrayList<>();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class NextRoomTransitionAddLessonLearnedEntryPatch {
        @SpirePrefixPatch
        public static void nextRoomTransitionAddLessonLearnedEntryPatch(AbstractDungeon __instance, SaveFile saveFile) {
            boolean isLoadingSave = CardCrawlGame.loadingSave && saveFile != null;
            if (!isLoadingSave) {
                if (LessonLearnedLog.lesson_learned_per_floor != null) {
                    LessonLearnedLog.lesson_learned_per_floor.add(new ArrayList<>());
                }
            }
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "lesson_learned_per_floor", LessonLearnedLog.lesson_learned_per_floor);
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class LessonLearnedField {
        public static final SpireField<List<String>> lessonLearned = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddLessonLearnedDataPatch {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "i" })
        public static void addLessonLearnedData(RunHistoryPath __instance, RunData newData, RunPathElement element, int i) throws NoSuchFieldException, IllegalAccessException {
            Field field = newData.getClass().getField("lesson_learned_per_floor");
            List lesson_learned_per_floor = (List)field.get(newData);
            if (lesson_learned_per_floor != null && i < lesson_learned_per_floor.size()) {
                Object lessonLearned = lesson_learned_per_floor.get(i);
                if (lessonLearned instanceof List) {
                    LessonLearnedField.lessonLearned.set(element, (List<String>)lessonLearned);
                }
                else {
                    logger.warn("Unrecognized lesson_learned_per_floor data");
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

    @SpirePatch(clz = LessonLearnedAction.class, method = "update")
    public static class LessonLearnedAddLogging {
        @SpireInsertPatch(locator = Locator.class)
        public static void lessonLearnedAddLogging(LessonLearnedAction __instance) {
            AbstractCard card = ReflectionHacks.getPrivate(__instance, LessonLearnedAction.class, "theCard");
            if (LessonLearnedLog.lesson_learned_per_floor != null && card != null) {
                List<String> l = LessonLearnedLog.lesson_learned_per_floor.get(LessonLearnedLog.lesson_learned_per_floor.size() - 1);
                if (l != null) {
                    l.add(card.getMetricID());
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(AbstractCard.class, "upgrade");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }
}
