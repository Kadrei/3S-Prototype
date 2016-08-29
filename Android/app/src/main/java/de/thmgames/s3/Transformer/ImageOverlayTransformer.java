package de.thmgames.s3.Transformer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.io.IOException;

import de.thmgames.s3.Utils.LogUtils;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Paint.ANTI_ALIAS_FLAG;

/**
 * Created by Benedikt on 11.11.2014.
 */
public class ImageOverlayTransformer implements Transformation {

    private static final String TAG = ImageOverlayTransformer.class.getName();
    private String mImageUrl;
    private final Picasso picasso;
    private int overlaySize;

    public ImageOverlayTransformer(String url, Picasso picasso, int sizeOfOverlay){
        mImageUrl=url;
        this.picasso = picasso;
        overlaySize=sizeOfOverlay;
    }
    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap logo;
        try {
            logo = picasso.load(mImageUrl).get();
        } catch (IOException e) {
            LogUtils.e(TAG, "Error while downloading Fractionlogo", e);
            return source;
        }
        Bitmap result = createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        Paint paint = new Paint(ANTI_ALIAS_FLAG);
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(source, 0, 0, paint);
        Bitmap bitmapResized = Bitmap.createScaledBitmap(logo, overlaySize, overlaySize, false);
        canvas.drawBitmap(bitmapResized, (canvas.getWidth()-overlaySize)/2, (canvas.getHeight()-overlaySize)/2,paint);
        source.recycle();
        logo.recycle();
        return result;
    }

    @Override
    public String key() {
        return "FractionOverlayTransformer_"+mImageUrl;
    }
}
