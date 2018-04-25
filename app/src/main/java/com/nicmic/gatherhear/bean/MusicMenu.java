package com.nicmic.gatherhear.bean;

import android.content.Context;

import com.nicmic.gatherhear.utils.MusicUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/23.
 * 歌单
 */
public class MusicMenu {

//    private int resId;//歌单封面图片id(以后在用)
    private String id;//歌单id
    private String title;//歌单名称
    private String desc;//歌单描述

    private List<Music> musics;

    public MusicMenu() {
    }

    public MusicMenu(String title, String desc, List<Music> musics) {
        this.title = title;
        this.desc = desc;
        this.musics = musics;
    }

    public MusicMenu(String id, String title, String desc) {
        this.id = id;
        this.title = title;
        this.desc = desc;
    }

    public MusicMenu(String title, String desc) {
        this.title = title;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<Music> getMusics() {
        return musics;
    }

    public void setMusics(List<Music> musics) {
        this.musics = musics;
    }

    public static List<MusicMenu> getMusicMenus(Context context){
        List<MusicMenu> musicMenus = new ArrayList<>();
        for(int i = 0; i <= 5; i ++){
            MusicMenu musicMenu = new MusicMenu();
            musicMenu.setTitle("我的歌单" + i);
            musicMenu.setDesc("你的眼眸，深邃而又明亮，让人猜不透" + i);
            musicMenu.setMusics(MusicUtils.getMusic(context));
            musicMenus.add(musicMenu);
        }
        return musicMenus;
    }

}
