package runhistoryplus.subscribers;

import basemod.interfaces.PostPotionUseSubscriber;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import runhistoryplus.savables.PotionUseLog;

import java.util.List;

public class PotionUseAddLoggingSubscriber implements PostPotionUseSubscriber {
    @Override
    public void receivePostPotionUse(AbstractPotion potion) {
        if (PotionUseLog.potion_use_per_floor != null && AbstractDungeon.floorNum > 0) {
            List<String> l = PotionUseLog.potion_use_per_floor.get(PotionUseLog.potion_use_per_floor.size() - 1);
            if (l != null) {
                l.add(potion.ID);
            }
        }
    }
}
