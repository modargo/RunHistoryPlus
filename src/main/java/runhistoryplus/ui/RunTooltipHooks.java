package runhistoryplus.ui;

import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;

public class RunTooltipHooks {
    public static void atEnd(RunPathElement element, StringBuilder sb) {
        FallingOptionsTooltip.build(element, sb);
        ShopContentsTooltip.build(element, sb);
        BlueKeyRelicSkippedTooltip.build(element, sb);
        PotionUseAndDiscardTooltip.build(element, sb);
        FloorExitPlaytimeTooltip.build(element, sb);
    }
}
