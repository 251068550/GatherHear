package com.nicmic.gatherhear.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.widget.Toast;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.constant.Constant;
import com.nicmic.gatherhear.service.MusicService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/17.
 */
public class SoundEffect {

    //均衡器
    public static Equalizer mEqualizer;
    //重低音
    public static BassBoost mBass;
    //预设音场
    public static PresetReverb mPresetReverb;
    //环绕音
    public static Virtualizer mVirtualizer;

    //均衡器相关
    public static List<String> freqNames;//所有频率名称
    public static short minEQLevel;//最小频率
    public static short maxEQLevel;//最大频率

    //重低音相关
    public static final int BASS_BOOST_MAX_STRENGTH = 1000;//重低音最大值
    public static final int BASS_BOOST_MIN_STRENGTH = 0;//重低音最小值

    //预设音场相关
    public static List<String> presetNames;//所有预设音场的名称
    public static List<Short> presets;//所有可使用的预设音场值，和名称一一对应

    //环绕音相关
    public static final int VIRTUALIZER_MAX_STRENGTH = 1000;//环绕音最大值
    public static final int VIRTUALIZER_MIN_STRENGTH = 0;//环绕音最小值

    /**
     * 初始化音响效果
     */
    public static boolean init() {
        if (MusicService.player != null) {
            initEqualizer();
            initBassBoost();
            initPresetReverb();
            initVirtualizer();
            return true;
        }
        return false;
    }

    /**
     * 初始化均衡器
     */
    public static void initEqualizer() {
        mEqualizer = new Equalizer(0, MusicService.player.getAudioSessionId());
        // 启用均衡控制效果
        mEqualizer.setEnabled(true);
        //最小频率（-15dB）
        minEQLevel = mEqualizer.getBandLevelRange()[0];
        //最大频率（15dB）
        maxEQLevel = mEqualizer.getBandLevelRange()[1];
        //获取均衡控制器支持的所有频率
        short bands = mEqualizer.getNumberOfBands();
        //初始化频率名称
        freqNames = new ArrayList<>();
        for (short i = 0; i < bands; i++) {
            freqNames.add((mEqualizer.getCenterFreq(i) / 1000) + "");
        }
    }

    /**
     * 初始化重低音
     */
    public static void initBassBoost() {
        mBass = new BassBoost(0, MusicService.player.getAudioSessionId());
        // 设置启用重低音效果
        mBass.setEnabled(true);
    }

    /**
     * 初始化预设音场
     */
    public static void initPresetReverb() {
        mPresetReverb = new PresetReverb(0, MusicService.player.getAudioSessionId());
        // 设置启用预设音场控制
        mPresetReverb.setEnabled(true);
        // 获取系统支持的所有预设音场
        presets = new ArrayList<>();
        presetNames = new ArrayList<>();
        for (short i = 0; i < mEqualizer.getNumberOfPresets(); i++) {
            presets.add(i);
            presetNames.add(translatePresetName(mEqualizer.getPresetName(i)));
        }
    }

    /**
     * 初始化环绕音
     */
    public static void initVirtualizer() {
        mVirtualizer = new Virtualizer(0, MusicService.player.getAudioSessionId());
        //启用环绕音
        mVirtualizer.setEnabled(true);
    }

