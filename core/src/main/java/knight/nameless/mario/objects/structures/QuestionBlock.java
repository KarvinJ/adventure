package knight.nameless.mario.objects.structures;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Fixture;
import knight.nameless.mario.helpers.Box2DBody;
import knight.nameless.mario.objects.Player;
import knight.nameless.mario.objects.items.Flower;
import knight.nameless.mario.objects.items.GreenMushroom;
import knight.nameless.mario.objects.items.Mushroom;
import knight.nameless.mario.ui.Hud;
import knight.nameless.mario.screens.GameScreen;

import static knight.nameless.mario.helpers.AssetsHelper.loadSound;
import static knight.nameless.mario.helpers.Box2DHelper.createStaticFixture;

public class QuestionBlock extends InteractiveStructure {

    private final TiledMapTileSet tileSet;
    private final MapObject mapObject;
    private final GameScreen gameScreen;
    private final Rectangle initialBounds;
    private final Sound bumpSound = loadSound("bump.wav");
    private final Sound spawnItemSound = loadSound("spawn.wav");
    private final boolean isTheBlockHidden;

    public QuestionBlock(Rectangle bounds, MapObject mapObject, GameScreen gameScreen) {
        super(bounds, gameScreen.world, gameScreen.tiledMap, "coin.wav");

        initialBounds = bounds;
        tileSet = gameScreen.tiledMap.getTileSets().getTileSet("OverWorld");
        this.mapObject = mapObject;
        this.gameScreen = gameScreen;

        isTheBlockHidden = mapObject.getProperties().containsKey("hidden");
        if (isTheBlockHidden)
            actualCell.setTile(null);
    }

    @Override
    protected Fixture createFixture() {

        return createStaticFixture(
            new Box2DBody(actualBounds, actualWorld, this)
        );
    }

    @Override
    public void hitByPlayer() {

        int BLANK_COIN = 3;
        int QUESTION_BLOCK = 5;

        if (actualCell.getTile() == null)
            actualCell.setTile(tileSet.getTile(QUESTION_BLOCK));

        if (actualCell.getTile().getId() == BLANK_COIN)
            bumpSound.play();

        else {

            if (mapObject.getProperties().containsKey("item")) {

                spawnItemSound.play();

                var itemBounds = new Rectangle(initialBounds.x, initialBounds.y + 16, initialBounds.width, initialBounds.height);

                if (isTheBlockHidden)
                    gameScreen.itemsToSpawn.put(GreenMushroom.class, itemBounds);

                else if (Player.isPlayerBig)
                    gameScreen.itemsToSpawn.put(Flower.class, itemBounds);

                else
                    gameScreen.itemsToSpawn.put(Mushroom.class, itemBounds);
            }
            else {

                Hud.addScore(200);
                Hud.addCoin(1);

                collisionSound.play();
            }

            actualCell.setTile(tileSet.getTile(BLANK_COIN));
        }
    }

    @Override
    public void childDispose() {

        bumpSound.dispose();
        spawnItemSound.dispose();
    }
}
