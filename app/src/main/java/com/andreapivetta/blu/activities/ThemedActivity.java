package com.andreapivetta.blu.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.utilities.Common;

public class ThemedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (getSharedPreferences(Common.PREF, 0).getInt(Common.PREF_THEME, 0)) {
            case 0:
                setTheme(R.style.AppTheme);
                break;
            case 1:
                setTheme(R.style.PinkAppTheme);
                break;
            default:
                setTheme(R.style.AppTheme);
                break;
        }
        super.onCreate(savedInstanceState);
    }
}
