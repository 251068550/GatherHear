package com.nicmic.gatherhear.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.MusicMenu;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.constant.Constant;
import com.nicmic.gatherhear.service.MusicService;
import com.gitonway.lee.niftynotification.lib.Configuration;
import com.gitonway.lee.niftynotification.lib.Effects;
import com.gitonway.lee.niftynotification.lib.NiftyNotificationView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/9/10.
 */
public class MusicUtils {

    /**
     * 扫描外部存储设备指定最短时间的所有音乐
     * (过滤掉leastTime秒以下的音乐)
     * @param context
     * @param leastTime 最小的音乐时长（单位：s）
     * @return
     */
    public static List<Map<String, String>> scanMusic(Context context, int leastTime){

        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        ContentResolver resolver = context.getContentResolver();
        String selection = "duration>?";
        String[] selectionArgs = new String[]{1000 * leastTime + ""};
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, selection, selectionArgs, null);
        while(cursor.moveToNext()) {

            //获取单个音乐文件的基本信息
//            歌曲的名称
            String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
//            歌曲的歌手名
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
//            歌曲的专辑名
            String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
//            歌曲文件的路径
            String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
//            歌曲的总播放时长
            String duration = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
//            歌曲文件的大小
            String size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
//            歌曲ID
            String songId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
//            专辑ID
            String albumId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

            //将音乐信息保存至map集合中
            Map<String, String> map = new HashMap<>();
            map.put("title", title);
            map.put("artist", artist);
            map.put("album", album);
            map.put("path", path);
            map.put("duration", duration);
            map.put("size", size);
            map.put("song_id", songId);
            map.put("album_id", albumId);

            list.add(map);
        }
        cursor.close();

