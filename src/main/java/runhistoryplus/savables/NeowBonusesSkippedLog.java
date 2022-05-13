package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class NeowBonusesSkippedLog implements CustomSavable<List<String>> {
    public final static String SaveKey = "NeowBonusesSkippedLog";

    public static List<String> neowBonusesSkippedLog;

    @Override
    public List<String> onSave() {
        return NeowBonusesSkippedLog.neowBonusesSkippedLog;
    }

    @Override
    public void onLoad(List<String> neowBonusesSkippedLog) {
        NeowBonusesSkippedLog.neowBonusesSkippedLog = neowBonusesSkippedLog;
    }
}
