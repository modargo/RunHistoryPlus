package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class PotionsObtainedAlchemizeLog implements CustomSavable<List<List<String>>> {
    public static final String SaveKey = "potionsObtainedAlchemizeLog";
    public static List<List<String>> potions_obtained_alchemize;

    @Override
    public List<List<String>> onSave() {
        return PotionsObtainedAlchemizeLog.potions_obtained_alchemize;
    }

    @Override
    public void onLoad(List<List<String>> potionsObtainedAlchemizeLog) {
        PotionsObtainedAlchemizeLog.potions_obtained_alchemize = potionsObtainedAlchemizeLog;
    }
}
