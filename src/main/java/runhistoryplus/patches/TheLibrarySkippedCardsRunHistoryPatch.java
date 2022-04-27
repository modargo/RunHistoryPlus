package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.TheLibrary;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@SpirePatch(clz = TheLibrary.class, method = "update")
public class TheLibrarySkippedCardsRunHistoryPatch {
    @SpireInsertPatch(locator = Locator.class)
    public static void logSkippedCards (TheLibrary __instance) {
        AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
        List<String> skippedCardIds = AbstractDungeon.gridSelectScreen.targetGroup.group.stream().filter(card -> card != c).map(card -> card.cardID).collect(Collectors.toList());
        HashMap<String, Object> choice = new HashMap<>();
        choice.put("picked", c.cardID);
        choice.put("not_picked", skippedCardIds);
        choice.put("floor", AbstractDungeon.floorNum);
        CardCrawlGame.metricData.card_choices.add(choice);
    }

    public static class Locator extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
            Matcher matcher = new Matcher.MethodCallMatcher(TheLibrary.class, "logMetricObtainCard");
            return LineFinder.findInOrder(ctMethodToPatch, matcher);
        }
    }
}
