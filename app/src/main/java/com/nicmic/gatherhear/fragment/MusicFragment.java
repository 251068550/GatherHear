package com.nicmic.gatherhear.fragment;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.adapter.PlayListAdapter;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.animation.MyAnimListener;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.lrc.DefaultLrcBuilder;
import com.nicmic.gatherhear.lrc.ILrcBuilder;
import com.nicmic.gatherhear.lrc.ILrcView;
import com.nicmic.gatherhear.lrc.LrcRow;
import com.nicmic.gatherhear.lrc.LrcView;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.Dialogs;
import com.nicmic.gatherhear.utils.ImageUtils;
import com.nicmic.gatherhear.utils.LrcUtils;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.nicmic.gatherhear.widget.CDView;
import com.nicmic.gatherhear.widget.Fab;
import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
import com.nineoldandroids.animation.Animator;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import co.mobiwise.library.ProgressLayout;
import de.hdodenhof.circleimageview.CircleImageView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

@SuppressLint({"NewApi", "ValidFragment"})
public class MusicFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, OnMenuItemClickListener{

    //外部传来的参数
    private RelativeLayout fragment_main, fragment_music;
    private RelativeLayout bottom_music;
    private ProgressLayout bottom_music_front;
    private RelativeLayout bottom_music_back;

    //中间CDView，歌词，迷你歌词
    private FrameLayout fragment_music_container;
    private CDView cd_view;
    private LrcView lrc_view;
    private LinearLayout mini_lrc_view;
    private TextView tvLrcLast;
    private TextView tvLrcNext;
    //歌词菜单相关
    private CircleImageView btnLrcMenu;
    private CircleImageView btnLrcSearch;
    private CircleImageView btnLrcFont;
    private CircleImageView btnLrcDecrease;
    private CircleImageView btnLrcIncrease;
    private CircleImageView btnLrcMode;

    //音乐界面相关
    private ImageButton btn_back, btn_menu;
    private ShimmerTextView tv_title;
    private TextView tv_artist;
    private TextView tv_progress, tv_duration;
    private SeekBar seekBar;
    private ImageButton btn_order, btn_last, btn_play, btn_next, btn_list;
    private ListView playList;

    //菜单相关
    public static DialogFragment mMenuDialogFragment;
    private FragmentManager fragmentManager;

    //播放列表相关
    public static PlayListAdapter adapter_playlist;
    public TextView tv_title_and_num;
    public RelativeLayout clear_all;

    //用于标识播放上一首和下一首的操作，在更新UI中实现不一样的动画
    private boolean isPlayLast = false;
    private boolean isPlayNext = false;

    public static final int MARK_LIKE = 1;
    public static final int ADD_2_MUSIC_MENU = 2;
    public static final int MUSIC_INFO = 3;
    public static final int PLAY_ANIM = 10;
    public static final int OPEN_MENU = 11;
    public static final int CLOSE_MENU = 12;
    public static final int HIDE_MUSIC_FRAGMENT = 13;
    public static final int UPDATE_UI = 14;
    public static final int UPDATE_PROGRESS = 15;
    public static final int STOP_UPDATE_PROGRESS = 16;
    public static final int BEGIN_LRC_PLAY = 17;
    public static final int STOP_LRC_PLAY = 18;
    public static final int RECOVER_LAST_STATUS = 19;
    public static final int UPDATE_MINI_LRC = 20;

    public static Handler staticHandler;//用于外界调用的handler
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PLAY_ANIM:
                    playAnims();
                    break;
                case OPEN_MENU:
                    showMenu();
                    break;
                case CLOSE_MENU:
                    mMenuDialogFragment.dismiss();
                    break;
                case HIDE_MUSIC_FRAGMENT:
                    if (!isFragmentSwitching){
                        showHideMusicFragment(App.sScreenWidth / 2, App.sScreenHeight / 2);
                    }
                    break;
                case MARK_LIKE:
                    if (PlayList.getPlayingMusic() != null){
                        addOrRemoveMyLike();
                    }else {
                        Toast.makeText(getActivity(), "当前没有歌曲可操作", 0).show();
                    }

