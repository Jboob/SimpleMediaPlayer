package com.ran.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;

import com.ran.media.constant.MusicPlayType;
import com.ran.media.constant.PlayMode;
import com.ran.media.interfaces.OnPlayerEventListener;
import com.ran.media.model.MusicInfo;
import com.ran.media.view.JToast;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * @author Ran
 * @date 2018/10/10.
 */

public class MediaPlayerManager implements MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener {

    private String TAG = "MediaPlayerManager";

    private MediaPlayer mediaPlayer;
    private String musicPath;
    private List<MusicInfo> mMusicInfos;
    private int playMode = PlayMode.ORDER;
    private Random random;

    private OnPlayerEventListener onPlayerEventListener;
    private Handler playerHandler;
    private Context mContext;

    //缓冲进度
    private int bufferPercentage;

    // Current playback position
    public int currentPosition = 0;

    //音乐总数量
    private int musicCount = 0;

    //当前播放第几首
    private int playNum = 0;

    private JToast toast;

    public MediaPlayerManager(Context context) {
        this.mContext = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        playerHandler = new Handler();
        toast = JToast.getInstance();
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
        if (currentPosition > 0) {
            resumePlay();
        } else {
            try {
                musicPath = getMusicPath();
                if (null != musicPath) {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(musicPath);//设置要播放的音频
                    mediaPlayer.prepareAsync();//异步加载音频
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
            playerHandler.removeCallbacks(playerRunnable);
        }
    }

    /**
     * Continue playing
     */
    public void resumePlay() {
        if (!isPlaying()) {
            mediaPlayer.start();
            onPlayerEventListener.onPlaySwitch(mMusicInfos.get(playNum));
            onPlayerEventListener.onPlayStart();
            playerHandler.post(playerRunnable);
        }
    }

    /**
     * Previous song
     */
    public void previous() {
        if (isCanPlay()) {
            return;
        }
        if (playMode == PlayMode.RANDOM) {
            playNum = getMusicNum(MusicPlayType.MUSIC_RANDOM);
        }
        playNum = getMusicNum(MusicPlayType.MUSIC_PREVIOUS);

        switchMusic();
    }

    /**
     * 下一曲
     */
    public void next() {
        if (isCanPlay()) {
            return;
        }
        if (playMode == PlayMode.RANDOM) {
            playNum = getMusicNum(MusicPlayType.MUSIC_RANDOM);
        }else if (playMode == PlayMode.LIST_LOOP){
            if (playNum == getMusicCount()){
                playNum = 0;
            }
        } else if (playMode == PlayMode.ORDER){
            playNum = getMusicNum(MusicPlayType.MUSIC_NEXT);
            if (playNum == getMusicCount() && !isPlaying()){
                if (isPlayEnd()){
                    onPlayerEventListener.onPlayProgress(0);
                    onPlayerEventListener.onPlayPause();
                }else {
                    resumePlay();
                }
            }
        }
        switchMusic();
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

    public int getBufferPercentage() {
        return bufferPercentage;
    }

    /**
     * 是否可以播放
     *
     * @return true 不可播放 false 可以播放
     */
    private boolean isCanPlay() {
        if (musicCount <= 0) {
            return true;
        }
        return false;
    }

    private void switchMusic() {
        playerHandler.removeCallbacks(playerRunnable);
        currentPosition = 0;
        this.play();
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
            int count = musicCount - 1;
            if (0 <= playNum && playNum <= count){
                return mMusicInfos.get(playNum).getMusicPath();
            }else {
                return null;
            }
        }
    }

    /**
     * 获取音乐播放顺序
     *
     * @return
     */
    private int getMusicNum(int flag) {
        int count = musicCount - 1;
        if (flag == MusicPlayType.MUSIC_RANDOM) {
            int min = 0;
            int max = count;
            if (null == random) {
                random = new Random();
            }
            int num = random.nextInt(max) % (max - min + 1) + min;
            return num;
        } else if (flag == MusicPlayType.MUSIC_NEXT) {
            if (playNum < count) {
                playNum = playNum + 1;
            }
            return playNum;
        } else if (flag == MusicPlayType.MUSIC_PREVIOUS) {
            if (playNum > 0)
                playNum = playNum - 1;
            return playNum;
        }
        return 0;
    }

    private int getMusicCount(){
        if (musicCount > 0){
            return musicCount - 1;
        }
        return 0;
    }

    /**
     * 是否播放结束
     * @return
     */
    private boolean isPlayEnd(){
        int temp = getDuration() - currentPosition;
        if (temp <= 150){
            return true;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        onPlayerEventListener.onPlaySwitch(mMusicInfos.get(playNum));
        onPlayerEventListener.onPlayStart();
        playerHandler.post(playerRunnable);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        this.bufferPercentage = percent;
    }

    /**
     * 销毁
     */
    public void destory() {
        if (isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
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
            } else {
                next();
            }
        }
    };


}
