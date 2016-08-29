package de.thmgames.s3.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

/**
 * Created by Benedikt on 31.10.2014.
 */
public final class LayoutUtils {

    public static int getStatusBarHeight(final Context context) {
        final int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int getNavigationBarHeight(final Context context) {
        final int id = context.getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
        if (id != 0 && context.getResources().getBoolean(id)) {
            final int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    public static boolean isInLandscape(Context ctx){
        return ctx.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static int getPaddingTopWithActionBar(ActionBarActivity act){
        return LayoutUtils.getActionBarHeight(act)+ ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !LayoutUtils.isInLandscape(act))? LayoutUtils.getStatusBarHeight(act) : 0);
    }

    public static int getPaddingTopWithoutActionBar(ActionBarActivity act){
        return ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !LayoutUtils.isInLandscape(act))? LayoutUtils.getStatusBarHeight(act) : 0);
    }

    public static int getPaddingBottom(Context ctx){
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !LayoutUtils.isInLandscape(ctx)) ? LayoutUtils.getNavigationBarHeight(ctx) : 0;
    }
    public static boolean isNavigationForcedBlack(Activity act) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return true;
        }
        final int windowFlags = act.getWindow().getAttributes().flags;
        int navControlFlags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            navControlFlags |= WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS;
        }
        if ((windowFlags & navControlFlags) == 0) {
            return true;
        }

        boolean deviceHasOpaqueSideLandscapeNav = LayoutUtils.getDeviceSmallestWidthDp(act) < 600;
        boolean isLandscape = isInLandscape(act);

        return deviceHasOpaqueSideLandscapeNav && isLandscape;
    }

    public static int getActionBarHeight(ActionBarActivity ctx){
        int actionBarHeight=0;
        TypedValue tv = new TypedValue();
        if (ctx.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,ctx.getResources().getDisplayMetrics());
        }
        return actionBarHeight;
    }
    public static float getDeviceSmallestWidthDp(Activity act) {
        DisplayMetrics dm = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getRealMetrics(dm);
        float widthDp = dm.widthPixels / dm.density;
        float heightDp = dm.heightPixels / dm.density;
        return Math.min(widthDp, heightDp);
    }

    public static int dpToPx(int dp, Context ctx) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public int pxToDp(int px, Context ctx) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
    }
}