        return list;
    }

    /**
     * 保存扫描出来的音乐到数据库中
     *
     * 保存前先从数据库中获取歌曲，如果已存在(根据路径)则覆盖原歌曲。
     * 如果不覆盖歌单对应个歌曲id会找不到歌曲
     * @param context
     * @param list
     */
    public static void saveScanedMusic(Context context, List<Map<String, String>> list){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        List<Music> musics = getMusic(context);

//        db.execSQL("delete from music");

        int position = 0;
        for(int i = 0; i < list.size(); i++) {
            boolean isExist = false;
            for (int j = 0; j < musics.size(); j++) {
                if (musics.get(j).getPath().equals(list.get(i).get("path"))){
                    isExist = true;
                }
            }
            if (!isExist){
                ContentValues values = new ContentValues();
                values.put("title", list.get(i).get("title"));
                values.put("artist", list.get(i).get("artist"));
                values.put("album", list.get(i).get("album"));
                values.put("path", list.get(i).get("path"));
                values.put("duration", list.get(i).get("duration"));
                values.put("size", list.get(i).get("size"));
                values.put("position", position++);
                values.put("my_like", list.get(i).get("my_like"));
                values.put("play_time", list.get(i).get("play_time"));
                values.put("song_id", list.get(i).get("song_id"));
                values.put("album_id", list.get(i).get("album_id"));
                db.insert("music", null, values);
            }
        }

        db.close();
    }

    /**
     * 保存排序后的音乐到数据库中
     * @param context
     * @param musics
     */
    public static void saveMusic(Context context, List<Music> musics){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        for(int i = 0; i < musics.size(); i++) {
            ContentValues values = new ContentValues();
            values.put("position", musics.get(i).getPosition());
            db.update("music", values, "_id = ?", new String[]{musics.get(i).getId()});
        }

        db.close();
    }

    /**
     * 将歌单中指定位置的歌曲移出当前歌单
     * （此方法仅限歌单使用）
     * @param context
     * @param musicMenu
     * @param position
     */
    public static void removeMusic(Context context, MusicMenu musicMenu, int position){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        String musicMenuId = musicMenu.getId();
        String musicId = musicMenu.getMusics().get(position).getId();
        try {
            db.delete("musicmenu_music", "musicmenu_id = ? and music_id = ?", new String[]{musicMenuId, musicId});
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            db.close();
        }

    }

    /**
     * 删除一首歌曲
     * 不删除文件
     * @param context
     * @param music
     * @return
     */
    public static boolean deleteMusic(Context context, Music music) {
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        //todo：如果用到排序，则position也需要排序

        db.beginTransaction();
        try {
            //删除music表中的歌曲
            int column1 = db.delete("music", "_id = ?", new String[]{music.getId()});
            if (column1 != 1){
                return false;
            }
            //删除musicmenu_music表中的关系
            int column2 = db.delete("musicmenu_music", "music_id = ?", new String[]{music.getId()});
            Log.e("MusicUtils", "共删除" + column2 + "张歌单中的该歌曲");

            db.setTransactionSuccessful();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            db.endTransaction();
            db.close();
        }
        /*小知识：
          1. finally 里 始终会被执行到， System.exit(0); 除这种被执行外。即使是发现了异常，如(3)中被注释的throw 异常，也会在抛异常前先执行finally.
          2. 即使try中有return ，也是先执行 return 后面的语句完了之后，不立马return，而是去执行finally中的语句。
          3. 当try中与finally里，同时出现return , 则只会返回 finally 中的return 结果。
          4. finally中的值不能影响try中 即将返回的结果值。
        */
    }

    /**
     * 播放列表中是否包含当前歌曲
     * @param music
     * @return 不存在则返回-1，存在则返回在播放列表中的位置
     */
    public static int inPlayListPosition(Music music){
        if (PlayList.isEmpty()){
            return -1;
        }
        //如果第一首没有id，说明是网络音乐，并且播放列表只有一首歌曲
        if (PlayList.musics.get(0).getId() == null) {
            return 0;
        }
        for (int i = 0; i < PlayList.musics.size(); i++) {
            if (PlayList.musics.get(i).getId().equals(music.getId())){
                return i;
            }
        }
        return -1;
    }

    /**
     * 从播放列表中移除一首歌曲
     * @param context
     * @param music
     */
    public static void removePlayListMusic(Context context, Music music) {
        // TODO: 2015/9/30 可能要考虑curentUI是并更新相关UI
        int position = inPlayListPosition(music);
        //1.移除的歌曲在播放列表中
        if (position != -1){
            //2.移除的是正在播放的歌曲
            if (music.getId().equals(PlayList.getPlayingMusic().getId())) {
                Log.e("MusicUtils", "移除的是正在播放的歌曲");
                //3.歌曲移除后播放列表没有歌曲
                if (PlayList.musics.size() == 1) {
                    //3.1清空播放列表
                    PlayList.clearPlayList(context);
                    //3.2停止播放
                    PlayList.playStatus = MusicService.PLAYING;
                    MusicService.stop();
                    Log.e("MusicUtils", "移除后播放列表没有歌曲，停止播放");
                }
                //4.歌曲移除后播放列表有歌曲
                if (PlayList.musics.size() > 1) {
                    //4.1移除在播放列表中的该歌曲
                    PlayList.musics.remove(position);
                    //4.2停止播放，并将播放的歌曲指定为播放列表的第一首歌曲
                    MusicService.playFirst();
                    Log.e("MusicUtils", "移除后播放列表有歌曲，播放第一首");
                }
            }else{
            //2.移除的不是正在播放的歌曲
                Log.e("MusicUtils", "移除的不是正在播放的歌曲");
                //3移除在播放列表中的该歌曲
                PlayList.musics.remove(position);
            }

            //5.保存播放列表
            savePlayList();
            //6.保存播放的位置
            savePlayPosition();
        }

    }

    /**
     * 获得本地音乐列表
     * @param context
     * @return
     */
    public static List<Music> getMusic(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        List<Music> musics = new ArrayList<Music>();

        Cursor cursor = db.query("music", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            String size = cursor.getString(cursor.getColumnIndex("size"));
            int position = cursor.getInt(cursor.getColumnIndex("position"));
            int my_like = cursor.getInt(cursor.getColumnIndex("my_like"));
            long play_time = cursor.getLong(cursor.getColumnIndex("play_time"));
            long song_id = cursor.getLong(cursor.getColumnIndex("song_id"));
            long album_id = cursor.getLong(cursor.getColumnIndex("album_id"));

            Music music = new Music(id, title, artist, album, path, duration, size, position, my_like, play_time, song_id, album_id);
            musics.add(music);
        }
        cursor.close();
        db.close();

        return sortMusicByPosition(musics);
    }

    /**
     * 将歌曲按照position升序排序
     * 冒泡排序法
     * @param musics
     * @return
     */
    public static List<Music> sortMusicByPosition(List<Music> musics) {
        Music temp;
        for (int i = 0; i < musics.size() - 1; i++) {//趟数
            for (int j = 0; j < musics.size() - i - 1; j++) {//比较次数
                if (musics.get(j).getPosition() > musics.get(j + 1).getPosition()) {
                    temp = musics.get(j);
                    musics.set(j, musics.get(j + 1));
                    musics.set(j + 1, temp);
                }
            }
        }

        return musics;
    }

    /**
     * 获得本地音乐列表
     * 以键值对形式返回
     * @param context
     * @return
     */
    public static Map<String, Music> getMusicMap(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Map<String, Music> map = new HashMap<>();

        Cursor cursor = db.query("music", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            String size = cursor.getString(cursor.getColumnIndex("size"));
            int position = cursor.getInt(cursor.getColumnIndex("position"));
            int my_like = cursor.getInt(cursor.getColumnIndex("my_like"));
            long play_time = cursor.getLong(cursor.getColumnIndex("play_time"));
            long song_id = cursor.getLong(cursor.getColumnIndex("song_id"));
            long album_id = cursor.getLong(cursor.getColumnIndex("album_id"));

            Music music = new Music(id, title, artist, album, path, duration, size, position, my_like, play_time, song_id, album_id);
            map.put(music.getId(), music);
        }
        cursor.close();
        db.close();

        return map;
    }

    /**
     * 获得按歌手分类的音乐集合
     * @param context
     * @return
     */
    public static Map<String, List<Music>> getMusicArtist(Context context) {
        List<Music> musics = getMusic(context);

        Map<String, List<Music>> map = new HashMap<>();
        for (int i = 0; i < musics.size(); i++) {
            String artist = musics.get(i).getArtist();
            if (map.get(artist) == null) {
                List<Music> list = new ArrayList<>();
                list.add(musics.get(i));
                map.put(artist, list);
            }else {
                List<Music> list = map.get(artist);
                list.add(musics.get(i));
                map.put(artist, list);
            }
        }

        return map;
    }

    /**
     * 获得按专辑分类的音乐集合
     * @param context
     * @return
     */
    public static Map<String, List<Music>> getMusicAlbum(Context context) {
        List<Music> musics = getMusic(context);

        Map<String, List<Music>> map = new HashMap<>();
        for (int i = 0; i < musics.size(); i++) {
            String album = musics.get(i).getAlbum();
            if (map.get(album) == null) {
                List<Music> list = new ArrayList<>();
                list.add(musics.get(i));
                map.put(album, list);
            }else {
                List<Music> list = map.get(album);
                list.add(musics.get(i));
                map.put(album, list);
            }
        }

        return map;
    }

    /**
     * 获得按文件夹分类的音乐集合
     * @param context
     * @return
     */
    public static Map<String, List<Music>> getMusicFile(Context context) {
        List<Music> musics = getMusic(context);

        Map<String, List<Music>> map = new HashMap<>();
        for (int i = 0; i < musics.size(); i++) {
            String path = musics.get(i).getPath();
            path = path.substring(0, path.lastIndexOf("/") + 1);

            if (map.get(path) == null) {
                List<Music> list = new ArrayList<>();
                list.add(musics.get(i));
                map.put(path, list);
            }else {
                List<Music> list = map.get(path);
                list.add(musics.get(i));
                map.put(path, list);
            }
        }

        return map;
    }

    /**
     * 保存播放的时间（用于最近播放）
     * 在播放上一首或下一首或播放完成自动播放下一首时调用
     */
    public static void savePlayTime() {
        Music music = PlayList.getPlayingMusic();
        if (music != null) {
            long playTime = System.currentTimeMillis();

            //修改播放列表该歌曲的播放时间
            PlayList.musics.get(PlayList.position).setPlayTime(playTime);
            //修改数据库music表中该歌曲的播放时间
            DBHelper helper = new DBHelper(App.sContext);
            SQLiteDatabase db = helper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put("play_time", playTime);
            int column = db.update("music", values, "_id = ?", new String[]{music.getId()});
            db.close();
            if (column == 1) {
                Log.e("MusicUtils", "savePlayTime：修改最近播放时间成功");
            }else{
                Log.e("MusicUtils", "savePlayTime：修改最近播放时间失败");
            }
        }
    }

    /**
     * 获得最近播放的音乐列表
     * ps：可以自己写个高效的算法进行排序
     * @param context
     * @return
     */
    public static List<Music> getRecentPlay(Context context){
        List<Music> musics = getMusic(context);
        List<Music> recentPlayMusics = new ArrayList<>();
        Collections.sort(musics);
        for (int i = 0; i < musics.size(); i++) {
            if (musics.get(i).getPlayTime() != 0) {
                recentPlayMusics.add(musics.get(i));
            }
        }
        return recentPlayMusics;
    }

    /**
     * 保存播放列表,播放位置,播放列表所处的UI
     * 在播放上一首或下一首或播放完成自动播放下一首时调用
     */
    public static void savePlayList() {
        DBHelper helper = new DBHelper(App.sContext);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.execSQL("delete from playlist");

        List<Music> musics = PlayList.musics;

        for(int i = 0; i < musics.size(); i++) {
            ContentValues values = new ContentValues();
            values.put("_id", musics.get(i).getId());
            values.put("title", musics.get(i).getTitle());
            values.put("artist", musics.get(i).getArtist());
            values.put("album", musics.get(i).getAlbum());
            values.put("path", musics.get(i).getPath());
            values.put("duration", musics.get(i).getDuration());
            values.put("size", musics.get(i).getSize());
            values.put("position", musics.get(i).getPosition());
            values.put("my_like", musics.get(i).getMyLike());
            values.put("play_time", musics.get(i).getPlayTime());
            values.put("song_id", musics.get(i).getSongId());
            values.put("album_id", musics.get(i).getAlbumId());
            db.insert("playlist", null, values);
        }

        db.close();

    }

    /**
     * 保存播放列表播放的位置
     */
    public static void savePlayPosition() {
        //保存播放位置,所处的UI
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("position", PlayList.position);
        editor.putInt("UI", MusicService.CURRENT_UI);
        editor.commit();
    }

    /**
     * 保存已播放时长和歌曲总时长
     */
    public static void saveProgressDuration(){
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (MusicService.player != null && MusicService.player.getDuration() != 0) {
            editor.putInt("progress", MusicService.player.getCurrentPosition());
            editor.putInt("duration", MusicService.player.getDuration());
            Log.e("MusicUtils", MusicUtils.getTimeString(MusicService.player.getCurrentPosition()) + "/" + MusicUtils.getTimeString(MusicService.player.getDuration()));
        }

        editor.commit();
    }

    /**
     * 获得播放列表
     * @return
     */
    public static List<Music> getPlayList(){
        DBHelper helper = new DBHelper(App.sContext);
        SQLiteDatabase db = helper.getReadableDatabase();

        List<Music> musics = new ArrayList<Music>();

        Cursor cursor = db.query("playlist", null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            String size = cursor.getString(cursor.getColumnIndex("size"));
            int position = cursor.getInt(cursor.getColumnIndex("position"));
            int my_like = cursor.getInt(cursor.getColumnIndex("my_like"));
            long play_time = cursor.getLong(cursor.getColumnIndex("play_time"));
            long song_id = cursor.getLong(cursor.getColumnIndex("song_id"));
            long album_id = cursor.getLong(cursor.getColumnIndex("album_id"));

            Music music = new Music(id, title, artist, album, path, duration, size, position, my_like, play_time, song_id, album_id);
            musics.add(music);
        }
        cursor.close();
        db.close();

        return sortMusicByPosition(musics);
    }

    /**
     * 获得播放列表的播放位置,并设置当前的UI
     * @param context
     * @return
     */
    public static int getPlayListPosition(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        MusicService.CURRENT_UI = sp.getInt("UI", MusicService.CURRENT_UI);
        //有个updateUI什么的也要保存
        return sp.getInt("position", -1);
    }

    /**
     * 获得播放列表播放歌曲的进度
     * @param context
     * @return
     */
    public static int getPlayListProgress(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt("progress", 0);
    }

    /**
     * 获得播放列表播放歌曲的总时长
     * @param context
     * @return
     */
    public static int getPlayListDuration(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt("duration", 1);
    }

    /**
     * 获得我喜欢的音乐列表
     * @param context
     * @return
     */
    public static List<Music> getMyLikeMusic(Context context){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        List<Music> musics = new ArrayList<Music>();

        Cursor cursor = db.query("music", null, "my_like = ?", new String[]{"1"}, null, null, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndex("_id"));
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String artist = cursor.getString(cursor.getColumnIndex("artist"));
            String album = cursor.getString(cursor.getColumnIndex("album"));
            String path = cursor.getString(cursor.getColumnIndex("path"));
            String duration = cursor.getString(cursor.getColumnIndex("duration"));
            String size = cursor.getString(cursor.getColumnIndex("size"));
            int position = cursor.getInt(cursor.getColumnIndex("position"));
            int my_like = cursor.getInt(cursor.getColumnIndex("my_like"));
            long play_time = cursor.getLong(cursor.getColumnIndex("play_time"));
            long song_id = cursor.getLong(cursor.getColumnIndex("song_id"));
            long album_id = cursor.getLong(cursor.getColumnIndex("album_id"));

            Music music = new Music(id, title, artist, album, path, duration, size, position, my_like, play_time, song_id, album_id);
            musics.add(music);
        }
        cursor.close();
        db.close();
        return musics;
    }

    /**
     * 添加或移除一首我喜欢的音乐
     * @param context
     * @param music
     */
    public static void addRemoveMyLike(Context context, Music music) {
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        int myLike = 0;
        if (music.getMyLike() == 0) {
            myLike = 1;
        }

        ContentValues values = new ContentValues();
        values.put("_id", music.getId());
        values.put("title", music.getTitle());
        values.put("artist", music.getArtist());
        values.put("album", music.getAlbum());
        values.put("path", music.getPath());
        values.put("duration", music.getDuration());
        values.put("size", music.getSize());
        values.put("position", music.getPosition());
        values.put("my_like", myLike);
        values.put("play_time", music.getPlayTime());
        values.put("song_id", music.getSongId());
        values.put("album_id", music.getAlbumId());

        int column = db.update("music", values, "_id = ?", new String[]{music.getId()});
        db.close();
        if (column == 1){
            Log.e("MusicUtils", "addRemoveMyLike修改了一行数据");
        }
    }

    /**
     * 添加一张歌单
     * @param context
     */
    public static void createMusicMenu(Context context, MusicMenu musicMenu) {
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", musicMenu.getTitle());
        values.put("desc", musicMenu.getDesc());
        db.insert("musicmenu", null, values);
        db.close();
    }

    /**
     * 删除一张歌单
     * @param context
     * @param musicMenu
     */
    public static void deleteMusicMenu(Context context, MusicMenu musicMenu){
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        //删除歌单信息
        int column1 = db.delete("musicmenu", "_id = ?", new String[]{musicMenu.getId()});
        //删除歌单和歌曲关联表信息
        int column2 = db.delete("musicmenu_music", "musicmenu_id = ?", new String[]{musicMenu.getId()});
        if (column1 == 1){
            Log.e("MusicUtils", "删除一张歌单成功");
            Log.e("MusicUtils", "共删除了歌单和歌曲关联表中的记录:" + column2);;
        }else{
            Log.e("MusicUtils", "删除一张歌单失败");
        }
        db.close();
    }

    /**
     * 修改一张歌单的信息
     * @param context
     */
    public static void modefyMusicMenu(Context context, MusicMenu musicMenu) {
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("_id", musicMenu.getId());
        values.put("title", musicMenu.getTitle());
        values.put("desc", musicMenu.getDesc());
        int column = db.update("musicmenu", values, "_id = ?", new String[]{musicMenu.getId()});
        if (column == 1){
            Log.e("MusicUtils", "modefyMusicMenu:修改一张歌单信息成功");
        }else{
            Log.e("MusicUtils", "modefyMusicMenu:修改一张歌单信息失败");
        }

        db.close();
    }

    /**
     * 往指定歌单中添加一首歌曲
     * @param context
     * @param musicMenu
     * @param music
     * @return true 添加成功， false 添加失败
     */
    public static boolean addMusic2MusicMenu(Context context, MusicMenu musicMenu, Music music) {
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from musicmenu_music where musicmenu_id = ? and music_id = ?",
                new String[]{musicMenu.getId(), music.getId()});

        //歌单中已存在这首歌曲
        if (cursor.moveToNext()){
            return false;
        }

        ContentValues values = new ContentValues();
        values.put("musicmenu_id", musicMenu.getId());
        values.put("music_id", music.getId());
        db.insert("musicmenu_music", null, values);

        cursor.close();
        db.close();

        return true;
    }

    /**
     * 往指定歌单中添加多首歌曲
     * @param context
     * @param musicMenu
     * @param musics
     * @return int[0]:失败的数目， int[1]成功的数目
     */
    public static int[] addMusics2MusicMenu(Context context, MusicMenu musicMenu, List<Music> musics) {
        int successNum = 0;//添加成功的数目
        int failureNum = 0;//添加失败的数目

        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        for (int i = 0; i < musics.size(); i++) {
            Music music = musics.get(i);
            Cursor cursor = db.rawQuery("select * from musicmenu_music where musicmenu_id = ? and music_id = ?",
                    new String[]{musicMenu.getId(), music.getId()});
            //歌单中已存在这首歌曲
            if (cursor.moveToNext()) {
                failureNum ++;
            }else{
                ContentValues values = new ContentValues();
                values.put("musicmenu_id", musicMenu.getId());
                values.put("music_id", music.getId());
                db.insert("musicmenu_music", null, values);

                successNum ++;
            }
            cursor.close();
        }
        db.close();

        return new int[]{failureNum, successNum};
    }

    /**
     * 获得歌单信息
     * @param context
     * @return
     */
    public static List<MusicMenu> getAllMusicMenu(Context context) {
        List<MusicMenu> musicMenus = new ArrayList<>();
        Map<String, Music> map = getMusicMap(context);

        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor1 = db.rawQuery("select * from musicmenu", null);
        while (cursor1.moveToNext()) {
            String id = cursor1.getString(cursor1.getColumnIndex("_id"));
            String title = cursor1.getString(cursor1.getColumnIndex("title"));
            String desc = cursor1.getString(cursor1.getColumnIndex("desc"));

            MusicMenu musicMenu = new MusicMenu(id, title, desc);

            List<Music> musics = new ArrayList<>();
            //查找歌单中的歌曲id
            Cursor cursor2 = db.rawQuery("select * from musicmenu_music where musicmenu_id = ?", new String[]{id});
            while (cursor2.moveToNext()) {
                String music_id = cursor2.getString(cursor2.getColumnIndex("music_id"));
                musics.add(map.get(music_id));
            }
            musicMenu.setMusics(musics);

            musicMenus.add(musicMenu);
            cursor2.close();
        }

        cursor1.close();
        db.close();

        return musicMenus;
    }

    /**
     * 修改歌曲的信息
     * @param context
     */
    public static void modefyMusicInfo(Context context, Music music) {
        DBHelper helper = new DBHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("_id", music.getId());
        values.put("title", music.getTitle());
        values.put("artist", music.getArtist());
        int column = db.update("music", values, "_id = ?", new String[]{music.getId()});
        if (column == 1){
            Log.e("MusicUtils", "modefyMusicInfo:修改歌曲信息成功");
        }else{
            Log.e("MusicUtils", "modefyMusicInfo:修改歌曲信息失败");
        }

        db.close();
    }

    public static int ORDER = 0;
    public static final int ORDER_SEQUENTIAL = 0;
    public static final int ORDER_LOOP = 1;
    public static final int ORDER_CYCLE = 2;
    public static final int ORDER_RANDOM = 3;

    /**
     * 设置播放顺序
     * @param context
     * @param order
     */
    public static void setPlayOrder(Context context, int order) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editer = sp.edit();
        editer.putInt("order", order);
        editer.commit();
        ORDER = order;
    }

    /**
     * 获得播放顺序
     * 在应用启动后应调用一次，以保持ORDER的值是最新的
     * @param context
     * @return
     */
    public static int getPlayOrder(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        int order = sp.getInt("order", ORDER_SEQUENTIAL);
        ORDER = order;
        return order;
    }

    public static List<Music> exchangeMusicPosition(Context context, List<Music> musics, int originalPosition, int newPosition){
        //交换位置
        if (newPosition > originalPosition) {
            for (int i = originalPosition; i <= newPosition; i++) {
                if (i == originalPosition){
                    musics.get(originalPosition).setPosition(newPosition);
                }else {
                    musics.get(i).setPosition(i - 1);
                }
            }
        }
        if (newPosition < originalPosition) {
            for (int i = originalPosition; i >= newPosition; i--) {
                if (i == originalPosition){
                    musics.get(originalPosition).setPosition(newPosition);
                }else {
                    musics.get(i).setPosition(i + 1);
                }
            }
        }
        //保存到数据库中
        saveMusic(context, musics);

        return sortMusicByPosition(musics);
    }

    /**
     * 获得下一首要播放的歌曲的位置
     * @return 下一首要播放的歌曲位置
     * -1：没有可播放的歌曲
     * -2：随机播放一首歌曲
     * >=0：播放列表中的歌曲
     */
    public static int getNextPlayMusicPosition(){
        // TODO: 2015/10/14

        int nextPosition = -1;

        //顺序播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_SEQUENTIAL){
            if (PlayList.position == PlayList.musics.size() - 1){//如果是最后一首，则暂停播放
                nextPosition = -1;
            }else{
                nextPosition = PlayList.position < PlayList.musics.size() - 1 ? PlayList.position + 1 : 0;
            }
        }

        //循环播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_LOOP){
            nextPosition = PlayList.position < PlayList.musics.size() - 1 ? PlayList.position + 1 : 0;
        }

        //单曲循环
        if (MusicUtils.ORDER == MusicUtils.ORDER_CYCLE){
            nextPosition = PlayList.position;
        }

        //随机播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_RANDOM){
            nextPosition = -2;
        }

        return nextPosition;
    }
    
    /**
     * 下一首歌曲的通知框
     * @param context
     * @param nextPosition -1:没有歌曲， -2:随机播放一首
     */
    public static void showNotify(Context context, int nextPosition){

        String msg = "";
        if (nextPosition == -1){
            msg = "即将播放\n没有可播放的歌曲";
        }else{
            if (nextPosition == -2){//随机播放的歌曲不可预测
                msg = "即将播放\n随机播放一首歌曲";
            }
            Music music = PlayList.musics.get(nextPosition);
            msg = "即将播放\n" + music.getTitle() + " - " + music.getArtist();
        }

        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION) {
            Configuration cfg = new Configuration.Builder()
                    .setAnimDuration(700)
                    .setDispalyDuration(2000)
                    .setBackgroundColor("#77FFFFFF")
                    .setTextColor("#000000")
                    .setIconBackgroundColor("#77FFFFFF")
                    .setTextPadding(5)                      //dp
                    .setViewHeight(48)                      //dp
                    .setTextLines(2)                        //You had better use setViewHeight and setTextLines together
                    .setTextGravity(Gravity.LEFT)         //only text def  Gravity.CENTER,contain icon Gravity.CENTER_VERTICAL
                    .build();

//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic1);
            Drawable drawable = context.getResources().getDrawable(R.drawable.default_cd_cover);
            if (nextPosition >= 0) {
                Music music = PlayList.musics.get(nextPosition);
                String uri = ImageUtils.getArtworkUri(context, music.getSongId(), music.getAlbumId(), true);
                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(uri);
                if (bitmap != null) {
                    drawable = new BitmapDrawable(bitmap);
                }
            }
            drawable.setBounds(0, 0, 48, 48);

            NiftyNotificationView.build((Activity) context, msg, Effects.thumbSlider, R.id.mLyout, cfg)
                    .setIcon(drawable).show();
        }
    }

    /**
     * 将毫秒数转化为时间值
     * 格式为（00：00）
     * @param t
     * @return
     */
    public static String getTimeString(long t){
        int time = (int) (t / 1000);
        int ms = time / 60;
        int ss = time % 60;
        ms = ms > 99 ? 99 : ms;

        StringBuilder sb = new StringBuilder();
        sb.append(ms < 10 ? "0" + ms + ":" : ms + ":");
        sb.append(ss < 10 ? "0" + ss : ss);

        return sb.toString();
    }

    /**
     * 将文件大小转化为GB,MB,KB
     * @param size
     * @return
     */
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.2f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.2f KB", f);
        } else
            return String.format("%d B", size);
    }

    /**
     * 根据content搜索本地音乐
     * 本地音乐中包含content中的内容
     * @param context
     * @param content
     * @return
     */
    public static List<Music> searchMusicByContent(Context context, String content) {
        List<Music> musics;
        List<Music> selectedMusics = getMusic(context);

        char[] words = content.toCharArray();
        for (int i = 0; i < words.length; i++) {
            musics = new ArrayList<>();
            musics.addAll(selectedMusics);

            String word = String.valueOf(words[i]);
            Log.e("MusicUtils", word + " : " + musics.size());
            for (int j = 0; j < musics.size(); j++) {
                if (!(musics.get(j).getTitle().contains(word) || musics.get(j).getArtist().contains(word)))  {
                    selectedMusics.remove(musics.get(j));
                }
            }
        }

        return selectedMusics;
    }

    /**
     * 清空我喜欢的歌曲列表
     */
    public static void clearMyLike() {
        DBHelper helper = new DBHelper(App.sContext);
        SQLiteDatabase db = helper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put("my_like", 0);
        db.update("music", values, null, null);

        db.close();
    }

    /**
     * 清空最近播放列表
     */
    public static void clearRecentPlay() {
        DBHelper helper = new DBHelper(App.sContext);
        SQLiteDatabase db = helper.getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put("play_time", 0);
        db.update("music", values, null, null);

        db.close();
    }

}
