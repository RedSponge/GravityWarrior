package com.redsponge.upsidedownbb.utils;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.TimeUtils;
import com.redsponge.upsidedownbb.assets.AnimationDescriptor;

import java.lang.reflect.Array;
import java.util.Random;

public class GeneralUtils {

    private static final Random random = new Random();

    public static float secondsSince(long time) {
        return (TimeUtils.nanoTime() - time) / 1000000000f;
    }

    /**
     * Joins arrays into one big array
     * @param arrs The arrays to join into one
     * @param <T> The type
     * @return An array consisting of the two arrays joined.
     */
    @SafeVarargs
    public static <T> T[] joinArrays(Class<T> c, T[]... arrs) {
        int size = 0;
        for (T[] ts : arrs) {
            size += ts.length;
        }

        T[] out = (T[]) Array.newInstance(c, size);

        int offset = 0;
        for (T[] arr : arrs) {
            System.arraycopy(arr, 0, out, offset, arr.length);
            offset += arr.length;
        }
        return out;
    }

    public static boolean rectanglesIntersect(IntVector2 pos1, IntVector2 size1, IntVector2 pos2, IntVector2 size2) {
        return pos1.x < pos2.x + size2.x &&
                pos1.x + size1.x > pos2.x &&
                pos1.y < pos2.y + size2.y &&
                pos1.y + size1.y > pos2.y;
    }

    public static float lerp(float current, float to, float a) {
        return (1 - a) * current + a * to;
    }

    public static com.badlogic.gdx.utils.Array<TextureRegion> queryAnimationFrames(TextureAtlas from, int numFrames, String frameName, int startsAt) {
        TextureRegion[] out = new TextureRegion[numFrames];
        for(int i = 0; i < numFrames; i++) {
            out[i] = from.findRegion(frameName, i + startsAt);
        }
        return new com.badlogic.gdx.utils.Array<>(out);
    }

    public static Animation<TextureRegion> getAnimation(AnimationDescriptor descriptor, TextureAtlas atlas, int startsAt) {
        return new Animation<TextureRegion>(descriptor.frameDuration, queryAnimationFrames(atlas, descriptor.numFrames, descriptor.name, startsAt), descriptor.playMode);
    }

    public static void playSoundRandomlyPitched(Sound sound) {
        playSoundRandomlyPitched(sound, Settings.soundVol);
    }

    public static void playSoundRandomlyPitched(Sound sound, float vol) {
        sound.play(vol, 0.5f + random.nextFloat(), 0);
    }
}
