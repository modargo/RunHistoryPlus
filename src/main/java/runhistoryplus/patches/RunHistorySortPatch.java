package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.mainMenu.MenuCancelButton;
import com.megacrit.cardcrawl.screens.options.DropdownMenu;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.stats.RunData;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;
import java.util.Comparator;

public class RunHistorySortPatch {
    private static final UIStrings uiStringsOptions = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:SortOptions");
    private static final UIStrings uiStringsDirection = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:SortDirections");
    private static DropdownMenu sortOptions;
    private static DropdownMenu sortDirection;

    @SpirePatch(clz = RunHistoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class ConstructorPatch {
        @SpirePostfixPatch
        public static void PostFix(RunHistoryScreen __instance){
            sortOptions = new DropdownMenu(__instance, uiStringsOptions.TEXT, FontHelper.cardDescFont_N, Settings.CREAM_COLOR);
            sortDirection = new DropdownMenu(__instance, uiStringsDirection.TEXT, FontHelper.cardDescFont_N, Settings.CREAM_COLOR);
        }
    }

    @SpirePatch(clz= RunHistoryScreen.class, method="update")
    public static class UpdateOpenPatch {
        @SpireInsertPatch(
            locator = Locator.class
        )
        public static SpireReturn<Void> Insert(RunHistoryScreen __instance) {
            if (sortOptions.isOpen) {
                sortOptions.update();
                return SpireReturn.Return();
            }
            if (sortDirection.isOpen) {
                sortDirection.update();
                return SpireReturn.Return();
            }
            return SpireReturn.Continue();
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate (CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(DropdownMenu.class, "update");
                int[] lineNums = LineFinder.findAllInOrder(ctMethodToPatch, finalMatcher);
                return new int[]{lineNums[5]}; // only call this after the fifth instance of DropdownMenu.update
            }
        }
    }

    @SpirePatch(clz= RunHistoryScreen.class, method="update")
    public static class UpdateNotOpenPatch {
        @SpireInsertPatch(
            locator = Locator.class
        )
        public static void Insert(RunHistoryScreen __instance) {
            sortOptions.update();
            sortDirection.update();
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate (CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(MenuCancelButton.class, "update");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz= RunHistoryScreen.class, method="renderFilters")
    public static class RenderFiltersPatch {
        @SpirePostfixPatch
        public static void Render(RunHistoryScreen __instance, SpriteBatch sb, float ___screenX, float ___scrollY) {
            float screenPosX = ___screenX + (1075.0F * Settings.xScale);
            float optionsPosY = ___scrollY + (1000.0F * Settings.yScale) - (54.0F * Settings.yScale);
            float directionPosY = optionsPosY - 65.0F;

            FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, uiStringsDirection.EXTRA_TEXT[0],
                    screenPosX, directionPosY,9999.0F,
                    50.0F * Settings.yScale, Settings.GOLD_COLOR);
            sortDirection.render(sb, screenPosX + (180.0F * Settings.xScale), directionPosY);

            FontHelper.renderSmartText(sb, FontHelper.cardDescFont_N, uiStringsOptions.EXTRA_TEXT[0],
                    screenPosX + (95 * Settings.xScale), optionsPosY, 9999.0F,
                    50.0F * Settings.yScale, Settings.GOLD_COLOR);
            sortOptions.render(sb, screenPosX + (180.0F * Settings.xScale), optionsPosY);
        }
    }

    @SpirePatch(clz= RunHistoryScreen.class, method="resetRunsDropdown")
    public static class FilterRunsPatch {
        private static int compare(int o1, int o2, boolean isDescending){
            if (o1 > o2){
                return isDescending ? -1 : 1;
            } else if (o1 == o2){
                return 0;
            } else {
                return isDescending ? 1 : -1;
            }
        }

        @SpireInsertPatch(
            locator = Locator.class,
            localvars = {"filteredRuns"}
        )
        public static void SortRuns(RunHistoryScreen __instance, ArrayList<RunData> filteredRuns) {
            boolean isDescending = sortDirection.getSelectedIndex() == 0;
            switch (sortOptions.getSelectedIndex()){
                case 0: // date
                    if (!isDescending){
                        filteredRuns.sort(Comparator.comparing(r -> r.timestamp));
                    }
                    break;
                case 1: // score
                    filteredRuns.sort((r1, r2) -> compare(r1.score, r2.score, isDescending));
                    break;
                case 2: // run time
                    filteredRuns.sort((r1, r2) -> compare(r1.playtime, r2.playtime, isDescending));
                    break;
                case 3: // relic count
                    filteredRuns.sort((r1, r2) -> compare(r1.relics.size(), r2.relics.size(), isDescending));
                    break;
                case 4: // card count
                    filteredRuns.sort((r1, r2) -> compare(r1.master_deck.size(), r2.master_deck.size(), isDescending));
                    break;
            }
        }

        private static class Locator extends SpireInsertLocator {
            public int[] Locate (CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.NewExprMatcher(ArrayList.class);
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }

    @SpirePatch(clz= RunHistoryScreen.class, method="changedSelectionTo")
    public static class DropdownUpdatedPatch {
        @SpirePostfixPatch
        public static void dropdownUpdated(RunHistoryScreen __instance, DropdownMenu dropdownMenu, int index, String optionText) {
            if (dropdownMenu == sortOptions || dropdownMenu == sortDirection) {
                ReflectionHacks.privateMethod(RunHistoryScreen.class, "resetRunsDropdown")
                        .invoke(__instance);
            }
        }
    }
}
