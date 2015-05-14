package com.andreapivetta.blu.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.andreapivetta.blu.R;

public abstract class ThemedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        switch (PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_themes), "B")) {
            case "B":
                setTheme(R.style.BlueAppTheme);
                break;
            case "P":
                setTheme(R.style.PinkAppTheme);
                break;
            default:
                setTheme(R.style.BlueAppTheme);
                break;
        }
        super.onCreate(savedInstanceState);
    }
}
