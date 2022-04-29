package runhistoryplus.panel;

import basemod.ModLabel;
import basemod.ModPanel;
import basemod.ModToggleButton;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import runhistoryplus.Config;

public class RunHistoryPlusModPanel extends ModPanel {
    private static final UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:RunHistoryPlusModPanel");
    private static final String[] TEXT = uiStrings.TEXT;

    private static final float BX = 400.0F;
    private static final float LX = BX + 32.0F;

    public RunHistoryPlusModPanel() {
        float y1 = 700.0f;
        ModToggleButton toggleButton = new ModToggleButton(BX, this.adjustY(y1), Config.timeSpentPerFloor(), true, this, x -> Config.setTimeSpentPerFloor(x.enabled));
        this.addUIElement(toggleButton);
        ModLabel label = new ModLabel(TEXT[0], LX, y1, this, x -> {});
        this.addUIElement(label);
    }

    private float adjustY(float y) {
        return y - 6.0F;
    }
}
