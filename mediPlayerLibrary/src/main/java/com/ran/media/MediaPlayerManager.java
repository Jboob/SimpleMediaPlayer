package com.ran.media;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;

import com.ran.media.constant.MusicPlayType;
import com.ran.media.constant.PlayMode;
import com.ran.media.interfaces.OnPlayerEventListener;
import com.ran.media.model.MusicInfo;
import com.ran.media.service.MusicService;
import com.ran.media.service.MusicServiceConnection;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * @author Ran
 * @date 2018/10/10.
 */

public class MediaPlayerManager implements MediaPlayer.OnPreparedListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener {

    private String TAG = "MediaPlayerManager";

    private static volatile MediaPlayerManager INSTANCE = null;

    private MediaPlayer mediaPlayer;
    private String musicPath;
    private List<MusicInfo> mMusicInfos;
    private int playMode = PlayMode.LIST_LOOP;
    private Random random;

    private OnPlayerEventListener onPlayerEventListener;
    private Handler playerHandler;

    private MusicInfo musicInfo;

    //缓冲进度
    private int bufferPercentage;

    // Current playback position
    public int currentPosition = 0;

    //音乐总数量
    private int musicCount = 0;

    //当前播放第几首
    private int playNum = 0;

    //加载完成
    public final int LOAD_FINISH = 1000025;

    //加载开始
    public final int LOAD_START = 1000026;

    private MediaPlayerManager() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        playerHandler = new Handler();
    }

    public static void init(Context context){
        Intent intent = new Intent(context, MusicService.class);
        context.bindService(intent,new MusicServiceConnection(),BIND_AUTO_CREATE);
    }

    public static MediaPlayerManager get() {
        if (INSTANCE == null) {
            synchronized (MediaPlayerManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MediaPlayerManager();
                }
            }
        }
        return INSTANCE;
    }

    public static void set(MediaPlayerManager mediaPlayerManager){
        INSTANCE = mediaPlayerManager;
    }

    public void addOnPlayerEventListener(OnPlayerEventListener listener) {
        this.onPlayerEventListener = listener;
    }

    public void setMusicPath(List<MusicInfo> listPath) {
        if (null != listPath) {
            if (listPath.size() > 0) {
                this.mMusicInfos = listPath;
                this.musicCount = listPath.size();
                this.musicPath = listPath.get(0).getMusicPath();
                this.musicInfo = listPath.get(0);
            }
        }
    }

    public void playMusicInfo(MusicInfo info) {
        if (null == info){
            return;
        }
        setMusicInfo(info);
        this.musicPath = info.getMusicPath();
        playerHandler.removeCallbacks(playerRunnable);
        currentPosition = 0;
        initPlayer();
    }

    public void setPlayMode(int mode) {
        this.playMode = mode;
        onPlayerEventListener.onPlayMode(playMode);
    }

    public int getPlayMode() {
        return playMode;
    }

    /**
     * 播放
     */
    public void play() {
        musicPath = getMusicPath();
        initPlayer();
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
            onPlayerEventListener.onPlaySwitch(getMusicInfo());
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
        switchMusic(MusicPlayType.MUSIC_PREVIOUS);
    }

    /**
     * 下一曲
     */
    public void next() {
        if (isCanPlay()) {
            return;
        }
        switchMusic(MusicPlayType.MUSIC_NEXT);
    }

    /**
     * 跳转播放
     *
     * @param position
     */
    public void seekTo(int position) {
        mediaPlayer.seekTo(position);
        onPlayerEventListener.isLoadIng(LOAD_START);
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

    /**
     * 初始化播放器
     */
    private void initPlayer(){
        try {
            if (null != musicPath) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(musicPath);//设置要播放的音频
                mediaPlayer.prepareAsync();//异步加载音频
                onPlayerEventListener.onPlaySwitch(getMusicInfo());
                onPlayerEventListener.isLoadIng(LOAD_START);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void switchMusic(int playType) {
        if (playMode == PlayMode.RANDOM) {
            playNum = getMusicNum(MusicPlayType.MUSIC_RANDOM);
        } else if (playMode == PlayMode.LIST_LOOP) {
            if (playNum == getMusicCount()) {
                playNum = 0;
            } else {
                playNum = getMusicNum(playType);
            }
        } else if (playMode == PlayMode.ORDER) {
            playNum = getMusicNum(playType);
            if (playNum == getMusicCount() && !isPlaying()) {
                if (isPlayEnd()) {
                    onPlayerEventListener.onPlayProgress(0);
                    onPlayerEventListener.onPlayPause();
                } else {
                    resumePlay();
                }
            }
        } else if (playMode == PlayMode.SINGLE_LOOP) {
            if (!isPlayEnd()) {
                playNum = getMusicNum(playType);
            }
        }

        playerHandler.removeCallbacks(playerRunnable);
        currentPosition = 0;
        this.play();
    }

    public MusicInfo getMusicInfo() {
        return musicInfo;
    }

    public int getPlayNum(){
        return playNum;
    }

    private void setMusicInfo(MusicInfo info) {
        this.musicInfo = info;
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
            if (0 <= playNum && playNum <= count) {
                setMusicInfo(mMusicInfos.get(playNum));
                return mMusicInfos.get(playNum).getMusicPath();
            } else {
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
            int max = musicCount;
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

    private int getMusicCount() {
        if (musicCount > 0) {
            return musicCount - 1;
        }
        return 0;
    }

    /**
     * 是否播放结束
     *
     * @return
     */
    private boolean isPlayEnd() {
        int temp = getDuration() - currentPosition;
        if (temp <= 150) {
            return true;
        }
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        onPlayerEventListener.onPlayStart();
        onPlayerEventListener.isLoadIng(LOAD_FINISH);
        playerHandler.post(playerRunnable);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int percent) {
        this.bufferPercentage = percent;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        onPlayerEventListener.isLoadIng(LOAD_FINISH);
    }

    /**
     * 销毁
     */
    public void destory() {
        if (isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
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
