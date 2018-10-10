package com.ran.media.model;

import java.io.Serializable;

/**
 * @author Ran
 * @date 2018/10/10.
 */

public class MusicInfo implements Serializable {

    private String musicId;

    private String musicTitle;

    private String musicPath;

    private String musicImg;

    public String getMusicId() {
        return musicId == null ? "" : musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public String getMusicTitle() {
        return musicTitle == null ? "" : musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getMusicPath() {
        return musicPath == null ? "" : musicPath;
    }

    public void setMusicPath(String musicPath) {
        this.musicPath = musicPath;
    }

    public String getMusicImg() {
        return musicImg == null ? "" : musicImg;
    }

    public void setMusicImg(String musicImg) {
        this.musicImg = musicImg;
    }
}