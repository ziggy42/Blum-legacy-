package com.andreapivetta.blu.utilities;

import android.content.Context;
import android.preference.PreferenceManager;

import com.andreapivetta.blu.R;

public class ThemeUtils {

    public static int getResourceColorPrimary(Context context) {
        switch (PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_themes), "B")) {
            case "B":
                return context.getResources().getColor(R.color.blueThemeColorPrimary);
            case "P":
                return context.getResources().getColor(R.color.pinkThemeColorPrimary);
            case "G":
                return context.getResources().getColor(R.color.greenThemeColorPrimary);
            case "D":
                return context.getResources().getColor(R.color.darkThemeColorPrimary);
            default:
                return context.getResources().getColor(R.color.blueThemeColorPrimary);
        }
    }

    public static int getColorPrimary(Context context) {
        switch (PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_themes), "B")) {
            case "B":
                return R.color.blueThemeColorPrimary;
            case "P":
                return R.color.pinkThemeColorPrimary;
            case "G":
                return R.color.greenThemeColorPrimary;
            case "D":
                return R.color.darkThemeColorPrimary;
            default:
                return R.color.blueThemeColorPrimary;
        }
    }

    public static int getColorPrimaryDark(Context context) {
        switch (PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_themes), "B")) {
            case "B":
                return R.color.blueThemeColorPrimaryDark;
            case "P":
                return R.color.pinkThemeColorPrimaryDark;
            case "G":
                return R.color.greenThemeColorPrimaryDark;
            case "D":
                return R.color.darkThemeColorPrimaryDark;
            default:
                return R.color.blueThemeColorPrimaryDark;
        }
    }
}
