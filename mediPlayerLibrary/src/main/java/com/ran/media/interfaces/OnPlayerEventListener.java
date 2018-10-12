package com.ran.media.interfaces;


import com.ran.media.model.MusicInfo;

/**
 * @author Ran
 * @date 2018/10/10.
 */

public interface OnPlayerEventListener {

    void onPlaySwitch(MusicInfo musicInfo);

    void onPlayStart();

    void onPlayPause();

    void onPlayProgress(int progress);

    void isLoadIng(int flag);

    void onPlayMode(int mode);
}
