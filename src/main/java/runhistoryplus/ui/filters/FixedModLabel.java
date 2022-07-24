package runhistoryplus.ui.filters;

import basemod.IUIElement;
import basemod.ModPanel;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import java.util.function.Consumer;

// This class exists because the BaseMod version incorrectly uses Settings.scale everywhere, instead of distinguishing
// between Settings.xScale and Settings.yScale. This is fine at 16:9, since that's the game's native resolution ratio,
// but increasing broken the further away you get from 16:9
public class FixedModLabel implements IUIElement {
    private final Consumer<FixedModLabel> update;
    public final ModPanel parent;
    public final String text;
    public float x;
    public float y;
    public final Color color;
    public final BitmapFont font;

    public FixedModLabel(String labelText, float xPos, float yPos, ModPanel p, Consumer<FixedModLabel> updateFunc) {
        this(labelText, xPos, yPos, Color.WHITE, FontHelper.buttonLabelFont, p, updateFunc);
    }

    public FixedModLabel(String labelText, float xPos, float yPos, Color color, ModPanel p, Consumer<FixedModLabel> updateFunc) {
        this(labelText, xPos, yPos, color, FontHelper.buttonLabelFont, p, updateFunc);
    }

    public FixedModLabel(String labelText, float xPos, float yPos, BitmapFont font, ModPanel p, Consumer<FixedModLabel> updateFunc) {
        this(labelText, xPos, yPos, Color.WHITE, font, p, updateFunc);
    }

    public FixedModLabel(String labelText, float xPos, float yPos, Color color, BitmapFont font, ModPanel p, Consumer<FixedModLabel> updateFunc) {
        this.text = labelText;
        this.x = xPos * Settings.xScale;
        this.y = yPos * Settings.yScale;
        this.color = color;
        this.font = font;
        this.parent = p;
        this.update = updateFunc;
    }

    public void render(SpriteBatch sb) {
        FontHelper.renderFontLeftDownAligned(sb, this.font, this.text, this.x, this.y, this.color);
    }

    public void update() {
        this.update.accept(this);
    }

    public int renderLayer() {
        return 2;
    }

    public int updateOrder() {
        return 0;
    }

    public void set(float xPos, float yPos) {
        this.x = xPos * Settings.scale;
        this.y = yPos * Settings.scale;
    }

    public void setX(float xPos) {
        this.x = xPos * Settings.scale;
    }

    public void setY(float yPos) {
        this.y = yPos * Settings.scale;
    }

    public float getX() {
        return this.x / Settings.scale;
    }

    public float getY() {
        return this.y / Settings.scale;
    }
}
