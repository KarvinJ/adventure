package knight.nameless.mario.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import knight.nameless.mario.helpers.Box2DBody;
import knight.nameless.mario.helpers.Box2DHelper;

import static knight.nameless.mario.helpers.AnimationHelper.makeAnimation;
import static knight.nameless.mario.helpers.AssetsHelper.loadSound;
import static knight.nameless.mario.helpers.Box2DHelper.createBody;
import static knight.nameless.mario.helpers.Constants.NOTHING_BIT;
import static knight.nameless.mario.helpers.Constants.PIXELS_PER_METER;

public class Player extends GameObject {

    public enum AnimationState {FALLING, JUMPING, STANDING, RUNNING, DYING, GROWING, CROUCH}
    private AnimationState currentState = AnimationState.STANDING;
    private AnimationState previousState = AnimationState.STANDING;
    private final TextureRegion idleRegion;
    private final TextureRegion bigIdleRegion;
    private final TextureRegion flowerIdleRegion;
    private final TextureRegion jumpRegion;
    private final TextureRegion bigJumpRegion;
    private final TextureRegion flowerJumpRegion;
    private final TextureRegion bigCrouchRegion;
    private final TextureRegion flowerCrouchRegion;
    private final TextureRegion dyingRegion;
    private final Animation<TextureRegion> growingAnimation;
    private final Animation<TextureRegion> runningAnimation;
    private final Animation<TextureRegion> bigRunningAnimation;
    private final Animation<TextureRegion> flowerRunningAnimation;
    private float invincibilityTimer;
    public boolean isMovingRight;
    private boolean isDead;
    public static boolean isPlayerBig;
    public boolean hasFirePower;
    private boolean shouldStartGrowingAnimation;
    private boolean isTimeToDefineBigMarioBody;
    private boolean isTimeToDefineLittleMarioBody;
    private final Sound deathSound = loadSound("mariodie.wav");
    private final Sound powerUpSound = loadSound("powerup.wav");
    private final Sound powerDownSound = loadSound("powerdown.wav");

    public Player(Rectangle bounds, World world, TextureAtlas atlas, int totalFrames) {
        super(
            bounds, world,
            new TextureRegion(
                atlas.findRegion("little-mario"), 0, 0,
                atlas.findRegion("little-mario").getRegionWidth() / totalFrames,
                atlas.findRegion("little-mario").getRegionHeight()
            )
        );

        //I initialized this here, because of a bug when mario dies. For some reason if my player dies when is big,
        // this variable stay with the value true, and for that reason I initialized the variable here.
        isPlayerBig = false;

        idleRegion = new TextureRegion(atlas.findRegion("little-mario"), 0, 0,  framesWidth, framesHeight);
        jumpRegion = new TextureRegion(atlas.findRegion("little-mario"), framesWidth * 5, 0, framesWidth, framesHeight);
        dyingRegion = new TextureRegion(atlas.findRegion("little-mario"), framesWidth * 6, 0, framesWidth, framesHeight);

        runningAnimation = makeAnimation(
            atlas.findRegion("little-mario"), framesWidth, 4, 0.1f, 1
        );

        Array<TextureRegion> growingFrames = new Array<>();

        bigIdleRegion = new TextureRegion(atlas.findRegion("big-mario"), 0, 0,  framesWidth, 32);
        flowerIdleRegion = new TextureRegion(atlas.findRegion("flower-mario"), 0, 0,  framesWidth, 32);

        growingFrames.add(bigIdleRegion);
        growingFrames.add(new TextureRegion(atlas.findRegion("big-mario"), framesWidth * 8, 0,  framesWidth, 32));
        growingFrames.add(bigIdleRegion);
        growingFrames.add(new TextureRegion(atlas.findRegion("big-mario"), framesWidth * 8, 0,  framesWidth, 32));

        growingAnimation = new Animation<>(0.2f, growingFrames);

        growingFrames.clear();

        bigRunningAnimation = makeAnimation(
            atlas.findRegion("big-mario"), framesWidth,  4, 0.1f, 1
        );

        bigJumpRegion = new TextureRegion(
            atlas.findRegion("big-mario"), framesWidth * 5, 0, framesWidth, 32
        );

        flowerRunningAnimation = makeAnimation(
            atlas.findRegion("flower-mario"), framesWidth, 4, 0.1f, 1
        );

        flowerJumpRegion = new TextureRegion(
            atlas.findRegion("flower-mario"), framesWidth * 5, 0, framesWidth, 32
        );

        bigCrouchRegion = new TextureRegion(
            atlas.findRegion("big-mario"), framesWidth * 6, 0, framesWidth, 32
        );

        flowerCrouchRegion = new TextureRegion(
            atlas.findRegion("flower-mario"), framesWidth * 6, 0, framesWidth, 32
        );
    }

    @Override
    protected Body createObjectBody() {

        return createBody(
            new Box2DBody(actualBounds, 10, actualWorld, this)
        );
    }

    private void movement(float deltaTime) {

        float playerSpeed = 350;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && body.getLinearVelocity().y == 0)
            applyLinearImpulse(new Vector2(0, 144));

        if (Gdx.input.isKeyPressed(Input.Keys.D) && body.getLinearVelocity().x <= 10)
            applyLinearImpulse(new Vector2(playerSpeed * deltaTime, 0));

