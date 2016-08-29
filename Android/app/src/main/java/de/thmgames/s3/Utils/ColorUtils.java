package de.thmgames.s3.Utils;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.widget.ImageView;

/**
 * Created by Benedikt on 23.10.2014.
 */
public final class ColorUtils {

    private static float interpolate(float a, float b, float proportion) {
        return (a + ((b - a) * proportion));
    }

    /** Returns an interpoloated color, between <code>a</code> and <code>b</code> */
    public static int interpolateColor(int a, int b, float proportion) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
    }

    public static void setSaturationOnImageView(ImageView v, int saturation){
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(saturation); //0 means grayscale
        ColorMatrixColorFilter cf = new ColorMatrixColorFilter(matrix);
        v.setColorFilter(cf);
    }
}
