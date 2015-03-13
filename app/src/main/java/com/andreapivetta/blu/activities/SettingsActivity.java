package com.andreapivetta.blu.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.receivers.AlarmReceiver;
import com.andreapivetta.blu.services.StreamNotificationService;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.utilities.Common;

import java.util.Calendar;


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
                    .add(R.id.container, new SettingsFragment())
                    .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private SharedPreferences mSharedPreferences;
        private Preference logoutPreference;
        private CheckBoxPreference animationsPreference, headsUpNotificationsPreference;
        private SwitchPreference streamServicePreference;

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            mSharedPreferences = getActivity().getSharedPreferences(Common.PREF, 0);

            logoutPreference = findPreference("pref_key_logout");
            animationsPreference = (CheckBoxPreference) findPreference("pref_key_animations");
            headsUpNotificationsPreference = (CheckBoxPreference) findPreference("pref_key_heads_up_notifications");
            streamServicePreference = (SwitchPreference) findPreference("pref_key_stream_service");

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

                                            mSharedPreferences.edit().remove(TwitterUtils.PREF_KEY_OAUTH_TOKEN)
                                                    .remove(TwitterUtils.PREF_KEY_OAUTH_SECRET)
                                                    .remove(TwitterUtils.PREF_KEY_TWITTER_LOGIN)
                                                    .remove(TwitterUtils.PREF_KEY_PICTURE_URL)
                                                    .apply();

                                            Toast.makeText(getActivity(), getString(R.string.logout_done), Toast.LENGTH_SHORT).show();

                                            getActivity().stopService(new Intent(getActivity(), StreamNotificationService.class));

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

            animationsPreference.setChecked(mSharedPreferences.getBoolean(Common.PREF_ANIMATIONS, true));
            animationsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mSharedPreferences.edit().putBoolean(
                            Common.PREF_ANIMATIONS, animationsPreference.isChecked()).apply();
                    return true;
                }
            });

            headsUpNotificationsPreference.setChecked(mSharedPreferences.getBoolean(Common.PREF_HEADS_UP_NOTIFICATIONS, true));
            headsUpNotificationsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mSharedPreferences.edit().putBoolean(
                            Common.PREF_HEADS_UP_NOTIFICATIONS, headsUpNotificationsPreference.isChecked()).apply();
                    return true;
                }
            });


            streamServicePreference.setChecked(mSharedPreferences.getBoolean(Common.PREF_STREAM_ON, false));
            streamServicePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (newValue.toString().equals("true"))
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getString(R.string.streamDialogTitle))
                                .setMessage(getString(R.string.streamDialogMessage))
                                .setPositiveButton(getString(R.string.streamContinue), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mSharedPreferences.edit().putBoolean(Common.PREF_STREAM_ON, true).apply();
                                        getActivity().startService(new Intent(getActivity(), StreamNotificationService.class));

                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0,
                                                new Intent(getActivity(), AlarmReceiver.class), 0);

                                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                                        alarmManager.cancel(pendingIntent);

                                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                                        intent.putExtra("exit", "exit");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intent);
                                    }
                                })
                                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        streamServicePreference.setChecked(false);
                                    }
                                })
                                .setCancelable(false)
                                .create()
                                .show();
                    else {
                        mSharedPreferences.edit().putBoolean(Common.PREF_STREAM_ON, false).apply();
                        getActivity().stopService(new Intent(getActivity(), StreamNotificationService.class));

                        int frequency = mSharedPreferences.getInt(Common.PREF_FREQ, 240);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0,
                                new Intent(getActivity(), AlarmReceiver.class), 0);

                        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.add(Calendar.SECOND, frequency);
                        alarmManager.setRepeating(
                                AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency * 1000, pendingIntent);

                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        intent.putExtra("exit", "exit");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                    }

                    return true;
                }
            });
        }
    }
}
