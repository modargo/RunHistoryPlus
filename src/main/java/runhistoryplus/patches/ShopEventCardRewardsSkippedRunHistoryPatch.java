package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.rewards.RewardItem;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.EventRoom;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;

public class ShopEventCardRewardsSkippedRunHistoryPatch {
    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class ShopEventCardRewardsSkippedAddLogging {
        @SpirePrefixPatch
        public static void shopContentsAddLogging(AbstractDungeon __instance, SaveFile saveFile) {
            boolean isLoadingSave = CardCrawlGame.loadingSave && saveFile != null;
            if (!isLoadingSave && AbstractDungeon.currMapNode != null && isRoomWithPotentialCardRewards(AbstractDungeon.currMapNode.room)) {
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

    private static boolean isRoomWithPotentialCardRewards(AbstractRoom room) {
        return room instanceof ShopRoom || room instanceof EventRoom;
    }
}
