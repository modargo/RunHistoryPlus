package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.CardChoiceStats;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class MultipleCardRewardsRunHistoryPatch {
    private static final Logger logger = LogManager.getLogger(MultipleCardRewardsRunHistoryPatch.class.getName());

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class CardChoicesField {
        public static final SpireField<List<CardChoiceStats>> cardChoiceStats = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = SpirePatch.CLASS)
    public static class CardChoicesByFloorField {
        public static final SpireField<Map<Integer, List<CardChoiceStats>>> cardChoicesByFloor = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class CalculateCardChoicesByFloor {
        @SpireInsertPatch(locator = Locator.class)
        public static void calculateCardChoicesByFloor(RunHistoryPath __instance, RunData newData) {
            Map<Integer, List<CardChoiceStats>> m = new HashMap<>();
            for (CardChoiceStats stats : newData.card_choices) {
                if (!m.containsKey(stats.floor)) {
                    m.put(stats.floor, new ArrayList<>());
                }
                List<CardChoiceStats> l = m.get(stats.floor);
                // Occasionally, the game will record two copies of the same card choice decision
                // I believe this to be independent of Run History Plus (i.e. not caused by any of the patches here)
                // To handle this, we assume that duplicate entries are anomalies and not real
                // This could turn out to be invalid (meaning that we don't show something we should have) if some mod
                // out there adds multiple rewards with pre-determined options, or with options from a very very small
                // pool of cards, but this should be good enough for the base game
                if (!duplicateEntryExists(l, stats)) {
                    l.add(stats);
                }
                else {
                    logger.info("Duplicate card_choices entry on floor " + stats.floor);
                }
            }
            CardChoicesByFloorField.cardChoicesByFloor.set(__instance, m);
        }

        private static boolean duplicateEntryExists(List<CardChoiceStats> cardChoices, CardChoiceStats cardChoice) {
            return cardChoices.stream().anyMatch(cc -> cc.floor == cardChoice.floor && cc.picked.equals(cardChoice.picked) && cc.not_picked.equals(cardChoice.not_picked));
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(RunData.class, "card_choices");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddCardChoicesData {
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "floor" })
        public static void addCardChoicesData(RunHistoryPath __instance, RunData newData, RunPathElement element, int floor) {
            Map<Integer, List<CardChoiceStats>> m = CardChoicesByFloorField.cardChoicesByFloor.get(__instance);
            if (m != null && m.containsKey(floor)) {
                CardChoicesField.cardChoiceStats.set(element, m.get(floor));
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
    public static class RemoveExistingCardChoiceAndSkipDisplay {
        public static class RemoveExistingCardChoiceDisplayExprEditor extends ExprEditor {
            private static int callCount = 0;

            @Override
            public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                String className = fieldAccess.getClassName();
                String fieldName = fieldAccess.getFieldName();
                if (className.equals(CardChoiceStats.class.getName()) && fieldName.equals("picked")) {
                    if (callCount == 0) {
                        // For callCount 0, this causes showCards to be true if the full card pick data has any picked cards
                        fieldAccess.replace(String.format("{ $_ = %1$s.hasAnyPickedCards(this) ? \"\" : \"SKIP\"; }", MultipleCardRewardsRunHistoryPatch.class.getName()));
                    }
                    if (callCount == 1) {
                        // This causes the if block that has the existing logic to not execute
                        fieldAccess.replace("{ $_ = \"\"; }");
                    }
                    callCount++;
                }
                if (className.equals(CardChoiceStats.class.getName()) && fieldName.equals("not_picked")) {
                    fieldAccess.replace("{ $_ = new java.util.ArrayList(); }");
                }
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor removeExistingCardChoiceDisplay() {
            return new RemoveExistingCardChoiceDisplayExprEditor();
        }
    }

    public static boolean hasAnyPickedCards(RunPathElement element) {
        List<CardChoiceStats> cardChoices = CardChoicesField.cardChoiceStats.get(element);
        return cardChoices != null && cardChoices.stream().anyMatch(cc -> !cc.picked.equals("SKIP"));
    }
}
