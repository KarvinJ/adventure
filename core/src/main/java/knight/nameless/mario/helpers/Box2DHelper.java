package knight.nameless.mario.helpers;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import knight.nameless.mario.objects.Player;
import knight.nameless.mario.objects.enemies.Enemy;
import knight.nameless.mario.objects.enemies.Koopa;
import knight.nameless.mario.objects.items.Item;
import knight.nameless.mario.objects.structures.InteractiveStructure;

import static knight.nameless.mario.helpers.Constants.*;

public class Box2DHelper {

    public static Fixture createStaticFixture(Box2DBody box2DBody){

        PolygonShape shape = new PolygonShape();

        FixtureDef fixtureDef = createBoxFixtureDef(box2DBody, shape);

        if (box2DBody.userData instanceof InteractiveStructure)
            fixtureDef.filter.categoryBits = BLOCK_BIT;
        else
            fixtureDef.filter.categoryBits = STOP_ENEMY_BIT;

        Body body = createBox2DBodyByType(box2DBody);

        Fixture fixture = body.createFixture(fixtureDef);

        fixture.setUserData(box2DBody.userData);

        shape.dispose();

        return fixture;
    }

    private static Body createBox2DBodyByType(Box2DBody box2DBody) {

        var bodyDef = new BodyDef();

        bodyDef.type = box2DBody.bodyType;

        bodyDef.position.set(box2DBody.bounds.x / PIXELS_PER_METER, box2DBody.bounds.y / PIXELS_PER_METER);

        bodyDef.fixedRotation = true;

        return box2DBody.world.createBody(bodyDef);
    }

    private static FixtureDef createKoopaBoxFixtureDef(Box2DBody box2DBody, PolygonShape shape) {

        //Since I want that my player to be rendered with a 32 px width, but I want that his collisions fixture to be of 16,
        // I got to create a new method just for my player or any other object with this case.
        shape.setAsBox(16f / 2 / PIXELS_PER_METER, box2DBody.bounds.height / 2 / PIXELS_PER_METER);

        var fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = box2DBody.density;

        return fixtureDef;
    }

    public static Body createFireBody(Box2DBody box2DBody) {

        PolygonShape shape = new PolygonShape();

        FixtureDef fixtureDef = createBoxFixtureDef(box2DBody, shape);

        fixtureDef.filter.categoryBits = FIRE_BIT;

        fixtureDef.restitution = 1.5f;

        Body body = createBox2DBodyByType(box2DBody);

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        shape.dispose();

        return body;
    }

    public static Body createBody(Box2DBody box2DBody) {

        var shape = new PolygonShape();

        Body body = createBox2DBodyByType(box2DBody);

        if (box2DBody.userData instanceof Player) {

            var playerFixtureDef = createCircleFixtureDef(box2DBody);
            createPlayerBody(box2DBody, playerFixtureDef, body);
        }

        else if (box2DBody.userData instanceof Enemy) {

            var enemyFixtureDef = createCircleFixtureDef(box2DBody);

            if (box2DBody.userData instanceof Koopa)
                enemyFixtureDef = createKoopaBoxFixtureDef(box2DBody, shape);

            createEnemyBody(box2DBody, enemyFixtureDef, body);
        }

        else if (box2DBody.userData instanceof Item) {

            var circleFixtureDef = createCircleFixtureDef(box2DBody);
            circleFixtureDef.filter.categoryBits = ITEM_BIT;
            body.createFixture(circleFixtureDef).setUserData(box2DBody.userData);
        }

        else {

            var fixtureDef = createBoxFixtureDef(box2DBody, shape);

            fixtureDef.filter.categoryBits = GROUND_BIT;
            body.createFixture(fixtureDef);
        }

        shape.dispose();

        return body;
    }

    private static FixtureDef createBoxFixtureDef(Box2DBody box2DBody, PolygonShape shape) {

        shape.setAsBox(box2DBody.bounds.width / 2 / PIXELS_PER_METER, box2DBody.bounds.height / 2 / PIXELS_PER_METER);

        FixtureDef fixtureDef = new FixtureDef();

        fixtureDef.shape = shape;
        fixtureDef.density = box2DBody.density;

        return fixtureDef;
    }

    private static FixtureDef createCircleFixtureDef(Box2DBody box2DBody) {

        CircleShape shape = new CircleShape();

        shape.setRadius(8 / PIXELS_PER_METER);

        FixtureDef fixtureDef = new FixtureDef();

        fixtureDef.shape = shape;
        fixtureDef.density = box2DBody.density;

//        shape.dispose(); // avoid crash on android

        return fixtureDef;
    }

