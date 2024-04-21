package knight.arkham.objects.structures;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.objects.Player;
import knight.arkham.scenes.Hud;

import static knight.arkham.helpers.AssetsHelper.loadSound;
import static knight.arkham.helpers.Box2DHelper.createStaticFixture;
import static knight.arkham.helpers.Constants.DESTROYED_BIT;

public class Brick extends InteractiveStructure {

    private final MapObject mapObject;
    private final TiledMapTileSet tileSet;
    private int availableCoins = 15;
    private final Sound breakBlockSound = loadSound("breakBlock.wav");
    private final Sound coinSound = loadSound("coin.wav");

    public Brick(Rectangle bounds, World world, TiledMap tiledMap, MapObject mapObject) {
        super(bounds, world, tiledMap, "bump.wav");

        tileSet = tiledMap.getTileSets().getTileSet("OverWorld");
        this.mapObject = mapObject;
    }

    @Override
    protected Fixture createFixture() {

        return createStaticFixture(
            new Box2DBody(actualBounds, actualWorld, this)
        );
    }

    private void setDestroyBit() {

        Filter filter = new Filter();

        filter.categoryBits = DESTROYED_BIT;
        fixture.setFilterData(filter);
    }

    @Override
    public void hitByPlayer() {

        if (mapObject.getProperties().containsKey("multiple-coins")) {

            if (availableCoins > 0) {

                coinSound.play();
                availableCoins--;
                Hud.addCoin(1);
                Hud.addScore(200);
            }
            else {

                int BLANK_COIN = 3;

                actualCell.setTile(tileSet.getTile(BLANK_COIN));
                collisionSound.play();
            }
        }

        else if (Player.isPlayerBig) {

            setDestroyBit();

            actualCell.setTile(null);

            breakBlockSound.play();
        }
        else
            collisionSound.play();
    }

    @Override
    public void childDispose() {
        breakBlockSound.dispose();
    }
}
