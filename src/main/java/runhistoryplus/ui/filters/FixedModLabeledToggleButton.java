package runhistoryplus.ui.filters;

import basemod.IUIElement;
import basemod.ModPanel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.function.Consumer;

// This class exists because the BaseMod version incorrectly uses Settings.scale everywhere, instead of distinguishing
// between Settings.xScale and Settings.yScale. This is fine at 16:9, since that's the game's native resolution ratio,
// but increasing broken the further away you get from 16:9
public class FixedModLabeledToggleButton implements IUIElement {
    private static final float TEXT_X_OFFSET = 40.0F;
    private static final float TEXT_Y_OFFSET = 8.0F;
    public final FixedModToggleButton toggle;
    public final FixedModLabel text;

    public FixedModLabeledToggleButton(String labelText, float xPos, float yPos, Color color, BitmapFont font, boolean enabled, ModPanel p, Consumer<FixedModLabel> labelUpdate, Consumer<FixedModToggleButton> c) {
        this.toggle = new FixedModToggleButton(xPos, yPos, enabled, false, p, c);
        this.text = new FixedModLabel(labelText, xPos + 40.0F, yPos + 8.0F, color, font, p, labelUpdate);
        this.toggle.wrapHitboxToText(labelText, 1000.0F, 0.0F, font);
    }

    public void render(SpriteBatch sb) {
        this.toggle.render(sb);
        this.text.render(sb);
    }

    public void update() {
        this.toggle.update();
        this.text.update();
    }

    public int renderLayer() {
        return 1;
    }

    public int updateOrder() {
        return 1;
    }

    public void set(float xPos, float yPos) {
        this.toggle.set(xPos, yPos);
        this.text.set(xPos + 40.0F, yPos + 8.0F);
    }

    public void setX(float xPos) {
        this.toggle.setX(xPos);
        this.text.setX(xPos + 40.0F);
    }

    public void setY(float yPos) {
        this.toggle.setY(yPos);
        this.text.setY(yPos + 8.0F);
    }

    public float getX() {
        return this.toggle.getX();
    }

    public float getY() {
        return this.toggle.getY();
    }
}
