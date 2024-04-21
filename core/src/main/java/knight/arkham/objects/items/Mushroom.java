package knight.arkham.objects.items;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.objects.Player;
import knight.arkham.scenes.Hud;

import static knight.arkham.helpers.Box2DHelper.createBody;

public class Mushroom extends Item {

    private boolean isMovingRight = true;
    private boolean setToDestroy;

    public Mushroom(Rectangle bounds, World world, TextureAtlas.AtlasRegion region) {
        super(
            bounds, world, new TextureRegion(region, 0, 0, 16, region.getRegionHeight())
        );
    }

    @Override
    protected Body createObjectBody() {
        return createBody(new Box2DBody(actualBounds, 2, actualWorld, this));
    }

    @Override
    protected void childUpdate(float deltaTime) {

        if (setToDestroy && !isDestroyed)
            destroyBody();

        else if (!isDestroyed) {

            if (isMovingRight && body.getLinearVelocity().x <= 2)
                applyLinearImpulse(new Vector2(2, 0));

            else if (!isMovingRight && body.getLinearVelocity().x >= -2)
                applyLinearImpulse(new Vector2(-2, 0));
        }
    }

    @Override
    public void draw(Batch batch) {
        if (!isDestroyed)
            super.draw(batch);
    }

    @Override
    public void powerUpPlayer(Player player) {

        setToDestroy = true;

        if (!Player.isPlayerBig)
            player.growPlayer();

        Hud.addScore(500);
    }

    public void changeDirection() {
        isMovingRight = !isMovingRight;
    }
}
