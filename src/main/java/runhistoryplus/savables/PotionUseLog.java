package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class PotionUseLog implements CustomSavable<List<List<String>>> {
    public static final String SaveKey = "PotionUseLog";
    public static List<List<String>> potion_use_per_floor;

    @Override
    public List<List<String>> onSave() {
        return PotionUseLog.potion_use_per_floor;
    }

    @Override
    public void onLoad(List<List<String>> potionUseLog) {
        PotionUseLog.potion_use_per_floor = potionUseLog;
    }
}
