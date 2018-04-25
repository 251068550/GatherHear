package com.nicmic.gatherhear.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nicmic.gatherhear.constant.Constant;

/**
 * Created by Administrator on 2015/9/10.
 */
public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, Constant.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //本地音乐
        db.execSQL("create table music ("
                + "_id integer primary key autoincrement not null,"
                + "title varchar(50),"
                + "artist varchar(50),"
                + "album varchar(50),"
                + "path varchar(100),"
                + "duration varchar(50),"
                + "size varchar(50),"
                + "position integer,"
                + "my_like integer default(0),"//我喜欢的：否0，是1
                + "play_time long default(0),"//播放时间
                + "song_id long,"
                + "album_id long)");

        //播放列表
        db.execSQL("create table playlist ("
                + "_id integer primary key autoincrement not null,"
                + "title varchar(50),"
                + "artist varchar(50),"
                + "album varchar(50),"
                + "path varchar(100),"
                + "duration varchar(50),"
                + "size varchar(50),"
                + "position integer,"
                + "my_like integer default(0),"//我喜欢的：否0，是1
                + "play_time long default(0),"//播放时间
                + "song_id long,"
                + "album_id long)");

        //歌单信息
        db.execSQL("create table musicmenu ("
                + "_id integer primary key autoincrement not null,"
                + "title varchar(50),"
                + "desc varchar(50))");

        //歌单和音乐的关联表
        db.execSQL("create table musicmenu_music ("
                + "_id integer primary key autoincrement not null,"
                + "musicmenu_id varchar(50),"
                + "music_id varchar(50))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
