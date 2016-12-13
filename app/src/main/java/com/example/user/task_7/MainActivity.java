package com.example.user.task_7;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    static final String TRACK_URL = "https://cs9-11v4.vk.me/p24/818c3fe7120c8c.mp3?extra=TEFIvJgymDs7CEhrUnzw8Ovgb_Mlb2mcWEwF5QwFAmcHdd45GmS839Q8D-acB5qlTisBXU3qddGa5mmPQxeeU-owfaBE55NaEslJJz0JLVHReMgEzlto6eG0hclW3Zm6XWkvE9-Oi1w";
    static final String TAG = "ACTIVITY_MAIN";

    MediaPlayer mediaPlayer;
    MediaPlayerService mediaPlayerService;
    boolean isBounded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button buttonPlay = (Button) findViewById(R.id.play);

//        buttonPlay.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mediaPlayer = new MediaPlayer();
//                try {
//                    mediaPlayer.setDataSource(TRACK_URL);
//                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                    mediaPlayer.setOnPreparedListener(MainActivity.this);
//                    mediaPlayer.prepareAsync();
//                } catch (IOException e) {
//                    Log.e(TAG, e.getLocalizedMessage());
//                }
//            }
//        });



    }


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MediaPlayerService.MediaPlayerBinder binder = (MediaPlayerService.MediaPlayerBinder) iBinder;
            mediaPlayerService = binder.getService();
            isBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBounded = false;
        }
    };


    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared");
        mediaPlayer.start();
    }
}
