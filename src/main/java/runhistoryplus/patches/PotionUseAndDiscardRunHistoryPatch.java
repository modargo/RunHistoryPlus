package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.actions.common.ObtainPotionAction;
import com.megacrit.cardcrawl.cards.green.Alchemize;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.MetricData;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import com.megacrit.cardcrawl.potions.EntropicBrew;
import com.megacrit.cardcrawl.potions.FairyPotion;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.RunData;
import com.megacrit.cardcrawl.ui.panels.PotionPopUp;
import com.megacrit.cardcrawl.ui.panels.TopPanel;
import com.megacrit.cardcrawl.vfx.ObtainPotionEffect;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import runhistoryplus.savables.PotionDiscardLog;
import runhistoryplus.savables.PotionUseLog;
import runhistoryplus.savables.PotionsObtainedAlchemizeLog;
import runhistoryplus.savables.PotionsObtainedEntropicBrewLog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PotionUseAndDiscardRunHistoryPatch {
    private static final Logger logger = LogManager.getLogger(PotionUseAndDiscardRunHistoryPatch.class.getName());

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

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class PotionDiscardPerFloorField {
        @SpireRawPatch
        public static void addPotionDiscard(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());
            String fieldSource = "public java.util.List potion_discard_per_floor;";
            CtField field = CtField.make(fieldSource, runData);
            runData.addField(field);
        }
    }

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class PotionsObtainedPerFloorAlchemizeField {
        @SpireRawPatch
        public static void addPotionsObtainedAlchemize(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());
            String fieldSource = "public java.util.List potions_obtained_alchemize;";
            CtField field = CtField.make(fieldSource, runData);
            runData.addField(field);
        }
    }

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class PotionsObtainedPerFloorEntropicBrewField {
        @SpireRawPatch
        public static void addPotionsObtainedEntropicBrew(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());
            String fieldSource = "public java.util.List potions_obtained_entropic_brew;";
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
        public static void initializePotionUseAndDiscardPerFloor() {
            PotionUseLog.potion_use_per_floor = new ArrayList<>();
            PotionDiscardLog.potion_discard_per_floor = new ArrayList<>();
            PotionsObtainedAlchemizeLog.potions_obtained_alchemize = new ArrayList<>();
            PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew = new ArrayList<>();
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class NextRoomTransitionAddPotionUseAndDiscardEntryPatch {
        @SpirePrefixPatch
        public static void nextRoomTransitionAddPotionUseAndDiscardEntryPatch(AbstractDungeon __instance, SaveFile saveFile) {
            boolean isLoadingSave = CardCrawlGame.loadingSave && saveFile != null;
            if (!isLoadingSave) {
                if (PotionUseLog.potion_use_per_floor != null) {
                    PotionUseLog.potion_use_per_floor.add(new ArrayList<>());
                }
                if (PotionDiscardLog.potion_discard_per_floor != null) {
                    PotionDiscardLog.potion_discard_per_floor.add(new ArrayList<>());
                }
                if (PotionsObtainedAlchemizeLog.potions_obtained_alchemize != null) {
                    PotionsObtainedAlchemizeLog.potions_obtained_alchemize.add(new ArrayList<>());
                }
                if (PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew != null) {
                    PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew.add(new ArrayList<>());
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
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "potion_discard_per_floor", PotionDiscardLog.potion_discard_per_floor);
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "potions_obtained_alchemize", PotionsObtainedAlchemizeLog.potions_obtained_alchemize);
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "potions_obtained_entropic_brew", PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew);
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class PotionUseField {
        public static final SpireField<List<String>> potionUse = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class PotionDiscardField {
        public static final SpireField<List<String>> potionDiscard = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class PotionsObtainedAlchemizeField {
        public static final SpireField<List<String>> potionsObtainedAlchemize = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class PotionsObtainedEntropicBrewField {
        public static final SpireField<List<String>> potionsObtainedEntropicBrew = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddPotionUseAndDiscardDataPatch {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "i" })
        public static void addPotionUseAndDiscardData(RunHistoryPath __instance, RunData newData, RunPathElement element, int i) throws NoSuchFieldException, IllegalAccessException {
            Field field1 = newData.getClass().getField("potion_use_per_floor");
            List potion_use_per_floor = (List)field1.get(newData);
            if (potion_use_per_floor != null && i < potion_use_per_floor.size()) {
                Object potionUse = potion_use_per_floor.get(i);
                if (potionUse instanceof List) {
                    PotionUseField.potionUse.set(element, (List<String>)potionUse);
                }
                else {
                    logger.warn("Unrecognized potion_use_per_floor data");
                }
            }

            Field field2 = newData.getClass().getField("potion_discard_per_floor");
            List potion_discard_per_floor = (List)field2.get(newData);
            if (potion_discard_per_floor != null && i < potion_discard_per_floor.size()) {
                Object potionDiscard = potion_discard_per_floor.get(i);
                if (potionDiscard instanceof List) {
                    PotionDiscardField.potionDiscard.set(element, (List<String>)potionDiscard);
                }
                else {
                    logger.warn("Unrecognized potion_discard_per_floor data");
                }
            }

            Field field3 = newData.getClass().getField("potions_obtained_alchemize");
            List potions_obtained_alchemize = (List)field3.get(newData);
            if (potions_obtained_alchemize != null && i < potions_obtained_alchemize.size()) {
                Object potionsObtained = potions_obtained_alchemize.get(i);
                if (potionsObtained instanceof List) {
                    PotionsObtainedAlchemizeField.potionsObtainedAlchemize.set(element, (List<String>)potionsObtained);
                }
                else {
                    logger.warn("Unrecognized potions_obtained_alchemize data");
                }
            }

            Field field4 = newData.getClass().getField("potions_obtained_entropic_brew");
            List potions_obtained_entropic_brew = (List)field4.get(newData);
            if (potions_obtained_entropic_brew != null && i < potions_obtained_entropic_brew.size()) {
                Object potionsObtained = potions_obtained_entropic_brew.get(i);
                if (potionsObtained instanceof List) {
                    PotionsObtainedEntropicBrewField.potionsObtainedEntropicBrew.set(element, (List<String>)potionsObtained);
                }
                else {
                    logger.warn("Unrecognized potions_obtained_entropic_brew data");
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

    // See PotionUseAddLoggingSubscriber for that logging

    @SpirePatch(clz = FairyPotion.class, method = "use", paramtypez = { AbstractCreature.class })
    public static class FairyPotionUseAddLoggingPatch {
        @SpirePostfixPatch
        public static void fairyPotionUseAddLogging(FairyPotion potion, AbstractCreature target) {
            if (PotionUseLog.potion_use_per_floor != null && AbstractDungeon.floorNum > 0) {
                PotionUseLog.potion_use_per_floor.get(PotionUseLog.potion_use_per_floor.size() - 1).add(potion.ID);
            }
        }
    }

    @SpirePatch(clz = PotionPopUp.class, method = "updateInput")
    public static class PotionDiscardAddLoggingPatch {
        @SpireInsertPatch(locator = Locator.class)
        public static void potionDiscardAddLogging(PotionPopUp __instance) {
            AbstractPotion potion = ReflectionHacks.getPrivate(__instance, PotionPopUp.class, "potion");
            if (PotionDiscardLog.potion_discard_per_floor != null && AbstractDungeon.floorNum > 0) {
                List<String> l = PotionDiscardLog.potion_discard_per_floor.get(PotionDiscardLog.potion_discard_per_floor.size() - 1);
                if (l != null) {
                    l.add(potion.ID);
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(AbstractPotion.class, "canDiscard");
                Matcher finalMatcher = new Matcher.MethodCallMatcher(TopPanel.class, "destroyPotion");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    public static void obtainPotionAlchemizeOrEntropicBrew(final AbstractPotion potion, String source) {
        switch(source) {
            case "Alchemize":
                if (PotionsObtainedAlchemizeLog.potions_obtained_alchemize != null && AbstractDungeon.floorNum > 0) {
                    PotionsObtainedAlchemizeLog.potions_obtained_alchemize.get(PotionsObtainedAlchemizeLog.potions_obtained_alchemize.size() - 1).add(potion.ID);
                }
                break;
            case "EntropicBrew":
                if (PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew != null && AbstractDungeon.floorNum > 0) {
                    PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew.get(PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew.size() - 1).add(potion.ID);
                }
                break;
        }
    }

    @SpirePatch(clz = ObtainPotionAction.class, method = "update")
    public static class ObtainPotionActionAddLogging {
        public static class ObtainPotionActionAddLoggingExprEditor extends ExprEditor {
            @Override
            public void edit(MethodCall methodCall) throws CannotCompileException {
                if (methodCall.getMethodName().equals("obtainPotion")) {
                    methodCall.replace(String.format("{ $_ = $proceed($$); if ($_) %1$s.obtainPotionAlchemizeOrEntropicBrew(this.potion, (String)%2$s.sourceField.get(this)); }",
                            PotionUseAndDiscardRunHistoryPatch.class.getName(), ActionSourceField.class.getName()));
                }
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor obtainPotionActionAddLoggingPatch() {
            return new ObtainPotionActionAddLoggingExprEditor();
        }
    }

    @SpirePatch(clz = ObtainPotionEffect.class, method = "update")
    public static class ObtainPotionEffectAddLogging {
        public static class ObtainPotionEffectAddLoggingExprEditor extends ExprEditor {
            @Override
            public void edit(MethodCall methodCall) throws CannotCompileException {
                if (methodCall.getMethodName().equals("obtainPotion")) {
                    methodCall.replace(String.format("{ $_ = $proceed($$); if ($_) %1$s.obtainPotionAlchemizeOrEntropicBrew(this.potion, (String)%2$s.sourceField.get(this)); }",
                            PotionUseAndDiscardRunHistoryPatch.class.getName(), EffectSourceField.class.getName()));
                }
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor obtainPotionEffectAddLoggingPatch() {
            return new ObtainPotionEffectAddLoggingExprEditor();
        }
    }

    @SpirePatch(clz = ObtainPotionAction.class, method = SpirePatch.CLASS)
    public static class ActionSourceField {
        public static final SpireField<String> sourceField = new SpireField<>(() -> "");
    }

    @SpirePatch(clz = ObtainPotionEffect.class, method = SpirePatch.CLASS)
    public static class EffectSourceField {
        public static final SpireField<String> sourceField = new SpireField<>(() -> "");
    }

    @SpirePatch(clz = Alchemize.class, method = "use")
    public static class AlchemizeExprEditor extends ExprEditor {
        @Override
        public void edit(NewExpr newExpr) throws CannotCompileException {
            if (newExpr.getClassName().equals(ObtainPotionAction.class.getName())) {
                newExpr.replace(String.format("{ $_ = $proceed($$); %1$s.sourceField.set($_, \"Alchemize\"); }", ActionSourceField.class.getName()));
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor alchemizeExprEditor() {
            return new AlchemizeExprEditor();
        }
    }

    @SpirePatch(clz = EntropicBrew.class, method = "use")
    public static class EntropicBrewExprEditor extends ExprEditor {
        @Override
        public void edit(NewExpr newExpr) throws CannotCompileException {
            if (newExpr.getClassName().equals(ObtainPotionAction.class.getName())) {
                newExpr.replace(String.format("{ $_ = $proceed($$); %1$s.sourceField.set($_, \"EntropicBrew\"); }", ActionSourceField.class.getName()));
            }
            if (newExpr.getClassName().equals(ObtainPotionEffect.class.getName())) {
                newExpr.replace(String.format("{ $_ = $proceed($$); %1$s.sourceField.set($_, \"EntropicBrew\"); }", EffectSourceField.class.getName()));
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor entropicBrewExprEditor() {
            return new EntropicBrewExprEditor();
        }
    }
}
