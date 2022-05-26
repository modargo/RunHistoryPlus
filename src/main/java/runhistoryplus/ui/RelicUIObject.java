package runhistoryplus.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import runhistoryplus.utils.ExtraColors;

public class RelicUIObject {

    private Hitbox hb;

    public String relicID;
    private float x, y, scroll;
    private Texture tex;
    private static final Texture TEX_SELECTED_BG = new Texture("runhistoryplus/images/relic_bg.png");
    private static final int HITBOX_OFFSET = 50;

    public boolean isEnabled = false;
    private RelicFilterScreen parent;

    public RelicUIObject(RelicFilterScreen parent, String relicID, float x, float y) {
        this.relicID = relicID;
        this.tex = ImageMaster.getRelicImg(relicID);
        this.x = x;
        this.y = y;
        this.parent = parent;

        int hbSize = 75;
        hb = new Hitbox(hbSize * Settings.xScale, hbSize * Settings.yScale);
    }

    public void scroll(float scrollY) {
        this.scroll = scrollY * ((Settings.SCROLL_SPEED / parent.RELICS_PER_ROW) - 1);
    }

    public float getScrollPosition(){
        return (y + this.scroll) * Settings.yScale;
    }

    public void enableHitbox() {
        hb.move((x + HITBOX_OFFSET) * Settings.xScale, (y + HITBOX_OFFSET + this.scroll) * Settings.yScale);
    }

    public void disableHitbox() {
        hb.move(-10000.0f, -10000.0f);
    }

    public void render(SpriteBatch sb) {
        // Grow a bit larger when hovered
        int size = 100;
        float s = (hb.hovered) ? size * 1.10f : size;

        if (isEnabled) {
            sb.setColor(ExtraColors.SEL_RELIC_BG);
            sb.draw(TEX_SELECTED_BG, x * Settings.xScale, (y + this.scroll) * Settings.yScale, s * Settings.xScale, s * Settings.yScale);

            sb.setColor(Color.WHITE);
        } else {
            sb.setColor(ExtraColors.DIM_RELIC);
        }

        sb.draw(tex, x * Settings.xScale, (y + this.scroll) * Settings.yScale, s * Settings.xScale, s * Settings.yScale);

        // DEBUG
        hb.render(sb);
    }

    private void handleClick() {
        if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
            CardCrawlGame.sound.play("BLOOD_SPLAT");
            parent.selectAll();
        }
        else if (Gdx.input.isKeyPressed(57) || Gdx.input.isKeyPressed(58)) {
            CardCrawlGame.sound.play("MAP_SELECT_3");
            parent.invertAll();
        }
        else {
            if (isEnabled) CardCrawlGame.sound.playA("UI_CLICK_1", 0.2f);
            else CardCrawlGame.sound.playA("UI_CLICK_1", -0.4f);

            isEnabled = !isEnabled;
            parent.refreshFilters();
        }
    }

    private void handleRightClick() {
        if (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60)) {
            CardCrawlGame.sound.play("APPEAR");
            parent.clearAll();
        }
        else {
            CardCrawlGame.sound.play("KEY_OBTAIN");
            parent.selectOnly(relicID);
        }
    }

    private boolean mouseDownRight = false;

    public void update() {
        hb.update();

        if (hb.justHovered) {
            CardCrawlGame.sound.playAV("UI_HOVER", -0.4f, 0.5f);
        }

        // Right clicks
        if (hb.hovered && InputHelper.isMouseDown_R) {
            mouseDownRight = true;
        } else {
            // We already had the mouse down, and now we released, so fire our right click event
            if (hb.hovered && mouseDownRight) {
                handleRightClick();
                mouseDownRight = false;
            }
        }

        // Left clicks
        if (this.hb.hovered && InputHelper.justClickedLeft) {
            this.hb.clickStarted = true;
        }

        if (hb.clicked) {
            hb.clicked = false;
            handleClick();
        }

    }
}
