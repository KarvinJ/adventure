package knight.arkham.objects.structures;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.Box2DHelper;

public class Brick extends InteractiveStructure {

    public Brick(Rectangle rectangle, World world, TiledMap tiledMap) {
        super(rectangle, world, tiledMap, "breakBlock.wav");
    }

    @Override
    protected Fixture createFixture() {

        return Box2DHelper.createStaticFixture(
            new Box2DBody(actualBounds, actualWorld, this)
        );
    }

    public void hitByPlayer() {

        setDestroyBit();

        getObjectCellInTheTileMap().setTile(null);
    }
}
