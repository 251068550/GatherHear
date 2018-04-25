package com.nicmic.gatherhear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.constant.Constant;

import java.io.File;

/**
 * Created by Administrator on 2015/10/10.
 */
public class FileUtils {

    public static int SD_CARD;
    public static final int SD_CARD_INNER = 0;
    public static final int SD_CARD_EXT = 1;

    public static final String INNER_SD_CARD_PATH = "/storage/emulated/0/";
    public static final String EXT_SD_CARD_PATH = "/storage/extSdCard/";

    /**
     * 创建歌词文件夹
     */
    public static void createLrcFile(){
        String sDir = "";
        if (getAppFilePath() == SD_CARD_INNER) {
            if (isSDCardMounted()) {//内置SD卡应该一直都是存在的
                sDir = INNER_SD_CARD_PATH + Constant.FILE_LRC;
            }
        }else if (getAppFilePath() == SD_CARD_EXT) {
            if (isExtSDCardMounted()) {
                sDir = EXT_SD_CARD_PATH + Constant.FILE_LRC;
            }else{//选中的是外置SD卡，但外置SD卡被拔出或者存储快满的时候默认选择内置SD卡
                if (isSDCardMounted()) {
                    sDir = Environment.getExternalStorageDirectory() + "/" + Constant.FILE_LRC;
                }
            }
        }
        //创建文件夹
        File destDir = new File(sDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    /**
     * 创建歌曲文件夹
     */
    public static void createSongFile(){
        String sDir = "";
        if (getAppFilePath() == SD_CARD_INNER) {
            if (isSDCardMounted()) {//内置SD卡应该一直都是存在的
                sDir = INNER_SD_CARD_PATH + Constant.FILE_SONG;
            }
        }else if (getAppFilePath() == SD_CARD_EXT) {
            if (isExtSDCardMounted()) {
                sDir = EXT_SD_CARD_PATH + Constant.FILE_SONG;
            }else{//选中的是外置SD卡，但外置SD卡被拔出或者存储快满的时候默认选择内置SD卡
                if (isSDCardMounted()) {
                    sDir = Environment.getExternalStorageDirectory() + "/" + Constant.FILE_SONG;
                }
            }
        }
        //创建文件夹
        File destDir = new File(sDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    /**
     * 获得歌词文件夹路径
     * @return
     */
    public static String getLrcFile(){
        String sDir = "";
        if (getAppFilePath() == SD_CARD_INNER) {
            if (isSDCardMounted()) {//内置SD卡应该一直都是存在的
                sDir = INNER_SD_CARD_PATH + Constant.FILE_LRC;
            }
        }else if (getAppFilePath() == SD_CARD_EXT) {
            if (isExtSDCardMounted()) {
                sDir = EXT_SD_CARD_PATH + Constant.FILE_LRC;
            }else{//选中的是外置SD卡，但外置SD卡被拔出或者存储快满的时候默认选择内置SD卡
                if (isSDCardMounted()) {
                    sDir = Environment.getExternalStorageDirectory() + "/" + Constant.FILE_LRC;
                }
            }
        }
        return sDir;
    }

    /**
     * 获得歌曲文件夹路径
     * @return
     */
    public static String getSongFile(){
        String sDir = "";
        if (getAppFilePath() == SD_CARD_INNER) {
            if (isSDCardMounted()) {//内置SD卡应该一直都是存在的
                sDir = INNER_SD_CARD_PATH + Constant.FILE_SONG;
            }
        }else if (getAppFilePath() == SD_CARD_EXT) {
            if (isExtSDCardMounted()) {
                sDir = EXT_SD_CARD_PATH + Constant.FILE_SONG;
            }else{//选中的是外置SD卡，但外置SD卡被拔出或者存储快满的时候默认选择内置SD卡
                if (isSDCardMounted()) {
                    sDir = Environment.getExternalStorageDirectory() + "/" + Constant.FILE_SONG;
                }
            }
        }
        return sDir;
    }

    /**
     * 获得SD卡总大小
     *
     * @return
     */
    public static String getSDTotalSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(App.sContext, blockSize * totalBlocks);
    }

    /**
     * 获得sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static String getSDAvailableSize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(App.sContext, blockSize * availableBlocks);
    }

    /**
     * 获得扩展SD卡总大小
     *
     * @return
     */
    public static String getExtSDTotalSize() {
        String path = EXT_SD_CARD_PATH;
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(App.sContext, blockSize * totalBlocks);
    }

    /**
     * 获得扩展sd卡剩余容量，即可用大小
     *
     * @return
     */
    public static String getExtSDAvailableSize() {
        String path = EXT_SD_CARD_PATH;
        StatFs stat = new StatFs(path);
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(App.sContext, blockSize * availableBlocks);
    }

    /**
     * 获得机身内存总大小
     *
     * @return
     */
    public static String getRomTotalSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Formatter.formatFileSize(App.sContext, blockSize * totalBlocks);
    }

    /**
     * 获得机身可用内存
     *
     * @return
     */
    public static String getRomAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Formatter.formatFileSize(App.sContext, blockSize * availableBlocks);
    }

    public static boolean isSDCardMounted(){
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return false;
        }
        return true;
    }

    public static boolean isExtSDCardMounted() {
        String path = "/storage/extSdCard";
        return checkFsWritable(path);
    }

    // 测试外置sd卡是否卸载，不能直接判断外置sd卡是否为null，因为当外置sd卡拔出时，仍然能得到外置sd卡路径。我这种方法是按照android谷歌测试DICM的方法，
    // 创建一个文件，然后立即删除，看是否卸载外置sd卡
    // 注意这里有一个小bug，即使外置sd卡没有卸载，但是存储空间不够大，或者文件数已至最大数，此时，也不能创建新文件。此时，统一提示用户清理sd卡吧
    private static boolean checkFsWritable(String dir) {

        if (dir == null)
            return false;

        File directory = new File(dir);

        if (!directory.isDirectory()) {
            if (!directory.mkdirs()) {
                return false;
            }
        }

        File f = new File(directory, ".keysharetestgzc");
        try {
            if (f.exists()) {
                f.delete();
            }
            if (!f.createNewFile()) {
                return false;
            }
            f.delete();
            return true;

        } catch (Exception e) {
        }
        return false;

    }

    /**
     * 保存应用文件夹的路径
     * @param path
     */
    public static void saveAppFilePath(int path) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("file_path", path);
        editor.commit();
    }

    /**
     * 获得应用文件夹路径
     * 并且修改常量值
     * @return
     */
    public static int getAppFilePath() {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        int path = sp.getInt("file_path", SD_CARD_INNER);
        SD_CARD = path;
        return path;
    }

}
