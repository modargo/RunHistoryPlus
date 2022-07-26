package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import runhistoryplus.patches.PotionRunHistoryPatch;

import java.util.List;

public class PotionUseAndDiscardTooltip {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:PotionUseAndDiscard").TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_OBTAIN_TYPE_POTION = TOOLTIP_TEXT[24];

    public static void build(RunPathElement element, StringBuilder sb) {
        List<String> potionsObtainedAlchemize = PotionRunHistoryPatch.PotionsObtainedAlchemizeField.potionsObtainedAlchemize.get(element);
        if (potionsObtainedAlchemize != null && !potionsObtainedAlchemize.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT[2]);
            for (String potionID : potionsObtainedAlchemize) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
            }
        }

        List<String> potionsObtainedEntropicBrew = PotionRunHistoryPatch.PotionsObtainedEntropicBrewField.potionsObtainedEntropicBrew.get(element);
        if (potionsObtainedEntropicBrew != null && !potionsObtainedEntropicBrew.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT[3]);
            for (String potionID : potionsObtainedEntropicBrew) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
            }
        }

        List<String> potionUse = PotionRunHistoryPatch.PotionUseField.potionUse.get(element);
        if (potionUse != null && !potionUse.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT[0]);
            for (String potionID : potionUse) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
            }
        }

        List<String> potionDiscard = PotionRunHistoryPatch.PotionDiscardField.potionDiscard.get(element);
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
