package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import runhistoryplus.patches.PotionUseAndDiscardRunHistoryPatch;

import java.util.List;

public class PotionUseAndDiscardTooltip {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:PotionUseAndDiscard").TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_OBTAIN_TYPE_POTION = TOOLTIP_TEXT[24];

    public static void build(RunPathElement element, StringBuilder sb) {
        List<String> potionUse = PotionUseAndDiscardRunHistoryPatch.PotionUseField.potionUse.get(element);
        if (potionUse != null && !potionUse.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT[0]);
            for (String potionID : potionUse) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
            }
        }

        List<String> potionDiscard = PotionUseAndDiscardRunHistoryPatch.PotionDiscardField.potionDiscard.get(element);
        if (potionDiscard != null && !potionDiscard.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT[1]);
            for (String potionID : potionDiscard) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
            }
        }

    }
}
