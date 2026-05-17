package knight.nameless.mario.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import knight.nameless.mario.Adventure;
import knight.nameless.mario.ui.*;
import knight.nameless.mario.objects.*;
import knight.nameless.mario.objects.enemies.*;
import knight.nameless.mario.objects.structures.*;
import knight.nameless.mario.objects.items.*;
import knight.nameless.mario.helpers.*;

import java.util.HashMap;
import java.util.Map;

import static knight.nameless.mario.helpers.AssetsHelper.loadMusic;
import static knight.nameless.mario.helpers.Constants.PIXELS_PER_METER;
import static knight.nameless.mario.helpers.Constants.TIME_STEP;

public class GameScreen extends ScreenAdapter {
    private final Adventure game = Adventure.INSTANCE;
    private final OrthographicCamera camera;
    private final Hud hud;
    private final Stage stage;
    public final TiledMap tiledMap;
    private final TextureAtlas atlas = new TextureAtlas("images/character.atlas");
    public final World world;
    private final Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final knight.nameless.mario.objects.Player player;
    private final Array<Enemy> enemies = new Array<>();
    private final Array<InteractiveStructure> structures = new Array<>();
    private final Array<Item> items = new Array<>();
    private final Array<Fireball> fireBalls = new Array<>();
    public final HashMap<Class<?>, Rectangle> itemsToSpawn = new HashMap<>();
    private final Music music = loadMusic("mario_music.ogg");
    private float accumulator;
    private boolean isDebugCamera;
    private boolean isDebugRendererActive;
    private boolean isGameOver;

    public GameScreen() {

        camera = game.camera;

        world = new World(new Vector2(0, -40), true);
        world.setContactListener(new GameContactListener());

        player = new Player(new Rectangle(150, 40, 32, 16), world, atlas, 8);

        tiledMap = new TmxMapLoader().load("maps/level1.tmx");
        mapRenderer = setupMap(tiledMap);

        music.play();
        music.setVolume(0.2f);
        music.setLooping(true);

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

    private OrthogonalTiledMapRenderer setupMap(TiledMap tiledMap) {

        for (MapLayer mapLayer : tiledMap.getLayers())
            parseMapObjectsToBox2DBodies(mapLayer.getObjects(), mapLayer.getName());

        return new OrthogonalTiledMapRenderer(tiledMap, 1 / PIXELS_PER_METER);
    }

    private void parseMapObjectsToBox2DBodies(MapObjects mapObjects, String objectsName) {

        for (MapObject mapObject : mapObjects) {

            Rectangle mapRectangle = getTileMapRectangle(((RectangleMapObject) mapObject).getRectangle());

            switch (objectsName) {

                case "Enemies":

                    if (mapObject.getName().equals("goomba"))
                        enemies.add(new Goomba(mapRectangle, world, atlas.findRegion("goomba"), 3));
                    else
                        enemies.add(new Koopa(mapRectangle, world, atlas.findRegion("turtle"), 4));
                    break;

                case "Blocks":

                    if (mapObject.getName().equals("question"))
                        structures.add(new QuestionBlock(mapRectangle, mapObject, this));
                    else
                        structures.add(new Brick(mapRectangle, world, tiledMap, mapObject));
                    break;

                case "Enemy-Stopper":
                    Box2DHelper.createStaticFixture(new Box2DBody(mapRectangle, world, null));
                    break;

                default:
                    Box2DHelper.createBody(new Box2DBody(mapRectangle, world, null));
                    break;
            }
        }
    }

    private Rectangle getTileMapRectangle(Rectangle rectangle) {
        return new Rectangle(
            rectangle.x + rectangle.width / 2,
            rectangle.y + rectangle.height / 2,
            rectangle.width, rectangle.height
        );
    }

    public static void controlCameraPosition(OrthographicCamera camera) {

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            camera.position.x += 0.1f;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            camera.position.x -= 0.1f;

        if (Gdx.input.isKeyPressed(Input.Keys.UP))
            camera.position.y += 0.1f;

        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
            camera.position.y -= 0.1f;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F3))
            camera.zoom += 0.1f;

