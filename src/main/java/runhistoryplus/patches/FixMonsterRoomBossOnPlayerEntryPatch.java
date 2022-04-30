package runhistoryplus.patches;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.metrics.MetricData;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoomBoss;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import java.util.ArrayList;

// MonsterRoomBoss.onPlayerEntry has a subtle bug that adds duplicate entries to CardCrawlGame.metricData.path_taken.
// AbstractRoom.onPlayerEntry is called by AbstractDungeon.nextRoomTransition.
// AbstractDungeon.nextRoomTransition is called both when:
// (1) entering a room for the first time
// (2) loading a post-combat save (some of which are completed events)
// Portions of the code in AbstractDungeon.nextRoomTransition have checks that ensure they only happen based on whether
// we are or aren't loading a post-combat save
// For example, AbstractRelic.onEnterRoom is only called when not loading a post-combat save. So is the code that adds
// an entry to CardCrawlGame.metricData.path_per_floor, the companion array to CardCrawlGame.metricData.path_taken
// However, AbstractRoom.onPlayerEntry is called under every condition except loading a completed event
// This means that if we save and load after combat in a boss room, the logic in MonsterRoomBoss.onPlayerEntry will
// be executed a second time. That logic includes adding an entry to CardCrawlGame.metricData.path_taken for the boss
// room, so we will get a duplicate entry in path_taken, and the logs for the run will be screwed up.
// Fixing this is a bit dicey, since we can't just nuke the method if we're loading a post-combat save (after all,
// some mod out there might have a Postfix patch on it). So we "pass through" (via a SpireField) whether we're loading
// a post-combat save, and have an Instrument patch remove the specific operations that we don't want to happen.
public class FixMonsterRoomBossOnPlayerEntryPatch {
    @SpirePatch(clz = MonsterRoomBoss.class, method = SpirePatch.CLASS)
    public static class IsLoadingPostCombatSaveField {
        public static final SpireField<Boolean> isLoadingPostCombatSave = new SpireField<>(() -> false);
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class PassNextRoomTransitionThrough {
        @SpireInsertPatch(locator = Locator.class, localvars = { "isLoadingPostCombatSave" })
        public static void passNextRoomTransitionThrough(AbstractDungeon __instance, boolean isLoadingPostCombatSave) {
            AbstractRoom room = AbstractDungeon.getCurrRoom();
            if (room instanceof MonsterRoomBoss) {
                IsLoadingPostCombatSaveField.isLoadingPostCombatSave.set(room, isLoadingPostCombatSave);
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(AbstractRoom.class, "onPlayerEntry");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }

    @SpirePatch(clz = MonsterRoomBoss.class, method = "onPlayerEntry")
    public static class FixMonsterRoomBossOnPlayerEntry {
        public static class FixMonsterRoomBossOnPlayerEntryExprEditor extends ExprEditor {
            @Override
            public void edit(FieldAccess fieldAccess) throws CannotCompileException {
                String className = fieldAccess.getClassName();
                String fieldName = fieldAccess.getFieldName();
                if (className.equals(MetricData.class.getName()) && fieldName.equals("path_taken")) {
                    fieldAccess.replace(String.format("{ $_ = %1$s.getPathTaken(this, $0.path_taken); }", FixMonsterRoomBossOnPlayerEntryPatch.class.getName()));
                }
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor fixMonsterRoomBossOnPlayerEntry() {
            return new FixMonsterRoomBossOnPlayerEntryExprEditor();
        }
    }

    public static ArrayList<String> getPathTaken(MonsterRoomBoss room, ArrayList<String> path_taken) {
        // If we are loading a post-combat save, we return a dummy copy of path_taken, so any changes are meaningless
        return IsLoadingPostCombatSaveField.isLoadingPostCombatSave.get(room) ? new ArrayList<>(path_taken) : path_taken;
    }
}

