package com.example.user.task_7;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mediaPlayer;
    static final String TRACK_URL = "https://cs9-6v4.vk.me/p15/2d1e0352a66c20.mp3?extra=SNZm2QRDVb8-MaB2JkBKxzimwTszfKK5D8QxrgRodsVaQszKerpxi17Hryl8fVW0cVutrLDrMUKgOr9Nghzw_ShBDIcmj6c9EbVBOdemwDZkqPDc1Qd6CR19HO0u33yA6mUVXB7s5Jk";
    static final String TAG = "MEDIA_PLAYER_SERVICE";
    int currentPos = 0;
    Button playPauseButton;
    AudioManager audioManager;


    private final IBinder mediaPlayerBinder = new MediaPlayerBinder();

    public MediaPlayerService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(TRACK_URL);
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
        Log.d(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mediaPlayerBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPrepared");
        mediaPlayer.start();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion");
        mediaPlayer.release();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {

        return false;
    }


    public class MediaPlayerBinder extends Binder{
        MediaPlayerService getService()
        {
            Log.d(TAG, "getService");
            return MediaPlayerService.this;
        }
    }

    public boolean isAudioPlaying(){
        return mediaPlayer.isPlaying() || currentPos == 0;
    }


//    public String getTrackName(){
//        return mediaPlayer.;
//    }

    public void playOrPause(){

        Log.d(TAG, "playOrPause started");

        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            currentPos = mediaPlayer.getCurrentPosition();
            return;
        }

        if (currentPos != 0){
            mediaPlayer.seekTo(currentPos);
            mediaPlayer.start();
            return;
        }

        mediaPlayer.prepareAsync();

    }


    public void stop(){
        mediaPlayer.stop();
        currentPos = 0;
    }
}
