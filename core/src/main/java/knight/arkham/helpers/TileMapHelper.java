package knight.arkham.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import knight.arkham.objects.*;

import static knight.arkham.helpers.CameraController.controlCameraPosition;
import static knight.arkham.helpers.Constants.PIXELS_PER_METER;
import static knight.arkham.helpers.Constants.TIME_STEP;
import static knight.arkham.helpers.GameDataHelper.saveGameData;

public class TileMapHelper {
    private final TiledMap tiledMap;
    private final TextureAtlas atlas;
    private final World world;
    private final Box2DDebugRenderer debugRenderer;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Player player;
    private final Array<GameObject> gameObjects;
    private float accumulator;
    private boolean isDebugCamera;
    private boolean isDebugRendererActive;

    public TileMapHelper(String mapFilePath, String atlasFilePath) {

        tiledMap = new TmxMapLoader().load(mapFilePath);
        atlas = new TextureAtlas(atlasFilePath);

        world = new World(new Vector2(0, -40), true);
        world.setContactListener(new GameContactListener());

        player = new Player(new Rectangle(20, 65, 32, 16), world, atlas);

        saveGameData(new GameData("first", player.getWorldPosition()));

        gameObjects = new Array<>();

        mapRenderer = setupMap();
        debugRenderer = new Box2DDebugRenderer();
    }

    public OrthogonalTiledMapRenderer setupMap() {

        for (MapLayer mapLayer : tiledMap.getLayers())
            parseMapObjectsToBox2DBodies(mapLayer.getObjects(), mapLayer.getName());

        return new OrthogonalTiledMapRenderer(tiledMap, 1 / PIXELS_PER_METER);
    }

    private void parseMapObjectsToBox2DBodies(MapObjects mapObjects, String objectsName) {

        for (MapObject mapObject : mapObjects) {

            Rectangle mapRectangle = getTileMapRectangle(((RectangleMapObject) mapObject).getRectangle());

            switch (objectsName) {

                case "Enemies":
                    gameObjects.add(new Enemy(mapRectangle, world, atlas.findRegion("snake"), 2));
                    break;

                case "Enemy-Stopper":
                    Box2DHelper.createFixture(new Box2DBody(mapRectangle, world, null));
                    break;

                default:
                    Box2DHelper.createBody(new Box2DBody(mapRectangle, world, null));
                    break;
            }
        }
    }

    private Rectangle getTileMapRectangle(Rectangle rectangle){
        return new Rectangle(
            rectangle.x + rectangle.width / 2,
            rectangle.y + rectangle.height / 2,
            rectangle.width, rectangle.height
        );
    }

    public void updateCameraPosition(OrthographicCamera camera) {

        controlCameraPosition(camera);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F5))
            isDebugCamera = !isDebugCamera;

        if (!isDebugCamera)
            camera.position.set(player.getWorldPosition().x, 5.2f, 0);

        camera.update();
    }

    public void update(float deltaTime, OrthographicCamera camera) {

        player.update(deltaTime);

        updateCameraPosition(camera);

        for (GameObject gameObject : gameObjects)
            gameObject.update(deltaTime);

        doPhysicsTimeStep(deltaTime);
    }

    private void doPhysicsTimeStep(float deltaTime) {

        float frameTime = Math.min(deltaTime, 0.25f);

        accumulator += frameTime;

        while(accumulator >= TIME_STEP) {
            world.step(TIME_STEP, 6,2);
            accumulator -= TIME_STEP;
        }
    }

    public void draw(OrthographicCamera camera){

        mapRenderer.setView(camera);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F1))
            isDebugRendererActive = !isDebugRendererActive;

        if (!isDebugRendererActive) {
            mapRenderer.render();

            mapRenderer.getBatch().setProjectionMatrix(camera.combined);

            mapRenderer.getBatch().begin();

            player.draw(mapRenderer.getBatch());

            for (GameObject gameObject : gameObjects)
                gameObject.draw(mapRenderer.getBatch());

            mapRenderer.getBatch().end();
        }

        else
            debugRenderer.render(world, camera.combined);
    }

    public void dispose(){

        player.dispose();
        tiledMap.dispose();
        atlas.dispose();
        mapRenderer.dispose();
        world.dispose();
        debugRenderer.dispose();

        for (GameObject gameObject : gameObjects)
            gameObject.dispose();
    }
}
