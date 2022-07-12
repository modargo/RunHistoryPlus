package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

public class GreenKeyTakenLog implements CustomSavable<Integer> {
    public static final String SaveKey = "greenKeyTakenLog";

    public static Integer greenKeyTakenLog;

    @Override
    public Integer onSave() {
        return GreenKeyTakenLog.greenKeyTakenLog;
    }

    @Override
    public void onLoad(Integer greenKeyTakenLog) {
        GreenKeyTakenLog.greenKeyTakenLog = greenKeyTakenLog;
    }
}
