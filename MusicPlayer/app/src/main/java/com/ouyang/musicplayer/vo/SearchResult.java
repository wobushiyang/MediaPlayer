package com.ouyang.musicplayer.vo;

/**
 * Created by ASUS-PC on 2016/7/13.
 * 搜索音乐对象
 */
public class SearchResult {


    private String musicName;//歌名
    private String url;//地址
    private String artist;//歌者
    private String album;//专辑

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
