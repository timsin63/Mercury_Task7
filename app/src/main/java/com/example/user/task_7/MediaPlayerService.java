package com.example.user.task_7;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mediaPlayer;
    static final String TRACK_URL = "https://cs1-49v4.vk-cdn.net/p14/5b0776b44aab02.mp3?extra=J05DiO6CaWQiUYj-nNnWOt-jtNdP_7PiNNW4tSpdckkALY49X-hcswsUoXi0WvssmmxBA3eaMiB27f1cchhmWtqVPia35K83My_-AZ0CobtZiI3trz9rXcWi5htUOXb4ZViM1K2Jz2k";
    static final String TAG = "MEDIA_PLAYER_SERVICE";
    int currentPos = 0;
    Button playPauseButton;
    AudioManager audioManager;
    Notification notification;
    String artist, title;
    Bitmap cover;

    MediaMetadataRetriever metadataRetriever;


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


        notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.headphones)
                .setContentTitle(title)
                .setContentText(artist)
                .build();
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

        metadataRetriever = new MediaMetadataRetriever();


        metadataRetriever.setDataSource(TRACK_URL, new HashMap<String, String>());
        title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        byte[] byteCover = metadataRetriever.getEmbeddedPicture();
        cover = BitmapFactory.decodeByteArray(byteCover, 0, byteCover.length);
        Log.d(TAG, title);

        mediaPlayer.prepareAsync();

    }

    public String getTrackName(){
        return artist + ": " + title;
    }

    public Bitmap getCover(){
        return cover;
    }

    public void stop(){
        mediaPlayer.stop();
        currentPos = 0;
    }
}