                    break;
                case ADD_2_MUSIC_MENU:
                    if (PlayList.getPlayingMusic() != null){
                        Dialogs.add2MusicMenuDialog(getActivity(), PlayList.getPlayingMusic());
                    }else {
                        Toast.makeText(getActivity(), "当前没有可操作的歌曲", 0).show();
                    }
                    break;
                case MUSIC_INFO:
                    if (PlayList.getPlayingMusic() != null){
                        Dialogs.musicInfoDialog(getActivity(), PlayList.getPlayingMusic());
                    }else {
                        Toast.makeText(getActivity(), "当前没有可操作的歌曲", 0).show();
                    }
                    break;
                case UPDATE_UI:
                    updateUI();
                    break;
                case UPDATE_PROGRESS:
                    updateProgress();
                    break;
                case STOP_UPDATE_PROGRESS:
                    handler.removeMessages(UPDATE_PROGRESS);
                    break;
                case BEGIN_LRC_PLAY:
                    beginLrcPlay();
                    Log.e(getTag(), "开始歌词播放2222222222222222222222222");
                    break;
                case STOP_LRC_PLAY:
                    stopLrcPlay();
                    Log.e(getTag(), "取消歌词播放11111111111111111");
                    break;
                case RECOVER_LAST_STATUS:
                    int progress = msg.arg1;
                    int duration = msg.arg2;
                    recoverLastStatus(progress, duration);
                    Log.e(getTag(), "恢复进度条最近一次保存的状态");
                    break;
                case UPDATE_MINI_LRC:
                    Bundle bundle = msg.getData();
                    int highlight = bundle.getInt("highlight");
                    String lrc1 = bundle.getString("lrc1", App.sContext.getResources().getString(R.string.default_music_title));
                    String lrc2 = bundle.getString("lrc2", App.sContext.getResources().getString(R.string.default_music_artist));
                    updateMiniLrc(highlight, lrc1, lrc2);
                    break;
            }
        }
    };

    public MusicFragment() {

    }

    public MusicFragment(RelativeLayout fragment_main, RelativeLayout fragment_music, RelativeLayout bottom_music,
                         ProgressLayout bottom_music_front, RelativeLayout bottom_music_back) {
        this.fragment_main = fragment_main;
        this.fragment_music = fragment_music;
        this.bottom_music = bottom_music;
        this.bottom_music_front = bottom_music_front;
        this.bottom_music_back = bottom_music_back;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //两个handler是一样的，第二个handler需要废弃
        staticHandler = handler;
        ContainerActivity.handlerMusicFragment = handler;

        findView();
        setClickListener();

        initFab();
        initCDView();
        initSeekBar();
        initLrcView();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_last:
                playLast();
                break;
            case R.id.btn_play:
                playOrPause();
                break;
            case R.id.btn_next:
                playNext();
                break;

            case R.id.btn_order:
                switchOrder();
                break;
            case R.id.fab:
            case R.id.btn_list:
                showPlayList();
                break;
            case R.id.btn_menu:
                showMenu();
                break;
            case R.id.btn_lrc_menu:
                togglelrcMenu();
                break;
            case R.id.btn_lrc_search:
                Toast.makeText(getActivity(), "搜索歌词功能暂未开放", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_lrc_font:
                Dialogs.lrcFontDialog(getActivity());
                togglelrcMenu();
                break;
            case R.id.btn_lrc_decrease_half_minute:
                Long offset1 = LrcUtils.offsetLrc(-500);
                if (offset1 != null) {
                    loadLrc();
                    Toast.makeText(getActivity(), "提前了500毫秒\n总延迟:" + offset1 + "毫秒", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_lrc_increase_half_minute:
                Long offset2 = LrcUtils.offsetLrc(500);
                if (offset2 != null) {
                    loadLrc();
                    Toast.makeText(getActivity(), "延迟了500毫秒\n总延迟:" + offset2 + "毫秒", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_lrc_mode:
                toggleLrcMode();
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP){
            switch (v.getId()) {
                case R.id.bottom_music:
                    if (materialSheetFab != null && isSheetShow){
                        materialSheetFab.hideSheet();
                    }else if (ContainerActivity.playlistDialog != null && ContainerActivity.playlistDialog.isShowing()){
                        ContainerActivity.playlistDialog.dismiss();
                    }else if (!isFragmentSwitching){
                        isFragmentSwitching = true;
                        float x = event.getX();
                        float y = event.getY();
                        showHideMusicFragment(x, y + fragment_music.getBottom());
                    }
                    break;
                case R.id.btn_back:
                    if (!isFragmentSwitching){
                        isFragmentSwitching = true;
                        float x1 = event.getX();
                        float y1 = event.getY();
                        showHideMusicFragment(x1, y1);
                    }
                    break;
            }
        }
        return true;
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        Toast.makeText(getActivity(), "点击了菜单位置：" + position, 0).show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("MusicFragment", "onHiddenChanged");
        //此方法未使用
//        AnimUtil.ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e("MusicFragment", "onPause 在手机屏幕关屏后或切换到其他Activity触发此方法");
        //关闭提示下一首的通知
        if (AnimUtil.getNextMusicNotification()) {
            AnimUtil.ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //开启提示下一首的通知
        if (AnimUtil.getNextMusicNotification()) {
            AnimUtil.ANIM_MUSIC_FRAGMENT_SHOW_NOTIFICATION = true;
        }
        //获取当前音乐播放的状态，更新界面
        initOrder();//重新获取播放顺序状态
        //获取歌词模式（全屏或迷你）
        initLrcMode();
        //发送更新进度条的消息
        handler.removeMessages(UPDATE_PROGRESS);
        handler.sendEmptyMessage(UPDATE_PROGRESS);
        //所有控件入场动画
        handler.sendEmptyMessageDelayed(PLAY_ANIM, 200);

    }

    /**
     * 初始化歌词模式
     */
    private void initLrcMode() {
        boolean b = LrcUtils.getLrcMode();
        if (b) {
            lrc_view.setVisibility(View.VISIBLE);
            mini_lrc_view.setVisibility(View.GONE);
            btnLrcMode.setImageResource(R.drawable.ic_fullscreen_exit_white);
        } else {
            lrc_view.setVisibility(View.GONE);
            mini_lrc_view.setVisibility(View.VISIBLE);
            btnLrcMode.setImageResource(R.drawable.ic_fullscreen_white);
        }
    }

    /**
     * 更新迷你歌词
     * @param highlight
     * @param lrc1
     * @param lrc2
     */
    private void updateMiniLrc(int highlight, String lrc1, String lrc2) {
        if (highlight == 1) {//高亮歌词在上面
            tvLrcLast.setText(lrc1);
            tvLrcLast.setTextColor(LrcView.mHignlightRowColor);
            tvLrcLast.setTextSize(LrcView.mLrcFontSize - 8);
            tvLrcNext.setText(lrc2);
            tvLrcNext.setTextColor(Color.WHITE);
            tvLrcNext.setTextSize(LrcView.mLrcFontSize - 8);
        } else {//高亮歌词在下面
            tvLrcNext.setText(lrc1);
            tvLrcNext.setTextColor(LrcView.mHignlightRowColor);
            tvLrcNext.setTextSize(LrcView.mLrcFontSize - 8);
            tvLrcLast.setText(lrc2);
            tvLrcLast.setTextColor(Color.WHITE);
            tvLrcLast.setTextSize(LrcView.mLrcFontSize - 8);
        }
    }

    /**
     * 添加或移除我喜欢的
     */
    private void addOrRemoveMyLike(){
        int myLike = Dialogs.add2LikeDialog(getActivity(), PlayList.getPlayingMusic());
        PlayList.getPlayingMusic().setMyLike(myLike);
    };

    /**
     * 初始化进度条
     */
    private void initSeekBar() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBar.setProgress(progress);
                tv_progress.setText(MusicUtils.getTimeString(progress));
//                Log.e("MusicFragment", "进度条已经改变了");
            }

            //在手指开始拖动进度条时调用
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (handler.obtainMessage(STOP_UPDATE_PROGRESS) == null) {//如果停止更新进度条的handler不存在
                    handler.sendEmptyMessage(STOP_UPDATE_PROGRESS);
                }
                tv_progress.setText(seekBar.getProgress() + "");
                Log.e("MusicFragment", "开始拖动进度条");
            }

            //在手指拖动进度条后离开时调用
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (MusicService.player != null && !PlayList.isEmpty()) {
                    if (handler.obtainMessage(UPDATE_PROGRESS) == null) {//如果更新进度条的handler不存在
                        handler.sendEmptyMessage(UPDATE_PROGRESS);
                    }
                    int progress = seekBar.getProgress();
                    MusicService.player.seekTo(progress);
                }
                Log.e("MusicFragment", "停止拖动进度条");
            }
        });
    }

    /**
     * 恢复当前界面的进度条为最近一次退出应用保存的进度条状态
     * @param progress
     * @param duration
     */
    private void recoverLastStatus(int progress, int duration) {
        tv_progress.setText(MusicUtils.getTimeString(progress));
        tv_duration.setText(MusicUtils.getTimeString(duration));
        seekBar.setMax(duration);
        seekBar.setProgress(progress);
        //加载歌词
        handler.sendEmptyMessage(BEGIN_LRC_PLAY);
    }

    //用于在进度条播放到最后5秒的标识
    private boolean isMoreThan5s = true;
    /**
     * 实时更新进度条（更新进度条及其左右文字）
     */
    private void updateProgress() {
        if (MusicService.player != null && MusicService.player.isPlaying()) {
            int progress = MusicService.player.getCurrentPosition();
            int duration = MusicService.player.getDuration();

            tv_progress.setText(MusicUtils.getTimeString(progress));
            tv_duration.setText(MusicUtils.getTimeString(duration));
            seekBar.setProgress(progress);
            seekBar.setMax(duration);

            //更新底部音乐进度条
            Message msg = Message.obtain();
            msg.what = ContainerActivity.UPDATE_BOTTOM_MUSIC_PROGRESS;
            msg.arg1 = progress;
            msg.arg2 = duration;
            if (ContainerActivity.staticHandler != null) {
                ContainerActivity.staticHandler.sendMessage(msg);
            }

            //如果当前歌曲播放到最后5秒，则弹出一个下一首的提示框
            if (duration - progress <= 5000 && isMoreThan5s) {
                isMoreThan5s = false;
                int nextPosition = MusicUtils.getNextPlayMusicPosition();
                if (getActivity() != null) {
                    MusicUtils.showNotify(getActivity(), nextPosition);
                }
            }
            if (duration - progress > 5000){
                isMoreThan5s = true;
            }
        }

        //发一个延迟一秒的消息
        handler.sendEmptyMessageDelayed(UPDATE_PROGRESS, 1000);
    }

    /**
     * 更新音乐播放界面的UI
     */
    private void updateUI() {
        Music music = PlayList.getPlayingMusic();
        if (music == null){
            tv_title.setText(R.string.default_music_title);
            tv_artist.setText(R.string.default_music_artist);
            tv_progress.setText("00:00");
            tv_duration.setText("00:00");
            seekBar.setMax(1);
            seekBar.setProgress(0);
        }else{
            if (isPlayLast){
                titleAnim(music);
                isPlayLast = false;
                Log.e("MusicFragment", "播放上一首标题的动画效果");
            }else if (isPlayNext){
                titleAnim(music);
                isPlayNext = false;
                Log.e("MusicFragment", "播放下一首标题的动画效果");
            }else{
                Log.e("MusicFragment", "无动画设置标题和歌手");
                tv_title.setText(music.getTitle());
                tv_artist.setText(music.getArtist());
            }
        }
        if(MusicService.player != null && MusicService.player.isPlaying()){
            btn_play.setBackgroundResource(R.drawable.btn_pause_circle_white);
        }else{
            btn_play.setBackgroundResource(R.drawable.btn_play_circle_white);
        }
        //更新CDVIEW
        refreshCDView();
        //更新播放顺序
        initOrder();
    }

    /**
     * 刷新CDView
     */
    private void refreshCDView(){
        Music music = PlayList.getPlayingMusic();
        if(music != null && cd_view != null){//有正在播放的音乐，CD显示正在播放音乐的封面
            //TODO:先去加载音乐是否有歌手图片，没有的话使用默认的图片(联网实现，未实现)
            if(MusicService.player != null && MusicService.player.isPlaying()){
                cd_view.start();
            }else{
                cd_view.pause();
            }
            //设置CD图片
            Bitmap bitmap = ImageUtils.getArtwork(App.sContext, music.getSongId(), music.getAlbumId(), true);
            if (bitmap != null) {
                cd_view.setImage(bitmap);
            }else {
                Log.e("MusicFragment", "没有获取到图片");
            }
        }
    }

    /**
     * 初始化CD_view的图片
     */
    private void initCDView() {
        //CD显示默认图片
        Bitmap bmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.default_cd_cover);
        cd_view.setImage(ImageUtils.scaleBitmap(bmp, (int) (App.sScreenWidth * 0.7)));
    }

    /**
     * 初始化播放列表
     */
    private void initPlayList() {
        if (adapter_playlist == null){
            tv_title_and_num.setText("播放列表(" + PlayList.musics.size() + ")");
            clear_all.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialogs.clearPlayList(getActivity(), Dialogs.PLAY_LIST_MUSIC_FRAGMENT);
                }
            });

            adapter_playlist = new PlayListAdapter(getActivity(), PlayList.musics);
            playList.setAdapter(adapter_playlist);
            playList.setSelection(PlayList.position);
            adapter_playlist.notifyDataSetInvalidated();
            Log.e("MusicFragment", "初始化播放列表并定位到当前播放位置");
        } else {
            tv_title_and_num.setText("播放列表(" + PlayList.musics.size() + ")");
            playList.setSelection(PlayList.position);
            adapter_playlist.notifyDataSetInvalidated();
            Log.e("MusicFragment", "更新播放列表并定位到当前播放位置");
        }

    }

    public static MaterialSheetFab<Fab> materialSheetFab;
    private  Fab fab;
    private void initFab() {
        fab = (Fab) getActivity().findViewById(R.id.fab);
        View sheetView = getActivity().findViewById(R.id.fab_sheet);
        View overlay = getActivity().findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.black_transparent);
        int fabColor = getResources().getColor(R.color.black_transparent);

        // Initialize material sheet FAB
        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay, sheetColor, fabColor);
        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                isSheetShow = true;
            }

            @Override
            public void onHideSheet() {
                isSheetShow = false;
            }
        });
        fab.setOnClickListener(this);
    }

    /**
     * 初始化音乐右上角菜单
     */
    private void initMusicMenu() {
        //step1
        MenuObject close = new MenuObject();
        close.setResource(R.drawable.ic_close_white_small);
        close.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
        close.setDividerColor(Color.rgb(255, 255, 255));

        MenuObject markLike = new MenuObject("标记喜欢");
        markLike.setResource(R.drawable.ic_favorite_outline_white_small);
        markLike.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
        markLike.setDividerColor(Color.rgb(255, 255, 255));
        Music music = PlayList.getPlayingMusic();
        if (music != null && music.getMyLike() == 1){
            markLike = new MenuObject("取消喜欢");
            markLike.setResource(R.drawable.ic_favorite_white_small);
            markLike.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
            markLike.setDividerColor(Color.rgb(255, 255, 255));
        }

        MenuObject add2MusicMenu = new MenuObject("加入歌单");
        add2MusicMenu.setResource(R.drawable.ic_playlist_add_white_small);
        add2MusicMenu.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
        add2MusicMenu.setDividerColor(Color.rgb(255, 255, 255));

        MenuObject musicInfo = new MenuObject("歌曲信息");
        musicInfo.setResource(R.drawable.ic_info_outline_white_small);
        musicInfo.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
        musicInfo.setDividerColor(Color.rgb(255, 255, 255));

        List<MenuObject> menuObjects = new ArrayList<>();
        menuObjects.add(close);
        menuObjects.add(markLike);
        menuObjects.add(add2MusicMenu);
        menuObjects.add(musicInfo);

        //step2
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.navbar_height));
        menuParams.setMenuObjects(menuObjects);
