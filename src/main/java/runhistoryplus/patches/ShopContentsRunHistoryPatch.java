package runhistoryplus.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.metrics.Metrics;
import com.megacrit.cardcrawl.monsters.MonsterGroup;
import com.megacrit.cardcrawl.rooms.ShopRoom;
import com.megacrit.cardcrawl.saveAndContinue.SaveFile;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryPath;
import com.megacrit.cardcrawl.screens.runHistory.RunHistoryScreen;
import com.megacrit.cardcrawl.screens.runHistory.RunPathElement;
import com.megacrit.cardcrawl.screens.stats.RunData;
import com.megacrit.cardcrawl.shop.ShopScreen;
import com.megacrit.cardcrawl.shop.StorePotion;
import com.megacrit.cardcrawl.shop.StoreRelic;
import javassist.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ShopContentsRunHistoryPatch {
    private static final String[] TOOLTIP_TEXT = CardCrawlGame.languagePack.getUIString("RunHistoryPathNodes").TEXT;
    private static final String TEXT_SKIP_HEADER = TOOLTIP_TEXT[19];
    private static final String TEXT_OBTAIN_TYPE_CARD = TOOLTIP_TEXT[22];
    private static final String TEXT_OBTAIN_TYPE_RELIC = TOOLTIP_TEXT[23];
    private static final String TEXT_OBTAIN_TYPE_POTION = TOOLTIP_TEXT[24];

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class ShopContentsRunDataField {
        @SpireRawPatch
        public static void addShopContents(CtBehavior ctBehavior) throws NotFoundException, CannotCompileException {
            CtClass runData = ctBehavior.getDeclaringClass().getClassPool().get(RunData.class.getName());

            String fieldSource = "public java.util.List shop_contents;";

            CtField field = CtField.make(fieldSource, runData);

            runData.addField(field);
        }
    }

    @SpirePatch(
            clz = AbstractDungeon.class,
            method = "generateSeeds"
    )
    public static class GenerateSeedsPatch {
        @SpirePostfixPatch
        public static void initializeShopContents() {
            ShopContentsLog.shopContentsLog = new ArrayList<>();
        }
    }

    @SpirePatch(clz = Metrics.class, method = "gatherAllData")
    public static class GatherAllDataPatch {
        @SpirePostfixPatch
        public static void gatherAllDataPatch(Metrics __instance, boolean death, boolean trueVictor, MonsterGroup monsters) {
            ReflectionHacks.privateMethod(Metrics.class, "addData", Object.class, Object.class)
                    .invoke(__instance, "shop_contents", ShopContentsLog.shopContentsLog);
        }
    }

    @SpirePatch(clz = RunHistoryScreen.class, method = "refreshData")
    public static class ShopContentsRefreshDataPatch {
        @SuppressWarnings("unchecked")
        @SpireInsertPatch(locator = Locator.class, localvars = { "data" })
        public static void shopContentsRefreshData(RunHistoryScreen __instance, RunData data) throws NoSuchFieldException, IllegalAccessException {
            Field field = data.getClass().getField("shop_contents");
            List<LinkedTreeMap<String, Object>> shop_contents = (List<LinkedTreeMap<String, Object>>)field.get(data);
            if (shop_contents == null) {
                return;
            }
            List<ShopContentsLog> l = new ArrayList<>();
            for (LinkedTreeMap<String, Object> ltm : shop_contents) {
                ShopContentsLog log = new ShopContentsLog();
                log.floor = ((Double)ltm.get("floor")).intValue();
                log.relics = (List<String>)ltm.get("relics");
                log.cards = (List<String>)ltm.get("cards");
                log.potions = (List<String>)ltm.get("potions");
                l.add(log);
            }
            field.set(data, l);
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.MethodCallMatcher(Gson.class, "fromJson");
                Matcher finalMatcher = new Matcher.FieldAccessMatcher(RunData.class, "timestamp");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = SpirePatch.CLASS)
    public static class ShopContentsField {
        public static final SpireField<ShopContentsLog> shopContents = new SpireField<>(() -> null);
    }

    @SpirePatch(clz = RunHistoryPath.class, method = "setRunData")
    public static class AddShopContentsDataPatch {
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SpireInsertPatch(locator = Locator.class, localvars = { "element", "floor" })
        public static void addShopContentsData(RunHistoryPath __instance, RunData newData, RunPathElement element, int floor) throws NoSuchFieldException, IllegalAccessException {
            Field field = newData.getClass().getField("shop_contents");
            List shop_contents = (List)field.get(newData);
            if (shop_contents != null) {
                List<ShopContentsLog> shopContentsThisFloor = (List<ShopContentsLog>)shop_contents.stream()
                        .filter(sc -> sc instanceof ShopContentsLog)
                        .filter(sc -> ((ShopContentsLog)sc).floor == floor)
                        .collect(Collectors.toList());
                if (!shopContentsThisFloor.isEmpty()) {
                    ShopContentsField.shopContents.set(element, shopContentsThisFloor.get(0));
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.NewExprMatcher(RunPathElement.class);
                Matcher finalMatcher = new Matcher.MethodCallMatcher(List.class, "add");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = RunPathElement.class, method = "getTipDescriptionText")
    public static class DisplayShopContentsDataPatch {
        @SpireInsertPatch(locator = Locator.class, localvars = { "sb" })
        public static void displayShopContentsData(RunPathElement __instance, StringBuilder sb) {
            ShopContentsLog shopContents = ShopContentsField.shopContents.get(__instance);
            if (shopContents != null && (!shopContents.cards.isEmpty() || !shopContents.relics.isEmpty() || !shopContents.potions.isEmpty())) {
                if (sb.length() > 0) {
                    sb.append(" NL ");
                }
                sb.append(TEXT_SKIP_HEADER);
                for (String relicID : shopContents.relics) {
                    sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_RELIC).append(RelicLibrary.getRelic(relicID).name);
                }
                for (String cardID : shopContents.cards) {
                    sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_CARD).append(CardLibrary.getCardNameFromMetricID(cardID));
                }
                for (String potionID : shopContents.potions) {
                    sb.append(" NL ").append(" TAB ").append(TEXT_OBTAIN_TYPE_POTION).append(PotionHelper.getPotion(potionID).name);
                }
            }
        }

        public static class Locator extends SpireInsertLocator {
            public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
                Matcher matcher = new Matcher.FieldAccessMatcher(RunPathElement.class, "shopPurges");
                Matcher finalMatcher = new Matcher.MethodCallMatcher(StringBuilder.class, "length");
                return LineFinder.findInOrder(ctMethodToPatch, Collections.singletonList(matcher), finalMatcher);
            }
        }
    }

    @SpirePatch(clz = AbstractDungeon.class, method = "nextRoomTransition", paramtypez = { SaveFile.class })
    public static class ShopContentsAddLogging {
        @SpirePrefixPatch
        public static void shopContentsAddLogging(AbstractDungeon __instance, SaveFile saveFile) {
            boolean isLoadingPostCombatSave = CardCrawlGame.loadingSave && saveFile != null && saveFile.post_combat;
            if (!isLoadingPostCombatSave && AbstractDungeon.currMapNode != null && AbstractDungeon.currMapNode.room instanceof ShopRoom) {
                ShopScreen shopScreen = AbstractDungeon.shopScreen;
                List<StoreRelic> relics = ReflectionHacks.getPrivate(shopScreen, ShopScreen.class, "relics");
                List<StorePotion> potions = ReflectionHacks.getPrivate(shopScreen, ShopScreen.class, "potions");
                ShopContentsLog shopContentsLog = new ShopContentsLog();
                shopContentsLog.floor = AbstractDungeon.floorNum;
                shopContentsLog.cards = new ArrayList<>();
                shopContentsLog.relics = relics.stream().map(r -> r.relic.relicId).collect(Collectors.toList());
                shopContentsLog.cards.addAll(shopScreen.coloredCards.stream().map(c -> c.cardID).collect(Collectors.toList()));
                shopContentsLog.cards.addAll(shopScreen.colorlessCards.stream().map(c -> c.cardID).collect(Collectors.toList()));
                shopContentsLog.potions = potions.stream().map(p -> p.potion.ID).collect(Collectors.toList());
                ShopContentsLog.shopContentsLog.add(shopContentsLog);
            }
        }
    }
}
