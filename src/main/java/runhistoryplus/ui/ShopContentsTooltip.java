package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import runhistoryplus.patches.ShopContentsRunHistoryPatch;
import runhistoryplus.savables.ShopContentsLog;

public class ShopContentsTooltip {
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_SKIP_HEADER = TOOLTIP_TEXT[19];
    private static final String TEXT_OBTAIN_TYPE_CARD = TOOLTIP_TEXT[22];
    private static final String TEXT_OBTAIN_TYPE_RELIC = TOOLTIP_TEXT[23];
    private static final String TEXT_OBTAIN_TYPE_POTION = TOOLTIP_TEXT[24];

    public static void build(RunPathElement element, StringBuilder sb) {
        ShopContentsLog shopContents = ShopContentsRunHistoryPatch.ShopContentsField.shopContents.get(element);
        if (shopContents != null && (!shopContents.cards.isEmpty() || !shopContents.relics.isEmpty() || !shopContents.potions.isEmpty())) {
            if (sb.length() > 0) {
                sb.append(" NL ");
            }
            sb.append(TEXT_SKIP_HEADER);
            for (String relicID : shopContents.relics) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_RELIC).append(RelicLibrary.getRelic(relicID).name);
            }
            for (String cardMetricID : shopContents.cards) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_CARD).append(CardLibrary.getCardNameFromMetricID(cardMetricID));
            }
            for (String potionID : shopContents.potions) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
            }
        }
    }
}
