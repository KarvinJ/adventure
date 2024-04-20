package knight.arkham.objects.items;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.objects.Player;

import static knight.arkham.helpers.Box2DHelper.getDrawBounds;

public abstract class Item {

    public Body body;
    protected final Rectangle actualBounds;
    protected final World actualWorld;
    protected TextureRegion actualRegion;
    protected boolean isDestroyed;

    protected Item(Rectangle bounds, World world, TextureRegion region) {

        actualBounds = bounds;
        actualWorld = world;
        actualRegion = region;

        body = createObjectBody();
    }

    protected abstract Body createObjectBody();

    protected abstract void childUpdate(float deltaTime);

    public abstract void powerUpPlayer(Player player);

    protected void destroyBody() {

        actualWorld.destroyBody(body);
        isDestroyed = true;
    }

    public void update(float deltaTime) {
        childUpdate(deltaTime);
    }

    public void draw(Batch batch) {

        Rectangle drawBounds = getDrawBounds(body.getPosition(), actualBounds.width, actualBounds.height);

        batch.draw(actualRegion, drawBounds.x, drawBounds.y, drawBounds.width, drawBounds.height);
    }

    protected void applyLinearImpulse(Vector2 impulseDirection) {
        body.applyLinearImpulse(impulseDirection, body.getWorldCenter(), true);
    }

    public void dispose() {actualRegion.getTexture().dispose();}
}
