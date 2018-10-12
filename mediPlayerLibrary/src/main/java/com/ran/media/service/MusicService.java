package com.ran.media.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.ran.media.MediaPlayerManager;

/**
 * @author Ran
 * @date 2018/10/12.
 */

public class MusicService extends Service {

    private MediaPlayerManager mediaPlayerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayerManager = MediaPlayerManager.get();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    public class MusicBinder extends Binder{

        public MediaPlayerManager getMediaPlayer(){
            return mediaPlayerManager;
        }

    }


}
