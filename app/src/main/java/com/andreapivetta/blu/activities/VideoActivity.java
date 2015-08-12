package com.andreapivetta.blu.activities;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.andreapivetta.blu.R;

public class VideoActivity extends AppCompatActivity {

    public static final String TAG_VIDEO = "video";
    public static final String TAG_TYPE = "type";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        String videoUrl = getIntent().getStringExtra(TAG_VIDEO);

        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoView);
        mc.setMediaPlayer(videoView);

        if ("animated_gif".equals(getIntent().getStringExtra(TAG_TYPE))) {
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoView.start();
                }
            });
        }

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                findViewById(R.id.loadingProgressBar).setVisibility(View.GONE);
            }
        });

        videoView.setMediaController(mc);
        videoView.setVideoURI(Uri.parse(videoUrl));
        videoView.requestFocus();
        videoView.start();
    }
}
