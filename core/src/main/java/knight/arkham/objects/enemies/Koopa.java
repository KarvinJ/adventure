package knight.arkham.objects.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.objects.Player;
import knight.arkham.scenes.Hud;

import static knight.arkham.helpers.AnimationHelper.makeAnimation;
import static knight.arkham.helpers.Box2DHelper.createBody;
import static knight.arkham.helpers.Constants.NOTHING_BIT;

public class Koopa extends Enemy {

    public enum AnimationState {WALKING, SHELL, MOVING_SHELL}
    public AnimationState currentState = AnimationState.WALKING;
    private AnimationState previousState = AnimationState.WALKING;
    private final Animation<TextureRegion> movingAnimation;
    private final TextureRegion hitRegion;
    private final TextureRegion recoveringRegion;

    public Koopa(Rectangle bounds, World world, TextureAtlas.AtlasRegion region, int totalFrames) {
        super(
            bounds, world,
            new TextureRegion(
                region, 0, 0, region.getRegionWidth() / totalFrames, region.getRegionHeight()
            )
        );

        movingAnimation = makeAnimation(region, framesWidth, framesHeight, 2, 0.4f, 0);
        hitRegion = new TextureRegion(region, framesWidth * 2, 0, framesWidth, framesHeight);
        recoveringRegion = new TextureRegion(region, framesWidth * 3, 0, framesWidth, framesHeight);
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

            setAnimationRegion(deltaTime);

            if (currentState ==  AnimationState.WALKING)
                movement(3);

            else if (currentState == AnimationState.MOVING_SHELL)
                movement(8);
        }
    }

    private void setAnimationRegion(float deltaTime) {

        if (currentState == AnimationState.SHELL && stateTimer > 5 && stateTimer < 8)
            actualRegion = recoveringRegion;

        else if (currentState == AnimationState.SHELL && stateTimer > 8)
            currentState = AnimationState.WALKING;

        else if (currentState == AnimationState.SHELL || currentState == AnimationState.MOVING_SHELL)
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
    public void hitByPlayer(Player player) {

        if (currentState == AnimationState.WALKING) {

            currentState = AnimationState.SHELL;
            stateTimer = 0;
        }
        else {

            currentState = AnimationState.MOVING_SHELL;

            boolean isPlayerBehindTheEnemy = player.getPixelPosition().x > getPixelPosition().x;

            isMovingRight = !isPlayerBehindTheEnemy;
        }

        hitSound.play();
        Hud.addScore(100);
    }

    @Override
    public void childDispose() {
        hitRegion.getTexture().dispose();
    }

    @Override
    public void hitByItem() {

        hitSound.play();
        Hud.addScore(100);

        Filter filter = new Filter();

        filter.maskBits = NOTHING_BIT;

        for (Fixture fixture : body.getFixtureList())
            fixture.setFilterData(filter);

        currentState = AnimationState.SHELL;
        stateTimer = 0;

        shouldFlipYRegion = true;

        applyLinearImpulse(new Vector2(0, 140));
    }
}