    private static void createPlayerBody(Box2DBody box2DBody, FixtureDef fixtureDef, Body body) {

        fixtureDef.filter.categoryBits = PLAYER_BIT;

        fixtureDef.filter.maskBits = (short) (GROUND_BIT | BLOCK_BIT |
            FINISH_BIT | GOOMBA_BIT | ENEMY_HEAD_BIT | ITEM_BIT | KOOPA_BIT);

        fixtureDef.friction = 1;

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        EdgeShape headCollider = getPlayerHeadCollider(fixtureDef);

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        headCollider.dispose();
    }

    private static EdgeShape getPlayerHeadCollider(FixtureDef fixtureDef) {

        EdgeShape headCollider = new EdgeShape();

        headCollider.set(
            new Vector2(-4 / PIXELS_PER_METER, 8 / PIXELS_PER_METER),
            new Vector2(4 / PIXELS_PER_METER, 8 / PIXELS_PER_METER)
        );

        fixtureDef.shape = headCollider;
        fixtureDef.filter.categoryBits = PLAYER_HEAD_BIT;
        fixtureDef.isSensor = true;

        return headCollider;
    }

    public static Body createBigPlayerBody(Box2DBody box2DBody){

        Body body = createBox2DBodyByType(box2DBody);

        CircleShape circleShape = new CircleShape();

        FixtureDef fixtureDef = new FixtureDef();

        circleShape.setRadius(8 / PIXELS_PER_METER);

        fixtureDef.shape = circleShape;

//  because big mario will have two bodies it will weigh more, so I'm going to reduce the density in half, to have a consistent weigh.
        fixtureDef.density = box2DBody.density / 2;
        fixtureDef.friction = 1;

        fixtureDef.filter.categoryBits = PLAYER_BIT;

        fixtureDef.filter.maskBits = (short) (GROUND_BIT | BLOCK_BIT |
            FINISH_BIT | GOOMBA_BIT | ENEMY_HEAD_BIT | ITEM_BIT | KOOPA_BIT);

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

// I'm going to create another fixture -8.5 px down to have two for the whole body of my player.
        circleShape.setPosition(new Vector2(0, -8.5f / PIXELS_PER_METER));

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        circleShape.dispose();

        EdgeShape headCollider = getPlayerHeadCollider(fixtureDef);

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        headCollider.dispose();

        return body;
    }


    private static void createEnemyBody(Box2DBody box2DBody, FixtureDef fixtureDef, Body body) {

        fixtureDef.filter.categoryBits = GOOMBA_BIT;

        if (box2DBody.userData instanceof Koopa)
            fixtureDef.filter.categoryBits = KOOPA_BIT;

        fixtureDef.filter.maskBits = (short) (GROUND_BIT | GOOMBA_BIT | PLAYER_BIT | STOP_ENEMY_BIT | BLOCK_BIT | KOOPA_BIT | FIRE_BIT);

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        PolygonShape headCollider = getGoombaHeadCollider();

        if (box2DBody.userData instanceof Koopa)
             headCollider = getKoopaHeadCollider();

        fixtureDef.shape = headCollider;
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = ENEMY_HEAD_BIT;

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        headCollider.dispose();
    }

    private static PolygonShape getKoopaHeadCollider() {

        PolygonShape head = new PolygonShape();

        Vector2[] vertices = new Vector2[4];

        vertices[0] = new Vector2(-8, 14).scl(1 / PIXELS_PER_METER);
        vertices[1] = new Vector2(8, 14).scl(1 / PIXELS_PER_METER);
        vertices[2] = new Vector2(-8, 12).scl(1 / PIXELS_PER_METER);
        vertices[3] = new Vector2(8, 12).scl(1 / PIXELS_PER_METER);

        head.set(vertices);

        return head;
    }

    private static PolygonShape getGoombaHeadCollider() {

        PolygonShape head = new PolygonShape();

        Vector2[] vertices = new Vector2[4];

        vertices[0] = new Vector2(-8, 12).scl(1 / PIXELS_PER_METER);
        vertices[1] = new Vector2(8, 12).scl(1 / PIXELS_PER_METER);
        vertices[2] = new Vector2(-8, 10).scl(1 / PIXELS_PER_METER);
        vertices[3] = new Vector2(8, 10).scl(1 / PIXELS_PER_METER);

        head.set(vertices);

        return head;
    }

    public static Rectangle getDrawBounds(Vector2 position, float width, float height) {

        return new Rectangle(
            position.x - (width / 2 / PIXELS_PER_METER),
            position.y - (height / 2 / PIXELS_PER_METER),
            width / PIXELS_PER_METER,
            height / PIXELS_PER_METER
        );
    }
}
