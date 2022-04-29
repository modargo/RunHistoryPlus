package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.CardChoiceStats;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.text.MessageFormat;
import java.util.*;

public class MultipleCardRewardsRunHistoryPatch {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:MultipleCardRewards").TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_SINGING_BOWL_CHOICE = TOOLTIP_TEXT[21];
    private static final String TEXT_OBTAIN_TYPE_CARD = TOOLTIP_TEXT[22];
    private static final String TEXT_OBTAIN_TYPE_SPECIAL = TOOLTIP_TEXT[25];

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
                m.get(stats.floor).add(stats);
            }
            CardChoicesByFloorField.cardChoicesByFloor.set(__instance, m);
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

    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class DisplayFullCardChoiceData {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void displayFullCardChoiceData(RunPathElement __instance, StringBuilder sb) {
            List<CardChoiceStats> cardChoices = CardChoicesField.cardChoiceStats.get(__instance);
            if (cardChoices != null) {
                int i = 0;
                for (CardChoiceStats cardChoice : cardChoices) {
                    if (!cardChoice.picked.isEmpty() && !cardChoice.picked.equals("SKIP")) {
                        String text;
                        if (cardChoice.picked.equals("Singing Bowl")) {
                            text = TEXT_OBTAIN_TYPE_SPECIAL + TEXT_SINGING_BOWL_CHOICE;
                        } else {
                            text = TEXT_OBTAIN_TYPE_CARD + CardLibrary.getCardNameFromMetricID(cardChoice.picked);
                        }

                        if (sb.length() > 0) {
                            sb.append(" NL ");
                        }

                        sb.append(" TAB ").append(text);

                        if (i > 0) {
                            sb.append(MessageFormat.format(TEXT[0], i + 1));
                        }
                        i++;
                    }
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(CardChoiceStats.class, "picked");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), matcher);
            }
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class DisplayFullCardSkipData {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb", "showCards" })
        public static void displayFullCardSkipData(RunPathElement __instance, StringBuilder sb, boolean showCards) {
            List<CardChoiceStats> cardChoices = CardChoicesField.cardChoiceStats.get(__instance);
            if (cardChoices != null && cardChoices.stream().anyMatch(cc -> cc.not_picked.size() > 0)) {
                int i = 0;
                for (CardChoiceStats cardChoice : cardChoices) {
                    int j = 0;
                    for (String cardMetricID : cardChoice.not_picked) {
                        String text = CardLibrary.getCardNameFromMetricID(cardMetricID);
                        sb.append(" TAB ").append(TEXT_OBTAIN_TYPE_CARD).append(text);
                        if (i > 0) {
                            sb.append(MessageFormat.format(TEXT[0], i + 1));
                        }
                        if (i < cardChoices.size() - 1 || j < cardChoice.not_picked.size() - 1) {
                            sb.append(" NL ");
                        }
                        j++;
                    }
                    i++;
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(CardChoiceStats.class, "not_picked");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }
}
