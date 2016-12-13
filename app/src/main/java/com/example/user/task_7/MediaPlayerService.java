package com.example.user.task_7;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class MediaPlayerService extends Service {

    private final IBinder mediaPlayerBinder = new MediaPlayerBinder();

    public MediaPlayerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mediaPlayerBinder;
    }


    public class MediaPlayerBinder extends Binder{
        MediaPlayerService getService(){
            return MediaPlayerService.this;
        }
    }
}
