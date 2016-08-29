package de.thmgames.s3.Animations;


import android.animation.Animator;
import android.view.ViewPropertyAnimator;

/**
 * Created by nikola on 9/7/14.
 */
public class RepeatableViewPropertyAnimatorWrapper implements Animator.AnimatorListener {

    public interface RepeatableAnimatorListener extends Animator.AnimatorListener {
        public boolean willRepeat();
    }

    private ViewPropertyAnimator mViewPropertyAnimator;
    private RepeatableAnimatorListener mViewPropertyAnimatorListener;
    private boolean willRepeat = true;
    private Animator mCurrentAnimator;

    public RepeatableViewPropertyAnimatorWrapper withAnimatorListener(RepeatableAnimatorListener animatorListener) {
        this.setAnimatorListener(animatorListener);
        return this;
    }

    private void setAnimatorListener(RepeatableAnimatorListener animatorListener) {
        mViewPropertyAnimatorListener = animatorListener;
    }

    public RepeatableViewPropertyAnimatorWrapper(ViewPropertyAnimator animator) {
        mViewPropertyAnimator = animator;
    }

    public RepeatableViewPropertyAnimatorWrapper(ViewPropertyAnimator animator, RepeatableAnimatorListener animatorListener) {
        mViewPropertyAnimator = animator;
        mViewPropertyAnimatorListener = animatorListener;
    }

    public boolean isRepeating() {
        if (mViewPropertyAnimatorListener != null) {
            return willRepeat && mViewPropertyAnimatorListener.willRepeat();
        }
        return willRepeat;
    }

    public RepeatableViewPropertyAnimatorWrapper setRepeat(boolean willRepeat) {
        this.willRepeat = willRepeat;
        return this;
    }

    private void setup() {
        mViewPropertyAnimator.setListener(this);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        mCurrentAnimator = animation;
        if (mViewPropertyAnimatorListener != null) {
            mViewPropertyAnimatorListener.onAnimationStart(animation);
        }
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (mViewPropertyAnimatorListener != null) {
            mViewPropertyAnimatorListener.onAnimationEnd(animation);
        }
        repeatAnimation();

    }

    public void start() {
        mViewPropertyAnimator.start();
    }

    private void repeatAnimation() {
        if (isRepeating()) {
            start();
            onAnimationRepeat(mCurrentAnimator);
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        if (mViewPropertyAnimatorListener != null) {
            mViewPropertyAnimatorListener.onAnimationCancel(animation);
        }
    }

    @Override
    public void onAnimationRepeat(Animator animation) {
        if (mViewPropertyAnimatorListener != null) {
            mViewPropertyAnimatorListener.onAnimationRepeat(animation);
        }

    }
}