        if (Gdx.input.isKeyPressed(Input.Keys.A) && body.getLinearVelocity().x >= -10)
            applyLinearImpulse(new Vector2(-playerSpeed * deltaTime, 0));
    }

    private void applyLinearImpulse(Vector2 impulseDirection) {
        body.applyLinearImpulse(impulseDirection, body.getWorldCenter(), true);
    }

    private void createBigMarioBody() {

        actualWorld.destroyBody(body);

        var actualPosition = getPixelPosition();

        actualBounds.height = actualBounds.height * 2;
        var currentBounds = new Rectangle(actualPosition.x, actualPosition.y, actualBounds.width, actualBounds.height);

        body = Box2DHelper.createBigPlayerBody(
            new Box2DBody(currentBounds, 10, actualWorld, this)
        );

        isTimeToDefineBigMarioBody = false;
    }

    private void createLittleMarioBody() {

        actualWorld.destroyBody(body);

        var actualPosition = getPixelPosition();

        actualBounds.height = actualBounds.height / 2;
        var currentBounds = new Rectangle(actualPosition.x, actualPosition.y, actualBounds.width, actualBounds.height);

        body = createBody(
            new Box2DBody(currentBounds, 10, actualWorld, this)
        );

        isTimeToDefineLittleMarioBody = false;
    }

    protected void childUpdate(float deltaTime) {

        getAnimationRegion(deltaTime);

        invincibilityTimer += deltaTime;

        if (!isDead)
            movement(deltaTime);

        if (getPixelPosition().y < -10)
            isDead = true;

        if (isTimeToDefineBigMarioBody)
            createBigMarioBody();

        if (isTimeToDefineLittleMarioBody)
            createLittleMarioBody();
    }

    public Vector2 getPixelPosition() {return body.getPosition().scl(PIXELS_PER_METER);}

    private AnimationState getCurrentAnimationState() {

        if (isDead)
            return AnimationState.DYING;

        else if ((isPlayerBig || hasFirePower) && Gdx.input.isKeyPressed(Input.Keys.S))
            return AnimationState.CROUCH;

        else if (shouldStartGrowingAnimation)
            return AnimationState.GROWING;

        else if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && previousState == AnimationState.JUMPING))
            return AnimationState.JUMPING;

        else if (body.getLinearVelocity().x != 0)
            return AnimationState.RUNNING;

        else if (body.getLinearVelocity().y < 0)
            return AnimationState.FALLING;

        else
            return AnimationState.STANDING;
    }

    private void getAnimationRegion(float deltaTime) {

        currentState = getCurrentAnimationState();

        switch (currentState) {

            case JUMPING:

                if (isPlayerBig && !hasFirePower)
                    actualRegion = bigJumpRegion;

                else if (hasFirePower && isPlayerBig)
                    actualRegion = flowerJumpRegion;

                else
                    actualRegion = jumpRegion;
                break;

            case CROUCH:

                if (isPlayerBig && !hasFirePower)
                    actualRegion = bigCrouchRegion;

                else if (hasFirePower && isPlayerBig)
                    actualRegion = flowerCrouchRegion;

                break;

            case GROWING:

                actualRegion = growingAnimation.getKeyFrame(stateTimer);
//When the animation is finished change the value to false.
                if (growingAnimation.isAnimationFinished(stateTimer))
                    shouldStartGrowingAnimation = false;
                break;

            case RUNNING:

                if (isPlayerBig && !hasFirePower)
                    actualRegion = bigRunningAnimation.getKeyFrame(stateTimer, true);

                else if (hasFirePower && isPlayerBig)
                    actualRegion = flowerRunningAnimation.getKeyFrame(stateTimer, true);

                else
                    actualRegion = runningAnimation.getKeyFrame(stateTimer, true);

                break;

            case DYING:
                actualRegion = dyingRegion;
                break;

            case FALLING:
            case STANDING:
            default:

                if (isPlayerBig && !hasFirePower)
                    actualRegion = bigIdleRegion;

                else if (hasFirePower && isPlayerBig)
                    actualRegion = flowerIdleRegion;

                else
                    actualRegion = idleRegion;
        }

        flipRegionOnXAxis(actualRegion);

        stateTimer = currentState == previousState ? stateTimer + deltaTime : 0;
        previousState = currentState;
    }

    private void flipRegionOnXAxis(TextureRegion region) {

        if ((body.getLinearVelocity().x > 0 || isMovingRight) && region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = true;
        }
        if ((body.getLinearVelocity().x < 0 || !isMovingRight) && !region.isFlipX()) {

            region.flip(true, false);
            isMovingRight = false;
        }
    }

    public void hitByEnemy() {

        if (isPlayerBig) {

           isTimeToDefineLittleMarioBody = true;
           powerDownSound.play();
           isPlayerBig = false;
           invincibilityTimer = 0;
           hasFirePower = false;
        }
        //Don't know if this is the best option to manage the invincibility of my player. But it works well.
        else if (invincibilityTimer > 1.5f){

            isDead = true;
            deathSound.play();

            Filter filter = new Filter();

            filter.maskBits = NOTHING_BIT;

            for (Fixture fixture : body.getFixtureList())
                fixture.setFilterData(filter);

            applyLinearImpulse(new Vector2(0, 140));
        }
    }

    public void growPlayer() {

        shouldStartGrowingAnimation = true;
        isPlayerBig = true;
        isTimeToDefineBigMarioBody = true;
        powerUpSound.play();
    }

    public void firePlayer() {

        hasFirePower = true;
        powerUpSound.play();
    }

    public void extraLive() {
        powerUpSound.play();
    }

    public AnimationState getCurrentState() {
        return currentState;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public Vector2 getWorldPosition() {return body.getPosition();}

    @Override
    public void dispose() {
        powerUpSound.dispose();
        deathSound.dispose();
        super.dispose();
    }
}
