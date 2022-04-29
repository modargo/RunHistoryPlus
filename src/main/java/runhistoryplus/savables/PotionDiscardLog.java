package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class PotionDiscardLog implements CustomSavable<List<List<String>>> {
    public static final String SaveKey = "PotionDiscardLog";
    public static List<List<String>> potion_discard_per_floor;

    @Override
    public List<List<String>> onSave() {
        return PotionDiscardLog.potion_discard_per_floor;
    }

    @Override
    public void onLoad(List<List<String>> potion_discard_per_floor) {
        PotionDiscardLog.potion_discard_per_floor = potion_discard_per_floor;
    }
}
