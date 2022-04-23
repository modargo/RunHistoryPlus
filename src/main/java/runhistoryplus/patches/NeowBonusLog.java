package runhistoryplus.patches;

import basemod.abstracts.CustomSavable;

import java.util.ArrayList;
import java.util.List;

public class NeowBonusLog implements CustomSavable<NeowBonusLog> {
    public static final String SaveKey = "NeowBonusLog";

    public static NeowBonusLog neowBonusLog;

    public List<String> cardsObtained = new ArrayList<>();
    public List<String> cardsUpgraded = new ArrayList<>();
    public List<String> cardsRemoved = new ArrayList<>();
    public List<String> cardsTransformed = new ArrayList<>();
    public List<String> relicsObtained = new ArrayList<>();
    public List<String> potionsObtained = new ArrayList<>();
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
