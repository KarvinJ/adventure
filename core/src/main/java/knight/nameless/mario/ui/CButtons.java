package knight.nameless.mario.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class CButtons extends Table {

    private final float controllerWidth;
    private float buttonSize;
    private final TextureAtlas atlas;

    public CButtons(float controllerWidth, float controllerHeight) {

        atlas = new TextureAtlas("images/game.atlas");

        this.controllerWidth = controllerWidth;
        setSize(controllerWidth, controllerHeight);
        build();
    }

    private void build() {

        setBackground(new TextureRegionDrawable(atlas.findRegion("Border")));

        makeMoveButtons();

        ImageButton fireButton = new ImageButton(
            new TextureRegionDrawable(atlas.findRegion("FireBtn")),
            new TextureRegionDrawable(atlas.findRegion("FireBtnDown"))
        );

        fireButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                Gdx.app.log("Touch", "fire");
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        float fireButtonSize = controllerWidth * 0.15f;

        fireButton.getImageCell().width(fireButtonSize).height(fireButtonSize);
        add(fireButton).width(fireButtonSize).height(fireButtonSize).expandX().right().padRight(controllerWidth * 0.025f);
    }

    public void makeMoveButtons() {

        buttonSize = controllerWidth * 0.1f;
        defaults().pad(buttonSize * 0.075f);

        ImageButton leftBtn = makeButton("LeftBtn", "LeftBtnDown");
        add(leftBtn).width(buttonSize).height(buttonSize);

        leftBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                Gdx.app.log("Touch", "left");
                return super.touchDown(event, x, y, pointer, button);
            }
        });

        ImageButton rightBtn = makeButton("RightBtn", "RightBtnDown");
        add(rightBtn).width(buttonSize).height(buttonSize).padLeft(100);

        rightBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                Gdx.app.log("Touch", "right");
                return super.touchDown(event, x, y, pointer, button);
            }
        });
    }

    private ImageButton makeButton(String upRegion, String downRegion) {

        ImageButton button = new ImageButton(
            new TextureRegionDrawable(atlas.findRegion(upRegion)),
            new TextureRegionDrawable(atlas.findRegion(downRegion))
        );

        button.getImageCell().width(buttonSize).height(buttonSize);

        return button;
    }
}
