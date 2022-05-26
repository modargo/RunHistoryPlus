package runhistoryplus.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MathHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBar;
import com.megacrit.cardcrawl.screens.mainMenu.ScrollBarListener;
import runhistoryplus.utils.ExtraColors;
import runhistoryplus.utils.ExtraFonts;

import java.util.*;

public class RelicFilterScreen implements ScrollBarListener {
    private TreeSet<String> relics = new TreeSet<>();
    private HashMap<String, RelicUIObject> relicUIObjects = new HashMap<>();
    private Texture TEX_BG = new Texture("images/config_screen_bg.png");
    private ActionButton returnButton = new ActionButton(256, 200, "Close");
    public ArrayList<String> selectedRelics = new ArrayList<>();
    public boolean isShowing = false;
    public final int RELICS_PER_ROW = 7;
    public ArrayList<String> initialRelics = new ArrayList<>();

    // Position
    public float x;
    public float y;

    // Scrolling
    private ScrollBar scrollBar = null;
    private boolean grabbedScreen = false;
    private float grabStartY = 0.0F;
    private float scrollTargetY = 0.0F;
    private float scrollY = 0.0F;
    private float scrollLowerBound = -Settings.DEFAULT_SCROLL_LIMIT;
    private float scrollUpperBound = Settings.DEFAULT_SCROLL_LIMIT;

    public RelicFilterScreen() {
        setup();

        // Setup scrollbar
        if (this.scrollBar == null) {
            calculateScrollBounds();
            this.scrollBar = new ScrollBar(this, 0, 0, 400.0F * Settings.yScale);
        }

        this.move(0, 0);
    }

    public void move(float x, float y) {
        this.x = x;
        this.y = y;

        scrollBar.setCenter(x + 1100f * Settings.xScale, y + 465f * Settings.yScale);
    }

    //  Begin scroll functions
    private void updateScrolling() {
        int y = InputHelper.mY;
        if (!this.grabbedScreen) {
            if (InputHelper.scrolledDown) {
                this.scrollTargetY += Settings.SCROLL_SPEED / ((Settings.SCROLL_SPEED / RELICS_PER_ROW) - 1);
            } else if (InputHelper.scrolledUp) {
                this.scrollTargetY -= Settings.SCROLL_SPEED / ((Settings.SCROLL_SPEED / RELICS_PER_ROW) - 1);
            }
            if (InputHelper.justClickedLeft) {
                this.grabbedScreen = true;
                this.grabStartY = y - this.scrollTargetY;
            }
        } else if (InputHelper.isMouseDown) {
            this.scrollTargetY = y - this.grabStartY;
        } else {
            this.grabbedScreen = false;
        }
        this.scrollY = MathHelper.scrollSnapLerpSpeed(this.scrollY, this.scrollTargetY);
        resetScrolling();
        updateBarPosition();
    }

    public void scrolledUsingBar(float newPercent) {
        this.scrollY = MathHelper.valueFromPercentBetween(this.scrollLowerBound, this.scrollUpperBound, newPercent);
        this.scrollTargetY = this.scrollY;
        updateBarPosition();
    }

    private void updateBarPosition() {
        float percent = MathHelper.percentFromValueBetween(this.scrollLowerBound, this.scrollUpperBound, this.scrollY);
        this.scrollBar.parentScrolledToPercent(percent);
    }

    private void calculateScrollBounds() {
        this.scrollUpperBound = 100.0F * Settings.yScale;
        this.scrollLowerBound = 0F * Settings.yScale;
    }

    private void resetScrolling() {
        if (this.scrollTargetY < this.scrollLowerBound) {
            this.scrollTargetY = MathHelper.scrollSnapLerpSpeed(this.scrollTargetY, this.scrollLowerBound);
        } else if (this.scrollTargetY > this.scrollUpperBound) {
            this.scrollTargetY = MathHelper.scrollSnapLerpSpeed(this.scrollTargetY, this.scrollUpperBound);
        }
    }
    //  End scroll functions

    private void populateRelics() {
        ArrayList<String> relics = new ArrayList<>();
        AbstractRelic.RelicTier[] tiers = new AbstractRelic.RelicTier[] {
                AbstractRelic.RelicTier.COMMON,
                AbstractRelic.RelicTier.UNCOMMON,
                AbstractRelic.RelicTier.RARE,
                AbstractRelic.RelicTier.BOSS,
                AbstractRelic.RelicTier.SHOP,
                AbstractRelic.RelicTier.SPECIAL
        };

        AbstractPlayer.PlayerClass[] classes = new AbstractPlayer.PlayerClass[]{
                AbstractPlayer.PlayerClass.IRONCLAD,
                AbstractPlayer.PlayerClass.THE_SILENT,
                AbstractPlayer.PlayerClass.DEFECT,
                AbstractPlayer.PlayerClass.WATCHER
        };

        for (AbstractRelic.RelicTier tier: tiers) {
            for (AbstractPlayer.PlayerClass c: classes){
                RelicLibrary.populateRelicPool(relics, tier, c);
            }
        }

        HashSet<String> unique = new HashSet<>(relics);
        this.relics.addAll(unique);
    }

