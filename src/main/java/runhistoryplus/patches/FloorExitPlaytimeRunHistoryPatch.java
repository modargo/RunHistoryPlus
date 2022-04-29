package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.CharStat;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import runhistoryplus.Config;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FloorExitPlaytimeRunHistoryPatch {
    private static final Logger logger = LogManager.getLogger(FloorExitPlaytimeRunHistoryPatch.class.getName());
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:FloorExitPlaytime").TEXT;

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class FloorExitPlaytimeLogRunDataField {
        @SpireRawPatch
        public static void addFloorExitPlaytime(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = "public java.util.List floor_exit_playtime;";

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
        public static void initializeFloorExitPlaytime() {
            FloorExitPlaytimeLog.floorExitPlaytimeLog = new ArrayList<>();
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "floor_exit_playtime", FloorExitPlaytimeLog.floorExitPlaytimeLog);
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class floorPlaytimeField {
        public static final SpireField<Integer> floorPlaytime = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddFloorExitPlaytimeDataPatch {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "i" })
        public static void addFloorExitPlaytimeData(RunHistoryPath __instance, RunData newData, RunPathElement element, int i) throws NoSuchFieldException, IllegalAccessException {
            Field field = newData.getClass().getField("floor_exit_playtime");
            Object fieldValue = field.get(newData);
            if (fieldValue != null) {
                List<Integer> floor_exit_playtime = ((List<Double>)fieldValue).stream().map(Double::intValue).collect(Collectors.toList());
                if (i < floor_exit_playtime.size() + 1) {
                    Integer currentFloorExit = i < floor_exit_playtime.size() ? floor_exit_playtime.get(i) : newData.playtime;
                    Integer previousFloorExit = i > 0 ? floor_exit_playtime.get(i - 1) : 0;
                    floorPlaytimeField.floorPlaytime.set(element, currentFloorExit - previousFloorExit);
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

    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class DisplayFloorExitPlaytimePatch {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void displayFloorExitPlaytime(RunPathElement __instance, StringBuilder sb) {
            if (Config.timeSpentPerFloor()) {
                Integer floorPlaytime = floorPlaytimeField.floorPlaytime.get(__instance);
                if (floorPlaytime != null) {
                    if (sb.length() > 0) {
                        sb.append(" NL ");
                    }
                    sb.append(MessageFormat.format(TEXT[0], CharStat.formatHMSM((float)floorPlaytime)));
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(RunPathElement.class, "shopPurges");
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(RunPathElement.class, "cachedTooltip");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class FloorExitPlaytimeAddLogging {
        @SpirePrefixPatch
        public static void floorExitPlaytimeAddLogging(AbstractDungeon __instance, SaveFile saveFile) {
            boolean isLoadingSave = CardCrawlGame.loadingSave && saveFile != null;
            if (!isLoadingSave && AbstractDungeon.currMapNode != null && AbstractDungeon.currMapNode.room != null && AbstractDungeon.floorNum != 0) {
                if (FloorExitPlaytimeLog.floorExitPlaytimeLog != null) {
                    FloorExitPlaytimeLog.floorExitPlaytimeLog.add((int)CardCrawlGame.playtime);
                    logger.info("FloorExitPlaytimeLog.floorExitPlaytimeLog (floor " + AbstractDungeon.floorNum + ", entries " + FloorExitPlaytimeLog.floorExitPlaytimeLog.size() + "): " + FloorExitPlaytimeLog.floorExitPlaytimeLog.stream().map(Object::toString).collect(Collectors.joining(", ")));
                }
            }
        }
    }
}
