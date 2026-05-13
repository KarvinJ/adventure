package knight.nameless.mario.objects.structures;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Fixture;
import knight.nameless.mario.helpers.Box2DBody;
import knight.nameless.mario.helpers.TileMapHelper;
import knight.nameless.mario.objects.Player;
import knight.nameless.mario.objects.items.Flower;
import knight.nameless.mario.objects.items.GreenMushroom;
import knight.nameless.mario.objects.items.Mushroom;
import knight.nameless.mario.ui.Hud;

import static knight.nameless.mario.helpers.AssetsHelper.loadSound;
import static knight.nameless.mario.helpers.Box2DHelper.createStaticFixture;

public class QuestionBlock extends InteractiveStructure {

    private final TiledMapTileSet tileSet;
    private final MapObject mapObject;
    private final TileMapHelper mapHelper;
    private final Rectangle initialBounds;
    private final Sound bumpSound = loadSound("bump.wav");
    private final Sound spawnItemSound = loadSound("spawn.wav");
    private final boolean isTheBlockHidden;

    public QuestionBlock(Rectangle bounds, MapObject mapObject, TileMapHelper mapHelper) {
        super(bounds, mapHelper.world, mapHelper.tiledMap, "coin.wav");

        initialBounds = bounds;
        tileSet = mapHelper.tiledMap.getTileSets().getTileSet("OverWorld");
        this.mapObject = mapObject;
        this.mapHelper = mapHelper;

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
                    mapHelper.itemsToSpawn.put(GreenMushroom.class, itemBounds);

                else if (Player.isPlayerBig)
                    mapHelper.itemsToSpawn.put(Flower.class, itemBounds);

                else
                    mapHelper.itemsToSpawn.put(Mushroom.class, itemBounds);
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
