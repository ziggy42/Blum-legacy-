package com.andreapivetta.blu.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.data.DatabaseManager;
import com.andreapivetta.blu.services.BasicNotificationService;
import com.andreapivetta.blu.services.CheckFollowingService;
import com.andreapivetta.blu.services.StreamNotificationService;
import com.andreapivetta.blu.twitter.TwitterUtils;
import com.andreapivetta.blu.twitter.UpdateTwitterStatus;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;


public class SettingsActivity extends ThemedActivity implements BillingProcessor.IBillingHandler {

    public BillingProcessor billingProcessor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        billingProcessor = new BillingProcessor(this, null, this);

        if (savedInstanceState == null)
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
    }

    @Override
    public void onProductPurchased(String s, TransactionDetails transactionDetails) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(getString(R.string.pref_ads_removed), true).commit();
        finish();
        final Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onPurchaseHistoryRestored() {

    }

    @Override
    public void onBillingError(int i, Throwable throwable) {

    }

    @Override
    public void onBillingInitialized() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!billingProcessor.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (billingProcessor != null)
            billingProcessor.release();

        super.onDestroy();
    }

    public static class SettingsFragment extends PreferenceFragment {

        private SharedPreferences mSharedPreferences;
        private SwitchPreference streamServicePreference;

        private ProgressDialog dialog;

        public SettingsFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            streamServicePreference = (SwitchPreference) findPreference(getString(R.string.pref_key_stream_service));

            if (mSharedPreferences.getBoolean(getString(R.string.pref_key_stream_service), false)) {
                findPreference(getString(R.string.pref_key_frequencies)).setEnabled(false);
                findPreference(getString(R.string.pref_key_fav_ret)).setEnabled(false);
                findPreference(getString(R.string.pref_key_mentions)).setEnabled(false);
                findPreference(getString(R.string.pref_key_followers)).setEnabled(false);
                findPreference(getString(R.string.pref_key_dms)).setEnabled(false);
            }

            findPreference(getString(R.string.pref_key_themes)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    restartApplication();
                    return true;
                }
            });

            findPreference(getString(R.string.pref_key_frequencies)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    BasicNotificationService.stopService(getActivity());
                    BasicNotificationService.startService(getActivity(), Integer.parseInt((String) newValue));

                    return true;
                }
            });

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
                                                getActivity().startService(
                                                        new Intent(getActivity(), StreamNotificationService.class));

                                                BasicNotificationService.stopService(getActivity());
                                                restartApplication();
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
                        getActivity().stopService(new Intent(getActivity(), StreamNotificationService.class));
                        BasicNotificationService.startService(getActivity());
                        restartApplication();
                    }

                    return true;
                }
            });

            final ListPreference adsPreference = (ListPreference) findPreference("pref_key_iap");
            if (mSharedPreferences.getBoolean(getString(R.string.pref_ads_removed), false)) {
                adsPreference.setEnabled(false);
            } else {
                adsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        String res = String.valueOf(newValue);
                        String[] cases = getResources().getStringArray(R.array.iap_values);
                        if (cases[0].equals(res)) {
                            ((SettingsActivity) getActivity()).billingProcessor.purchase(getActivity(), "remove_ads_099");
                        } else if (cases[1].equals(res)) {
                            ((SettingsActivity) getActivity()).billingProcessor.purchase(getActivity(), "remove_ads_199");
                        } else if (cases[2].equals(res)) {
                            ((SettingsActivity) getActivity()).billingProcessor.purchase(getActivity(), "remove_ads_049");
                        } else if (cases[3].equals(res)) {
                            ((SettingsActivity) getActivity()).billingProcessor.purchase(getActivity(), "remove_ads_999");
                        } else {
                            new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.pay_with_tweet_title))
                                    .setMessage(getString(R.string.pay_with_tweet_status))
                                    .setPositiveButton(getString(R.string.tweet), new DialogInterface.OnClickListener() {
                                        @SuppressLint("CommitPrefEdits")
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            new UpdateTwitterStatus(getActivity(), TwitterUtils.getTwitter(getActivity()), -1l)
                                                    .execute(getString(R.string.pay_with_tweet_status));
                                            mSharedPreferences.edit().putBoolean(getString(R.string.pref_ads_removed), true).commit();
                                            restartApplication();
                                        }
                                    }).setNegativeButton(getString(R.string.keep_ads), null)
                                    .create().show();
                        }

                        return true;
                    }
                });
            }

            findPreference("pref_key_edit").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent i = new Intent(getActivity(), EditProfileActivity.class);
                    getActivity().startActivity(i);

                    return false;
                }
            });

            findPreference("pref_key_feedback").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity(), FeedbackActivity.class));
                    return true;
                }
            });

            findPreference("pref_key_licenses").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    View content = View.inflate(getActivity(), R.layout.fragment_licenses, null);
                    WebView mWebView = (WebView) content.findViewById(R.id.licensesFragmentWebView);
                    mWebView.loadUrl("http://andreapivetta.altervista.org/Blum/licenses.html");

                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.licenses_pref_title))
                            .setView(content)
                            .setPositiveButton(getString(R.string.ok), null)
                            .create()
                            .show();

                    return true;
                }
            });

            findPreference("pref_key_about").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View dialogView = View.inflate(getActivity(), R.layout.dialog_about, null);

                    final TextView mTwitterTextView = (TextView) dialogView.findViewById(R.id.aboutTextView);
                    mTwitterTextView.setText(Html.fromHtml(getString(R.string.my_twitter)));
                    mTwitterTextView.setMovementMethod(LinkMovementMethod.getInstance());

                    builder.setView(dialogView).create().show();

                    return true;
                }
            });

            findPreference("pref_key_logout").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.logout_title_dialog))
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
        }

        void performLogout() {
            dialog = ProgressDialog.show(getActivity(), getString(R.string.performing_logout),
                    getString(R.string.please_wait), true);
            dialog.show();

            new PerformLogOut().execute();
        }

        void restartApplication() {
            getActivity().finish();
            final Intent intent = new Intent(getActivity(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
            getActivity().startActivity(intent);
        }

        private class PerformLogOut extends AsyncTask<Void, Void, Void> {

            @SuppressLint("CommitPrefEdits")
            @Override
            protected Void doInBackground(Void... params) {
                if (mSharedPreferences.getBoolean(getString(R.string.pref_key_stream_service), false))
                    getActivity().stopService(
                            new Intent(getActivity(), StreamNotificationService.class));
                else
                    BasicNotificationService.stopService(getActivity());
                CheckFollowingService.stopService(getActivity());

                mSharedPreferences.edit().clear().commit();
                DatabaseManager.getInstance(getActivity()).clearDatabase();
                TwitterUtils.nullTwitter();

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoidValue) {
                Toast.makeText(getActivity(), getString(R.string.logout_done), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                restartApplication();
            }
        }

    }
}
