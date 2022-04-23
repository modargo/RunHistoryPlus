package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.GameCursor;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.*;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.neow.NeowReward;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.stats.CardChoiceStats;
import com.megacrit.cardcrawl.screens.stats.RunData;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndObtainEffect;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class NeowBonusRunHistoryPatch {
    private static final Logger logger = LogManager.getLogger(NeowBonusRunHistoryPatch.class.getName());
    private static final String[] TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPlus:NeowBonus").TEXT;
    private static final CharacterStrings BONUS_STRINGS = CardCrawlGame.languagePack.getCharacterString("Neow Reward");
    private static final String[] BONUS_TEXT = BONUS_STRINGS.TEXT;
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_GOLD_FORMAT = TOOLTIP_TEXT[17];
    private static final String TEXT_OBTAIN_HEADER = TOOLTIP_TEXT[18];
    private static final String TEXT_SKIP_HEADER = TOOLTIP_TEXT[19];
    private static final String TEXT_OBTAIN_TYPE_CARD = TOOLTIP_TEXT[22];
    private static final String TEXT_OBTAIN_TYPE_RELIC = TOOLTIP_TEXT[23];
    private static final String TEXT_OBTAIN_TYPE_POTION = TOOLTIP_TEXT[24];
    private static final String TEXT_REMOVE_OPTION = TOOLTIP_TEXT[28];
    private static final String TEXT_TOOK = TOOLTIP_TEXT[33];
    private static final String TEXT_LOST = TOOLTIP_TEXT[34];
    private static final String TEXT_GENERIC_MAX_HP_FORMAT = TOOLTIP_TEXT[35];
    private static final String TEXT_GAINED = TOOLTIP_TEXT[37];
    private static final String TEXT_EVENT_DAMAGE = TOOLTIP_TEXT[42];
    private static final String TEXT_UPGRADED = TOOLTIP_TEXT[43];
    private static final String TEXT_TRANSFORMED = TOOLTIP_TEXT[44];

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class NeowBonusField {
        @SpireRawPatch
        public static void addNeowBonus(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = String.format("public %1$s neow_bonus_log;", NeowBonusLog.class.getName());

            CtField field = CtField.make(fieldSource, runData);

            runData.addField(field);
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = SpirePatch.CLASS)
    public static class NeowBonusHitboxField {
        public static final SpireField<Hitbox> neowBonusHitbox = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = SpirePatch.CONSTRUCTOR)
    public static class InitializeNeowBonusHitbox {
        @SpirePostfixPatch
        public static void initializeNeowBonusHitbox(RunHistoryScreen __instance) {
            NeowBonusHitboxField.neowBonusHitbox.set(__instance, new Hitbox(0, 0));
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = "renderRunHistoryScreen")
    public static class DisplayNeowBonus {
        @SpireInsertPatch(locator = Locator.class, localvars = { "header2x", "yOffset"})
        public static void displayNeowBonus(RunHistoryScreen __instance, SpriteBatch sb, float header2x, @ByRef float[] yOffset) {
            RunData runData = ReflectionHacks.getPrivate(__instance, RunHistoryScreen.class, "viewedRun");
            String headerText = TEXT[1];
            String neowBonusText = getNeowBonusText(NeowReward.NeowRewardType.valueOf(runData.neow_bonus), NeowReward.NeowRewardDrawback.valueOf(runData.neow_cost));

            if (neowBonusText == null) {
                return;
            }

            ReflectionHacks.RMethod renderSubHeadingWithMessageMethod = ReflectionHacks.privateMethod(RunHistoryScreen.class, "renderSubHeadingWithMessage", SpriteBatch.class, String.class, String.class, float.class, float.class);
            renderSubHeadingWithMessageMethod.invoke(__instance, sb, headerText, neowBonusText, header2x, yOffset[0]);

            Hitbox neowBonusHitbox = NeowBonusHitboxField.neowBonusHitbox.get(__instance);
            float w1 = FontHelper.getSmartWidth(FontHelper.buttonLabelFont, headerText, 99999.0F, 0.0F);
            float w2 = FontHelper.getSmartWidth(FontHelper.cardDescFont_N, neowBonusText, 99999.0F, 0.0F);
            float width = w1 + w2;
            neowBonusHitbox.resize(width, 40.0F);
            neowBonusHitbox.move(header2x + width / 2.0F, yOffset[0]);
            neowBonusHitbox.render(sb);

            ReflectionHacks.RMethod screenPosY = ReflectionHacks.privateMethod(RunHistoryScreen.class, "screenPosY", float.class);
            yOffset[0] = yOffset[0] - (float)screenPosY.invoke(__instance, 40.0F);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(RunHistoryScreen.class, "renderRelics");
                return LineFinder.findInOrder(ctMethodToPatch, matcher);
            }
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = "update")
    public static class DisplayNeowBonusTooltip {
        @SpirePostfixPatch
        public static void displayNeowBonusTooltip(RunHistoryScreen __instance) throws NoSuchFieldException, IllegalAccessException {
            Hitbox hb = NeowBonusHitboxField.neowBonusHitbox.get(__instance);
            hb.update();
            String text = getNeowBlessingDescription(__instance);
            if (hb.hovered && text != null) {
                CardCrawlGame.cursor.changeType(GameCursor.CursorType.INSPECT);
                float tipX = hb.x;
                float tipY = hb.y - 40.0F * Settings.scale;
                String header = TEXT[0];
                TipHelper.renderGenericTip(tipX, tipY, header, text);
            }
        }
    }

    @SpirePatch(clz = NeowReward.class, method = "activate")
    public static class AddLoggingToNeowRewardActivate {

        @SpirePrefixPatch
        public static void setNeowBonusLog(NeowReward __instance) {
            NeowBonusLog.neowBonusLog = new NeowBonusLog();
        }

        public static class AddLoggingToNeowRewardActivateExprEditor extends ExprEditor {
            @Override
            public void edit(MethodCall methodCall) throws CannotCompileException {
                String className = methodCall.getClassName();
                String methodName = methodCall.getMethodName();
                if (className.equals(AbstractRoom.class.getName()) && methodName.equals("spawnRelicAndObtain")) {
                    methodCall.replace(String.format("{ %1$s.logObtainRelic($3); $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
                if (className.equals(AbstractPlayer.class.getName()) && methodName.equals("gainGold")) {
                    methodCall.replace(String.format("{ %1$s.logGainGold($1); $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
                if (className.equals(AbstractPlayer.class.getName()) && methodName.equals("loseGold")) {
                    methodCall.replace(String.format("{ %1$s.logLoseGold($1); $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
                if (className.equals(AbstractPlayer.class.getName()) && methodName.equals("increaseMaxHp")) {
                    methodCall.replace(String.format("{ %1$s.logGainMaxHp($1); $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
                if (className.equals(AbstractPlayer.class.getName()) && methodName.equals("decreaseMaxHealth")) {
                    methodCall.replace(String.format("{ %1$s.logLoseMaxHp($1); $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
            }

            @Override
            public void edit(NewExpr newExpr) throws CannotCompileException {
                String className = newExpr.getClassName();
                if (className.equals(ShowCardAndObtainEffect.class.getName())) {
                    newExpr.replace(String.format("{ %1$s.logObtainCard($1); $_ = $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
                if (className.equals(DamageInfo.class.getName())) {
                    newExpr.replace(String.format("{ %1$s.logTakeDamage($2); $_ = $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor addLoggingToNeowRewardActivatePatch() {
            return new AddLoggingToNeowRewardActivateExprEditor();
        }
    }

    @SpirePatch(clz = NeowReward.class, method = "update")
    public static class AddLoggingToNeowRewardUpdate {
        public static class AddLoggingToNeowRewardUpdateExprEditor extends ExprEditor {
            @Override
            public void edit(MethodCall methodCall) throws CannotCompileException {
                String className = methodCall.getClassName();
                String methodName = methodCall.getMethodName();
                if (className.equals(AbstractCard.class.getName()) && methodName.equals("upgrade")) {
                    methodCall.replace(String.format("{ %1$s.logUpgradeCard($0); $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
                if (className.equals(CardGroup.class.getName()) && methodName.equals("removeCard")) {
                    methodCall.replace(String.format("{ %1$s.logRemoveOrTransformCard($1, this.type); $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
            }

            @Override
            public void edit(NewExpr newExpr) throws CannotCompileException {
                String className = newExpr.getClassName();
                if (className.equals(ShowCardAndObtainEffect.class.getName())) {
                    newExpr.replace(String.format("{ %1$s.logObtainCard($1); $_ = $proceed($$); }", NeowBonusRunHistoryPatch.class.getName()));
                }
            }
        }

        @SpireInstrumentPatch
        public static ExprEditor addLoggingToNeowRewardUpdatePatch() {
            return new AddLoggingToNeowRewardUpdateExprEditor();
        }
    }

    public static void logObtainCard(AbstractCard card) {
        NeowBonusLog.neowBonusLog.cardsObtained.add(card.cardID);
    }

    public static void logObtainRelic(AbstractRelic relic) {
        NeowBonusLog.neowBonusLog.relicsObtained.add(relic.relicId);
    }

    public static void logGainGold(int gold) {
        NeowBonusLog.neowBonusLog.goldGained = gold;
    }

    public static void logLoseGold(int gold) {
        NeowBonusLog.neowBonusLog.goldLost = gold;
    }

    public static void logGainMaxHp(int maxHp) {
        NeowBonusLog.neowBonusLog.maxHpGained = maxHp;
    }

    public static void logLoseMaxHp(int maxHp) {
        NeowBonusLog.neowBonusLog.maxHpLost = maxHp;
    }

    public static void logTakeDamage(int damage) {
        NeowBonusLog.neowBonusLog.damageTaken = damage;
    }

    public static void logUpgradeCard(AbstractCard card) {
        NeowBonusLog.neowBonusLog.cardsUpgraded.add(card.cardID);
    }

    public static void logRemoveOrTransformCard(AbstractCard card, NeowReward.NeowRewardType rewardType) {
        if (rewardType == NeowReward.NeowRewardType.REMOVE_CARD || rewardType == NeowReward.NeowRewardType.REMOVE_TWO) {
            NeowBonusLog.neowBonusLog.cardsRemoved.add(card.cardID);
        }
        else if (rewardType == NeowReward.NeowRewardType.TRANSFORM_CARD || rewardType == NeowReward.NeowRewardType.TRANSFORM_TWO_CARDS){
            NeowBonusLog.neowBonusLog.cardsTransformed.add(card.cardID);
        }
        else {
            logger.error("Unrecognized rewardType for removing or transforming: " + rewardType.name());
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
                ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                        .invoke(__instance, "neow_bonus_log", NeowBonusLog.neowBonusLog);
        }
    }

    private static String getNeowBonusText(NeowReward.NeowRewardType neowRewardType, NeowReward.NeowRewardDrawback neowCost) {
        String rewardText = cleanupBonusString(getNeowRewardTypeText(neowRewardType));
        if (rewardText == null) {
            return null;
        }
        String costText = cleanupBonusString(getNeowCostText(neowCost));
        return MessageFormat.format(costText == null ? TEXT[2] : TEXT[3], rewardText, costText);
    }

    private static String getNeowRewardTypeText(NeowReward.NeowRewardType neowRewardType) {
        switch (neowRewardType) {
            case THREE_CARDS:
                return BONUS_TEXT[0];
            case ONE_RANDOM_RARE_CARD:
                return BONUS_TEXT[1];
            case REMOVE_CARD:
                return BONUS_TEXT[2];
            case UPGRADE_CARD:
                return BONUS_TEXT[3];
            case TRANSFORM_CARD:
                return BONUS_TEXT[4];
            case THREE_SMALL_POTIONS:
                return BONUS_TEXT[5];
            case RANDOM_COMMON_RELIC:
                return BONUS_TEXT[6];
            case TEN_PERCENT_HP_BONUS:
                return BONUS_TEXT[7] + "10%";
            case HUNDRED_GOLD:
                return BONUS_TEXT[8] + "100" + BONUS_TEXT[9];
            case REMOVE_TWO:
                return BONUS_TEXT[10];
            case ONE_RARE_RELIC:
                return BONUS_TEXT[11];
            case THREE_RARE_CARDS:
                return BONUS_TEXT[12];
            case TWO_FIFTY_GOLD:
                return BONUS_TEXT[13] + 250 + BONUS_TEXT[14];
            case TRANSFORM_TWO_CARDS:
                return BONUS_TEXT[15];
            case TWENTY_PERCENT_HP_BONUS:
                return BONUS_TEXT[16] + "20%";
            case THREE_ENEMY_KILL:
                return BONUS_TEXT[28];
            case RANDOM_COLORLESS:
                return BONUS_TEXT[30];
            case RANDOM_COLORLESS_2:
                return BONUS_TEXT[31];
            case BOSS_RELIC:
                return BONUS_STRINGS.UNIQUE_REWARDS[0];
            default:
                return neowRewardType.name();
        }
    }

    private static String getNeowCostText(NeowReward.NeowRewardDrawback neowCost) {
        if (neowCost == NeowReward.NeowRewardDrawback.NONE) {
            return null;
        }
        switch (neowCost) {
            case TEN_PERCENT_HP_LOSS:
                return BONUS_TEXT[17] + "10%" + BONUS_TEXT[18];
            case NO_GOLD:
                return BONUS_TEXT[19];
            case CURSE:
                return BONUS_TEXT[20];
            case PERCENT_DAMAGE:
                return BONUS_TEXT[21] + "30%" + BONUS_TEXT[29];
            default:
                return neowCost.name();
        }
    }

    private static String cleanupBonusString(String s) {
        if (s == null) {
            return null;
        }
        return s.replace("[ ", "")
                .replace(" ]", "")
                .replace("#g", "")
                .replace("#r", "");
    }

    public static String getNeowBlessingDescription(RunHistoryScreen screen) throws NoSuchFieldException, IllegalAccessException {
        RunData runData = ReflectionHacks.getPrivate(screen, RunHistoryScreen.class, "viewedRun");
        Field field = runData.getClass().getField("neow_bonus_log");
        NeowBonusLog neowBonusLog = (NeowBonusLog)field.get(runData);
        if (neowBonusLog == null) {
            neowBonusLog = new NeowBonusLog();
        }

        List<CardChoiceStats> neowCardChoices = runData.card_choices.stream().filter(cc -> cc.floor == 0).collect(Collectors.toList());
        List<String> neowCardsObtained = neowCardChoices.stream().map(cc -> cc.picked).collect(Collectors.toList());
        List<String> allCardsObtained = new ArrayList<>();
        allCardsObtained.addAll(neowBonusLog.cardsObtained);
        allCardsObtained.addAll(neowCardsObtained);
        List<String> neowCardsNotTaken = neowCardChoices.stream().map(cc -> cc.not_picked).flatMap(Collection::stream).collect(Collectors.toList());
        
        StringBuilder sb = new StringBuilder();
        String nl = " NL ";
        String tab = " TAB ";

        if (neowBonusLog.maxHpLost != 0) {
            sb.append(TEXT_LOST).append(String.format(TEXT_GENERIC_MAX_HP_FORMAT, neowBonusLog.maxHpLost)).append(nl);
        }

        if (neowBonusLog.damageTaken != 0) {
            sb.append(TEXT_TOOK).append(String.format(TEXT_EVENT_DAMAGE, neowBonusLog.damageTaken)).append(nl);
        }

        if (neowBonusLog.goldLost != 0) {
            sb.append(TEXT_LOST).append(String.format(TEXT_GOLD_FORMAT, neowBonusLog.damageTaken)).append(nl);
        }

        if (neowBonusLog.maxHpGained != 0) {
            sb.append(TEXT_GAINED).append(String.format(TEXT_GENERIC_MAX_HP_FORMAT, neowBonusLog.maxHpGained)).append(nl);
        }

        if (neowBonusLog.goldGained != 0) {
            sb.append(TEXT_GAINED).append(String.format(TEXT_GOLD_FORMAT, neowBonusLog.goldGained)).append(nl);
        }

        for (String cardID : neowBonusLog.cardsRemoved) {
            String cardName = CardLibrary.getCardNameFromMetricID(cardID);
            sb.append(String.format(TEXT_REMOVE_OPTION, cardName)).append(nl);
        }

        for (String cardID : neowBonusLog.cardsUpgraded) {
            String cardName = CardLibrary.getCardNameFromMetricID(cardID);
            sb.append(String.format(TEXT_UPGRADED, cardName)).append(nl);
        }

        for (String cardID : neowBonusLog.cardsTransformed) {
            String cardName = CardLibrary.getCardNameFromMetricID(cardID);
            sb.append(String.format(TEXT_TRANSFORMED, cardName)).append(nl);
        }

        if (!allCardsObtained.isEmpty() || !neowCardsObtained.isEmpty() || !neowBonusLog.relicsObtained.isEmpty() || !neowBonusLog.potionsObtained.isEmpty()) {
            sb.append(TEXT_OBTAIN_HEADER).append(nl);
            for (String relicID : neowBonusLog.relicsObtained) {
                String relicName = RelicLibrary.getRelic(relicID).name;
                sb.append(tab).append(TEXT_OBTAIN_TYPE_RELIC).append(relicName).append(nl);
            }
            for (String cardID : allCardsObtained) {
                String cardName = CardLibrary.getCardNameFromMetricID(cardID);
                sb.append(tab).append(TEXT_OBTAIN_TYPE_CARD).append(cardName).append(nl);
            }
            for (String potionID : neowBonusLog.potionsObtained) {
                String potionName = PotionHelper.getPotion(potionID).name;
                sb.append(tab).append(TEXT_OBTAIN_TYPE_POTION).append(potionName).append(nl);
            }
        }

        if (!neowCardsNotTaken.isEmpty()) {
            sb.append(TEXT_SKIP_HEADER).append(nl);
            for (String cardID : neowCardsNotTaken) {
                String cardName = CardLibrary.getCardNameFromMetricID(cardID);
                sb.append(tab).append(TEXT_OBTAIN_TYPE_CARD).append(cardName).append(nl);
            }
        }

        String s = sb.toString();
        if (s.endsWith(nl)) {
            s = s.substring(0, s.length() - nl.length());
        }
        return s.length() != 0 ? s : null;
    }
}