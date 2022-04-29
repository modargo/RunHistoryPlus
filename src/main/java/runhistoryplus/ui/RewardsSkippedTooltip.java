package runhistoryplus.ui;

import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import runhistoryplus.patches.RewardsSkippedLogRunHistoryPatch;
import runhistoryplus.savables.RewardsSkippedLog;

public class RewardsSkippedTooltip {
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_SKIP_HEADER = TOOLTIP_TEXT[19];
    private static final String TEXT_OBTAIN_TYPE_RELIC = TOOLTIP_TEXT[23];
    private static final String TEXT_OBTAIN_TYPE_POTION = TOOLTIP_TEXT[24];

    public static void build(RunPathElement element, StringBuilder sb) {
        RewardsSkippedLog rewardsSkipped = RewardsSkippedLogRunHistoryPatch.RewardsSkippedField.rewardsSkipped.get(element);
        if (rewardsSkipped != null && (!rewardsSkipped.relics.isEmpty() || !rewardsSkipped.potions.isEmpty())) {
            if (ReflectionHacks.getPrivate(element, RunPathElement.class, "cardChoiceStats") == null) {
                if (sb.length() > 0) {
                    sb.append(" NL ");
                }
                sb.append(TEXT_SKIP_HEADER);
            }
            for (String relicID : rewardsSkipped.relics) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_RELIC).append(RelicLibrary.getRelic(relicID).name);
            }
            for (String potionID : rewardsSkipped.potions) {
                sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
            }
        }
    }
}
