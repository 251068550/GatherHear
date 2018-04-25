package com.nicmic.gatherhear;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.utils.DBHelper;
import com.nicmic.gatherhear.utils.FileUtils;
import com.nicmic.gatherhear.widget.MusicNotification;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/23.
 */
public class App extends Application {

    public static Context sContext;
    public static int sScreenWidth;
    public static int sScreenHeight;

    public static List<Activity> sActivitys = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;

        initData();
        initImageLoader();
    }

    private void initImageLoader() {
        File cacheDir = StorageUtils.getCacheDirectory(sContext);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCache(new UnlimitedDiskCache(cacheDir))
                .build();
        ImageLoader.getInstance().init(config);
    }

    public void initData() {
        //创建数据库
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        db.close();
        //创建应用文件夹
        FileUtils.createLrcFile();
        FileUtils.createSongFile();
        //初始化通知栏
        MusicNotification.initNotification();
        //加载动画开关配置
        AnimUtil.loadAnimStatus();
        //加载是否显示即将播放下一首的通知
        AnimUtil.getNextMusicNotification();
    }
}
