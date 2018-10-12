package com.ran.media.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.ran.media.MediaPlayerManager;

/**
 * @author Ran
 * @date 2018/10/12.
 */

public class MusicServiceConnection implements ServiceConnection {

    private String TAG = "MediaPlayerManager";

    private MusicService.MusicBinder musicControl;

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder service) {
        Log.d(TAG, "onServiceConnected: Service 已連接");
        musicControl = (MusicService.MusicBinder)service;
        MediaPlayerManager.set(musicControl.getMediaPlayer());
        if (null != musicControl.getMediaPlayer()){
            Log.d(TAG, "onServiceConnected: 拿到播放器");
        }
        
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {

    }


}
