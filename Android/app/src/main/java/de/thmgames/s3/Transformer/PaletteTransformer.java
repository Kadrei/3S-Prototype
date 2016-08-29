package de.thmgames.s3.Transformer;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import com.squareup.picasso.Transformation;

import java.util.Map;
import java.util.WeakHashMap;

import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 05.11.2014.
 */
public final class PaletteTransformer implements Transformation {
    private static final PaletteTransformer INSTANCE = new PaletteTransformer();
    private static final Map<Bitmap, Palette> CACHE = new WeakHashMap<Bitmap, Palette>();

    public static PaletteTransformer instance() {
        return INSTANCE;
    }

    public static Palette getPalette(Bitmap bitmap) {
        return CACHE.get(bitmap);
    }

    private PaletteTransformer() {}

    @Override public Bitmap transform(Bitmap source) {
        Palette palette = Palette.generate(source);
        CACHE.put(source, palette);
        return source;
    }

    @Override
    public String key() {
        return "";
    }
}
