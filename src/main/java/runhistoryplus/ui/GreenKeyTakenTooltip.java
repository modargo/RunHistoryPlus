package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import runhistoryplus.patches.GreenKeyTakenRunHistoryPatch;

public class GreenKeyTakenTooltip {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:GreenKeyTaken").TEXT;

    public static void build(RunPathElement element, StringBuilder sb) {
        Boolean greenKeyTaken = GreenKeyTakenRunHistoryPatch.GreenKeyTakenLogField.greenKeyTaken.get(element);
        if (greenKeyTaken) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT[0]);
        }
    }
}
