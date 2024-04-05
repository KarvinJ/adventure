package knight.arkham.objects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;

import static knight.arkham.helpers.Box2DHelper.createBody;

public class Mushroom extends GameObject {

    public Mushroom(Rectangle bounds, World world) {
        //Need to use texture atlas to do this correctly.
        super(bounds, world, new TextureRegion(new Texture("images/mushroom.png")));

        body.setActive(false);
    }

    @Override
    protected Body createObjectBody() {
        return createBody(new Box2DBody(actualBounds, 2, actualWorld, this));
    }

    @Override
    protected void childUpdate(float deltaTime) {
        if ( body.getLinearVelocity().x <= 4)
            applyLinealImpulse(new Vector2(2, 0));
    }

    @Override
    public void draw(Batch batch) {
        if (body.isActive())
            super.draw(batch);
    }
}
