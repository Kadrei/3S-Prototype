package de.thmgames.s3.Views.Widgets;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 28.01.2015.
 */
public class LoadingImageView extends FrameLayout implements Target {

    private Context mContext;
    private ImageView targetView;
    private ImageView loadingView;
    private ImageLoadingCallback mCallback;
    private View greyBackground;
    private boolean placeholder = false;

    public LoadingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUpView(context);
    }
    public LoadingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUpView(context);
    }
    public LoadingImageView(Context context) {
        super(context);
        setUpView(context);
    }

    public void setPlaceholderResource(int r){
        targetView.setImageResource(r);
        placeholder=true;
    }

    public void setImageRatio(float ratio){
        int mPhotoHeightPixels = (int) (this.getWidth() / ratio);
        ViewGroup.LayoutParams lp;
        lp = this.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            this.setLayoutParams(lp);
        }
    }

    public void setUpView(Context ctx){
        this.mContext = ctx;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.loading_imageview, this);
        this.setForegroundGravity(Gravity.CENTER);
        targetView = (ImageView) findViewById(R.id.targetView);
        loadingView = (ImageView) findViewById(R.id.loadingView);
        LayoutParams centerParams = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
        greyBackground = findViewById(R.id.greyBackground);
        centerParams.gravity =  Gravity.CENTER;
        targetView.setLayoutParams(centerParams);
        setUpRotationAnimation();
    }

    public ImageView getTargetView(){
        return targetView;
    }

    public ImageView getLoadingView(){
        return loadingView;
    }

    public void setLoadingListener(ImageLoadingCallback mCallback){
        this.mCallback=mCallback;
    }

    public void showLoading(boolean show){
        loadingView.clearAnimation();
        loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
        greyBackground.setVisibility(show? View.VISIBLE : View.GONE);
        targetView.setVisibility((show && !placeholder) ? View.INVISIBLE : View.VISIBLE);
        if(show){
            loadingView.setAnimation(mRotationAnimation);
        }
    }

    private Animation mRotationAnimation;

    public void setUpRotationAnimation(){
        mRotationAnimation = AnimationUtils.loadAnimation(mContext, R.anim.infinite_rotation);
    }
    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        showLoading(false);
        targetView.setImageBitmap(bitmap);
        if(mCallback!=null) mCallback.onSuccessfullyFinishedLoading(bitmap, from);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        showLoading(false);
        if(errorDrawable!=null) targetView.setImageDrawable(errorDrawable);
        if(mCallback!=null) mCallback.onFailure(errorDrawable);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        if(placeHolderDrawable!=null) targetView.setImageDrawable(placeHolderDrawable);
        showLoading(true);
        if(mCallback!=null) mCallback.onStartLoading(placeHolderDrawable);
    }

    public interface ImageLoadingCallback{
        public void onStartLoading(Drawable placeHolderDrawable);
        public void onSuccessfullyFinishedLoading(Bitmap bmp, Picasso.LoadedFrom from);
        public void onFailure(Drawable errorDrawable);
    }
}
