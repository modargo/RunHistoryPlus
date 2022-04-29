package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import runhistoryplus.patches.FallingOptionsRunHistoryPatch;

import java.util.List;

public class FallingOptionsTooltip {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:FallingOptions").TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_OBTAIN_TYPE_CARD = TOOLTIP_TEXT[22];

    public static void build(RunPathElement element, StringBuilder sb) {
        List<String> fallingOptionsLog = FallingOptionsRunHistoryPatch.FallingOptionsLogField.fallingOptionsLog.get(element);
        if (fallingOptionsLog != null) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT[0]);
            for (String cardMetricID : fallingOptionsLog) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_CARD).append(CardLibrary.getCardNameFromMetricID(cardMetricID));
            }
        }
    }
}
