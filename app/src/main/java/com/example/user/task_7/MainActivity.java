package com.example.user.task_7;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    static final String TAG = "ACTIVITY_MAIN";
    public static final String STOP_ACTION = "com.example.user.task_7.ACTION_STOP";
    public static final String PLAYPAUSE_ACTION = "com.example.user.task_7.ACTION_PLAY_PAUSE";
    public static final String ACTION_STARTED = "com.example.user.task_7.ACTION_ACTIVITY_STARTED";
    public static final String EXTRA_POSITION = "com.example.user.task_7.POSITION";

    AudioManager audioManager;
    Button buttonPlay, buttonStop, buttonVolume;
    SeekBar volumeBar, trackProgressBar;
    int volume;
    BroadcastReceiver volumeReceiver, notificationReceiver;
    int oldVolume;
    TextView trackTimeView, trackNameView;
    Handler handler;
    ImageView coverView;


    MediaPlayerService mediaPlayerService;
    boolean isBounded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        buttonPlay = (Button) findViewById(R.id.play);

        trackNameView = (TextView) findViewById(R.id.track_title);
        coverView = (ImageView) findViewById(R.id.cover);

        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                buttonPlay.setBackgroundResource(mediaPlayerService.isAudioPlaying() ? R.drawable.play : R.drawable.pause);

                mediaPlayerService.playOrPause();
                trackNameView.setText(mediaPlayerService.getTrackName());
                coverView.setImageBitmap(mediaPlayerService.getCover());
            }
        });


        buttonStop = (Button) findViewById(R.id.stop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayerService.stop();
                trackProgressBar.setProgress(0);
                buttonPlay.setBackgroundResource(R.drawable.play);
            }
        });

        volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        buttonVolume = (Button) findViewById(R.id.volume_btn);
        if (volume == 0) {
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
                if (volume == 0) {
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


        notificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(STOP_ACTION)) {
                    buttonPlay.setBackgroundResource(R.drawable.play);
                    trackProgressBar.setProgress(0);
                    trackTimeView.setText(R.string.null_position);
                } else {
                    buttonPlay.setBackgroundResource(mediaPlayerService.isAudioPlaying()? R.drawable.pause : R.drawable.play);
                }
            }
        };



        trackProgressBar = (SeekBar) findViewById(R.id.track_progress);
        trackTimeView = (TextView) findViewById(R.id.track_time);
        trackTimeView.setText("0:00");

        trackProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {

                if (trackProgressBar.getMax() == 100) {
                    trackProgressBar.setMax(mediaPlayerService.getDuration());
                }
                if (fromUser) {
                    mediaPlayerService.setTrackProgress(seekBar.getProgress());
                    trackTimeView.setText(mediaPlayerService.getTime());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MediaPlayerService.class);


        ServiceConnection serviceConnection = new ServiceConnection() {
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


        getApplicationContext().bindService(intent, serviceConnection, BIND_AUTO_CREATE);


        IntentFilter volumeIntentFilter = new IntentFilter();
        volumeIntentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");

        registerReceiver(volumeReceiver, volumeIntentFilter);

        IntentFilter stoppingIntentFilter = new IntentFilter();
        stoppingIntentFilter.addAction(STOP_ACTION);
        stoppingIntentFilter.addAction(PLAYPAUSE_ACTION);

        registerReceiver(notificationReceiver, stoppingIntentFilter);


        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayerService != null) {
                    try {
                        if (mediaPlayerService.isAudioPlaying()) {
                            trackTimeView.setText(mediaPlayerService.getTime());
                            trackProgressBar.setProgress(mediaPlayerService.getTrackProgress());
                        }
                    } catch (Exception e) {
                        trackProgressBar.setProgress(0);
                        trackTimeView.setText(R.string.null_position);
                        buttonPlay.setBackgroundResource(R.drawable.play);
                        handler.removeCallbacks(this);
                    }
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);


        if (getIntent().getAction().equals(ACTION_STARTED)){

            final Handler onRestartHandler = new Handler();
                    onRestartHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayerService != null) {
                        buttonPlay.setBackgroundResource(mediaPlayerService.isAudioPlaying() ? R.drawable.pause : R.drawable.play);
                        
                        trackProgressBar.setProgress(mediaPlayerService.getTrackProgress());
                        trackTimeView.setText(mediaPlayerService.getTime());
                        coverView.setImageBitmap(mediaPlayerService.getCover());
                        trackNameView.setText(mediaPlayerService.getTrackName());
                        onRestartHandler.removeCallbacks(this);
                    }
                }
            }, 1000);

            Log.d(TAG, "Started by notification");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        volumeBar.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / 15 + 1);
        if (mediaPlayerService != null) {
            buttonPlay.setBackgroundResource(mediaPlayerService.isAudioPlaying() ? R.drawable.pause : R.drawable.play);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(volumeReceiver);
        unregisterReceiver(notificationReceiver);

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}


