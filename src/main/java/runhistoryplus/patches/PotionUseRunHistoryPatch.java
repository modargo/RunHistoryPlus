package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.potions.FairyPotion;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PotionUseRunHistoryPatch {
    private static final Logger logger = LogManager.getLogger(PotionUseRunHistoryPatch.class.getName());
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:PotionUse").TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_OBTAIN_TYPE_POTION = TOOLTIP_TEXT[24];
    
    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class PotionUsePerFloorField {
        @SpireRawPatch
        public static void addPotionUse(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = "public java.util.List potion_use_per_floor;";

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
        public static void initializePotionUsePerFloor() {
            PotionUseLog.potion_use_per_floor = new ArrayList<>();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class NextRoomTransitionAddPotionUseEntryPatch {
        @SpirePrefixPatch
        public static void nextRoomTransitionAddPotionUseEntryPatch(AbstractDungeon __instance, SaveFile saveFile) {
            boolean isLoadingSave = CardCrawlGame.loadingSave && saveFile != null;
            if (!isLoadingSave) {
                if (PotionUseLog.potion_use_per_floor != null) {
                    PotionUseLog.potion_use_per_floor.add(new ArrayList<>());
                }
            }
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "potion_use_per_floor", PotionUseLog.potion_use_per_floor);
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class PotionUseField {
        public static final SpireField<List<String>> potionUse = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddPotionUseDataPatch {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "i" })
        public static void addPotionUseData(RunHistoryPath __instance, RunData newData, RunPathElement element, int i) throws NoSuchFieldException, IllegalAccessException {
            Field field = newData.getClass().getField("potion_use_per_floor");
            List potion_use_per_floor = (List)field.get(newData);
            if (potion_use_per_floor != null && i < potion_use_per_floor.size()) {
                Object potionUse = potion_use_per_floor.get(i);
                if (potionUse instanceof List) {
                    PotionUseField.potionUse.set(element, (List<String>)potionUse);
                }
                else {
                    logger.warn("Unrecognized potion_use_per_floor data");
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
    public static class DisplayPotionUseDataPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void displayPotionUseData(RunPathElement __instance, StringBuilder sb) {
            List<String> potionUse = PotionUseField.potionUse.get(__instance);
            if (potionUse != null && !potionUse.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" NL ");
                }
                sb.append(TEXT[0]);
                for (String potionID : potionUse) {
                    sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
                }
            }
            List<String> potionDiscard = PotionDiscardRunHistoryPatch.PotionDiscardField.potionDiscard.get(__instance);
            if (potionDiscard != null && !potionDiscard.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(" NL ");
                }
                sb.append(TEXT[1]);
                for (String potionID : potionDiscard) {
                    sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(RunPathElement.class, "shopPurchases");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }

    @SpirePatch(clz = FairyPotion.class, method = "use", paramtypez = { AbstractCreature.class })
    public static class FairyPotionUseAddLoggingPatch {
        @SpirePostfixPatch
        public static void fairyPotionUseAddLogging(FairyPotion potion, AbstractCreature target) {
            if (PotionUseLog.potion_use_per_floor != null) {
                PotionUseLog.potion_use_per_floor.get(PotionUseLog.potion_use_per_floor.size() - 1).add(potion.ID);
            }
        }
    }

}
