package com.nicmic.gatherhear.animation;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.constant.Constant;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/9/12.
 */
public class AnimUtil {

    //所有动画开启和关闭的标识（需读取配置文件并初始化）
    public static boolean ANIM_OPENED = true;//动画总开关
    public static boolean ANIM_MAIN_FRAGMENT_LAST_NEXT = true;//MainFragment上一首和下一首的动画
    public static boolean ANIM_MUSIC_FRAGMENT_SWITCH = true;//MainFragment和MusicFragment切换时的动画
    public static boolean ANIM_MUSIC_FRAGMENT_BUTTONS_SEEKBAR = true;//MusicFragment按钮组和进度条进入动画
    public static boolean ANIM_MUSIC_FRAGMENT_TITLE_SHIMMER = true;//MusicFragment标题正常状态下闪动的动画
    public static boolean ANIM_MUSIC_FRAGMENT_TITLE_ARTIST_ANIM = true;//MusicFragment手动切换上下首歌曲时标题和歌手的动画效果
    public static boolean ANIM_SONG_LIST_ENTER = true;//所有歌曲列表的进入动画
    public static boolean ANIM_FRAGMENT_ENTER_EXIT = true;//4个fragment界面进入和退出时的动画
    public static boolean ANIM_SOUND_EFFECT_ENTER = true;//SoundEffectActivity界面RecyclerView进入动画

    //以下为特例，暂不属于动画效果
    public static boolean ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION = true;//MusicFragment当歌曲播放到最后5秒内时弹出下一首歌曲信息的通知栏

    //方便其他类设置动画状态
    public static List<String> animNames;
    static {
        animNames = new ArrayList<>();
        animNames.add("ANIM_MAIN_FRAGMENT_LAST_NEXT");
        animNames.add("ANIM_MUSIC_FRAGMENT_SWITCH");
        animNames.add("ANIM_MUSIC_FRAGMENT_BUTTONS_SEEKBAR");
        animNames.add("ANIM_MUSIC_FRAGMENT_TITLE_SHIMMER");
        animNames.add("ANIM_MUSIC_FRAGMENT_TITLE_ARTIST_ANIM");
        animNames.add("ANIM_SONG_LIST_ENTER");
        animNames.add("ANIM_FRAGMENT_ENTER_EXIT");
        animNames.add("ANIM_SOUND_EFFECT_ENTER");
    }

    public static void loadAnimStatus() {
        for (int i = 0; i < animNames.size(); i++) {
            boolean b = getAnimSwitch(animNames.get(i));
            setAnimStatus(i, b);
        }
    }

    public static void setAnimStatus(int index, boolean b) {
        switch (index) {
            case 0:
                ANIM_MAIN_FRAGMENT_LAST_NEXT = b;
                break;
            case 1:
                ANIM_MUSIC_FRAGMENT_SWITCH = b;
                break;
            case 2:
                ANIM_MUSIC_FRAGMENT_BUTTONS_SEEKBAR = b;
                break;
            case 3:
                ANIM_MUSIC_FRAGMENT_TITLE_SHIMMER = b;
                break;
            case 4:
                ANIM_MUSIC_FRAGMENT_TITLE_ARTIST_ANIM = b;
                break;
            case 5:
                ANIM_SONG_LIST_ENTER = b;
                break;
            case 6:
                ANIM_FRAGMENT_ENTER_EXIT = b;
                break;
            case 7:
                ANIM_SOUND_EFFECT_ENTER = b;
                break;
        }
    }

    /**
     * 保存动画开关状态，并设置动画状态值
     * @param index
     * @param animName
     * @param b
     */
    public static void saveAnimSwitch(int index, String animName, boolean b) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(animName, b);
        editor.commit();

