package com.andreapivetta.blu.utilities;

import android.content.Context;

import com.andreapivetta.blu.R;

public class ThemeUtils {

    public static int getResourceColorPrimary(Context context) {
        switch (context.getSharedPreferences(Common.PREF, 0).getInt(Common.PREF_THEME, 0)) {
            case 0:
                return context.getResources().getColor(R.color.blueThemeColorPrimary);
            case 1:
                return context.getResources().getColor(R.color.pinkThemeColorPrimary);
            default:
                return context.getResources().getColor(R.color.blueThemeColorPrimary);
        }
    }

    public static int getColorPrimary(Context context) {
        switch (context.getSharedPreferences(Common.PREF, 0).getInt(Common.PREF_THEME, 0)) {
            case 0:
                return R.color.blueThemeColorPrimary;
            case 1:
                return R.color.pinkThemeColorPrimary;
            default:
                return R.color.blueThemeColorPrimary;
        }
    }

    public static int getColorPrimaryDark(Context context) {
        switch (context.getSharedPreferences(Common.PREF, 0).getInt(Common.PREF_THEME, 0)) {
            case 0:
                return R.color.blueThemeColorPrimaryDark;
            case 1:
                return R.color.pinkThemeColorPrimaryDark;
            default:
                return R.color.blueThemeColorPrimaryDark;
        }
    }
}
