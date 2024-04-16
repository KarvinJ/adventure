package knight.arkham.objects.items;

import com.badlogic.gdx.math.Rectangle;

public class ItemDefinition {
    public Rectangle bounds;
    //   generic class.
    public Class<?> classType;

    public ItemDefinition(Rectangle bounds, Class<?> classType) {
        this.bounds = bounds;
        this.classType = classType;
    }
}
