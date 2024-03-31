package knight.arkham.objects;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;

import static knight.arkham.helpers.AssetsHelper.loadSound;
import static knight.arkham.helpers.Box2DHelper.createBody;

public class Enemy extends GameObject {
    private final Animation<TextureRegion> movingAnimation;
    private float animationTimer;
    public boolean isMovingRight;
    private boolean setToDestroy;
    private boolean isDestroyed;
    private final Sound hitSound;

    public Enemy(Rectangle bounds, World world, TextureAtlas.AtlasRegion region, int totalFrames) {
        super(
            bounds, world,
            new TextureRegion(
                region, 0, 0, region.getRegionWidth() / totalFrames, region.getRegionHeight()
            )
        );

        movingAnimation = null;

        hitSound = loadSound("stomp.wav");
    }

    @Override
    protected Body createObjectBody() {

        return createBody(
            new Box2DBody(actualBounds, 10, actualWorld, this)
        );
    }


    private void destroyBody() {

        actualWorld.destroyBody(body);
        isDestroyed = true;
    }

    @Override
    protected void childUpdate(float deltaTime) {

        animationTimer += deltaTime;

        if (setToDestroy && !isDestroyed)
            destroyBody();

        else if (!isDestroyed) {

            actualRegion = movingAnimation.getKeyFrame(animationTimer, true);

            flipRegionOnXAxis(actualRegion);

            if (isMovingRight && body.getLinearVelocity().x <= 4)
                applyLinealImpulse(new Vector2(2, 0));

            else if (!isMovingRight && body.getLinearVelocity().x >= -4)
                applyLinealImpulse(new Vector2(-2, 0));
        }
    }

    @Override
    public void draw(Batch batch) {
        if (!isDestroyed)
            super.draw(batch);
    }

    private void flipRegionOnXAxis(TextureRegion region) {

        if ((body.getLinearVelocity().x < 0 || !isMovingRight) && region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = false;
        } else if ((body.getLinearVelocity().x > 0 || isMovingRight) && !region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = true;
        }
    }

    public void changeDirection(){
        isMovingRight = !isMovingRight;
    }

    public void hitByPlayer() {
        hitSound.play();
        setToDestroy = true;
    }

    @Override
    public void dispose() {
        hitSound.dispose();
        super.dispose();
    }
}
