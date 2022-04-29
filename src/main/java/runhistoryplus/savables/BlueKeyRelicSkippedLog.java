package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

public class BlueKeyRelicSkippedLog implements CustomSavable<BlueKeyRelicSkippedLog> {
    public static final String SaveKey = "BlueKeyRelicSkippedLog";

    public static BlueKeyRelicSkippedLog blueKeyRelicSkippedLog;

    public int floor;
    public String relicID;

    @Override
    public BlueKeyRelicSkippedLog onSave() {
        return BlueKeyRelicSkippedLog.blueKeyRelicSkippedLog;
    }

    @Override
    public void onLoad(BlueKeyRelicSkippedLog blueKeyRelicSkippedLog) {
        BlueKeyRelicSkippedLog.blueKeyRelicSkippedLog = blueKeyRelicSkippedLog;
    }
}
