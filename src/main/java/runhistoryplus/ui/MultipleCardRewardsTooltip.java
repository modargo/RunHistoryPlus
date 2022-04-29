package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.CardChoiceStats;
import runhistoryplus.patches.MultipleCardRewardsRunHistoryPatch;

import java.text.MessageFormat;
import java.util.List;

public class MultipleCardRewardsTooltip {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:MultipleCardRewards").TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_SINGING_BOWL_CHOICE = TOOLTIP_TEXT[21];
    private static final String TEXT_OBTAIN_TYPE_CARD = TOOLTIP_TEXT[22];
    private static final String TEXT_OBTAIN_TYPE_SPECIAL = TOOLTIP_TEXT[25];

    public static void buildChoices(RunPathElement element, StringBuilder sb) {
        List<CardChoiceStats> cardChoices = MultipleCardRewardsRunHistoryPatch.CardChoicesField.cardChoiceStats.get(element);
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

    public static void buildSkips(RunPathElement element, StringBuilder sb) {
        List<CardChoiceStats> cardChoices = MultipleCardRewardsRunHistoryPatch.CardChoicesField.cardChoiceStats.get(element);
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
}
