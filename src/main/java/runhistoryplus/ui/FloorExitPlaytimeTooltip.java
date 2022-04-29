package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.CharStat;
import runhistoryplus.Config;
import runhistoryplus.patches.FloorExitPlaytimeRunHistoryPatch;

import java.text.MessageFormat;

public class FloorExitPlaytimeTooltip {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:FloorExitPlaytime").TEXT;

    public static void build(RunPathElement element, StringBuilder sb) {
        if (Config.timeSpentPerFloor()) {
            Integer floorPlaytime = FloorExitPlaytimeRunHistoryPatch.floorPlaytimeField.floorPlaytime.get(element);
            if (floorPlaytime != null) {
                if (sb.length() > 0) {
                    sb.append(" NL ");
                }
                sb.append(MessageFormat.format(TEXT[0], CharStat.formatHMSM((float)floorPlaytime)));
            }
        }
    }
}
