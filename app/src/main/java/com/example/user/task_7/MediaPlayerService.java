package com.example.user.task_7;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.icu.util.ULocale;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

public class MediaPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, Serializable {

    private MediaPlayer mediaPlayer;
    static final String TRACK_URL = "https://cs1-36v4.vk-cdn.net/p11/9f6da2e90aa73f.mp3?extra=PovjN6v7ZJVjdDA_TN1SVob5B4lalZtafuUPC8ThC6MDe-hrk8jsoTiEtmshQe7dpHH9SmfPmmiorHOCb-5zEjRgY9W0PmelpQSvwnC80rUwT-hyAbBsFZ6IUVZlSB-lwb70imlp7Q";
    static final String TAG = "MEDIA_PLAYER_SERVICE";
    static final String ACTION_STOP = "ACTION_STOP";
    static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    int currentPos = 0;
    Button playPauseButton;
    AudioManager audioManager;
    Notification notification;
    String artist, title;
    Bitmap cover;

    MediaMetadataRetriever metadataRetriever;


    private final IBinder mediaPlayerBinder = new MediaPlayerBinder();


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(ACTION_STOP)) {
                MediaPlayerService.this.stop();
            } else {
                MediaPlayerService.this.playOrPause();
                sendBroadcast(new Intent(MainActivity.PLAYPAUSE_ACTION));
            }
        }
    };

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

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilter.addAction(ACTION_STOP);
        intentFilter.addAction(ACTION_PLAY_PAUSE);
        registerReceiver(receiver, intentFilter);
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

        Intent stopIntent = new Intent(ACTION_STOP);
        Intent playOrPauseIntent = new Intent(ACTION_PLAY_PAUSE);
        Intent startActivityIntent = new Intent(this, MainActivity.class);
        startActivityIntent.setAction(MainActivity.ACTION_STARTED);
        startActivityIntent.putExtra(MainActivity.EXTRA_POSITION, mediaPlayer.getCurrentPosition());


        PendingIntent pendingStopIntent = PendingIntent.getBroadcast(this, 100, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingPlayOrPauseIntent = PendingIntent.getBroadcast(this, 100, playOrPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingStartActivityIntent = PendingIntent.getActivity(this, 100, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.headphones)
                .setContentTitle(title)
                .setContentText(artist)
                .addAction(R.drawable.play_notification, "play/pause", pendingPlayOrPauseIntent)
                .addAction(R.drawable.stop_notification, "stop", pendingStopIntent)
                .setLargeIcon(cover)
                .setContentIntent(pendingStartActivityIntent)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onCompletion");
        currentPos = 0;
        mediaPlayer.seekTo(0);
        mediaPlayer.pause();
        Intent intent = new Intent();
        intent.setAction(MainActivity.STOP_ACTION);
        sendBroadcast(intent);
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
        return mediaPlayer.isPlaying();
    }



    public void playOrPause(){

        Log.d(TAG, "playOrPause started");

        if (mediaPlayer == null){
            Log.d(TAG, "player is null");
            mediaPlayer = new MediaPlayer();
        }

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

        try {
            metadataRetriever = new MediaMetadataRetriever();

            metadataRetriever.setDataSource(TRACK_URL, new HashMap<String, String>());
            title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            byte[] byteCover = metadataRetriever.getEmbeddedPicture();
            cover = BitmapFactory.decodeByteArray(byteCover, 0, byteCover.length);

            mediaPlayer.prepareAsync();
        } catch (IllegalStateException e){
            mediaPlayer.start();
            startForeground(1, notification);
        }


    }

    public String getTrackName(){
        return artist + ": " + title;
    }

    public Bitmap getCover(){
        return cover;
    }

    public void stop(){

        mediaPlayer.stop();
        stopForeground(true);
        currentPos = 0;
        Intent intent = new Intent();
        intent.setAction(MainActivity.STOP_ACTION);
        sendBroadcast(intent);
    }


    public int getTrackProgress(){
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public String getTime(){
        if (mediaPlayer.isPlaying()) {
            int minutes = mediaPlayer.getCurrentPosition() / 60000;
            int seconds = mediaPlayer.getCurrentPosition() % 60000 / 1000;
            return seconds < 10 ? minutes + ":0" + seconds : minutes + ":" + seconds;
        }
        return getResources().getString(R.string.null_position);
    }

    public void setTrackProgress(int trackProgress){
        currentPos = trackProgress;
        mediaPlayer.seekTo(trackProgress);
    }
}
