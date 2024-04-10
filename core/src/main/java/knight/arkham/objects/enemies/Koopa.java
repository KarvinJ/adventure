package knight.arkham.objects.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.scenes.Hud;

import static knight.arkham.helpers.AnimationHelper.makeAnimation;
import static knight.arkham.helpers.Box2DHelper.createBody;

public class Koopa extends Enemy {
    private final Animation<TextureRegion> movingAnimation;
    private final TextureRegion hitRegion;
    private float animationTimer;
    private boolean setToDestroy;
    private boolean isDestroyed;

    public Koopa(Rectangle bounds, World world, TextureAtlas.AtlasRegion region, int totalFrames) {
        super(
            bounds, world,
            new TextureRegion(
                region, 0, 0, region.getRegionWidth() / totalFrames, region.getRegionHeight()
            )
        );

        movingAnimation = makeAnimation(region, framesWidth, framesHeight, 2, 0.2f, 0);
        hitRegion = new TextureRegion(region, framesWidth * 2, 0,  framesWidth, framesHeight);
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

        actualRegion = hitRegion;
        animationTimer = 0;
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
                applyLinearImpulse(new Vector2(4, 0));

            else if (!isMovingRight && body.getLinearVelocity().x >= -4)
                applyLinearImpulse(new Vector2(-4, 0));
        }

        if (getPixelPosition().y < 0)
            setToDestroy = true;
    }

    @Override
    public void draw(Batch batch) {
        if (!isDestroyed || animationTimer < 1)
            super.draw(batch);
    }

    private void flipRegionOnXAxis(TextureRegion region) {

        //With this code most of the time if my enemy stop with a collision it will change direction automatically.
        if ((body.getLinearVelocity().x < 0 || !isMovingRight) && region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = false;
        } else if ((body.getLinearVelocity().x > 0 || isMovingRight) && !region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = true;
        }
    }

    public void hitByPlayer() {

        hitSound.play();
        setToDestroy = true;

        Hud.addScore(100);
    }
}
