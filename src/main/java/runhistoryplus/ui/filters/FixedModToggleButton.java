package runhistoryplus.ui.filters;

import basemod.IUIElement;
import basemod.ModPanel;
import basemod.helpers.UIElementModificationHelper;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import java.util.function.Consumer;

// This class exists because the BaseMod version incorrectly uses Settings.scale everywhere, instead of distinguishing
// between Settings.xScale and Settings.yScale. This is fine at 16:9, since that's the game's native resolution ratio,
// but increasing broken the further away you get from 16:9
public class FixedModToggleButton implements IUIElement {
    private static final float TOGGLE_Y_DELTA = 0.0F;
    private static final float TOGGLE_X_EXTEND = 12.0F;
    private static final float HB_WIDTH_EXTENDED = 200.0F;
    private final Consumer<FixedModToggleButton> toggle;
    private final Hitbox hb;
    private float x;
    private float y;
    private final float w;
    private final float h;
    private final boolean extendedHitbox;
    public boolean enabled;
    public final ModPanel parent;

    public FixedModToggleButton(float xPos, float yPos, ModPanel p, Consumer<FixedModToggleButton> c) {
        this(xPos, yPos, false, true, p, c);
    }

    public FixedModToggleButton(float xPos, float yPos, boolean enabled, boolean extendedHitbox, ModPanel p, Consumer<FixedModToggleButton> c) {
        this.x = xPos * Settings.xScale;
        this.y = yPos * Settings.yScale;
        this.w = (float)ImageMaster.OPTION_TOGGLE.getWidth();
        this.h = (float)ImageMaster.OPTION_TOGGLE.getHeight();
        this.extendedHitbox = extendedHitbox;
        if (extendedHitbox) {
            this.hb = new Hitbox(this.x - 12.0F * Settings.xScale, this.y - 0.0F * Settings.yScale, 200.0F * Settings.xScale, this.h * Settings.yScale);
        } else {
            this.hb = new Hitbox(this.x, this.y - 0.0F * Settings.yScale, this.w * Settings.xScale, this.h * Settings.yScale);
        }

        this.enabled = enabled;
        this.parent = p;
        this.toggle = c;
    }

    public void wrapHitboxToText(String text, float lineWidth, float lineSpacing, BitmapFont font) {
        float tWidth = FontHelper.getSmartWidth(font, text, lineWidth, lineSpacing);
        this.hb.width = tWidth + this.h * Settings.scale + 12.0F;
    }

    public void render(SpriteBatch sb) {
        if (this.hb.hovered) {
            sb.setColor(Color.CYAN);
        } else if (this.enabled) {
            sb.setColor(Color.LIGHT_GRAY);
        } else {
            sb.setColor(Color.WHITE);
        }

        sb.draw(ImageMaster.OPTION_TOGGLE, this.x, this.y, this.w * Settings.scale, this.h * Settings.scale);
        if (this.enabled) {
            sb.setColor(Color.WHITE);
            sb.draw(ImageMaster.OPTION_TOGGLE_ON, this.x, this.y, this.w * Settings.scale, this.h * Settings.scale);
        }

        this.hb.render(sb);
    }

    public void update() {
        this.hb.update();
        if (this.hb.justHovered) {
            CardCrawlGame.sound.playV("UI_HOVER", 0.75F);
        }

        if (this.hb.hovered && InputHelper.justClickedLeft) {
            CardCrawlGame.sound.playA("UI_CLICK_1", -0.1F);
            this.hb.clickStarted = true;
        }

        if (this.hb.clicked) {
            this.hb.clicked = false;
            this.onToggle();
        }

    }

    private void onToggle() {
        this.enabled = !this.enabled;
        this.toggle.accept(this);
    }

    public void toggle() {
        this.onToggle();
    }

    public int renderLayer() {
        return 1;
    }

    public int updateOrder() {
        return 1;
    }

    public void set(float xPos, float yPos) {
        this.x = xPos * Settings.scale;
        this.y = yPos * Settings.scale;
        if (this.extendedHitbox) {
            UIElementModificationHelper.moveHitboxByOriginalParameters(this.hb, this.x - 12.0F * Settings.scale, this.y - 0.0F * Settings.scale);
        } else {
            UIElementModificationHelper.moveHitboxByOriginalParameters(this.hb, this.x, this.y - 0.0F * Settings.scale);
        }

    }

    public void setX(float xPos) {
        this.set(xPos, this.y / Settings.scale);
    }

    public void setY(float yPos) {
        this.set(this.x / Settings.scale, yPos);
    }

    public float getX() {
        return this.x / Settings.scale;
    }

    public float getY() {
        return this.y / Settings.scale;
    }
}
