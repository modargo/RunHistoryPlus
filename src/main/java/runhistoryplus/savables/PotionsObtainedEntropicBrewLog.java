package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.List;

public class PotionsObtainedEntropicBrewLog implements CustomSavable<List<List<String>>> {
    public static final String SaveKey = "potionsObtainedEntropicBrewLog";
    public static List<List<String>> potions_obtained_entropic_brew;

    @Override
    public List<List<String>> onSave() {
        return PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew;
    }

    @Override
    public void onLoad(List<List<String>> potionsObtainedEntropicBrewLog) {
        PotionsObtainedEntropicBrewLog.potions_obtained_entropic_brew = potionsObtainedEntropicBrewLog;
    }
}
