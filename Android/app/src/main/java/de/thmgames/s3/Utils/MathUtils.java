package de.thmgames.s3.Utils;

/**
 * Created by Benedikt on 05.11.2014.
 */
public final class MathUtils {

    public static float getProgress(int value, int min, int max) {
        if (min == max) {
            throw new IllegalArgumentException("Max (" + max + ") cannot equal min (" + min + ")");
        }
        return (value - min) / (float) (max - min);
    }
}
