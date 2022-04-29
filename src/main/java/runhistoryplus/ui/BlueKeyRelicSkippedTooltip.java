package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import runhistoryplus.patches.BlueKeyRelicSkippedRunHistoryPatch;
import runhistoryplus.savables.BlueKeyRelicSkippedLog;

public class BlueKeyRelicSkippedTooltip {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:BlueKeyRelicSkipped").TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_OBTAIN_TYPE_RELIC = TOOLTIP_TEXT[23];

    public static void build(RunPathElement element, StringBuilder sb) {
        BlueKeyRelicSkippedLog blueKeyRelicSkippedLog = BlueKeyRelicSkippedRunHistoryPatch.BlueKeyRelicSkippedLogField.blueKeyRelicSkippedLog.get(element);
        if (blueKeyRelicSkippedLog != null) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT[0]);
            sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_RELIC).append(RelicLibrary.getRelic(blueKeyRelicSkippedLog.relicID).name);
        }
    }
}
