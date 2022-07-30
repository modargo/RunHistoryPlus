package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.blue.GeneticAlgorithm;
import com.megacrit.cardcrawl.cards.colorless.RitualDagger;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.CannotCompileException;
import javassist.NotFoundException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImprovableCardRunHistoryPatch {
    private static Map<String, List<Integer>> improvableCardsLog;
    private static final ArrayList<String> improvableCards = new ArrayList<String>() {{
        add(GeneticAlgorithm.ID);
        add(RitualDagger.ID);
    }};

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
            improvableCardsLog = new HashMap<>();
            for (final AbstractCard card: AbstractDungeon.player.masterDeck.group) {
                if (improvableCards.contains(card.cardID)) {
                    String metricId = card.getMetricID();
                    if (!improvableCardsLog.containsKey(metricId)) {
                        improvableCardsLog.put(metricId, new ArrayList<Integer>());
                    }
                    improvableCardsLog.get(metricId).add(card.misc);
                }
            }
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "improvable_cards", improvableCardsLog);
        }
    }
}
