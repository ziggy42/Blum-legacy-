package com.andreapivetta.blu.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.andreapivetta.blu.R;
import com.andreapivetta.blu.internet.ConnectionDetector;
import com.andreapivetta.blu.twitter.TwitterOAuthActivity;

public class LoginActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final ConnectionDetector connectionDetector = new ConnectionDetector(this);

        Button loginImageView = (Button) findViewById(R.id.loginImageView);
        loginImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( connectionDetector.isConnectingToInternet() )
                    startActivityForResult(new Intent(LoginActivity.this, TwitterOAuthActivity.class), 0);
                else
                    Toast.makeText(getApplicationContext(), getString(R.string.internet_connection_required),
                            Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK)
            finish();
    }
}