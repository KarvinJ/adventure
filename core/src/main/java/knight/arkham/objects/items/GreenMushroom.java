package knight.arkham.objects.items;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.objects.GameObject;
import knight.arkham.objects.Player;
import knight.arkham.scenes.Hud;

import static knight.arkham.helpers.Box2DHelper.createBody;

public class GreenMushroom extends GameObject {
    private boolean setToDestroy;
    private boolean isDestroyed;

    public GreenMushroom(Rectangle bounds, World world, TextureAtlas.AtlasRegion region) {
        super(
            bounds, world, new TextureRegion(region, 16, 0, 16, region.getRegionHeight())
        );
    }

    @Override
    protected Body createObjectBody() {
        return createBody(new Box2DBody(actualBounds, 2, actualWorld, this));
    }

    private void destroyBody() {

        actualWorld.destroyBody(body);
        isDestroyed = true;
    }
    @Override
    protected void childUpdate(float deltaTime) {

        if (setToDestroy && !isDestroyed)
            destroyBody();

        else if (!isDestroyed) {

            if (body.getLinearVelocity().x <= 2)
                applyLinearImpulse(new Vector2(2, 0));
        }
    }

    @Override
    public void draw(Batch batch) {
        if (!isDestroyed)
            super.draw(batch);
    }

    public void lifeUpPlayer(Player player) {

        setToDestroy = true;

        player.firePlayer();

        Hud.addScore(500);
    }
}
