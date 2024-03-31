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
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.GameDataHelper;

import static knight.arkham.helpers.AnimationHelper.makeAnimation;
import static knight.arkham.helpers.AssetsHelper.loadSound;
import static knight.arkham.helpers.Box2DHelper.createBody;
import static knight.arkham.helpers.Constants.PIXELS_PER_METER;

public class Player extends GameObject {
    private enum AnimationState {FALLING, JUMPING, STANDING, RUNNING, DYING, ATTACKING}
    private AnimationState actualState = AnimationState.STANDING;
    private AnimationState previousState = AnimationState.STANDING;
    private final TextureRegion idleRegion;
    private final TextureRegion jumpRegion;
    private final TextureRegion dyingRegion;
    private final Animation<TextureRegion> runningAnimation;
    private float animationTimer;
    private float deadTimer;
    private boolean isMovingRight;
    private boolean isDead;
    private final Sound jumpSound = loadSound("magic.wav");
    private final Sound deathSound = loadSound("fall.wav");

    public Player(Rectangle bounds, World world, TextureAtlas atlas) {
        super(
            bounds, world,
            new TextureRegion(atlas.findRegion("little-mario"), 0, 0, 32, 17)
        );

        idleRegion = new TextureRegion(atlas.findRegion("little-mario"), 0, 0, 32, 17);
        jumpRegion = new TextureRegion(atlas.findRegion("little-mario"), 32 * 5, 0, 32, 17);
        dyingRegion = new TextureRegion(atlas.findRegion("little-mario"), 32 * 6, 0, 32, 17);

        runningAnimation = makeAnimation(atlas.findRegion("little-mario"), 32, 17, 4, 0.1f, 1);
    }

    @Override
    protected Body createObjectBody() {

        return createBody(
            new Box2DBody(actualBounds, 5, actualWorld, this)
        );
    }

    private void movement() {

        if (Gdx.input.isKeyPressed(Input.Keys.D) && body.getLinearVelocity().x <= 12)
            applyLinealImpulse(new Vector2(6, 0));

        else if (Gdx.input.isKeyPressed(Input.Keys.A) && body.getLinearVelocity().x >= -12)
            applyLinealImpulse(new Vector2(-6, 0));

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && body.getLinearVelocity().y == 0) {

            applyLinealImpulse(new Vector2(0, 140));
            jumpSound.play();
        }
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
    }


    private void spawnToPreviousCheckpoint() {

        body.setLinearVelocity(0, 0);

        Vector2 savedPosition = GameDataHelper.loadGameData().position;

        body.setTransform(savedPosition, 0);
    }

    private AnimationState getCurrentAnimationState() {

        if (isDead)
            return AnimationState.DYING;

        else if (body.getLinearVelocity().y > 0 || (body.getLinearVelocity().y < 0 && previousState == AnimationState.JUMPING))
            return AnimationState.JUMPING;

        else if (body.getLinearVelocity().x < 0 || body.getLinearVelocity().x > 0)
            return AnimationState.RUNNING;

        else if (Gdx.input.isKeyPressed(Input.Keys.F))
            return AnimationState.ATTACKING;

        else if (body.getLinearVelocity().y < 0)
            return AnimationState.FALLING;

        else
            return AnimationState.STANDING;
    }

    private void getAnimationRegion(float deltaTime) {

        actualState = getCurrentAnimationState();

        switch (actualState) {

            case JUMPING:
                actualRegion = jumpRegion;
                break;

            case RUNNING:
                actualRegion = runningAnimation.getKeyFrame(animationTimer, true);
                break;

            case DYING:
                actualRegion = dyingRegion;
                break;

            case FALLING:
            case STANDING:
            default:
                actualRegion = idleRegion;
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
        isDead = true;
        deathSound.play();
    }

    @Override
    public void dispose() {
        jumpSound.dispose();
        super.dispose();
    }
}
