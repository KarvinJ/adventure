package knight.arkham.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;

import static knight.arkham.helpers.Box2DHelper.createFireBody;

public class Fireball extends GameObject {

    private boolean setToDestroy;
    private boolean isDestroyed;
    private float stateTimer;

    public Fireball(Rectangle bounds, World world) {
        super(bounds, world, new TextureRegion(new Texture("images/fireball.png")));
    }

    private void destroyBody() {

        actualWorld.destroyBody(body);
        isDestroyed = true;
    }

    @Override
    protected Body createObjectBody() {
        return createFireBody(
            new Box2DBody(actualBounds, 2, actualWorld, this)
        );
    }

    @Override
    protected void childUpdate(float deltaTime) {

        stateTimer += deltaTime;

        if (setToDestroy && !isDestroyed)
            destroyBody();

        if (stateTimer > 0.5f)
            setToDestroy = true;
    }

    @Override
    public void draw(Batch batch) {
        if (!isDestroyed)
            super.draw(batch);
    }

    public void collisionWithEnemy() {
        setToDestroy = true;
    }
}
