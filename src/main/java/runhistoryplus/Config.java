package runhistoryplus;

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;

import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String TIME_SPENT_PER_FLOOR = "timeSpentPerFloor";
    public static SpireConfig config = null;

    public static boolean timeSpentPerFloor() {
        return config != null && config.getBool(TIME_SPENT_PER_FLOOR);
    }

    public static void setTimeSpentPerFloor(boolean timeSpentPerFloor) {
        if (config != null) {
            config.setBool(TIME_SPENT_PER_FLOOR, timeSpentPerFloor);
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void initialize() {
        try {
            Properties defaults = new Properties();
            defaults.put(TIME_SPENT_PER_FLOOR, Boolean.toString(false));
            config = new SpireConfig("RunHistoryPlus", "Config", defaults);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
