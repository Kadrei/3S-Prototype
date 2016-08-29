package de.thmgames.s3.Transformer;

/**
 * Created by Benedikt on 06.11.2014.
 */


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.squareup.picasso.Transformation;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;

public class ColorFilterTransformer implements Transformation {

    private final int color;
    private  PorterDuff.Mode mode = PorterDuff.Mode.SCREEN;
    public ColorFilterTransformer(int color) {
        this.color=color;
    }

    public ColorFilterTransformer(int color, PorterDuff.Mode porterMode) {
        this.color=color;
        this.mode=porterMode;
    }

    @Override public Bitmap transform(Bitmap source) {
        Bitmap result = createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        Paint paint = new Paint(ANTI_ALIAS_FLAG);

        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0, 0, paint);
        paint.setColor(color);
        paint.setXfermode(new PorterDuffXfermode(mode));
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
        source.recycle();
        return result;
    }
    @Override public String key() {
        return "colorFilterTransformer()";
    }
}