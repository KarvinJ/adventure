package knight.arkham.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.ScreenUtils;
import knight.arkham.Adventure;
import knight.arkham.helpers.TileMapHelper;

import static knight.arkham.helpers.AssetsHelper.loadMusic;

public class GameScreen extends ScreenAdapter {
    private final Adventure game;
    private final OrthographicCamera camera;
    private final TileMapHelper mapHelper;
    private final Music music = loadMusic("mario_music.ogg");


    public GameScreen() {

        game = Adventure.INSTANCE;

        camera = game.camera;

        mapHelper = new TileMapHelper("maps/level1.tmx", "images/character.atlas");

        music.play();
        music.setVolume(0.2f);
        music.setLooping(true);
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
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        mapHelper.dispose();
        music.dispose();
    }
}
