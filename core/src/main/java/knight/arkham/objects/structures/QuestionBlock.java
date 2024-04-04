package knight.arkham.objects.structures;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.Box2DHelper;

public class QuestionBlock extends InteractiveStructure {
    private final TiledMapTileSet tileSet;

    public QuestionBlock(Rectangle rectangle, World world, TiledMap tiledMap) {
        super(rectangle, world, tiledMap, "coin.wav");

        tileSet = tiledMap.getTileSets().getTileSet("OverWorld");
    }

    @Override
    protected Fixture createFixture() {

        return Box2DHelper.createStaticFixture(
            new Box2DBody(actualBounds, actualWorld, this)
        );
    }

    public void hitByPlayer() {

        collisionSound.play();

        int BLANK_COIN = 3;

//        Hay 2 sonidos que podemos tocar, uno cuando hay un coin disponible y otro cuando el bloque esta vació.
//        Comparo él, id actual del tile si es igual a un Blank_coin significa que el tile esta vació.
        if(getObjectCellInTheTileMap().getTile().getId() != BLANK_COIN){

            getObjectCellInTheTileMap().setTile(tileSet.getTile(BLANK_COIN));
        }

//        collisionWithPlayer();
//
//        getObjectCellInTheTileMap().setTile(null);
    }
}
