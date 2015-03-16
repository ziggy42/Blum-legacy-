package com.andreapivetta.blu.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.utilities.Common;

import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class FeedbackActivity extends ActionBarActivity {

    private static final String FORM_URL =
            "https://docs.google.com/forms/d/1x6_qUNrJQoD5wNB9U9LuA56a7ohHIgFS-6ackdYTkoo/formResponse";
    private static final String EMAIL_KEY = "entry_2058179740";
    private static final String SUBJECT_KEY = "entry_1238104500";
    private static final String CONTENT_KEY = "entry_1191776422";

    private EditText subjectEditText, emailEditText, contentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

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

        subjectEditText = (EditText) findViewById(R.id.subjectEditText);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        contentEditText = (EditText) findViewById(R.id.contentEditText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_feedback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send) {
            if (TextUtils.isEmpty(emailEditText.getText().toString()) ||
                    TextUtils.isEmpty(subjectEditText.getText().toString()) ||
                    TextUtils.isEmpty(contentEditText.getText().toString())) {
                Toast.makeText(FeedbackActivity.this, getString(R.string.all_fields_needed), Toast.LENGTH_LONG).show();
                return false;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.getText().toString()).matches()) {
                Toast.makeText(FeedbackActivity.this, getString(R.string.provide_valid_email), Toast.LENGTH_LONG).show();
                return false;
            }

            new PostDataTask().execute(emailEditText.getText().toString(), subjectEditText.getText().toString(),
                    contentEditText.getText().toString());

            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class PostDataTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... contactData) {

            try {
                HttpsURLConnection connection = (HttpsURLConnection) new URL(FORM_URL).openConnection();
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("user-agent", Common.USER_AGENT);
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(EMAIL_KEY + "=" + URLEncoder.encode(contactData[0], "UTF-8") + "&" +
                        SUBJECT_KEY + "=" + URLEncoder.encode(contactData[1], "UTF-8") + "&" +
                        CONTENT_KEY + "=" + URLEncoder.encode(contactData[2], "UTF-8"));
                wr.flush();
                wr.close();

                return connection.getResponseCode() == 200;
            } catch (Exception exception) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Toast.makeText(FeedbackActivity.this,
                    result ? getString(R.string.message_sent) : getString(R.string.sending_message_error),
                    Toast.LENGTH_LONG).show();
        }
    }

}
