package runhistoryplus.savables;


import basemod.abstracts.CustomSavable;

import java.util.List;

public class FallingOptionsLog implements CustomSavable<List<String>> {
    public static final String SaveKey = "FallingOptionsLog";

    public static List<String> fallingOptionsLog;

    @Override
    public List<String> onSave() {
        return FallingOptionsLog.fallingOptionsLog;
    }

    @Override
    public void onLoad(List<String> fallingOptionsLog) {
        FallingOptionsLog.fallingOptionsLog = fallingOptionsLog;
    }
}
