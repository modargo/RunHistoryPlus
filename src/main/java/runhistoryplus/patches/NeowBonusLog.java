package runhistoryplus.patches;

import basemod.abstracts.CustomSavable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class NeowBonusLog implements CustomSavable<NeowBonusLog> {
    private static final Logger logger = LogManager.getLogger(NeowBonusLog.class.getName());
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
        logger.info("Loading NeowBonusLog, " + (neowBonusLog == null ? "null" : "not null"));
        if (neowBonusLog != null && !neowBonusLog.relicsObtained.isEmpty()) {
            logger.info("NeowBonusLog relic obtained: " + neowBonusLog.relicsObtained.get(0));
        }

        NeowBonusLog.neowBonusLog = neowBonusLog;
    }
}
