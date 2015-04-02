package com.andreapivetta.blu.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DirectMessagesDatabaseManager;
import com.andreapivetta.blu.data.FavoritesDatabaseManager;
import com.andreapivetta.blu.data.FollowersDatabaseManager;
import com.andreapivetta.blu.data.MentionsDatabaseManager;
import com.andreapivetta.blu.data.NotificationsDatabaseManager;
import com.andreapivetta.blu.data.RetweetsDatabaseManager;
import com.andreapivetta.blu.services.BasicNotificationService;
import com.andreapivetta.blu.services.StreamNotificationService;
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
                    .add(R.id.container, new SettingsFragment())
                    .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private SharedPreferences mSharedPreferences;
        private Preference logoutPreference, sharePreference, aboutPreference, feedbackPreference;
        private CheckBoxPreference animationsPreference, headsUpPreference;
        private SwitchPreference streamServicePreference;
        private ListPreference favoritesRetweetsListPreference, mentionsListPreference,
                followersListPreference, messagesListPreference, frequencyListPreference;

        private ProgressDialog dialog;

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            mSharedPreferences = getActivity().getSharedPreferences(Common.PREF, 0);

            logoutPreference = findPreference("pref_key_logout");
            animationsPreference = (CheckBoxPreference) findPreference("pref_key_animations");
            headsUpPreference = (CheckBoxPreference) findPreference("pref_key_heads_up_notifications");
            streamServicePreference = (SwitchPreference) findPreference("pref_key_stream_service");
            frequencyListPreference = (ListPreference) findPreference("pref_key_frequencies");
            sharePreference = findPreference("pref_key_share");
            aboutPreference = findPreference("pref_key_about");
            feedbackPreference = findPreference("pref_key_feedback");
            favoritesRetweetsListPreference = (ListPreference) findPreference("pref_key_fav_ret");
            mentionsListPreference = (ListPreference) findPreference("pref_key_mentions");
            followersListPreference = (ListPreference) findPreference("pref_key_followers");
            messagesListPreference = (ListPreference) findPreference("pref_key_dms");

            if (mSharedPreferences.getBoolean(Common.PREF_STREAM_ON, false)) {
                frequencyListPreference.setEnabled(false);
                favoritesRetweetsListPreference.setEnabled(false);
                mentionsListPreference.setEnabled(false);
                followersListPreference.setEnabled(false);
                messagesListPreference.setEnabled(false);
            }

            switch (mSharedPreferences.getString(Common.PREF_RET_FAV_NOTS, Common.WIFI_ONLY)) {
                case Common.NEVER:
                    favoritesRetweetsListPreference.setValueIndex(0);
                    break;
                case Common.WIFI_ONLY:
                    favoritesRetweetsListPreference.setValueIndex(1);
                    break;
                case Common.ALWAYS:
                    favoritesRetweetsListPreference.setValueIndex(2);
                    break;
            }

            favoritesRetweetsListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mSharedPreferences.edit()
                            .putString(Common.PREF_RET_FAV_NOTS, newValue.toString()).apply();
                    return true;
                }
            });

            switch (mSharedPreferences.getString(Common.PREF_MENTIONS_NOTS, Common.ALWAYS)) {
                case Common.NEVER:
                    mentionsListPreference.setValueIndex(0);
                    break;
                case Common.WIFI_ONLY:
                    mentionsListPreference.setValueIndex(1);
                    break;
                case Common.ALWAYS:
                    mentionsListPreference.setValueIndex(2);
                    break;
            }

            mentionsListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mSharedPreferences.edit()
                            .putString(Common.PREF_MENTIONS_NOTS, newValue.toString()).apply();
                    return true;
                }
            });

            switch (mSharedPreferences.getString(Common.PREF_FOLLOWERS_NOTS, Common.WIFI_ONLY)) {
                case Common.NEVER:
                    followersListPreference.setValueIndex(0);
                    break;
                case Common.WIFI_ONLY:
                    followersListPreference.setValueIndex(1);
                    break;
                case Common.ALWAYS:
                    followersListPreference.setValueIndex(2);
                    break;
            }

            followersListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mSharedPreferences.edit()
                            .putString(Common.PREF_FOLLOWERS_NOTS, newValue.toString()).apply();
                    return true;
                }
            });

            switch (mSharedPreferences.getString(Common.PREF_DMS_NOTS, Common.ALWAYS)) {
                case Common.NEVER:
                    messagesListPreference.setValueIndex(0);
                    break;
                case Common.WIFI_ONLY:
                    messagesListPreference.setValueIndex(1);
                    break;
                case Common.ALWAYS:
                    messagesListPreference.setValueIndex(2);
                    break;
            }

            messagesListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mSharedPreferences.edit()
                            .putString(Common.PREF_DMS_NOTS, newValue.toString()).apply();
                    return true;
                }
            });

            switch (mSharedPreferences.getInt(Common.PREF_FREQ, 1200)) {
                case 300:
                    frequencyListPreference.setValueIndex(0);
                    break;
                case 600:
                    frequencyListPreference.setValueIndex(1);
                    break;
                case 900:
                    frequencyListPreference.setValueIndex(2);
                    break;
                case 1200:
                    frequencyListPreference.setValueIndex(3);
                    break;
                case 1800:
                    frequencyListPreference.setValueIndex(4);
                    break;
                case 3600:
                    frequencyListPreference.setValueIndex(5);
                    break;
                case 7200:
                    frequencyListPreference.setValueIndex(6);
                    break;
                default:
                    frequencyListPreference.setValueIndex(0);
                    break;
            }

            frequencyListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    mSharedPreferences.edit()
                            .putInt(Common.PREF_FREQ, Integer.parseInt(newValue.toString())).apply();

                    BasicNotificationService.stopService(getActivity());
                    BasicNotificationService.startService(getActivity());

                    return true;
                }
            });

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
                                            dialog.dismiss();
                                            performLogout();
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

            headsUpPreference.setChecked(mSharedPreferences.getBoolean(Common.PREF_HEADS_UP_NOTIFICATIONS, true));
            headsUpPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    mSharedPreferences.edit().putBoolean(
                            Common.PREF_HEADS_UP_NOTIFICATIONS, headsUpPreference.isChecked()).apply();
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
                                .setPositiveButton(getString(R.string.streamContinue),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                mSharedPreferences.edit().putBoolean(Common.PREF_STREAM_ON, true).apply();
                                                getActivity().startService(
                                                        new Intent(getActivity(), StreamNotificationService.class));

                                                BasicNotificationService.stopService(getActivity());

                                                Intent i = new Intent(getActivity(), HomeActivity.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                                        .putExtra("exit", "exit");
                                                startActivity(i);
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
                        BasicNotificationService.startService(getActivity());

                        Intent i = new Intent(getActivity(), HomeActivity.class);
                        i.putExtra("exit", "exit")
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                    }

                    return true;
                }
            });

            feedbackPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), FeedbackActivity.class));
                    return true;
                }
            });

            sharePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_TEXT, getString(R.string.share_this_app))
                            .setType("text/plain");
                    startActivity(intent);

                    return true;
                }
            });

            aboutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), UserProfileActivity.class);
                    i.putExtra("ID", 290695594L);
                    getActivity().startActivity(i);

                    return true;
                }
            });

        }

        void performLogout() {
            dialog = ProgressDialog.show(getActivity(), getString(R.string.performing_logout),
                    getString(R.string.please_wait), true);
            dialog.show();

            new PerformLogOut().execute(null, null, null);
        }

        private class PerformLogOut extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... params) {
                if (mSharedPreferences.getBoolean(Common.PREF_STREAM_ON, false))
                    getActivity().stopService(
                            new Intent(getActivity(), StreamNotificationService.class));
                else
                    BasicNotificationService.stopService(getActivity());

                mSharedPreferences.edit().clear().apply();

                DirectMessagesDatabaseManager directMessagesDatabaseManager =
                        new DirectMessagesDatabaseManager(getActivity());
                directMessagesDatabaseManager.open();
                directMessagesDatabaseManager.clearDatabase();
                directMessagesDatabaseManager.close();

                FavoritesDatabaseManager favoritesDatabaseManager =
                        new FavoritesDatabaseManager(getActivity());
                favoritesDatabaseManager.open();
                favoritesDatabaseManager.clearDatabase();
                favoritesDatabaseManager.close();

                RetweetsDatabaseManager retweetsDatabaseManager =
                        new RetweetsDatabaseManager(getActivity());
                retweetsDatabaseManager.open();
                retweetsDatabaseManager.clearDatabase();
                retweetsDatabaseManager.close();

                FollowersDatabaseManager followersDatabaseManager =
                        new FollowersDatabaseManager(getActivity());
                followersDatabaseManager.open();
                followersDatabaseManager.clearDatabase();
                followersDatabaseManager.close();

                MentionsDatabaseManager mentionsDatabaseManager =
                        new MentionsDatabaseManager(getActivity());
                mentionsDatabaseManager.open();
                mentionsDatabaseManager.clearDatabase();
                mentionsDatabaseManager.close();

                NotificationsDatabaseManager notificationsDatabaseManager =
                        new NotificationsDatabaseManager(getActivity());
                notificationsDatabaseManager.open();
                notificationsDatabaseManager.clearDatabase();
                notificationsDatabaseManager.close();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoidValue) {
                Toast.makeText(getActivity(), getString(R.string.logout_done), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                Intent i = new Intent(getActivity(), HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .putExtra("exit", "exit");
                startActivity(i);
            }
        }

    }
}
