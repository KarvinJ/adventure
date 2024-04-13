package knight.arkham.objects.structures;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.objects.Player;

import static knight.arkham.helpers.AssetsHelper.loadSound;
import static knight.arkham.helpers.Box2DHelper.createStaticFixture;
import static knight.arkham.helpers.Constants.DESTROYED_BIT;

public class Brick extends InteractiveStructure {
    private final Sound breakBlockSound = loadSound("breakBlock.wav");

    public Brick(Rectangle rectangle, World world, TiledMap tiledMap) {
        super(rectangle, world, tiledMap, "bump.wav");
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

    public void hitByPlayer() {

        if (Player.isMarioBig) {

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
