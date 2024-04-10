package knight.arkham.objects.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;

import static knight.arkham.helpers.AssetsHelper.loadSound;
import static knight.arkham.helpers.Constants.PIXELS_PER_METER;

public abstract class Enemy {
    public Body body;
    protected final Rectangle actualBounds;
    protected final World actualWorld;
    protected TextureRegion actualRegion;
    protected final int framesWidth;
    protected final int framesHeight;
    protected boolean isMovingRight;
    protected final Sound hitSound = loadSound("stomp.wav");

    protected Enemy(Rectangle bounds, World world, TextureRegion region) {

        actualBounds = bounds;
        actualWorld = world;
        actualRegion = region;
        framesWidth = region.getRegionWidth();
        framesHeight = region.getRegionHeight();

        body = createObjectBody();
        body.setActive(false);
    }

    protected abstract Body createObjectBody();

    protected abstract void childUpdate(float deltaTime);

    public abstract void hitByPlayer();

    public void update(float deltaTime) {

        if (body.isActive())
            childUpdate(deltaTime);
    }

    private Rectangle getDrawBounds() {

        return new Rectangle(
            body.getPosition().x - (actualBounds.width / 2 / PIXELS_PER_METER),
            body.getPosition().y - (actualBounds.height / 2 / PIXELS_PER_METER),
            actualBounds.width / PIXELS_PER_METER,
            actualBounds.height / PIXELS_PER_METER
        );
    }

    public void draw(Batch batch) {

        Rectangle drawBounds = getDrawBounds();

        batch.draw(actualRegion, drawBounds.x, drawBounds.y, drawBounds.width, drawBounds.height);
    }

    public void changeDirection(){
        isMovingRight = !isMovingRight;
    }

    public Vector2 getPixelPosition() {return body.getPosition().scl(PIXELS_PER_METER);}

    protected void applyLinearImpulse(Vector2 impulseDirection) {
        body.applyLinearImpulse(impulseDirection, body.getWorldCenter(), true);
    }

    public void dispose() {
        actualRegion.getTexture().dispose();
        hitSound.dispose();
    }
}
