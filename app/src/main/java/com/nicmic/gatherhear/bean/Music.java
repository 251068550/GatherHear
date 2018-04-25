package com.nicmic.gatherhear.bean;

/**
 * Created by Administrator on 2015/9/10.
 */
public class Music implements Comparable<Music>{

    private String id;
    private String title;
    private String artist;
    private String album;
    private String path;
    private String duration;
    private String size;
    private int position;//用于排序
    private int myLike;
    private long playTime;

    private long songId;//用于显示内置图片
    private long albumId;//用于显示内置图片

    public Music() {
    }

    public Music(String id, String title, String artist, String album, String path, String duration, String size, int position,
    int myLike, long playTime, long songId, long albumId) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.path = path;
        this.duration = duration;
        this.size = size;
        this.position = position;
        this.myLike = myLike;
        this.playTime = playTime;
        this.songId = songId;
        this.albumId = albumId;
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getMyLike() {
        return myLike;
    }

    public void setMyLike(int myLike) {
        this.myLike = myLike;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    @Override
    public int compareTo(Music another) {
        //降序排序
        if (this.playTime > another.playTime){
            return -1;
        }else if (this.playTime == another.playTime) {
            return 0;
        }
        return 1;
    }
}