    private void makeUIObjects() {
        // Note: relic textures are 128x128 originally, with some internal spacing
        float left = 410.0f;
        float top = 587.0f;

        float spacing = 84.0f;

        int ix = 0;
        int iy = 0;

        for (String id : relics) {
            float tx = left + ix * spacing;
            float ty = top - iy * spacing;

            relicUIObjects.put(id, new RelicUIObject(this, id, tx, ty));

            ix++;
            if (ix == RELICS_PER_ROW) {
                ix = 0;
                iy++;
            }
        }
    }

    private void setup() {
        populateRelics();
        makeUIObjects();
    }

    public void renderForeground(SpriteBatch sb) {
        sb.setColor(Color.WHITE);

        for (RelicUIObject relicUIObject : relicUIObjects.values()){
            if (relicUIObject.getScrollPosition() > ((this.scrollLowerBound + 200) * Settings.yScale) &&
                    relicUIObject.getScrollPosition() < ((this.scrollUpperBound + 500) * Settings.yScale)){
                relicUIObject.render(sb);
            }
        }

        this.returnButton.render(sb);
        this.scrollBar.render(sb);

        // Title text
        float titleLeft = 386.0f;
        float titleBottom = 819.0f;
        FontHelper.renderFontLeftDownAligned(sb, ExtraFonts.configTitleFont(), "Relic List", titleLeft * Settings.xScale, titleBottom * Settings.yScale, Settings.GOLD_COLOR);
        float infoLeft = 1160.0f;
        float infoTopMain = 667.0f;
        float infoTopControls = 472.0f;

        FontHelper.renderSmartText(sb,
                FontHelper.tipBodyFont,
                "This filter allows you to choose which Relics a run must have to show in the history.",
                infoLeft * Settings.xScale,
                infoTopMain * Settings.yScale,
                371.0f * Settings.xScale,
                30.0f * Settings.yScale,
                Settings.CREAM_COLOR);

        FontHelper.renderSmartText(sb,
                FontHelper.tipBodyFont,
                "Controls: NL Click to toggle NL Right+Click to select just one NL NL Shift+Click to select all NL Shift+Right+Click to clear all NL Alt+Click to invert all",
                infoLeft * Settings.xScale,
                infoTopControls * Settings.yScale,
                371.0f * Settings.xScale,
                30.0f * Settings.yScale,
                Color.GRAY);
    }

    public void enableHitboxes(boolean enabled) {
        for (RelicUIObject obj : relicUIObjects.values()) {
            if (enabled)
                obj.enableHitbox();
            else
                obj.disableHitbox();
        }

        if (enabled && isShowing){
            this.returnButton.show();
        } else{
            this.returnButton.hide();
            isShowing = false;
        }
    }

    public void clearSelections(){
        this.selectedRelics.clear();
        for (RelicUIObject relicObject : relicUIObjects.values()) {
            relicObject.isEnabled = false;
        }
    }

    public void render(SpriteBatch sb) {
        sb.setColor(ExtraColors.SCREEN_DIM);
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0, 0, Settings.WIDTH, Settings.HEIGHT);

        // Draw our screen texture in the center
        sb.setColor(Color.WHITE);
        sb.draw(TEX_BG,
                (Settings.WIDTH - (TEX_BG.getWidth() * Settings.xScale)) * 0.5f,
                (Settings.HEIGHT - (TEX_BG.getHeight() * Settings.yScale)) * 0.5f,
                TEX_BG.getWidth() * Settings.xScale,
                TEX_BG.getHeight() * Settings.yScale
        );

        renderForeground(sb);
    }

    public void update() {
        this.returnButton.update();
        for (RelicUIObject relicObject : relicUIObjects.values()) {
            relicObject.update();
            relicObject.scroll(this.scrollY);
        }

        if (this.returnButton.hb.clickStarted){
            enableHitboxes(false);
            if (this.initialRelics.size() != this.selectedRelics.size() ||
                    !this.initialRelics.equals(this.selectedRelics)) {
                CardCrawlGame.mainMenuScreen.runHistoryScreen.refreshData();
            }
        }

        boolean isDraggingScrollBar = this.scrollBar.update();
        if (!isDraggingScrollBar){
            updateScrolling();
        }
    }

    // --------------------------------------------------------------------------------

    public void clearAll() {
        for (RelicUIObject obj : relicUIObjects.values()) {
            obj.isEnabled = false;
        }

        refreshFilters();
    }

    private void select(String id) {
        if (relicUIObjects.containsKey(id)) {
            relicUIObjects.get(id).isEnabled = true;
            refreshFilters();
        }
    }

    public void selectOnly(String id) {
        if (relicUIObjects.containsKey(id)) {
            clearAll();
            relicUIObjects.get(id).isEnabled = true;
            refreshFilters();
        }
    }

    public void invertAll() {
        for (RelicUIObject obj : relicUIObjects.values()) {
            obj.isEnabled = !obj.isEnabled;
        }

        refreshFilters();
    }

    public void selectAll() {
        for (RelicUIObject obj : relicUIObjects.values()) {
            obj.isEnabled = true;
        }

        refreshFilters();
    }

    // --------------------------------------------------------------------------------

    public ArrayList<String> getEnabledRelics() {
        ArrayList<String> list = new ArrayList<>();

        for (RelicUIObject obj : relicUIObjects.values()) {
            if (obj.isEnabled)
                list.add(obj.relicID);
        }

        return list;
    }

    public void refreshFilters() {
        this.selectedRelics = getEnabledRelics();
    }
}
