package knight.arkham.objects.structures;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Fixture;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.TileMapHelper;
import knight.arkham.objects.items.Flower;
import knight.arkham.objects.items.ItemDefinition;
import knight.arkham.objects.items.Mushroom;
import knight.arkham.objects.Player;
import knight.arkham.scenes.Hud;

import static knight.arkham.helpers.AssetsHelper.loadSound;
import static knight.arkham.helpers.Box2DHelper.createStaticFixture;

public class QuestionBlock extends InteractiveStructure {
    private final TiledMapTileSet tileSet;
    private final MapObject mapObject;
    private final TileMapHelper mapHelper;
    private final Rectangle initialBounds;
    private final Sound bumpSound = loadSound("bump.wav");
    private final Sound spawnItemSound = loadSound("spawn.wav");

    public QuestionBlock(Rectangle bounds, MapObject mapObject, TileMapHelper mapHelper) {
        super(bounds, mapHelper.world, mapHelper.tiledMap, "coin.wav");

        initialBounds = bounds;
        tileSet = mapHelper.tiledMap.getTileSets().getTileSet("OverWorld");
        this.mapObject = mapObject;
        this.mapHelper = mapHelper;

        if (mapObject.getProperties().containsKey("hidden"))
            actualCell.setTile(null);
    }

    @Override
    protected Fixture createFixture() {

        return createStaticFixture(
            new Box2DBody(actualBounds, actualWorld, this)
        );
    }

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

                if (Player.isMarioBig)
                    mapHelper.setItemToSpawn(new ItemDefinition(itemBounds, Flower.class));
                else
                    mapHelper.setItemToSpawn(new ItemDefinition(itemBounds, Mushroom.class));
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
