package knight.arkham.objects.structures;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.Box2DHelper;

import static knight.arkham.helpers.AssetsHelper.loadSound;

public class QuestionBlock extends InteractiveStructure {
    private final TiledMapTileSet tileSet;
    private final MapObject mapObject;
    private final Sound bumpSound = loadSound("bump.wav");
    private final Sound spawnItemSound = loadSound("spawn.wav");

    public QuestionBlock(Rectangle bounds, World world, TiledMap tiledMap, MapObject mapObject) {
        super(bounds, world, tiledMap, "coin.wav");

        tileSet = tiledMap.getTileSets().getTileSet("OverWorld");
        this.mapObject = mapObject;
    }

    @Override
    protected Fixture createFixture() {

        return Box2DHelper.createStaticFixture(
            new Box2DBody(actualBounds, actualWorld, this)
        );
    }

    public void hitByPlayer() {

        int BLANK_COIN = 3;

        var actualCell = getObjectCellInTheTileMap();

        if (actualCell.getTile().getId() == BLANK_COIN)
            bumpSound.play();

        else {

            if (mapObject.getProperties().containsKey("mushroom"))
                spawnItemSound.play();
            else
                collisionSound.play();

            actualCell.setTile(tileSet.getTile(BLANK_COIN));
        }
    }
}
