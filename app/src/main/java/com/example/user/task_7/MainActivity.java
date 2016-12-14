package com.example.user.task_7;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    static final String TAG = "ACTIVITY_MAIN";

    AudioManager audioManager;
    Button buttonPlay, buttonStop, buttonVolume;
    SeekBar volumeBar;
    int volume;
    BroadcastReceiver volumeReceiver;
    int oldVolume;


    MediaPlayerService mediaPlayerService;
    boolean isBounded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        buttonPlay = (Button) findViewById(R.id.play);

        final TextView trackNameView = (TextView) findViewById(R.id.track_title);
        final ImageView coverView = (ImageView) findViewById(R.id.cover);

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayerService.playOrPause();
                if (mediaPlayerService.isAudioPlaying()) {
                    buttonPlay.setBackgroundResource(R.drawable.pause);
                    trackNameView.setText(mediaPlayerService.getTrackName());
                    coverView.setImageBitmap(mediaPlayerService.getCover());
                } else {
                    buttonPlay.setBackgroundResource(R.drawable.play);

                }
            }
        });

        buttonStop = (Button) findViewById(R.id.stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.stop();
                buttonPlay.setBackgroundResource(R.drawable.play);
            }
        });

        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        buttonVolume = (Button) findViewById(R.id.volume_btn);
        if (volume == 0){
            buttonVolume.setBackgroundResource(R.drawable.volume_off);
        }

        oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        buttonVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0) {
                    oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
                    buttonVolume.setBackgroundResource(R.drawable.volume_off);
                } else {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, oldVolume, AudioManager.FLAG_SHOW_UI);
                    buttonVolume.setBackgroundResource(R.drawable.volume_on);
                }
            }
        });

        volumeBar = (SeekBar) findViewById(R.id.volume_bar);
        volumeBar.setProgress(volume);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                volume = (audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * seekBar.getProgress()) / 100;
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_SHOW_UI);
                if (volume == 0){
                    buttonVolume.setBackgroundResource(R.drawable.volume_off);
                } else {
                    buttonVolume.setBackgroundResource(R.drawable.volume_on);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        volumeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / 15 + 1);
            }
        };
    }



    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MediaPlayerService.class);
        getApplicationContext().bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");

        registerReceiver(volumeReceiver, intentFilter);
    }


    @Override
    protected void onResume() {
        super.onResume();
        volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / 15 + 1);
        if (mediaPlayerService != null) {
            if (mediaPlayerService.isAudioPlaying()) {
                buttonPlay.setBackgroundResource(R.drawable.pause);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(volumeReceiver);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}


