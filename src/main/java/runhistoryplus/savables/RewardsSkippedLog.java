package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class RewardsSkippedLog implements CustomSavable<List<RewardsSkippedLog>> {
    public static final String SaveKey = "RewardsSkippedLog";

    public static List<RewardsSkippedLog> rewardsSkippedLog;

    public int floor;
    public List<String> relics;
    public List<String> potions;

    @Override
    public List<RewardsSkippedLog> onSave() {
        return RewardsSkippedLog.rewardsSkippedLog;
    }

    @Override
    public void onLoad(List<RewardsSkippedLog> rewardsSkippedLogs) {
        RewardsSkippedLog.rewardsSkippedLog = rewardsSkippedLogs;
    }
}
