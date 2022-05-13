package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class NeowCostsSkippedLog implements CustomSavable<List<String>> {
    public final static String SaveKey = "NeowCostsSkippedLog";

    public static List<String> neowCostsSkippedLog;

    @Override
    public List<String> onSave() {
        return NeowCostsSkippedLog.neowCostsSkippedLog;
    }

    @Override
    public void onLoad(List<String> neowBonusesSkippedLog) {
        NeowCostsSkippedLog.neowCostsSkippedLog = neowBonusesSkippedLog;
    }
}