    /**
     * 将预设音场的场景英文名称翻译为中文名称
     * @param presetName
     * @return
     */
    public static String translatePresetName(String presetName) {
        if (presetName.equals("Normal")) {
            return "正常";
        }
        if (presetName.equals("Classical")) {
            return "古典";
        }
        if (presetName.equals("Dance")) {
            return "舞曲";
        }
        if (presetName.equals("Flat")) {
            return "平和";
        }
        if (presetName.equals("Folk")) {
            return "民间";
        }
        if (presetName.equals("Heavy Metal")) {
            return "重音";
        }
        if (presetName.equals("Hip Hop")) {
            return "说唱";
        }
        if (presetName.equals("Jazz")) {
            return "爵士";
        }
        if (presetName.equals("Pop")) {
            return "流行";
        }
        if (presetName.equals("Rock")) {
            return "摇滚";
        }
        if (presetName.equals("Acoustic")) {
            return "原声";
        }
        if (presetName.equals("Bass Booster")) {
            return "重低音";
        }
        if (presetName.equals("Bass Reducer")) {
            return "轻低音";
        }
        if (presetName.equals("Deep")) {
            return "强烈";
        }
        if (presetName.equals("R&B")) {
            return "说唱";
        }
        if (presetName.equals("Small Speakers")) {
            return "小喇叭";
        }
        if (presetName.equals("Treble Booster")) {
            return "高音强化";
        }
        if (presetName.equals("Treble Reducer")) {
            return "高音弱化";
        }
        if (presetName.equals("Vocal Booster")) {
            return "声乐助推";
        }
        return presetName;
    }

    /**
     * 保存预设音场
     * @param context
     * @param preset
     */
    public static void savePresetReverb(Context context, Short preset) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("preset", preset);
        editor.commit();
    }

    /**
     * 获得预设音场
     * -2是自定义的标识
     * @param context
     * @return
     */
    public static Short getPresetReverb(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return (short) sp.getInt("preset", -1);
    }

    /**
     * 保存均衡器单个频率band的值level
     * @param context
     * @param band
     * @param level
     */
    public static void saveBandLevel(Context context, Short band, Short level) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("band:" + band, level);
        editor.commit();
    }

    /**
     * 获得均衡器指定频率band的level
     * @param context
     * @param band
     * @return
     */
    public static Short getBandLevel(Context context, Short band) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return (short) sp.getInt("band:" + band, 0);
    }

    /**
     * 保存重低音的值
     * (0 - 1000)
     * @param context
     * @param strength (0 - 1000)
     */
    public static void saveBassBoost(Context context, Short strength) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("bass_boost", strength);
        editor.commit();
    }

    /**
     * 获取保存的重低音的值
     * @param context
     * @return
     */
    public static Short getBassBoost(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return (short) sp.getInt("bass_boost", 500);
    }

    /**
     * 保存环绕音的值
     * (0 - 1000)
     * @param context
     * @param strength (0 - 1000)
     */
    public static void saveVirtualizer(Context context, Short strength) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("virtualizer", strength);
        editor.commit();
    }

    /**
     * 获取保存的环绕音的值
     * @param context
     * @return
     */
    public static Short getVirtualizer(Context context) {
        SharedPreferences sp = context.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return (short) sp.getInt("virtualizer", 500);
    }

    /**
     * 启用音效
     */
    public static void start(){
        boolean b = SoundEffect.init();
        if (b) {
            if (getPresetReverb(App.sContext) != -1) {//1.判断是否开启音效
                if (getPresetReverb(App.sContext) == -2) {//1.1音效是自定义
                    //均衡器
                    for (int i = 0; i < freqNames.size(); i++) {
                        short band = (short) i;
                        Short level = SoundEffect.getBandLevel(App.sContext, band);
                        SoundEffect.mEqualizer.setBandLevel(band, level);
                    }
                    //重低音
                    Short strengthBassBoost = getBassBoost(App.sContext);
                    mBass.setStrength(strengthBassBoost);
                    //环绕音
                    Short strengthVirtualizer = getVirtualizer(App.sContext);
                    mVirtualizer.setStrength(strengthVirtualizer);

                } else {//1.2音效是预设音场
                    int preset = SoundEffect.getPresetReverb(App.sContext);
                    SoundEffect.mEqualizer.usePreset((short) preset);
                }
            } else {//2.释放音效相关资源
                mPresetReverb.release();
                mEqualizer.release();
                mBass.release();
                mVirtualizer.release();
            }
        }else {
            Toast.makeText(App.sContext, "音乐服务还未启动，启动音效控制器失败", Toast.LENGTH_SHORT).show();
        }
    }
}
