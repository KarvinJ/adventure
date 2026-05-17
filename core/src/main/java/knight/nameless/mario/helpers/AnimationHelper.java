package knight.nameless.mario.helpers;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class AnimationHelper {

    public static Animation<TextureRegion> makeAnimation(TextureRegion region, int frameWidth, int totalFrames, float frameDuration, int firstFramePosition) {



        Array<TextureRegion> animationFrames = new Array<>();

        for (int i = firstFramePosition; i < totalFrames; i++)
            animationFrames.add(new TextureRegion(region, i * frameWidth, 0, frameWidth, region.getRegionHeight()));

        return new Animation<>(frameDuration, animationFrames);
    }
}