//        menuParams.setClosableOutside(true);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    private void setClickListener() {
        btn_order.setOnClickListener(this);
        btn_last.setOnClickListener(this);
        btn_play.setOnClickListener(this);
        btn_next.setOnClickListener(this);
        btn_list.setOnClickListener(this);

        btn_menu.setOnClickListener(this);
        btn_back.setOnTouchListener(this);
        bottom_music.setOnTouchListener(this);
        btnLrcMenu.setOnClickListener(this);
        btnLrcSearch.setOnClickListener(this);
        btnLrcFont.setOnClickListener(this);
        btnLrcDecrease.setOnClickListener(this);
        btnLrcIncrease.setOnClickListener(this);
        btnLrcMode.setOnClickListener(this);
    }

    private void findView() {
        btn_back = (ImageButton) getActivity().findViewById(R.id.btn_back);
        btn_menu = (ImageButton) getActivity().findViewById(R.id.btn_menu);
        btn_order = (ImageButton) getActivity().findViewById(R.id.btn_order);
        btn_last = (ImageButton) getActivity().findViewById(R.id.btn_last);
        btn_play = (ImageButton) getActivity().findViewById(R.id.btn_play);
        btn_next = (ImageButton) getActivity().findViewById(R.id.btn_next);
        btn_list = (ImageButton) getActivity().findViewById(R.id.btn_list);
        tv_progress = (TextView) getActivity().findViewById(R.id.tv_progress);
        tv_duration = (TextView) getActivity().findViewById(R.id.tv_duration);
        tv_title = (ShimmerTextView) getActivity().findViewById(R.id.tv_title);
        tv_artist = (TextView) getActivity().findViewById(R.id.tv_artist);
        seekBar = (SeekBar) getActivity().findViewById(R.id.seekBar);
        fragment_music_container = (FrameLayout) getActivity().findViewById(R.id.fragment_music_container);
        cd_view = (CDView) getActivity().findViewById(R.id.cd_view);
        playList = (ListView) getActivity().findViewById(R.id.playList);
        tv_title_and_num = (TextView) getActivity().findViewById(R.id.tv_title_and_num);
        clear_all = (RelativeLayout) getActivity().findViewById(R.id.clear_all);
        lrc_view = (LrcView) getActivity().findViewById(R.id.lrc_view);
        mini_lrc_view = (LinearLayout) getActivity().findViewById(R.id.mini_lrc_view);
        tvLrcLast = (TextView) getActivity().findViewById(R.id.tv_lrc_up);
        tvLrcNext = (TextView) getActivity().findViewById(R.id.tv_lrc_down);
        btnLrcMenu = (CircleImageView) getActivity().findViewById(R.id.btn_lrc_menu);
        btnLrcSearch = (CircleImageView) getActivity().findViewById(R.id.btn_lrc_search);
        btnLrcFont = (CircleImageView) getActivity().findViewById(R.id.btn_lrc_font);
        btnLrcDecrease = (CircleImageView) getActivity().findViewById(R.id.btn_lrc_decrease_half_minute);
        btnLrcIncrease = (CircleImageView) getActivity().findViewById(R.id.btn_lrc_increase_half_minute);
        btnLrcMode = (CircleImageView) getActivity().findViewById(R.id.btn_lrc_mode);
    }

    private boolean isShowLrcMenu = false;

    /**
     * 歌词菜单的开关
     */
    public void togglelrcMenu() {
        if (isShowLrcMenu) {//关闭歌词菜单
            AnimUtil.lrcMenuTranslationAnim(btnLrcSearch, -80, 500);
            AnimUtil.lrcMenuTranslationAnim(btnLrcFont, -160, 600);
            AnimUtil.lrcMenuTranslationAnim(btnLrcDecrease, -240, 700);
            AnimUtil.lrcMenuTranslationAnim(btnLrcIncrease, -320, 800);
            AnimUtil.lrcMenuTranslationAnim(btnLrcMode, -400, 900);
            isShowLrcMenu = false;
        } else {//展开歌词菜单
            AnimUtil.lrcMenuTranslationAnim(btnLrcSearch, 80, 500);
            AnimUtil.lrcMenuTranslationAnim(btnLrcFont, 160, 600);
            AnimUtil.lrcMenuTranslationAnim(btnLrcDecrease, 240, 700);
            AnimUtil.lrcMenuTranslationAnim(btnLrcIncrease, 320, 800);
            AnimUtil.lrcMenuTranslationAnim(btnLrcMode, 400, 900);
            btnLrcSearch.setVisibility(View.VISIBLE);
            btnLrcFont.setVisibility(View.VISIBLE);
            btnLrcDecrease.setVisibility(View.VISIBLE);
            btnLrcIncrease.setVisibility(View.VISIBLE);
            btnLrcMode.setVisibility(View.VISIBLE);
            isShowLrcMenu = true;
        }
    }

    /**
     * 歌词切换模式的开关
     */
    private void toggleLrcMode() {
        if (mini_lrc_view.getVisibility() == View.GONE){
            mini_lrc_view.setVisibility(View.VISIBLE);
            lrc_view.setVisibility(View.GONE);
            btnLrcMode.setImageResource(R.drawable.ic_fullscreen_white);
            LrcUtils.saveLrcMode(false);
        }else{
            mini_lrc_view.setVisibility(View.GONE);
            lrc_view.setVisibility(View.VISIBLE);
            btnLrcMode.setImageResource(R.drawable.ic_fullscreen_exit_white);
            LrcUtils.saveLrcMode(true);
        }
        //关闭歌词菜单
        togglelrcMenu();
    }

    //------------------------动画相关begin-----------------------

    //是否显示音乐播放界面,一开始没有打开播放界面，初始值为true
    private boolean showMusicFragment = true;
    //音乐界面和主页面是否处于动画切换状态，防止用户多次点击导致页面动画执行不完整
    private boolean isFragmentSwitching = false;

    /**
     * 显示或隐藏音乐播放界面
     * @param cx 点击的x坐标
     * @param cy 点击的y坐标
     */
    public void showHideMusicFragment(float cx, float cy) {
        if (showMusicFragment) {
            playAnims();
            showMusicFragment(fragment_music, cx, cy);
            showMusicFragment = false;
            //由于切换到音乐播放界面，要开始更新音乐界面的进度条
            if (Message.obtain(handler, UPDATE_PROGRESS) == null){
                handler.sendEmptyMessage(UPDATE_PROGRESS);
            }
            Log.e("MusicFragment", "开始更新音乐界面的进度条");
        } else {
            hideMusicFragment(fragment_music, cx, cy);
            showMusicFragment = true;
            //由于切换到其他页面，要停止更新音乐界面的进度条
//            handler.sendEmptyMessage(STOP_UPDATE_PROGRESS);
            Log.e("MusicFragment", "停止更新音乐界面的进度条");
        }
        MusicService.updateUI();
    }

    /**
     * 显示音乐播放界面
     */
    private void showMusicFragment(View myView, float cx, float cy) {

        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_MUSIC_FRAGMENT_SWITCH){
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight());
            SupportAnimator animator =
                    ViewAnimationUtils.createCircularReveal(myView, (int) cx, (int) cy, 0, finalRadius);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(500);
            animator.addListener(new MyAnimListener() {
                @Override
                public void onAnimationStart() {
                    fragment_music.setVisibility(View.VISIBLE);
                    fragment_main.setVisibility(View.VISIBLE);
                    bottomMusicShowFront(false);
                }

                @Override
                public void onAnimationEnd() {
                    fragment_music.setVisibility(View.VISIBLE);
                    fragment_main.setVisibility(View.INVISIBLE);
                }
            });
            animator.start();
        }else {
            fragment_music.setVisibility(View.VISIBLE);
            fragment_main.setVisibility(View.INVISIBLE);

            bottom_music_front.setVisibility(View.INVISIBLE);
            bottom_music_back.setVisibility(View.VISIBLE);
        }
        isFragmentSwitching = false;
    }

    /**
     * 隐藏音乐播放界面
     */
    private void hideMusicFragment(View myView, float cx, float cy) {

        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_MUSIC_FRAGMENT_SWITCH) {
            int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

            SupportAnimator animator =
                    ViewAnimationUtils.createCircularReveal(myView, (int) cx, (int) cy, finalRadius, 0);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.setDuration(500);
            animator.addListener(new MyAnimListener() {
                @Override
                public void onAnimationStart() {
                    fragment_music.setVisibility(View.VISIBLE);
                    fragment_main.setVisibility(View.VISIBLE);
                    bottomMusicShowFront(true);
                }

                @Override
                public void onAnimationEnd() {
                    fragment_music.setVisibility(View.INVISIBLE);
                    fragment_main.setVisibility(View.VISIBLE);
                }

            });
            animator.start();
        } else {
            fragment_music.setVisibility(View.INVISIBLE);
            fragment_main.setVisibility(View.VISIBLE);

            bottom_music_front.setVisibility(View.VISIBLE);
            bottom_music_back.setVisibility(View.INVISIBLE);
        }
        isFragmentSwitching = false;
    }

    /**
     * 底部音乐显示正面或反面
     * @param isShowFront true正面    false反面
     */
    public void bottomMusicShowFront(final boolean isShowFront) {
        //底部音乐部件下移动画
        ObjectAnimator animator1 = ObjectAnimator.ofInt(bottom_music, "top", bottom_music.getTop(), bottom_music.getTop() + bottom_music.getHeight());
        animator1.setDuration(500);
        animator1.setInterpolator(new DecelerateInterpolator());
        animator1.addListener(new MyAnimListener() {

            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (!isShowFront) {
                    bottom_music_front.setVisibility(View.INVISIBLE);
                    bottom_music_back.setVisibility(View.VISIBLE);
                } else {
                    bottom_music_front.setVisibility(View.VISIBLE);
                    bottom_music_back.setVisibility(View.INVISIBLE);
                }
            }
        });
        animator1.start();

        //底部音乐部件上移动画
        ObjectAnimator animator2 = ObjectAnimator.ofInt(bottom_music, "top", bottom_music.getTop() + bottom_music.getHeight(), bottom_music.getTop());
        animator2.setDuration(500);
        animator2.setStartDelay(500);
        animator2.setInterpolator(new AccelerateInterpolator());
        animator2.addListener(new MyAnimListener() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                isFragmentSwitching = false;
            }
        });
        animator2.start();
    }

    /**
     * 更换音乐标题,歌手的动画
     */
    private void titleAnim(final Music music) {
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_MUSIC_FRAGMENT_TITLE_ARTIST_ANIM) {
            YoYo.with(Techniques.TakingOff).duration(500).playOn(tv_artist);
            YoYo.with(Techniques.Hinge).duration(500).withListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    tv_title.setText(music.getTitle());
                    tv_artist.setText(music.getArtist());
                    YoYo.with(Techniques.DropOut).duration(500).playOn(tv_title);
                    YoYo.with(Techniques.Landing).duration(500).playOn(tv_artist);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            }).playOn(tv_title);
        } else {
            tv_title.setText(music.getTitle());
            tv_artist.setText(music.getArtist());
        }
    }

    /**
     * 播放所有控件入场动画
     */
    private void playAnims() {
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_MUSIC_FRAGMENT_TITLE_SHIMMER) {
            topAnim();
        }
        centerAnim();
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_MUSIC_FRAGMENT_BUTTONS_SEEKBAR) {
            bottomAnim(500);
        } else {
            bottomAnim(0);
        }
    }

    /**
     * 标题栏动画
     */
    private void topAnim() {
        Shimmer shimmer = new Shimmer();
        shimmer.setDuration(2000);
        shimmer.start(tv_title);
        // TODO: 2016/5/24 这里的动画效果失效了，原因不明，要使用动画不能设置字体白色
    }

    /**
     * 中间部件动画（未实现）
     */
    private void centerAnim() {

    }

    /**
     * 按钮组，seekbar及文字动画
     */
    private void bottomAnim(long duration) {
        AnimUtil.overshootRightIn(tv_progress, duration, 100);
        AnimUtil.overshootRightIn(tv_duration, duration, 300);
        AnimUtil.overshootRightIn(seekBar, duration, 200);

        AnimUtil.overshootBottomIn(btn_order, duration, 300);
        AnimUtil.overshootBottomIn(btn_last, duration, 400);
        AnimUtil.overshootBottomIn(btn_play, duration, 500);
        AnimUtil.overshootBottomIn(btn_next, duration, 600);
        AnimUtil.overshootBottomIn(btn_list, duration, 700);

    }
    //------------------------动画相关end-----------------------

    /**
     * 显示右上角菜单
     */
    public void showMenu() {
        initMusicMenu();
        mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
    }

    //播放列表界面是否显示，初始为不显示
    public static boolean isSheetShow = false;

    /**
     * 显示音乐播放列表界面
     */
    private void showPlayList() {
//        materialSheetFab.showFab();
        materialSheetFab.showSheet();
        initPlayList();
    }

    /**
     * 播放下一首
     */
    private void playNext() {
        //先判断播放列表是否有音乐
        if (PlayList.isEmpty()){
            Toast.makeText(getActivity(), "没有可播放的音乐", 0).show();
        }else{
            isPlayNext = true;
            MusicService.playNext();
        }
    }

    /**
     * 播放上一首
     */
    private void playLast() {
        //先判断播放列表是否有音乐
        if (PlayList.isEmpty()){
            Toast.makeText(getActivity(), "没有可播放的音乐", 0).show();
        }else{
            isPlayLast = true;
            MusicService.playLast();
        }
    }

    /**
     * 播放或暂停
     */
    private void playOrPause(){
        if (PlayList.isEmpty()) {//音乐列表为空
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_cd_cover);
            cd_view.setImage(ImageUtils.scaleBitmap(bmp, (int) (App.sScreenWidth * 0.7)));
            Toast.makeText(getActivity(), "当前没有可播放的音乐", 0).show();
            Log.e("MusicFragment", "播放列表没有音乐");
        }else{
            MusicService.playOrPause();
            if (MusicService.player.isPlaying()){
                cd_view.pause();
                Log.e("MusicFragment", "停止播放");
            }else {
                cd_view.start();
                Log.e("MusicFragment", "继续播放");
            }
        }
        //TODO:先去加载音乐是否有歌手图片，没有的话使用默认的图片(联网实现，未实现)

    }

    /**
     * 初始化播放顺序
     * 顺序播放，单曲循环，随机播放
     */
    private void initOrder() {
        int order = MusicUtils.getPlayOrder(getActivity());
        if (order == MusicUtils.ORDER_SEQUENTIAL){
            btn_order.setBackgroundResource(R.drawable.btn_repeat_white);
        }else if (order == MusicUtils.ORDER_LOOP){
            btn_order.setBackgroundResource(R.drawable.btn_loop_white);
        }else if (order == MusicUtils.ORDER_CYCLE){
            btn_order.setBackgroundResource(R.drawable.btn_repeat_one_white);
        }else if (order == MusicUtils.ORDER_RANDOM) {
            btn_order.setBackgroundResource(R.drawable.btn_shuffle_white);
        }
    }


    private void switchOrder() {
        //如果当前是顺序播放，则切换为循环播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_SEQUENTIAL){
            MusicUtils.setPlayOrder(getActivity(), MusicUtils.ORDER_LOOP);
            Toast.makeText(getActivity(), "循环播放", 0).show();
            btn_order.setBackgroundResource(R.drawable.btn_loop_white);
        }else
        //如果当前是循环播放，则切换为单曲循环
        if (MusicUtils.ORDER == MusicUtils.ORDER_LOOP){
            MusicUtils.setPlayOrder(getActivity(), MusicUtils.ORDER_CYCLE);
            Toast.makeText(getActivity(), "单曲循环", 0).show();
            btn_order.setBackgroundResource(R.drawable.btn_repeat_one_white);
        }else
        //如果当前是单曲循环，则转换为随机播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_CYCLE){
            MusicUtils.setPlayOrder(getActivity(), MusicUtils.ORDER_RANDOM);
            Toast.makeText(getActivity(), "随机播放", 0).show();
            btn_order.setBackgroundResource(R.drawable.btn_shuffle_white);
        }else
        //如果当前是随机播放，则转换为顺序播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_RANDOM){
            MusicUtils.setPlayOrder(getActivity(), MusicUtils.ORDER_SEQUENTIAL);
            Toast.makeText(getActivity(), "顺序播放", 0).show();
            btn_order.setBackgroundResource(R.drawable.btn_repeat_white);
        }
    }

    //------------------------------歌词相关--------------------------
    private void initLrcView() {
        lrc_view.setListener(new ILrcView.LrcViewListener() {
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (MusicService.player != null) {
                    Log.d("MusicFragment", "onLrcSeeked:" + row.time);
                    MusicService.player.seekTo((int)row.time);
                }
            }
        });
    }

    public int mPalyTimerDuration = 500;
    public Timer mTimer;
    public TimerTask mTask;

    /**
     * 开始播放歌词
     */
    public void beginLrcPlay(){
        if(mTimer == null){
            loadLrc();

            mTimer = new Timer();
            mTask = new LrcTask();
            mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration);
        }
    }

    /**
     * 停止播放歌词
     */
    public void stopLrcPlay(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
            mTask.cancel();
            mTask = null;
        }
    }

    /**
     * 加载当前正在播放音乐的歌词
     */
    private void loadLrc(){
        String lrc = LrcUtils.getLrc();
        Log.d("MusicFragment", "lrc:" + lrc);

        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrc);

        lrc_view.setLrc(rows);
        //更新主界面歌词为默认提示文字，以便没有找到歌词导致主界面是前一首的歌词
        updateMainFragmentLrc(null);
    }

    class LrcTask extends TimerTask {

        long beginTime = -1;
        @Override
        public void run() {
            if(beginTime == -1) {
                beginTime = System.currentTimeMillis();
            }

            final long timePassed = MusicService.player.getCurrentPosition();
            if (getActivity() != null){
                getActivity().runOnUiThread(new Runnable() {

                    public void run() {
                        List<LrcRow> lrcRows = lrc_view.seekLrcToTime(timePassed);
                        //更新主界面的双行歌词
                        updateMainFragmentLrc(lrcRows);
                        //更新当前界面双行歌词
                        updateMiniLrc(lrcRows);
                    }
                });
            }

        }

    };

    public void updateMiniLrc(List<LrcRow> lrcRows) {
        if (lrcRows != null && handler != null && mini_lrc_view.getVisibility() == View.VISIBLE) {
            Message message = Message.obtain();
            message.what = UPDATE_MINI_LRC;
            Bundle data = new Bundle();
            data.putString("lrc1", lrcRows.get(0).content);
            if (lrcRows.get(1) != null) {
                data.putString("lrc2", lrcRows.get(1).content);
            }else {
                data.putString("lrc2", "");
            }
            if (lrcRows.get(0).getIndex() % 2 == 0) {//高亮歌词是偶数
                data.putInt("highlight", 1);
            } else {
                data.putInt("highlight", 2);
            }
            message.setData(data);
            handler.sendMessage(message);
        }
    }

    public void updateMainFragmentLrc(List<LrcRow> lrcRows){
        if (lrcRows != null && MainFragment.staticHandler != null) {
            Message message = Message.obtain();
            message.what = MainFragment.UPDATE_LRC;
            Bundle data = new Bundle();
            data.putString("lrc1", lrcRows.get(0).content);
            if (lrcRows.get(1) != null) {
                data.putString("lrc2", lrcRows.get(1).content);
            }else {
                data.putString("lrc2", "");
            }
            if (lrcRows.get(0).getIndex() % 2 == 0) {//高亮歌词是偶数
                data.putInt("highlight", 1);
            } else {
                data.putInt("highlight", 2);
            }
            message.setData(data);
            MainFragment.staticHandler.sendMessage(message);
        }else if (MainFragment.staticHandler != null){
            Message message = Message.obtain();
            message.what = MainFragment.UPDATE_LRC;
            Bundle data = new Bundle();
            data.putInt("highlight", 1);
            message.setData(data);
            MainFragment.staticHandler.sendMessage(message);
        }
    }

}
