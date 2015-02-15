package com.andreapivetta.blu.utilities;

import android.content.Context;
import android.util.DisplayMetrics;

public class Common {

    public static final String PREF = "MyPref";
    public static final String PREF_ANIMATIONS = "Anim";

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}
