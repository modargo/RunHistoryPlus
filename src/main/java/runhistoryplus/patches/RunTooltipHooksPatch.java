package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import runhistoryplus.ui.RunTooltipHooks;

import java.util.Arrays;

public class RunTooltipHooksPatch {
    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class AtEndHook {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void hook(RunPathElement __instance, StringBuilder sb) {
            RunTooltipHooks.atEnd(__instance, sb);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(RunPathElement.class, "shopPurges");
                Matcher secondMatcher = new Matcher.MethodCallMatcher(CardLibrary.class, "getCardNameFromMetricID");
                Matcher finalMatcher = new Matcher.MethodCallMatcher(StringBuilder.class, "length");
                return LineFinder.findInOrder(ctMethodToPatch, Arrays.asList(matcher, secondMatcher), finalMatcher);
            }
        }
    }
}
