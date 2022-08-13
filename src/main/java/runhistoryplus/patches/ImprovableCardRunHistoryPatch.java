package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.blue.GeneticAlgorithm;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.runHistory.TinyCard;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImprovableCardRunHistoryPatch {
    private static final ArrayList<String> improvableCards = new ArrayList<String>() {{
        add(GeneticAlgorithm.ID);
        add(RitualDagger.ID);
    }};
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:ImprovableCards").TEXT;

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class ImprovableCardsField {
        @SpireRawPatch
        public static void addImprovableCardsField(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());
            String fieldSource = "public java.util.Map improvable_cards;";
            CtField field = CtField.make(fieldSource, runData);
            runData.addField(field);
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            Map<String, List<Integer>> improvableCardsLog = new HashMap<>();
            for (final AbstractCard card: AbstractDungeon.player.masterDeck.group) {
                if (improvableCards.contains(card.cardID)) {
                    String metricID = card.getMetricID();
                    if (!improvableCardsLog.containsKey(metricID)) {
                        improvableCardsLog.put(metricID, new ArrayList<>());
                    }
                    improvableCardsLog.get(metricID).add(card.misc);
                }
            }
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "improvable_cards", improvableCardsLog);
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = "update")
    public static class RunHistoryScreenUpdatePatch {
        @SpireInsertPatch(locator = Locator.class, localvars = "addMe")
        public static void updateCardsWithFinalValues(RunHistoryScreen __instance, RunData ___viewedRun, TinyCard addMe) throws NoSuchFieldException, IllegalAccessException {
            if (improvableCards.contains(addMe.card.cardID)) {
                Field improvableCardsField = ___viewedRun.getClass().getField("improvable_cards");
                Map<String, List<Double>> improvableCardsLog = (Map<String, List<Double>>)improvableCardsField.get(___viewedRun);
                String metricID = addMe.card.getMetricID();
                if (improvableCardsLog != null && improvableCardsLog.containsKey(metricID)) {
                    Integer value = Collections.max(improvableCardsLog.get(metricID)).intValue();
                    addMe.card.baseDamage = value;
                    addMe.card.baseBlock = value;
                    if (!addMe.card.rawDescription.contains(TEXT[0])) {
                        ArrayList<Integer> values = new ArrayList<>();
                        for (Double f : improvableCardsLog.get(metricID)) {
                            values.add(f.intValue());
                        }
                        Collections.sort(values);
                        addMe.card.rawDescription += TEXT[0] + values.toString();
                        System.out.println(addMe.card.rawDescription);
                        addMe.card.initializeDescription();
                        for (int i = 0; i < addMe.card.description.size(); ++i) {
                            System.out.println(addMe.card.description.get(i).text);
                        }
                    }
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(CardGroup.class, "addToTop");
                return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), matcher);
            }
        }
    }

    @SpirePatch(clz = AbstractCard.class, method = "makeStatEquivalentCopy")
    public static class AbstractCardCopyDescriptionPatch {
        @SpirePostfixPatch
        public static AbstractCard copyWithdescription(AbstractCard card, AbstractCard __instance) {
            card.description = __instance.description;
            return card;
        }
    }
}