        if (Gdx.input.isKeyJustPressed(Input.Keys.F4))
            camera.zoom -= 0.1f;
    }

    public void updateCameraPosition(OrthographicCamera camera) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5))
            isDebugCamera = !isDebugCamera;

        if (!isDebugCamera && player.getPixelPosition().x > 145 && player.getCurrentState() != Player.AnimationState.DYING)
            camera.position.set(player.getWorldPosition().x, 7, 0);

        controlCameraPosition(camera);

        camera.update();
    }

    private void shootFire() {

        var playerPosition = player.getPixelPosition();

        float fireballPosition = playerPosition.x - 10;

        var impulseDirection = new Vector2(-8, 0);

        if (player.isMovingRight) {

            fireballPosition = playerPosition.x + 10;
            impulseDirection.x = 8;
        }

        var fireBounds = new Rectangle(fireballPosition, playerPosition.y, 8, 8);

        var fireBall = new Fireball(fireBounds, world);

        fireBall.body.applyLinearImpulse(impulseDirection, fireBall.body.getWorldCenter(), true);

        fireBalls.add(fireBall);
    }

    public void update(float deltaTime) {

        if (player.getCurrentState() == Player.AnimationState.DYING)
            music.pause();

        if (player.getCurrentState() == Player.AnimationState.DYING && player.getStateTimer() > 2.6f)
            isGameOver = true;

        if (isGameOver)
            game.setScreen(new GameOverScreen());

        else {

            player.update(deltaTime);

            if (player.hasFirePower && Gdx.input.isKeyJustPressed(Input.Keys.C))
                shootFire();

            updateCameraPosition(camera);

            for (Item item : items)
                item.update(deltaTime);

            for (Fireball fireBall : fireBalls)
                fireBall.update(deltaTime);

            for (Enemy enemy : enemies) {

                var distanceBetweenPlayerAndEnemy = player.getPixelPosition().dst(enemy.getPixelPosition());

                if (!enemy.body.isActive() && distanceBetweenPlayerAndEnemy < 300)
                    enemy.body.setActive(true);

                enemy.update(deltaTime);
            }

            initializeItems();

            doPhysicsTimeStep(deltaTime);
        }
    }

    private void doPhysicsTimeStep(float deltaTime) {

        float frameTime = Math.min(deltaTime, 0.25f);

        accumulator += frameTime;

        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }
    }

    private void initializeItems() {

        for (Map.Entry<Class<?>, Rectangle> entry : itemsToSpawn.entrySet()) {

            var itemClassType = entry.getKey();
            var itemBounds = entry.getValue();

            if (itemClassType == Mushroom.class)
                items.add(new Mushroom(itemBounds, world, atlas.findRegion("items")));

            else if (itemClassType == Flower.class)
                items.add(new Flower(itemBounds, world, atlas.findRegion("items")));

            else
                items.add(new GreenMushroom(itemBounds, world, atlas.findRegion("items")));

            itemsToSpawn.clear();
        }
    }

    public void draw() {

        if (!isGameOver) {

            mapRenderer.setView(camera);

            if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
                isDebugRendererActive = !isDebugRendererActive;

            if (!isDebugRendererActive) {

                mapRenderer.render();

                mapRenderer.getBatch().setProjectionMatrix(camera.combined);

                mapRenderer.getBatch().begin();

                player.draw(mapRenderer.getBatch());

                for (Item item : items)
                    item.draw(mapRenderer.getBatch());

                for (Enemy enemy : enemies)
                    enemy.draw(mapRenderer.getBatch());

                for (Fireball fireBall : fireBalls)
                    fireBall.draw(mapRenderer.getBatch());

                mapRenderer.getBatch().end();
            }
            else
                debugRenderer.render(world, camera.combined);
        }

    }

    @Override
    public void render(float deltaTime) {

        ScreenUtils.clear(0, 0, 0, 0);

        update(deltaTime);

        draw();

        hud.update(deltaTime);
        hud.stage.draw();
    }

    @Override
    public void hide() {
//        dispose();
    }

    @Override
    public void dispose() {
        hud.dispose();
        atlas.dispose();
        tiledMap.dispose();
        mapRenderer.dispose();
        world.dispose();
        debugRenderer.dispose();
        music.dispose();

        for (Item item : items)
            item.dispose();

        for (InteractiveStructure structure : structures)
            structure.dispose();

        for (Enemy enemy : enemies)
            enemy.dispose();
    }
}
