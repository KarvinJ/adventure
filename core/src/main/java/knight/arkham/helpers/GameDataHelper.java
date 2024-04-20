package knight.arkham.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.Vector2;

public class GameDataHelper {

    private static final String dataFilename = "Adventure";

    public static void savePosition(Vector2 position) {

        Preferences preferences = Gdx.app.getPreferences(dataFilename);

        preferences.putFloat("positionX", position.x);
        preferences.putFloat("positionY", position.y);

        preferences.flush();
    }

    public static Vector2 loadPosition() {

        Preferences preferences = Gdx.app.getPreferences(dataFilename);

        float positionX = preferences.getFloat("positionX");
        float positionY = preferences.getFloat("positionY");

        return new Vector2(positionX, positionY);
    }
}