        setAnimStatus(index, b);
    }

    /**
     * 获得指定名称动画的开关状态
     * @param animName
     * @return
     */
    public static boolean getAnimSwitch(String animName) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean(animName, true);
    }

    /**
     * 自定义动画保存的临时开关状态
     * @param animName
     * @param b
     */
    public static void saveCustomAnimSwitch(String animName, boolean b) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("custom_" + animName, b);
        editor.commit();
    }

    /**
     * 获得指定名称自定义动画的开关状态
     * @param animName
     * @return
     */
    public static boolean getCustomAnimSwitch(String animName) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return sp.getBoolean("custom_" + animName, true);
    }


    public static final int ANIM_GRADE_LOW = 0;
    public static final int ANIM_GRADE_MIDDLE = 1;
    public static final int ANIM_GRADE_HIGH = 2;
    public static final int ANIM_GRADE_CUSTOM = 3;
    /**
     * 设置动画档位的值
     */
    public static void setAnimGrade(int grade) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("anim_grade", grade);
        editor.commit();
    }

    /** 获得设动画档位的值
     * 默认为高档
     */
    public static int getAnimGrade() {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        return sp.getInt("anim_grade", 2);
    }

    /**
     * 设置动画效果为低档
     * @param animGrade
     */
    public static void setLowGradeAnim(int animGrade) {
        setAnimGrade(animGrade);
        Boolean[] booleans = new Boolean[]{false, false, false, false, false, false, false, false};
        //设置相关参数
        for (int i = 0; i < animNames.size(); i++) {
            saveAnimSwitch(i, animNames.get(i), booleans[i]);
        }
    }

    /**
     * 设置动画效果为中档
     * @param animGrade
     */
    public static void setMiddleGradeAnim(int animGrade) {
        setAnimGrade(animGrade);
        Boolean[] booleans = new Boolean[]{true, false, true, false, false, false, true, false};
        //设置相关参数
        for (int i = 0; i < animNames.size(); i++) {
            saveAnimSwitch(i, animNames.get(i), booleans[i]);
        }
    }

    /**
     * 设置动画效果为高档
     * @param animGrade
     */
    public static void setHighGradeAnim(int animGrade) {
        setAnimGrade(animGrade);
        Boolean[] booleans = new Boolean[]{true, true, true, true, true, true, true, true};
        //设置相关参数
        for (int i = 0; i < animNames.size(); i++) {
            saveAnimSwitch(i, animNames.get(i), booleans[i]);
        }
    }

    /**
     * 设置动画效果为自定义档
     * @param animGrade
     */
    public static void setCustomGradeAnim(int animGrade) {
        setAnimGrade(animGrade);
        //设置相关参数
        for (int i = 0; i < animNames.size(); i++) {
            boolean b = getCustomAnimSwitch(animNames.get(i));
            setAnimStatus(i, b);
        }
    }

    /**
     * 歌词菜单移动动画
     * @param target
     * @param distance
     * @param duration
     */
    public static void lrcMenuTranslationAnim(View target, float distance, long duration) {
        ObjectAnimator.ofFloat(target, "translationY", distance) .setDuration(duration).start();
        ObjectAnimator.ofFloat(target, "rotation", 0F, 360F).setDuration(duration).start();//360度旋转
    }

    public static void overshootRightIn(View target, long duration, long delay) {
        TranslateAnimation ta = new TranslateAnimation(500, 0, 0, 0);
        ta.setInterpolator(new AnticipateOvershootInterpolator());
        AlphaAnimation aa = new AlphaAnimation(0, 1);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(ta);
        set.addAnimation(aa);
        set.setStartOffset(delay);
        set.setDuration(duration);
        target.startAnimation(set);

        target.setVisibility(View.VISIBLE);
    }

    public static void overshootBottomIn(View target, long duration, long delay) {
        TranslateAnimation ta = new TranslateAnimation(0, 0, 300, 0);
        ta.setInterpolator(new OvershootInterpolator());
        AlphaAnimation aa = new AlphaAnimation(0, 1);

        AnimationSet set = new AnimationSet(false);
        set.addAnimation(ta);
        set.addAnimation(aa);
        set.setStartOffset(delay);
        set.setDuration(duration);
        target.startAnimation(set);

        target.setVisibility(View.VISIBLE);
    }

    /**
     * dp转px
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static final long DELAY = 400;
    /**
     * listview的进入动画
     * @param lv
     * @param startOffSet 动画延迟执行时间
     */
    public static void listviewEnterAnim(ListView lv, long startOffSet) {
        //TODO:先判断是否开启了listview的动画

        //如果开启，判断用户选择的是哪个动画效果

        //根据用户选择的动画效果进行设置
        if (ANIM_OPENED && ANIM_SONG_LIST_ENTER) {
            Animation animation = AnimationUtils.loadAnimation(App.sContext, R.anim.swing_right_in);
            animation.setStartOffset(startOffSet);
            LayoutAnimationController lac = new LayoutAnimationController(animation);
            lac.setDelay(0.1f);
            lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
            lv.setLayoutAnimation(lac);
        }
    }

    /**
     * recyclerView格子视图的进入动画
     * @param recyclerView
     * @param startOffSet 动画延迟执行时间
     */
    public static void listviewGridEnterAnim(RecyclerView recyclerView, long startOffSet){
        //TODO:先判断是否开启了listview的动画

        //如果开启，判断用户选择的是哪个动画效果

        //根据用户选择的动画效果进行设置
        if (ANIM_OPENED && ANIM_SOUND_EFFECT_ENTER) {
            Animation animation = AnimationUtils.loadAnimation(App.sContext, R.anim.boost_in);
            animation.setStartOffset(startOffSet);
            animation.setInterpolator(new AnticipateOvershootInterpolator());
            LayoutAnimationController lac = new LayoutAnimationController(animation);
            lac.setDelay(0.2f);
            lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
            recyclerView.setLayoutAnimation(lac);
        }
    }

    /**
     * MainFragment歌曲切换时的动画
     * 标题和歌手的动画效果
     * @param textView
     * @param flag
     */
    public static void mainFragmentSwitchMusicTitleAnim(final TextView textView, String flag) {
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_MAIN_FRAGMENT_LAST_NEXT) {
            Techniques inTechniques = Techniques.Landing;
            if (flag.equals("playLast")) {
                inTechniques = Techniques.BounceInLeft;
            } else if (flag.equals("playNext")) {
                inTechniques = Techniques.BounceInRight;
            }

            final Techniques finalInTechniques = inTechniques;
            YoYo.with(finalInTechniques).duration(1000).playOn(textView);
        }
    }

    /**
     * MainFragment歌曲切换时的动画
     * 标题和歌手的动画效果
     * @param textView
     * @param flag
     */
    public static void mainFragmentSwitchMusicArtistAnim(final TextView textView, final String flag) {
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_MAIN_FRAGMENT_LAST_NEXT) {
            YoYo.with(Techniques.Landing).duration(1000).playOn(textView);
        }
    }

    /**
     * 保存是否显示播放下一首音乐的通知
     * @param b
     */
    public static void saveNextMusicNotification(boolean b) {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION", b);
        editor.commit();

        ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION = b;
    }

    /**
     * 获得是否显示播放下一首音乐的通知
     * 默认显示
     * @return
     */
    public static boolean getNextMusicNotification() {
        SharedPreferences sp = App.sContext.getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        boolean b = sp.getBoolean("ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION", true);
        ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION = b;
        return b;
    }

}
