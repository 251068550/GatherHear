package com.nicmic.gatherhear.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.constant.Constant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * Created by Administrator on 2015/10/10.
 */
public class LrcUtils {

    static String TAG = "LrcUtils";

    //获取歌词
    public static String getFromAssets(Context context, String fileName){
        try {
            InputStreamReader inputReader = new InputStreamReader( context.getResources().getAssets().open(fileName) );
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null){
                if(line.trim().equals(""))
                    continue;
                Result += line + "\r\n";
            }
            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得歌词输入流，不可识别
     * @return
     */
    public static String getLrc() {
        try {
            String filepath = PlayList.getPlayingMusic().getPath();
            String filename = filepath.substring(filepath.lastIndexOf("/") + 1).replace("mp3", "lrc");
            File file = new File(FileUtils.getLrcFile() + filename);
            Log.e(TAG, FileUtils.getLrcFile() + filename);
            //获取文件编码
            String codeString = codeString(file);
            Log.e("编码", codeString);

            InputStream is = new FileInputStream(file);
            InputStreamReader inputReader = new InputStreamReader(is, codeString);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = bufReader.readLine()) != null) {
                if (line.trim().equals(""))
                    continue;
                result += line + "\r\n";
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获得歌词的文本，歌词文本内容
     * @return
     */
    public static String getLrcContent(String rawLrc) {
        if(rawLrc == null || rawLrc.length() == 0){
            return null;
        }

        StringReader reader = new StringReader(rawLrc);
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        String content = "";
        try{
            do{
                line = br.readLine();
                if(line != null && line.length() > 0) {
                    content += line + "\n";
                }

            }while(line != null);
            return content;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }finally{
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
        }
    }

    /**
     * 调整歌词时间偏移
     * @param offset 偏移量
     * @return 修改后的偏移量  null 修改失败
     */
    public static Long offsetLrc(long offset) {
        //1.获得歌词文件的内容
        String lrc = getLrc();
        if(lrc == null || lrc.length() == 0){
            return null;
        }
        //2.获得歌词中的偏移量并与偏移量相加
        long oldOffset = getOffset(lrc);
        long newOffset = offset + oldOffset;
        //3.保存修改偏移量后的歌词
        String lrcContent = getLrcContent(lrc);
        boolean b = saveNewOffset(lrcContent, oldOffset, newOffset);
        if (b) {
            return newOffset;
        } else {
            return null;
        }
    }

    private static boolean saveNewOffset(String lrc, long oldOffset, long newOffset) {
        //修改字符串
        lrc = lrc.replace("offset:" + oldOffset, "offset:" + newOffset);
        Log.e(TAG, "旧的偏移量:" + oldOffset + ",新的偏移量:" + newOffset);

        //保存字符串
        String filepath = PlayList.getPlayingMusic().getPath();
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1).replace("mp3", "lrc");
        File file = new File(FileUtils.getLrcFile() + filename);

        try {
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            byte[] bytes = lrc.getBytes("GBK");
            for (int i = 0; i < bytes.length; i++) {
                bos.write(bytes[i]);
            }

            bos.flush();
            bos.close();
        } catch (FileNotFoundException e) {
            Log.e("LrcUtils", e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e("LrcUtils", e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static long getOffset(String lrc){

        StringReader reader = new StringReader(lrc);
        BufferedReader br = new BufferedReader(reader);
        String line = null;
        long offset = 0;
        try{
            do{
                line = br.readLine();
                Log.e("asdadasdasdasdasdasd", line);
                //判断offset
                if (line.length() >= 10  && line.substring(1, 7).equals("offset")) {
                    String offsetStr = line.substring(8, line.lastIndexOf("]"));
                    Log.e("offsetStr", offsetStr);
                    try {
                        offset = Integer.parseInt(offsetStr);
                    } catch (Exception e) {
                        offset = 0;
                        e.printStackTrace();
                    }

                    return offset;
                }
            }while(line != null);

        }catch(Exception e){
            e.printStackTrace();
            return 0;
        }finally{
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            reader.close();
        }
        return offset;
    }

    /**
     * 判断文件的编码格式
     * @param file :file
     * @return 文件编码格式
     * @throws Exception
     */
    public static String codeString(File file) throws Exception{
        // TODO: 2015/11/4 这个方法没有效果，返回的都是GBK
        BufferedInputStream bin = new BufferedInputStream(
                new FileInputStream(file));
        int p = (bin.read() << 8) + bin.read();
        Log.e("编码数字", p + "");
        String code = null;

        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;
            case 0x5c75:
                code = "ANSI|ASCII" ;
                break ;
            default:
                code = "GBK";
        }

        return code;
    }

    /**
     * 保存歌词字体大小
     * @param size
     */
    public static void saveFontSize(int size) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("lrc_font_size", size);
        editor.commit();
    }

    /**
     * 获取歌词字体大小
     * @return
     */
    public static int getFontSize() {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt("lrc_font_size", 23);
    }

    /**
     * 保存歌词字体颜色
     * @param color
     */
    public static void saveFontColor(int color) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("lrc_font_color", color);
        editor.commit();
    }

    /**
     * 获取歌词字体颜色
     * @return
     */
    public static int getFontColor() {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt("lrc_font_color", App.sContext.getResources().getColor(R.color.pink_dark));
    }

    /**
     * 保存歌词模式
     * @param b true:全屏歌词，false:迷你歌词
     */
    public static void saveLrcMode(boolean b) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("lrc_mode", b);
        editor.commit();
    }

    /**
     * 获取歌词模式
     * 默认全屏歌词
     * @return true:全屏歌词，false:迷你歌词
     */
    public static boolean getLrcMode() {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean("lrc_mode", true);
    }

}
