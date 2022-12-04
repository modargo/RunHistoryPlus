package runhistoryplus;

import basemod.BaseMod;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.localization.UIStrings;
import runhistoryplus.panel.RunHistoryPlusModPanel;
import runhistoryplus.savables.*;
import runhistoryplus.subscribers.PotionUseAddLoggingSubscriber;

import static com.megacrit.cardcrawl.core.Settings.GameLanguage;
import static com.megacrit.cardcrawl.core.Settings.language;

@SpireInitializer
public class RunHistoryPlus implements
        PostInitializeSubscriber,
        EditStringsSubscriber {
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
        BaseMod.addSaveField(GreenKeyTakenLog.SaveKey, new GreenKeyTakenLog());
        BaseMod.addSaveField(LessonLearnedLog.SaveKey, new LessonLearnedLog());
        BaseMod.addSaveField(NeowBonusLog.SaveKey, new NeowBonusLog());
        BaseMod.addSaveField(NeowBonusesSkippedLog.SaveKey, new NeowBonusesSkippedLog());
        BaseMod.addSaveField(NeowCostsSkippedLog.SaveKey, new NeowCostsSkippedLog());
        BaseMod.addSaveField(PotionDiscardLog.SaveKey, new PotionDiscardLog());
        BaseMod.addSaveField(PotionUseLog.SaveKey, new PotionUseLog());
        BaseMod.addSaveField(PotionsObtainedAlchemizeLog.SaveKey, new PotionsObtainedAlchemizeLog());
        BaseMod.addSaveField(PotionsObtainedEntropicBrewLog.SaveKey, new PotionsObtainedEntropicBrewLog());
        BaseMod.addSaveField(RewardsSkippedLog.SaveKey, new RewardsSkippedLog());
        BaseMod.addSaveField(ShopContentsLog.SaveKey, new ShopContentsLog());

        BaseMod.subscribe(new PotionUseAddLoggingSubscriber());
    }

    private static String makeLocPath(Settings.GameLanguage language, String filename)
    {
        String ret = "localization/";
        switch (language) {
            case ZHS:
                ret += "zhs";
                break;
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
