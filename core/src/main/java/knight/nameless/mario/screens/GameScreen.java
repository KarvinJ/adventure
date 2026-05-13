package knight.nameless.mario.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import knight.nameless.mario.Adventure;
import knight.nameless.mario.helpers.TileMapHelper;
import knight.nameless.mario.ui.*;

public class GameScreen extends ScreenAdapter {

    private final Adventure game = Adventure.INSTANCE;
    private final OrthographicCamera camera;
    private final TileMapHelper mapHelper;
    private final Hud hud;
    private final Stage stage;

    public GameScreen() {

        camera = game.camera;

        mapHelper = new TileMapHelper("maps/level1.tmx");

        hud = new Hud();

        Viewport viewport = new FitViewport(1200, 1200);
        stage = new Stage(viewport);
        Table root = new Table();

        root.bottom();
        stage.addActor(root);
        root.setFillParent(true);
        Gdx.input.setInputProcessor(stage);

        float controllerWidth = Gdx.graphics.getWidth() * 0.45f;
        float controllerHeight = controllerWidth * 0.3f;

        CButtons controllerButtons = new CButtons(controllerWidth, controllerHeight);
        controllerButtons.setTransform(true);
        controllerButtons.setOrigin(Align.center);
        root.add(controllerButtons).width(controllerWidth).height(controllerHeight);
        controllerButtons.addAction(Actions.scaleTo(0.15f, 0.15f));
        controllerButtons.addAction(Actions.scaleTo(1f, 1f, 0.75f, Interpolation.swingOut));
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height);
    }

    @Override
    public void render(float deltaTime) {

        ScreenUtils.clear(0, 0, 0, 0);

        mapHelper.update(deltaTime, camera);

        mapHelper.draw(camera);

//        stage.act(deltaTime);
//        stage.draw();

        hud.update(deltaTime);
        hud.stage.draw();
    }

    @Override
    public void hide() {
//        dispose();
    }

    @Override
    public void dispose() {
        mapHelper.dispose();
        hud.dispose();
    }
}
