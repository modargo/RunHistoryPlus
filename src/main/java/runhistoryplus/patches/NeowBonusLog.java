package runhistoryplus.patches;

import basemod.abstracts.CustomSavable;

import java.util.ArrayList;
import java.util.List;

public class NeowBonusLog implements CustomSavable<NeowBonusLog> {
    public static final String SaveKey = "NeowBonusLog";

    public static NeowBonusLog neowBonusLog;

    public final List<String> cardsObtained = new ArrayList<>();
    public final List<String> cardsUpgraded = new ArrayList<>();
    public final List<String> cardsRemoved = new ArrayList<>();
    public final List<String> cardsTransformed = new ArrayList<>();
    public final List<String> relicsObtained = new ArrayList<>();
    public final List<String> potionsObtained = new ArrayList<>();
    public int maxHpGained;
    public int goldGained;

    public int damageTaken;
    public int goldLost;
    public int maxHpLost;

    @Override
    public NeowBonusLog onSave() {
        return NeowBonusLog.neowBonusLog;
    }

    @Override
    public void onLoad(NeowBonusLog neowBonusLog) {
        NeowBonusLog.neowBonusLog = neowBonusLog;
    }
}
