package knight.nameless.mario;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import knight.nameless.mario.screens.GameScreen;

import static knight.nameless.mario.helpers.Constants.PIXELS_PER_METER;

public class Adventure extends Game {

    public static Adventure INSTANCE;
    public OrthographicCamera camera;
    public Viewport viewport;
    public int screenWidth;
    public int screenHeight;

    public Adventure() {
        INSTANCE = this;
    }

    @Override
    public void create() {

        camera = new OrthographicCamera();

        screenWidth = 960;
        screenHeight = 720;

        viewport = new FitViewport(screenWidth / PIXELS_PER_METER, screenHeight / PIXELS_PER_METER, camera);

        camera.zoom -= 0.7f;

        camera.position.set(screenWidth / 2f / PIXELS_PER_METER, screenHeight / 2f / PIXELS_PER_METER, 0);

        setScreen(new GameScreen());
    }
}
