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
import knight.arkham.objects.items.ItemDefinition;
import knight.arkham.objects.items.Mushroom;
import knight.arkham.objects.structures.Brick;
import knight.arkham.objects.structures.QuestionBlock;

import static knight.arkham.helpers.CameraController.controlCameraPosition;
import static knight.arkham.helpers.Constants.*;
import static knight.arkham.helpers.GameDataHelper.savePosition;

public class TileMapHelper {
    public final TiledMap tiledMap;
    private final TextureAtlas atlas;
    public final World world;
    private final Box2DDebugRenderer debugRenderer;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Player player;
    private final Array<GameObject> gameObjects;
    private final Array<ItemDefinition> itemsToSpawn;
    private float accumulator;
    private boolean isDebugCamera;
    private boolean isDebugRendererActive;

    public TileMapHelper(String mapFilePath, String atlasFilePath) {

        world = new World(new Vector2(0, -40), true);
        world.setContactListener(new GameContactListener());

        atlas = new TextureAtlas(atlasFilePath);

        player = new Player(new Rectangle(150, 40, 32, 16), world, atlas, 8);
        savePosition(player.getWorldPosition());

        gameObjects = new Array<>();
        itemsToSpawn = new Array<>();

        tiledMap = new TmxMapLoader().load(mapFilePath);
        mapRenderer = setupMap(tiledMap);

        debugRenderer = new Box2DDebugRenderer();
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
                        gameObjects.add(new Enemy(mapRectangle, world, atlas.findRegion("goomba"), 3));
                    else
                        gameObjects.add(new Enemy(mapRectangle, world, atlas.findRegion("turtle"), 4));
                    break;

                case "Blocks":

                    if (mapObject.getName().equals("question"))
                        new QuestionBlock(mapRectangle, mapObject, this);
                    else
                        new Brick(mapRectangle, world, tiledMap);
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

        if (!isDebugCamera && player.getPixelPosition().x > 145)
            camera.position.set(player.getWorldPosition().x, 7, 0);

        camera.update();
    }

    public void update(float deltaTime, OrthographicCamera camera) {

        player.update(deltaTime);

        updateCameraPosition(camera);

        for (GameObject gameObject : gameObjects)
            gameObject.update(deltaTime);

        initializeItems();

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

    private void initializeItems() {

        for (ItemDefinition item : itemsToSpawn) {

            if (item.classType == Mushroom.class) {
                gameObjects.add(new Mushroom(item.bounds, world, atlas.findRegion("items")));

                itemsToSpawn.clear();
            }
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

    public void setItemToSpawn(ItemDefinition itemDefinition) {

        itemsToSpawn.add(itemDefinition);
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
