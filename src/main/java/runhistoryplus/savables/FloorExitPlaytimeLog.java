package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class FloorExitPlaytimeLog implements CustomSavable<List<Integer>> {
    public static final String SaveKey = "FloorExitPlaytimeLog";

    public static List<Integer> floorExitPlaytimeLog = null;

    @Override
    public List<Integer> onSave() {
        return FloorExitPlaytimeLog.floorExitPlaytimeLog;
    }

    @Override
    public void onLoad(List<Integer> floorExitPlaytimeLog) {
        FloorExitPlaytimeLog.floorExitPlaytimeLog = floorExitPlaytimeLog;
    }
}
