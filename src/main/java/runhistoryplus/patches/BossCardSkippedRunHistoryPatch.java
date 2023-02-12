package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.ui.buttons.ProceedButton;

@SpirePatch(clz = ProceedButton.class, method = "goToTreasureRoom")
public class BossCardSkippedRunHistoryPatch {
    @SpirePrefixPatch
    public static void logSkippedRewards(ProceedButton __instance) {
        if (!AbstractDungeon.combatRewardScreen.hasTakenAll) {
            for (RewardItem item : AbstractDungeon.combatRewardScreen.rewards) {
                if (item != null && item.type == RewardItem.RewardType.CARD) {
                    item.recordCardSkipMetrics();
                }
            }
        }
    }
}
