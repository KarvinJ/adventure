package knight.arkham.helpers;

import com.badlogic.gdx.physics.box2d.*;
import knight.arkham.objects.enemies.Enemy;
import knight.arkham.objects.Player;
import knight.arkham.objects.items.Mushroom;
import knight.arkham.objects.structures.Brick;
import knight.arkham.objects.structures.QuestionBlock;

import static knight.arkham.helpers.Constants.*;

public class GameContactListener implements ContactListener {

    @Override
    public void beginContact(Contact contact) {

        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        int collisionBits = fixtureA.getFilterData().categoryBits | fixtureB.getFilterData().categoryBits;

        switch (collisionBits) {

            case PLAYER_BIT | ENEMY_HEAD_BIT:

                if (fixtureA.getFilterData().categoryBits == ENEMY_HEAD_BIT)
                    ((Enemy) fixtureA.getUserData()).hitByPlayer();
                else
                    ((Enemy) fixtureB.getUserData()).hitByPlayer();
                break;

            case PLAYER_BIT | ENEMY_BIT:

                if (fixtureA.getFilterData().categoryBits == PLAYER_BIT)
                    ((Player) fixtureA.getUserData()).hitByEnemy();
                else
                    ((Player) fixtureB.getUserData()).hitByEnemy();
                break;

            case ENEMY_BIT:

                ((Enemy) fixtureA.getUserData()).changeDirection();
                ((Enemy) fixtureB.getUserData()).changeDirection();
                break;

            case PLAYER_HEAD_BIT | BRICK_BIT:

                if (fixtureA.getFilterData().categoryBits == PLAYER_HEAD_BIT)
                    ((Brick) fixtureB.getUserData()).hitByPlayer();
                else
                    ((Brick) fixtureA.getUserData()).hitByPlayer();
                break;

            case PLAYER_HEAD_BIT | QUESTION_BLOCK_BIT:

                if (fixtureA.getFilterData().categoryBits == PLAYER_HEAD_BIT)
                    ((QuestionBlock) fixtureB.getUserData()).hitByPlayer();
                else
                    ((QuestionBlock) fixtureA.getUserData()).hitByPlayer();
                break;

            case MUSHROOM_BIT | STOP_ENEMY_BIT:

                if (fixtureA.getFilterData().categoryBits == MUSHROOM_BIT)
                    ((Mushroom) fixtureA.getUserData()).changeDirection();
                else
                    ((Mushroom) fixtureB.getUserData()).changeDirection();
                break;

            case MUSHROOM_BIT | PLAYER_BIT:

                if (fixtureA.getFilterData().categoryBits == MUSHROOM_BIT)
                    ((Mushroom) fixtureA.getUserData()).growUpPlayer(((Player) fixtureB.getUserData()));
                else
                    ((Mushroom) fixtureB.getUserData()).growUpPlayer(((Player) fixtureA.getUserData()));
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {}

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}
