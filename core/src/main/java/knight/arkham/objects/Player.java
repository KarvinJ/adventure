package knight.arkham.objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.Box2DHelper;
import knight.arkham.helpers.GameDataHelper;

import static knight.arkham.helpers.AnimationHelper.makeAnimation;
import static knight.arkham.helpers.AssetsHelper.loadSound;
import static knight.arkham.helpers.Constants.PIXELS_PER_METER;

public class Player extends GameObject {
    private enum AnimationState {FALLING, JUMPING, STANDING, RUNNING, DYING, GROWING}
    private AnimationState actualState = AnimationState.STANDING;
    private AnimationState previousState = AnimationState.STANDING;
    private final TextureRegion idleRegion;
    private final TextureRegion bigPlayerIdleRegion;
    private final TextureRegion jumpRegion;
    private final TextureRegion bigPlayerJumpRegion;
    private final TextureRegion dyingRegion;
    private final Animation<TextureRegion> runningAnimation;
    private final Animation<TextureRegion> bigPlayerRunningAnimation;
    private final Animation<TextureRegion> growingAnimation;

    private float animationTimer;
    private float deadTimer;
    private boolean isMovingRight;
    private boolean isDead;
    public boolean isMarioBig;
    private boolean shouldStartGrowingAnimation;
    private boolean isTimeToDefineBigMarioBody;
    private boolean isTimeToDefineLittleMarioBody;
    private final Sound jumpSound = loadSound("coin.wav");
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

        idleRegion = new TextureRegion(atlas.findRegion("little-mario"), 0, 0,  framesWidth, framesHeight);
        jumpRegion = new TextureRegion(atlas.findRegion("little-mario"), framesWidth * 5, 0, framesWidth, framesHeight);
        dyingRegion = new TextureRegion(atlas.findRegion("little-mario"), framesWidth * 6, 0, framesWidth, framesHeight);

        runningAnimation = makeAnimation(atlas.findRegion("little-mario"), framesWidth, framesHeight, 4, 0.1f, 1);

        Array<TextureRegion> growingFrames = new Array<>();

        bigPlayerIdleRegion = new TextureRegion(atlas.findRegion("big-mario"), 0, 0,  framesWidth, 32);

        growingFrames.add(bigPlayerIdleRegion);
        growingFrames.add(new TextureRegion(atlas.findRegion("big-mario"), framesWidth * 8, 0,  framesWidth, 32));
        growingFrames.add(bigPlayerIdleRegion);
        growingFrames.add(new TextureRegion(atlas.findRegion("big-mario"), framesWidth * 8, 0,  framesWidth, 32));

        growingAnimation = new Animation<>(0.2f, growingFrames);

        growingFrames.clear();

        bigPlayerRunningAnimation = makeAnimation(atlas.findRegion("big-mario"), framesWidth, 32, 4, 0.1f, 1);

        bigPlayerJumpRegion = new TextureRegion(atlas.findRegion("big-mario"), framesWidth * 5, 0, framesWidth, 32);

    }

    @Override
    protected Body createObjectBody() {

        return Box2DHelper.createBody(
            new Box2DBody(actualBounds, 10, actualWorld, this)
        );
    }


    private void movement() {

        if (Gdx.input.isKeyPressed(Input.Keys.D) && body.getLinearVelocity().x <= 12)
            applyLinearImpulse(new Vector2(6, 0));

        else if (Gdx.input.isKeyPressed(Input.Keys.A) && body.getLinearVelocity().x >= -12)
            applyLinearImpulse(new Vector2(-6, 0));

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && body.getLinearVelocity().y == 0) {

            applyLinearImpulse(new Vector2(0, 144));
//            jumpSound.play();
        }
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

        body = Box2DHelper.createBody(
            new Box2DBody(currentBounds, 10, actualWorld, this)
        );

        isTimeToDefineLittleMarioBody = false;
    }

    protected void childUpdate(float deltaTime) {

        getAnimationRegion(deltaTime);

        if (!isDead)
            movement();

        else {

            deadTimer += deltaTime;

            if (deadTimer >= 4) {

                isDead = false;
                deadTimer = 0;
                actualState = AnimationState.STANDING;

                spawnToPreviousCheckpoint();
            }
        }

        if (getPixelPosition().y < -100)
            spawnToPreviousCheckpoint();

        if (isTimeToDefineBigMarioBody)
            createBigMarioBody();

        if (isTimeToDefineLittleMarioBody)
            createLittleMarioBody();
    }


    private void spawnToPreviousCheckpoint() {

        body.setLinearVelocity(0, 0);
        body.setTransform(GameDataHelper.loadPosition(), 0);
    }

    private AnimationState getCurrentAnimationState() {

        if (isDead)
            return AnimationState.DYING;

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

        actualState = getCurrentAnimationState();

        switch (actualState) {

            case JUMPING:
                actualRegion = isMarioBig ? bigPlayerJumpRegion : jumpRegion;
                break;

            case GROWING:

                actualRegion = growingAnimation.getKeyFrame(animationTimer);
//When the animation is finished change the value to false.
                if (growingAnimation.isAnimationFinished(animationTimer))
                    shouldStartGrowingAnimation = false;
                break;

            case RUNNING:
                actualRegion = isMarioBig ? bigPlayerRunningAnimation.getKeyFrame(animationTimer, true) :
                    runningAnimation.getKeyFrame(animationTimer, true);
                break;

            case DYING:
                actualRegion = dyingRegion;
                break;

            case FALLING:
            case STANDING:
            default:
                actualRegion = isMarioBig ? bigPlayerIdleRegion : idleRegion;
        }

        flipRegionOnXAxis(actualRegion);

        animationTimer = actualState == previousState ? animationTimer + deltaTime : 0;
        previousState = actualState;
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

    public Vector2 getWorldPosition() {return body.getPosition();}
    public Vector2 getPixelPosition() {return body.getPosition().scl(PIXELS_PER_METER);}

    public void hitByEnemy() {

        if (isMarioBig) {

           isTimeToDefineLittleMarioBody = true;
           powerDownSound.play();
           isMarioBig = false;
        }
        else {

            isDead = true;
            deathSound.play();
        }
    }

    public void growPlayer() {

        shouldStartGrowingAnimation = true;
        isMarioBig = true;
        isTimeToDefineBigMarioBody = true;

        powerUpSound.play();
    }

    @Override
    public void dispose() {
        jumpSound.dispose();
        powerUpSound.dispose();
        deathSound.dispose();
        super.dispose();
    }
}
