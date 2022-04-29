package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.CardChoiceStats;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import runhistoryplus.ui.RunTooltipHooks;

import java.util.Arrays;
import java.util.Collections;

public class RunTooltipHooksPatch {
    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class ReplacingCardChoices {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void hook(RunPathElement __instance, StringBuilder sb) {
            RunTooltipHooks.replacingCardChoices(__instance, sb);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(CardChoiceStats.class, "picked");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), matcher);
            }
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class ReplacingCardSkips {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void hook(RunPathElement __instance, StringBuilder sb) {
            RunTooltipHooks.replacingCardSkips(__instance, sb);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(CardChoiceStats.class, "not_picked");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class WithSkippedRewardsHook {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void hook(RunPathElement __instance, StringBuilder sb) {
            RunTooltipHooks.withSkippedRewards(__instance, sb);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(RunPathElement.class, "shopPurchases");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }

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
