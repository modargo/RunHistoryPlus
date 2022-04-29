package runhistoryplus.savables;

import basemod.abstracts.CustomSavable;

import java.util.ArrayList;
import java.util.List;

public class ShopContentsLog implements CustomSavable<List<ShopContentsLog>> {
    public static final String SaveKey = "ShopContentsLog";
    public static List<ShopContentsLog> shopContentsLog;

    public int floor;
    public List<String> relics = new ArrayList<>();
    public List<String> cards = new ArrayList<>();
    public List<String> potions = new ArrayList<>();

    @Override
    public List<ShopContentsLog> onSave() {
        return ShopContentsLog.shopContentsLog;
    }

    @Override
    public void onLoad(List<ShopContentsLog> shopContentsLogs) {
        ShopContentsLog.shopContentsLog = shopContentsLogs;
    }
}
