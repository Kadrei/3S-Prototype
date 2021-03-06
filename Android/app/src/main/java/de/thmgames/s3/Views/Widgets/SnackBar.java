package de.thmgames.s3.Views.Widgets;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.thmgames.s3.Listener.SwipeDismissTouchListener;
import de.thmgames.s3.R;

/**
 * View that provides quick feedback about an operation in a small popup at the base of the screen
 */
public class SnackBar extends RelativeLayout {

    public interface ActionClickListener {
        void onActionClicked();
    }

    /**
     * Interface used to notify of all {@link SnackBar} display events. Useful if you want
     * to move other views while the Snackbar is on screen.
     */
    public interface EventListener {
        /**
         * Called when a {@link SnackBar} is about to enter the screen
         *
         * @param height {@link SnackBar} height, in DP
         */
        public void onShow(int height);

        /**
         * Called when a {@link SnackBar} had just been dismissed
         *
         * @param height {@link SnackBar} height, in DP
         */
        public void onDismiss(int height);
    }



    public enum SnackbarType {

        SINGLE_LINE(56, 1), MULTI_LINE(80, 2);

        private int height;
        private int maxLines;

        SnackbarType(int height, int maxLines) {
            this.height = height;
            this.maxLines = maxLines;
        }

        public int getHeight() {
            return height;
        }

        public int getHeightInPx(float scale) {
            return (int) (height * scale + 0.5f);
        }

        public int getMaxLines() {
            return maxLines;
        }
    }


    public enum SnackbarDuration {
        LENGTH_SHORT(2000), LENGTH_LONG(3500);

        private long duration;

        SnackbarDuration(long duration) {
            this.duration = duration;
        }

        public long getDuration() {
            return duration;
        }
    }

    private SnackbarType mType = SnackbarType.SINGLE_LINE;
    private SnackbarDuration mDuration = SnackbarDuration.LENGTH_LONG;
    private CharSequence mText;
    private int mColor = -1;
    private int mTextColor = -1;
    private CharSequence mActionLabel;
    private int mActionColor = -1;
    private boolean mAnimated = true;
    private long mCustomDuration = -1;
    private ActionClickListener mActionClickListener;
    private boolean mShouldDismissOnActionClicked = true;
    private EventListener mEventListener;
    private boolean mIsShowing = false;
    private boolean mCanSwipeToDismiss = true;
    private boolean mIsDismissing = false;

    private SnackBar(Context context) {
        super(context);
    }

    public static SnackBar with(Context context) {
        return new SnackBar(context);
    }

    /**
     * Sets the type of {@link SnackBar} to be displayed.
     *
     * @param type the {@link SnackbarType} of this instance
     * @return
     */
    public SnackBar type(SnackbarType type) {
        mType = type;
        return this;
    }

    /**
     * Sets the text to be displayed in this {@link SnackBar}
     *
     * @param text
     * @return
     */
    public SnackBar text(CharSequence text) {
        mText = text;
        return this;
    }

    public SnackBar withPadding(int left, int top, int right, int bottom){
        this.setPadding(left, top, right, bottom);
        return this;
    }
    /**
     * Sets the background color of this {@link SnackBar}
     *
     * @param color
     * @return
     */
    public SnackBar color(int color) {
        mColor = color;
        return this;
    }

    /**
     * Sets the text color of this {@link SnackBar}
     *
     * @param textColor
     * @return
     */
    public SnackBar textColor(int textColor) {
        mTextColor = textColor;
        return this;
    }

    /**
     * Sets the action label to be displayed, if any. Note that if this is not set, the action
     * button will not be displayed
     *
     * @param actionButtonLabel
     * @return
     */
    public SnackBar actionLabel(CharSequence actionButtonLabel) {
        mActionLabel = actionButtonLabel;
        return this;
    }

    /**
     * Sets the color of the action button label. Note that you must set a button label with
     * {@link SnackBar#actionLabel(CharSequence)} for this button to be displayed
     *
     * @param actionColor
     * @return
     */
    public SnackBar actionColor(int actionColor) {
        mActionColor = actionColor;
        return this;
    }

    /**
     * Determines whether this {@link SnackBar} should dismiss when the action button is touched
     *
     * @param shouldDismiss
     * @return
     */
    public SnackBar dismissOnActionClicked(boolean shouldDismiss) {
        mShouldDismissOnActionClicked = shouldDismiss;
        return this;
    }

    /**
     * Sets the listener to be called when the {@link SnackBar} action is
     * selected. Note that you must set a button label with
     * {@link SnackBar#actionLabel(CharSequence)} for this button to be displayed
     *
     * @param listener
     * @return
     */
    public SnackBar actionListener(ActionClickListener listener) {
        mActionClickListener = listener;
        return this;
    }

    /**
     * Sets the listener to be called when the {@link SnackBar} is dismissed.
     *
     * @param listener
     * @return
     */
    public SnackBar eventListener(EventListener listener) {
        mEventListener = listener;
        return this;
    }

