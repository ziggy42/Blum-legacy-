package com.andreapivetta.blu.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;


public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
    }

    public static class PlaceholderFragment extends PreferenceFragment {

        private Preference logoutPreference;
        private CheckBoxPreference animationsPreference, headsUpNotificationsPreference;
        private SharedPreferences myPref;

        public PlaceholderFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            myPref = getActivity().getSharedPreferences(Common.PREF, 0);

            logoutPreference = findPreference("pref_key_logout");
            animationsPreference = (CheckBoxPreference) findPreference("pref_key_animations");
            headsUpNotificationsPreference = (CheckBoxPreference) findPreference("pref_key_heads_up_notifications");

            logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.logout_title_dialog))
                            .setPositiveButton(getResources().getString(R.string.yes),
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {

                                            myPref.edit().remove(TwitterUtils.PREF_KEY_OAUTH_TOKEN)
                                                    .remove(TwitterUtils.PREF_KEY_OAUTH_SECRET)
                                                    .remove(TwitterUtils.PREF_KEY_TWITTER_LOGIN)
                                                    .remove(TwitterUtils.PREF_KEY_PICTURE_URL)
                                                    .apply();

                                            Toast.makeText(getActivity(), getString(R.string.logout_done), Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(getActivity(), HomeActivity.class);
                                            intent.putExtra("exit", "exit");
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                            startActivity(intent);
                                        }
                                    }
                            ).setNegativeButton(getString(R.string.cancel), null).create().show();

                    return true;
                }
            });

            animationsPreference.setChecked(myPref.getBoolean(Common.PREF_ANIMATIONS, true));
            animationsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    myPref.edit().putBoolean(
                            Common.PREF_ANIMATIONS, animationsPreference.isChecked()).apply();
                    return true;
                }
            });

            headsUpNotificationsPreference.setChecked(myPref.getBoolean(Common.PREF_HEADS_UP_NOTIFICATIONS, true));
            headsUpNotificationsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    myPref.edit().putBoolean(
                            Common.PREF_HEADS_UP_NOTIFICATIONS, headsUpNotificationsPreference.isChecked()).apply();
                    return true;
                }
            });

        }
    }
}
