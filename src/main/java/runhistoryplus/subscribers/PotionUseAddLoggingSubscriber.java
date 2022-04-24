package runhistoryplus.subscribers;

import basemod.interfaces.PostPotionUseSubscriber;
import com.megacrit.cardcrawl.potions.AbstractPotion;
import runhistoryplus.patches.PotionUseLog;

public class PotionUseAddLoggingSubscriber implements PostPotionUseSubscriber {
    @Override
    public void receivePostPotionUse(AbstractPotion potion) {
        PotionUseLog.potion_use_per_floor.get(PotionUseLog.potion_use_per_floor.size() - 1).add(potion.ID);
    }
}