    /**
     * Sets on/off animation for this {@link SnackBar}
     *
     * @param withAnimation
     * @return
     */
    public SnackBar animation(boolean withAnimation) {
        mAnimated = withAnimation;
        return this;
    }

    /**
     * Determines whether this {@link SnackBar} can be swiped off from the screen
     *
     * @param canSwipeToDismiss
     * @return
     */
    public SnackBar swipeToDismiss(boolean canSwipeToDismiss) {
        mCanSwipeToDismiss = canSwipeToDismiss;
        return this;
    }

    /**
     * Sets the duration of this {@link SnackBar}. See
     * {@link SnackBar.SnackbarDuration} for available options
     *
     * @param duration
     * @return
     */
    public SnackBar duration(SnackbarDuration duration) {
        mDuration = duration;
        return this;
    }

    /**
     * Sets a custom duration of this {@link SnackBar}
     *
     * @param duration
     * @return
     */
    public SnackBar duration(long duration) {
        mCustomDuration = duration;
        return this;
    }

    private void init(Activity parent) {
        RelativeLayout layout = (RelativeLayout) LayoutInflater.from(parent)
                .inflate(R.layout.snackbar, this, true);

        layout.setBackgroundColor(mColor != -1 ? mColor :
                getResources().getColor(R.color.snackbar_background));

        int height = mType.getHeightInPx(getResources().getDisplayMetrics().density);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height);

        layout.setLayoutParams(params);

        TextView snackbarText = (TextView) layout.findViewById(R.id.snackbar_text);
        snackbarText.setText(mText);

        if (mTextColor != -1) {
            snackbarText.setTextColor(mTextColor);
        }

        snackbarText.setMaxLines(mType.getMaxLines());

        TextView snackbarAction = (TextView) layout.findViewById(R.id.snackbar_action);
        if (!TextUtils.isEmpty(mActionLabel)) {
            requestLayout();
            snackbarAction.setText(mActionLabel);

            if (mActionColor != -1) {
                snackbarAction.setTextColor(mActionColor);
            }

            snackbarAction.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mActionClickListener != null) {
                        mActionClickListener.onActionClicked();
                    }
                    if (mShouldDismissOnActionClicked) {
                        dismiss();
                    }
                }
            });
            snackbarAction.setMaxLines(mType.getMaxLines());
        } else {
            snackbarAction.setVisibility(GONE);
        }

        setClickable(true);

        if (mCanSwipeToDismiss) {
            setOnTouchListener(new SwipeDismissTouchListener(this, null,
                    new SwipeDismissTouchListener.DismissCallbacks() {
                        @Override
                        public boolean canDismiss(Object token) {
                            return true;
                        }

                        @Override
                        public void onDismiss(View view, Object token) {
                            if (view != null) {
                                finish();
                            }
                        }
                    }));
        }

    }

    /**
     * Displays the {@link SnackBar} at the bottom of the
     * {@link android.app.Activity} provided.
     *
     * @param targetActivity
     */
    public void show(Activity targetActivity) {
        init(targetActivity);

        ViewGroup root = (ViewGroup) targetActivity.findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;

        root.addView(this, params);

        mIsShowing = true;

        if (mEventListener != null) {
            mEventListener.onShow(mType.getHeight());
        }

        if (!mAnimated) {
            startTimer();
            return;
        }

        Animation slideIn = AnimationUtils.loadAnimation(getContext(), R.anim.snackbar_in);
        slideIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        startTimer();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startAnimation(slideIn);
    }

    private void startTimer() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, getDuration());
    }

    public void dismiss() {

        if (!mAnimated) {
            finish();
            return;
        }

        if (mIsDismissing) {
            return;
        }

        final Animation slideOut = AnimationUtils.loadAnimation(getContext(), R.anim.snackbar_out);
        slideOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mIsDismissing = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        startAnimation(slideOut);
    }

    private void finish() {
        clearAnimation();
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        if (mEventListener != null && mIsShowing) {
            mEventListener.onDismiss(mType.getHeight());
        }
        mIsShowing = false;
    }

    public int getActionColor() {
        return mActionColor;
    }

    public CharSequence getActionLabel() {
        return mActionLabel;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public int getColor() {
        return mColor;
    }

    public CharSequence getText() {
        return mText;
    }

    public long getDuration() {
        return mCustomDuration == -1 ? mDuration.getDuration() : mCustomDuration;
    }

    public SnackbarType getType() {
        return mType;
    }

    public boolean isAnimated() {
        return mAnimated;
    }

    public boolean shouldDismissOnActionClicked() {
        return mShouldDismissOnActionClicked;
    }

    /**
     * Returns whether this {@link SnackBar} is currently showing
     *
     * @return
     */
    public boolean isShowing() {
        return mIsShowing;
    }

    /**
     * Returns whether this {@link SnackBar} has been dismissed
     *
     * @return
     */
    public boolean isDismissed() {
        return !mIsShowing;
    }
}
