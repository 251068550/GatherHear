package com.nicmic.gatherhear.bean;

import android.content.Context;
import android.util.Log;

import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.MusicUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/24.
 * 播放列表，所有要播放的音乐信息都在这个类中
 */
public class PlayList {
    /**
     * 播放列表音乐集合
     */
    public static List<Music> musics = new ArrayList<>();

    /**
     * 正在播放的音乐在播放列表中的位置
     * -1表示没有正在播放的音乐
     * 如果播放列表不为空，则position的值也不为-1，因为setPlayList()的同时也设置了position的值
     */
    public static int position = -1;

    /**
     * 音乐播放状态  0：初始状态   1：播放状态   2：暂停状态
     */
    public static int playStatus = 0;

    /**
     *判断播放列表是否为空
     * @return
     */
    public static boolean isEmpty(){
        if (musics.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 往播放列表中添加一首音乐
     * @param music
     */
    public static void addMusic(Music music){
        musics.add(music);
    }

    /**
     * 往播放列表中添加多首音乐
     * @param list
     */
    public static void addMusics(List<Music> list){
        musics.addAll(list);
    }

    /**
     * 设置播放列表
     * （每次点击某个音乐都需要调用此方法）
     * @param list 播放集合
     * @param position 当前播放的位置
     */
    public static void setPlayList(List<Music> list, int position){
        if (!musics.equals(list)){
            musics.clear();
            musics.addAll(list);
        }
        PlayList.position = position;
        PlayList.playStatus = MusicService.INITIAL;
        Log.e("PlayList", "数量：" + musics.size());
        Log.e("PlayList", "位置：" + position);
    }

    /**
     * 获得当前正在播放的音乐
     * @return
     */
    public static Music getPlayingMusic(){
        if (musics.isEmpty()){
            return null;
        }
        return  musics.get(position);
    }

    /**
     * 清空播放列表
     */
    public static boolean clearPlayList(Context context){
        if (isEmpty()){
            return false;
        }
        musics.clear();
        position = -1;
        playStatus = 0;
        MusicUtils.savePlayList();
        MusicUtils.savePlayPosition();
        return true;
    }

}
