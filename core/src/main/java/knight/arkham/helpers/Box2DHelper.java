package knight.arkham.helpers;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import knight.arkham.objects.Enemy;
import knight.arkham.objects.Player;
import knight.arkham.objects.structures.Brick;
import knight.arkham.objects.items.Mushroom;
import knight.arkham.objects.structures.QuestionBlock;

import static knight.arkham.helpers.Constants.*;

public class Box2DHelper {

    public static Fixture createStaticFixture(Box2DBody box2DBody){

        PolygonShape shape = new PolygonShape();

        FixtureDef fixtureDef = createBoxFixtureDef(box2DBody, shape);

        if (box2DBody.userData instanceof Brick)
            fixtureDef.filter.categoryBits = BRICK_BIT;

        else if (box2DBody.userData instanceof QuestionBlock)
            fixtureDef.filter.categoryBits = QUESTION_BLOCK_BIT;

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

    private static FixtureDef createCharactersBoxFixtureDef(Box2DBody box2DBody, PolygonShape shape) {

        //Since I want that my player to be rendered with a 32 px width, but I want that his collisions fixture to be of 16,
        // I got to create a new method just for my player or any other object with this case.
        shape.setAsBox(16f / 2 / PIXELS_PER_METER, box2DBody.bounds.height / 2 / PIXELS_PER_METER);

        var fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = box2DBody.density;

        return fixtureDef;
    }

    public static Body createBody(Box2DBody box2DBody) {

        PolygonShape shape = new PolygonShape();

        FixtureDef fixtureDef = createBoxFixtureDef(box2DBody, shape);

        Body body = createBox2DBodyByType(box2DBody);

        if (box2DBody.userData instanceof Player) {

            var playerFixtureDef = createCharactersBoxFixtureDef(box2DBody, shape);
            createPlayerBody(box2DBody, playerFixtureDef, body);
        }

        else if (box2DBody.userData instanceof Enemy) {

            var enemyFixtureDef = createCharactersBoxFixtureDef(box2DBody, shape);
            createEnemyBody(box2DBody, enemyFixtureDef, body);
        }
        else if (box2DBody.userData instanceof Mushroom) {

            fixtureDef.filter.categoryBits = MUSHROOM_BIT;
            body.createFixture(fixtureDef).setUserData(box2DBody.userData);
        }

        else {

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

    private static void createPlayerBody(Box2DBody box2DBody, FixtureDef fixtureDef, Body body) {

        fixtureDef.filter.categoryBits = PLAYER_BIT;

        fixtureDef.filter.maskBits = (short) (GROUND_BIT | BRICK_BIT | QUESTION_BLOCK_BIT |
            FINISH_BIT | ENEMY_BIT | ENEMY_HEAD_BIT | MUSHROOM_BIT);

        fixtureDef.friction = 1;

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        EdgeShape headCollider = getPlayerHeadCollider(fixtureDef);

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        headCollider.dispose();
    }

    private static EdgeShape getPlayerHeadCollider(FixtureDef fixtureDef) {

        EdgeShape headCollider = new EdgeShape();

        headCollider.set(
            new Vector2(-8 / PIXELS_PER_METER, 10 / PIXELS_PER_METER),
            new Vector2(8 / PIXELS_PER_METER, 10 / PIXELS_PER_METER)
        );

        fixtureDef.shape = headCollider;
        fixtureDef.filter.categoryBits = PLAYER_HEAD_BIT;
        fixtureDef.isSensor = true;

        return headCollider;
    }

    private static void createEnemyBody(Box2DBody box2DBody, FixtureDef fixtureDef, Body body) {

        fixtureDef.filter.categoryBits = ENEMY_BIT;

        fixtureDef.filter.maskBits = (short) (GROUND_BIT | STOP_ENEMY_BIT | ENEMY_BIT | PLAYER_BIT);

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        PolygonShape headCollider = getEnemyHeadHeadCollider();

        fixtureDef.shape = headCollider;
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = ENEMY_HEAD_BIT;

        body.createFixture(fixtureDef).setUserData(box2DBody.userData);

        headCollider.dispose();
    }

    private static PolygonShape getEnemyHeadHeadCollider() {

        PolygonShape head = new PolygonShape();

        Vector2[] vertices = new Vector2[4];

        vertices[0] = new Vector2(-8, 13).scl(1 / PIXELS_PER_METER);
        vertices[1] = new Vector2(8, 13).scl(1 / PIXELS_PER_METER);
        vertices[2] = new Vector2(-8, 11).scl(1 / PIXELS_PER_METER);
        vertices[3] = new Vector2(8, 11).scl(1 / PIXELS_PER_METER);

        head.set(vertices);

        return head;
    }

    public static Rectangle getDrawBounds(Body body, Rectangle bounds) {

        return new Rectangle(
            body.getPosition().x - (bounds.width / 2 / PIXELS_PER_METER),
            body.getPosition().y - (bounds.height / 2 / PIXELS_PER_METER),
            bounds.width / PIXELS_PER_METER,
            bounds.height / PIXELS_PER_METER
        );
    }
}
