package runhistoryplus;

import basemod.BaseMod;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.UIStrings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import runhistoryplus.patches.*;
import runhistoryplus.subscribers.PotionUseAddLoggingSubscriber;
import runhistoryplus.ui.RunHistoryPlusModPanel;

import static com.megacrit.cardcrawl.core.Settings.GameLanguage;
import static com.megacrit.cardcrawl.core.Settings.language;

@SpireInitializer
public class RunHistoryPlus implements
        PostInitializeSubscriber,
        EditStringsSubscriber {
    private static final Logger logger = LogManager.getLogger(RunHistoryPlus.class.getName());

    public RunHistoryPlus() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new RunHistoryPlus();
        Config.initialize();
    }

    @Override
    public void receivePostInitialize() {
        Texture badgeTexture = new Texture("runhistoryplus/images/RunHistoryPlusBadge.png");
        BaseMod.registerModBadge(badgeTexture, "Run History Plus", "modargo", "Adds additional information to run history.", new RunHistoryPlusModPanel());

        BaseMod.addSaveField(BlueKeyRelicSkippedLog.SaveKey, new BlueKeyRelicSkippedLog());
        BaseMod.addSaveField(FallingOptionsLog.SaveKey, new FallingOptionsLog());
        BaseMod.addSaveField(FloorExitPlaytimeLog.SaveKey, new FloorExitPlaytimeLog());
        BaseMod.addSaveField(NeowBonusLog.SaveKey, new NeowBonusLog());
        BaseMod.addSaveField(PotionUseLog.SaveKey, new PotionUseLog());
        BaseMod.addSaveField(RewardsSkippedLog.SaveKey, new RewardsSkippedLog());
        BaseMod.addSaveField(ShopContentsLog.SaveKey, new ShopContentsLog());

        BaseMod.subscribe(new PotionUseAddLoggingSubscriber());
    }

    private static String makeLocPath(Settings.GameLanguage language, String filename)
    {
        String ret = "localization/";
        switch (language) {
            default:
                ret += "eng";
                break;
        }
        return "runhistoryplus/" + ret + "/" + filename + ".json";
    }

    private void loadLocFiles(GameLanguage language)
    {
        BaseMod.loadCustomStringsFile(UIStrings.class, makeLocPath(language, "RunHistoryPlus-ui"));
    }

    @Override
    public void receiveEditStrings()
    {
        loadLocFiles(GameLanguage.ENG);
        if (language != GameLanguage.ENG) {
            loadLocFiles(language);
        }
    }
}