package runhistoryplus.ui;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import runhistoryplus.patches.LessonLearnedRunHistoryPatch;

import java.util.List;

public class LessonLearnedTooltip {
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:LessonLearned").TEXT;

    public static void build(RunPathElement element, StringBuilder sb) {
        List<String> lessonLearned = LessonLearnedRunHistoryPatch.LessonLearnedField.lessonLearned.get(element);
        if (lessonLearned != null) {
            for (String cardId : lessonLearned) {
                sb.append(" NL ").append(TEXT[0]).append(CardLibrary.getCardNameFromMetricID(cardId));
            }
        }
    }
}
