package knight.arkham.objects.structures;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.*;

import static knight.arkham.helpers.AssetsHelper.loadSound;
import static knight.arkham.helpers.Constants.PIXELS_PER_METER;

public abstract class InteractiveStructure {
    protected final Rectangle actualBounds;
    protected final World actualWorld;
    protected final Fixture fixture;
    protected final Body body;
    private final TiledMap tiledMap;
    protected final TiledMapTileLayer.Cell actualCell;
    protected final Sound collisionSound;

    public InteractiveStructure(Rectangle rectangle, World world, TiledMap map, String soundPath) {

        actualBounds = rectangle;
        actualWorld = world;
        tiledMap = map;
        collisionSound = loadSound(soundPath);

        fixture = createFixture();
        body = fixture.getBody();

        actualCell = getBlockCellInTheTileMap();
    }

    protected abstract Fixture createFixture();

    private TiledMapTileLayer.Cell getBlockCellInTheTileMap() {

        TiledMapTileLayer mapLayer = (TiledMapTileLayer) tiledMap.getLayers().get("Blocks-Layer");

        int positionX = (int) (body.getPosition().x * PIXELS_PER_METER / 16);
        int positionY = (int) (body.getPosition().y * PIXELS_PER_METER / 16);

//        Here I search for the cell by position.
        return mapLayer.getCell(positionX, positionY);
    }

    public abstract void childDispose();

    public void dispose() {

        collisionSound.dispose();
        childDispose();
    }
}
