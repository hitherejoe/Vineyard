package com.hitherejoe.vineyard.livechannels;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.VideoView;

import com.hitherejoe.vineyard.R;

import java.io.IOException;

public class VideoPlayer extends Activity {
    private String TAG = "VideoPlayer";
    private boolean hasActiveHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        final MediaPlayer mediaPlayer = new MediaPlayer();

        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                synchronized (this) {
                    hasActiveHolder = true;
                    this.notifyAll();
                    mediaPlayer.setDisplay(holder);
                    beginPlaying(mediaPlayer);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                synchronized (this) {
                    hasActiveHolder = false;
                    this.notifyAll();
                }
            }
        });

        Log.d(TAG, "Allo");
        String url = "http://v.cdn.vine.co/r/videos/E0D564E56F1296627994655391744_4660c9745af.5.1.1117527718074812317.mp4?versionId=oIqacFAI5Q6_K4JHQtElESwkZYCnij9v";
        try {
            Log.d(TAG, "Set "+url);
            mediaPlayer.setDataSource(url);
            Log.d(TAG, "Set display");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void beginPlaying(MediaPlayer mediaPlayer) {
        try {
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d(TAG, "Prepared");
                    mp.setLooping(true);
                    mp.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    Log.d(TAG, "Completed");
                }
            });

            mediaPlayer.prepare();
            Log.d(TAG, "Prepare");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

