package knight.arkham.objects.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.scenes.Hud;

import java.util.Objects;

import static knight.arkham.helpers.AnimationHelper.makeAnimation;
import static knight.arkham.helpers.Box2DHelper.createBody;

public class Koopa extends Enemy {
    private enum AnimationState {WALKING, SHELL, MOVING_SHELL}
    private AnimationState currentState = AnimationState.WALKING;
    private AnimationState previousState = AnimationState.WALKING;
    private final Animation<TextureRegion> movingAnimation;
    private final TextureRegion hitRegion;
    private boolean hasBeenHit;

    public Koopa(Rectangle bounds, World world, TextureAtlas.AtlasRegion region, int totalFrames) {
        super(
            bounds, world,
            new TextureRegion(
                region, 0, 0, region.getRegionWidth() / totalFrames, region.getRegionHeight()
            )
        );

        movingAnimation = makeAnimation(region, framesWidth, framesHeight, 2, 0.2f, 0);
        hitRegion = new TextureRegion(region, framesWidth * 2, 0, framesWidth, framesHeight);
    }

    @Override
    protected Body createObjectBody() {

        return createBody(
            new Box2DBody(actualBounds, 10, actualWorld, this)
        );
    }

    @Override
    protected void childUpdate(float deltaTime) {

        stateTimer += deltaTime;

        if (setToDestroy && !isDestroyed)
            destroyBody(hitRegion);

        else if (!isDestroyed) {

            getAnimationRegion(deltaTime);

            if (currentState != AnimationState.SHELL)
                movement();
        }
    }

    private AnimationState getCurrentAnimationState() {

        if (hasBeenHit)
            return AnimationState.SHELL;
        else
            return AnimationState.WALKING;
    }

    private void getAnimationRegion(float deltaTime) {

        currentState = getCurrentAnimationState();

        if (Objects.requireNonNull(currentState) == AnimationState.SHELL)
            actualRegion = hitRegion;
        else
            actualRegion = movingAnimation.getKeyFrame(stateTimer, true);


        flipRegionOnXAxis(actualRegion);

        stateTimer = currentState == previousState ? stateTimer + deltaTime : 0;
        previousState = currentState;
    }



    @Override
    public void draw(Batch batch) {
        if (!isDestroyed || stateTimer < 1)
            super.draw(batch);
    }

    @Override
    public void hitByPlayer() {

        hitSound.play();
        hasBeenHit = true;

        Hud.addScore(100);
    }

    @Override
    public void childDispose() {
        hitRegion.getTexture().dispose();
    }
}
