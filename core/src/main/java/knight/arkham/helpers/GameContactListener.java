package knight.arkham.helpers;

import com.badlogic.gdx.physics.box2d.*;
import knight.arkham.objects.enemies.Enemy;
import knight.arkham.objects.Player;
import knight.arkham.objects.enemies.Goomba;
import knight.arkham.objects.items.Item;
import knight.arkham.objects.items.Mushroom;
import knight.arkham.objects.structures.InteractiveStructure;

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
                    ((Enemy) fixtureA.getUserData()).hitByPlayer(((Player) fixtureB.getUserData()));
                else
                    ((Enemy) fixtureB.getUserData()).hitByPlayer(((Player) fixtureA.getUserData()));
                break;

            case PLAYER_BIT | GOOMBA_BIT:
            case PLAYER_BIT | KOOPA_BIT:

                if (fixtureA.getFilterData().categoryBits == PLAYER_BIT)
                    ((Player) fixtureA.getUserData()).hitByEnemy();
                else
                    ((Player) fixtureB.getUserData()).hitByEnemy();
                break;

            case GOOMBA_BIT:

                ((Goomba) fixtureA.getUserData()).collisionWithEnemy();
                ((Goomba) fixtureB.getUserData()).collisionWithEnemy();
                break;

            case PLAYER_HEAD_BIT | BLOCK_BIT:

                if (fixtureA.getFilterData().categoryBits == PLAYER_HEAD_BIT)
                    ((InteractiveStructure) fixtureB.getUserData()).hitByPlayer();
                else
                    ((InteractiveStructure) fixtureA.getUserData()).hitByPlayer();
                break;

            case ITEM_BIT | STOP_ENEMY_BIT:

                if (fixtureA.getFilterData().categoryBits == ITEM_BIT)
                    ((Mushroom) fixtureA.getUserData()).changeDirection();
                else
                    ((Mushroom) fixtureB.getUserData()).changeDirection();
                break;

            case KOOPA_BIT | GOOMBA_BIT:

                if (fixtureA.getFilterData().categoryBits == GOOMBA_BIT)
                    ((Goomba) fixtureA.getUserData()).hitByKoopa();
                else
                    ((Goomba) fixtureB.getUserData()).hitByKoopa();
                break;

            case PLAYER_BIT | ITEM_BIT  :

                if (fixtureA.getFilterData().categoryBits == ITEM_BIT)
                    ((Item) fixtureA.getUserData()).powerUpPlayer(((Player) fixtureB.getUserData()));
                else
                    ((Item) fixtureB.getUserData()).powerUpPlayer(((Player) fixtureA.getUserData()));
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
