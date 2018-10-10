package com.ran.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import com.ran.media.interfaces.OnPlayerEventListener;
import com.ran.media.model.MusicInfo;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * @author Ran
 * @date 2018/10/10.
 */

public class MediaPlayerManager {

    private MediaPlayer mediaPlayer;
    private String musicPath;
    private List<MusicInfo> mMusicInfos;
    private int playMode = PlayMode.ORDER;
    private Random random;

    private OnPlayerEventListener onPlayerEventListener;
    private Handler playerHandler;
    private Context mContext;

    // Current playback position
    public int currentPosition = 0;

    //音乐总数量
    private int musicCount = 0;

    //当前播放第几首
    private int playNum = 0;

    public MediaPlayerManager(Context context) {
        this.mContext = context;
        mediaPlayer = new MediaPlayer();
        playerHandler = new Handler();
    }

    public void addOnPlayerEventListener(OnPlayerEventListener listener) {
        this.onPlayerEventListener = listener;
    }

    public void setMusicPath(List<MusicInfo> listPath) {
        this.mMusicInfos = listPath;
        this.musicCount = listPath.size();
    }

    public void setPlayMode(int mode) {
        this.playMode = mode;
    }

    public int getPlayMode() {
        return playMode;
    }

    /**
     * 播放
     */
    public void play() {
        if (currentPosition > 0){
            resumePlay();
        }else {
            try {
                musicPath = getMusicPath();
                if (null != musicPath) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(musicPath);//设置要播放的音频
                    mediaPlayer.prepare();//预加载音频
                    mediaPlayer.start();//开始播放
                    onPlayerEventListener.onPlaySwitch(mMusicInfos.get(playNum));
                    onPlayerEventListener.onPlayStart();
                    playerHandler.post(playerRunnable);

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Pause
     */
    public void pause() {
        if (isPlaying()) {
            mediaPlayer.pause();
            onPlayerEventListener.onPlayPause();
        }
    }

    /**
     * Continue playing
     */
    public void resumePlay(){
        if (!isPlaying()){
//            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
        }
    }

    /**
     * Previous song
     */
    public void previous() {
        if (playMode == PlayMode.RANDOM) {
            playNum = getMusicNum();
        }
        if (playNum > 0)
            playNum = playNum - 1;
        this.play();
    }

    /**
     * 下一曲
     */
    public void next() {
        if (playMode == PlayMode.RANDOM) {
            playNum = getMusicNum();
        }
        if (playNum < musicCount) {
            playNum = playNum + 1;
        }

        this.play();
    }

    /**
     * 跳转播放
     *
     * @param position
     */
    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    /**
     * 更新播放模式
     *
     * @param playMode
     */
    private void updatePlayer(int playMode) {
        if (playMode == PlayMode.LIST_LOOP ||
                playMode == PlayMode.SINGLE_LOOP ||
                playMode == PlayMode.RANDOM) {
//            mediaPlayer.setLooping(true);
        }
    }

    /**
     * 根据播放模式获取播放路径
     */
    private String getMusicPath() {
        if (null == mMusicInfos) {
            return null;
        } else {
            if (mMusicInfos.size() <= 0) {
                return null;
            }
            return mMusicInfos.get(playNum).getMusicPath();
        }
    }

    /**
     * 获取音乐播放顺序
     *
     * @return
     */
    private int getMusicNum() {
        int min = 0;
        int max = musicCount;
        if (null == random) {
            random = new Random();
        }
        int num = random.nextInt(max) % (max - min + 1) + min;
        return num;
    }

    /**
     * 销毁
     */
    public void destory() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        playerHandler.removeCallbacks(playerRunnable);
    }

    private Runnable playerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying()) {
                currentPosition = mediaPlayer.getCurrentPosition();
                onPlayerEventListener.onPlayProgress(currentPosition);
                playerHandler.post(playerRunnable);
            }
        }
    };


}