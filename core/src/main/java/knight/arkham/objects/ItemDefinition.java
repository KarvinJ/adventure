package knight.arkham.objects;

import com.badlogic.gdx.math.Rectangle;

public class ItemDefinition {
    public Rectangle bounds;
    //    Utilizaré una clase generica, para poder recibir clases de cualquier tipo.
    public Class<?> classType;

    public ItemDefinition(Rectangle bounds, Class<?> classType) {
        this.bounds = bounds;
        this.classType = classType;
    }
}
