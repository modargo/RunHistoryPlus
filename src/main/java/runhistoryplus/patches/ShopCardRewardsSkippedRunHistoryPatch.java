package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;

public class ShopCardRewardsSkippedRunHistoryPatch {
    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class ShopCardRewardsSkippedAddLogging {
        @SpirePrefixPatch
        public static void shopContentsAddLogging(AbstractDungeon __instance, SaveFile saveFile) {
            boolean isLoadingSave = CardCrawlGame.loadingSave && saveFile != null;
            if (!isLoadingSave && AbstractDungeon.currMapNode != null && AbstractDungeon.currMapNode.room instanceof ShopRoom) {
                if (!AbstractDungeon.combatRewardScreen.hasTakenAll) {
                    for (RewardItem r : AbstractDungeon.combatRewardScreen.rewards) {
                        if (r.type == RewardItem.RewardType.CARD) {
                            r.recordCardSkipMetrics();
                        }
                    }
                }
            }
        }
    }
}
