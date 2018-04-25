package com.nicmic.gatherhear.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.activity.BaseActivity;
import com.nicmic.gatherhear.activity.ScanMusicActivity;
import com.nicmic.gatherhear.activity.SettingsActivity;
import com.nicmic.gatherhear.activity.SoundEffectActivity;
import com.nicmic.gatherhear.adapter.PlayListAdapter;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.lrc.LrcView;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.Dialogs;
import com.nicmic.gatherhear.utils.ImageUtils;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.nicmic.gatherhear.widget.MusicNotification;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import co.mobiwise.library.ProgressLayout;

public class ContainerActivity extends BaseActivity implements View.OnClickListener, OnMenuItemClickListener, OnMenuItemLongClickListener {

    //3个主要fragment
    private FrameLayout fragment_container;
    private RelativeLayout fragment_main, fragment_music;
    public static MainFragment mainFragment;
    public static MusicFragment musicFragment;
    private FragmentManager fragmentManager;
    //底部音乐相关
    private RelativeLayout bottom_music;
    private ProgressLayout bottom_music_front;
    private RelativeLayout bottom_music_back;
    private TextView bottom_music_progress_text;
    //底部音乐正面部件
    private static CircularImageView bottom_music_profile;
    private static TextView bottom_music_title;
    private static TextView bottom_music_artist;
    private RelativeLayout bottom_music_play;
    private static ImageView bottom_music_play_icon;
    private RelativeLayout bottom_music_playlist;
    //底部音乐播放列表
    private ListView playList;
    public static PlayListAdapter adapter_playlist;
    //左侧抽屉菜单
    public static Drawer drawer;
    //用来调用其他fragment方法的handler
    public static Handler handlerMusicFragment;
    public static Handler handlerMainFragment;
    public static Handler handlerLocalMusicFragment;
    public static Handler handlerMusicMenuFragment;

    //用来被音乐服务调用的handler
    public static final int UPDATE_BOTTOM_MUSIC = 0;
    public static final int UPDATE_BOTTOM_MUSIC_PROGRESS = 1;
    public static Handler staticHandler;//供外界调用的handler
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_BOTTOM_MUSIC:
                    updateBottomMusic();
                    break;
                case UPDATE_BOTTOM_MUSIC_PROGRESS:
                    int progress = msg.arg1;
                    int duration = msg.arg2;
                    updateBottomMusicProgress(progress, duration);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);

        staticHandler = handler;

        //获取最后一次退出时保存的音乐播放列表播放位置
        PlayList.musics.clear();
        PlayList.addMusics(MusicUtils.getPlayList());
        PlayList.position = MusicUtils.getPlayListPosition(this);

        findView();
        initListener();
        initFragment();
        addFragment(mainFragment, false, R.id.fragment_main);
        addFragment(musicFragment, false, R.id.fragment_music);
        initDrawer();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //其他应用调用当前Activity，并传入音乐信息
        playOtherAppMusic();
    }

    /**
     * 获得从其他应用调用当前Activity时传入的音乐信息
     */
    private void playOtherAppMusic() {
        //  获得其他应用程序传递过来的数据
        Intent intent = getIntent();
        if (intent.getData() != null) {
            //  获得Host，也就是play://后面的内容
            String host = getIntent().getData().getHost();
            System.out.println("host = " + host);
            //获得歌曲信息
            String title = intent.getStringExtra("title");
            String artist = intent.getStringExtra("artist");
            String duration = intent.getStringExtra("duration");
            String url = intent.getStringExtra("url");
            System.out.println("传来title = " + title);
            System.out.println("传来artist = " + artist);
            System.out.println("传来duration = " + duration);
            System.out.println("传来url = " + url);

            Music music = new Music();
            music.setId(UUID.randomUUID().toString());
            music.setTitle(title);
            music.setArtist(artist);
            music.setDuration(duration);
            music.setPath(url);
            List<Music> songs = new ArrayList<>();
            songs.add(music);
            //设置播放列表并播放当前歌曲
            PlayList.setPlayList(songs, 0);
//            MusicService.play();
//            playOrPause();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MusicService.play();
                    System.out.println("一秒后播放");
                }
            }, 1000);

            intent.setData(null);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //打开音乐播放服务
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);

        //恢复底部音乐进度条和音乐界面进度条的最近一次保存的状态
        recoverLastProgressStatus();

        //显示通知
        MusicNotification.showNotification();

        //刷新UI(必须放在oncreate创建完后的周期中，否则Fragment的getActivity()获得的上下文都是null)
        MusicService.updateUI();
        Log.e("ContainerActivity", "刷新UI");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bottom_music_play:
                playOrPause();
                break;
            case R.id.bottom_music_playlist:
                togglePlayList();
                break;
        }
    }

    /**
     * 恢复底部音乐和音乐播放界面最近一次保存的进度条状态
     */
    private void recoverLastProgressStatus() {
        if (PlayList.position != -1) {
            //更新底部音乐的进度条为最近一次保存的状态
            int progress = MusicUtils.getPlayListProgress(this);
            int duration = MusicUtils.getPlayListDuration(this);
            Log.e("ContainerActivity", MusicUtils.getTimeString(progress) + "/" + MusicUtils.getTimeString(duration));
            bottom_music_progress_text.setText(MusicUtils.getTimeString(progress) + "/" + MusicUtils.getTimeString(duration));
            bottom_music_front.setMaxProgress(duration);
            bottom_music_front.setCurrentProgress(progress);
            //更新播放界面的进度条和进度文字
            if (MusicFragment.staticHandler != null) {
                Message msg = Message.obtain();
                msg.what = MusicFragment.RECOVER_LAST_STATUS;
                msg.arg1 = progress;
                msg.arg2 = duration;
                MusicFragment.staticHandler.sendMessage(msg);
            }
        }
    }

    /**
     * 更新底部音乐信息
     */
    private static void updateBottomMusic() {
        Music music = PlayList.getPlayingMusic();
        if (music == null){
            bottom_music_title.setText(R.string.default_music_title);
            bottom_music_artist.setText(R.string.default_music_artist);
        }else{
            bottom_music_title.setText(music.getTitle());
            bottom_music_artist.setText(music.getArtist());
        }
        if (MusicService.player != null && MusicService.player.isPlaying()){
            bottom_music_play_icon.setImageResource(R.drawable.btn_pause_circle_white);
            Log.e("ContainerActivity", "更新为暂停图标");
        }else {
            bottom_music_play_icon.setImageResource(R.drawable.btn_play_circle_white);
            Log.e("ContainerActivity", "更新为播放图标");
        }

        //设置专辑图片
        if (music != null) {
            String uri = ImageUtils.getArtworkUri(App.sContext, music.getSongId(), music.getAlbumId(), true);
            ImageLoader.getInstance().displayImage(uri, bottom_music_profile);
        }

    }

    /**
     * 更新底部音乐的进度条和进度条文字
     * @param progress
     * @param duration
     */
    public void updateBottomMusicProgress(int progress, int duration){
        bottom_music_progress_text.setText(MusicUtils.getTimeString(progress) + "/" + MusicUtils.getTimeString(duration));
        bottom_music_front.setMaxProgress(duration);
        bottom_music_front.setCurrentProgress(progress);
    }

    private void initListener() {
        bottom_music_play.setOnClickListener(this);
        bottom_music_playlist.setOnClickListener(this);
    }

    //初始化抽屉菜单
    private void initDrawer() {
        //初始化头部
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.bg_rectangle_custom_color)
//                .addProfiles(
//                        new ProfileDrawerItem().withName("时光的倒影").withEmail("251068550@qq.com").withIcon(R.drawable.pic7)
//                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        Toast.makeText(ContainerActivity.this, "dianji", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                })
                .build();

        //初始化items
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIcon(R.drawable.ic_sound_black).withName(R.string.drawer_item_sound).withIdentifier(1);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIcon(R.drawable.ic_skin_black).withName(R.string.drawer_item_skin).withIdentifier(2);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIcon(R.drawable.ic_scanner_black).withName(R.string.drawer_item_scan_music).withIdentifier(3);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIcon(R.drawable.ic_settings_black).withName(R.string.drawer_item_settings).withIdentifier(4);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIcon(R.drawable.ic_exit_to_app_black).withName(R.string.drawer_item_exit).withIdentifier(5);

        //初始化drawer
        DrawerBuilder drawerBuilder = new DrawerBuilder();
        drawerBuilder.withActivity(this)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        item1, item2, item3, item4
                )
                .addStickyDrawerItems(
//                        new DividerDrawerItem(),
                        item5
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (position == -1) {
                            finish();
                        }
                        if (position == 1) {
                            Intent intent = new Intent(ContainerActivity.this, SoundEffectActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_right_in, R.anim.shrink_out);
                        }
                        if (position == 2) {
                            Toast.makeText(ContainerActivity.this, "换肤，功能未实现", Toast.LENGTH_SHORT).show();
                        }
                        if (position == 3) {
                            Intent intent = new Intent(ContainerActivity.this, ScanMusicActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_right_in, R.anim.shrink_out);
                        }
                        if (position == 4) {
                            Intent intent = new Intent(ContainerActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_right_in, R.anim.shrink_out);
                        }
                        return false;
                    }
                });
//                .withDrawerGravity(Gravity.END)
        drawer = drawerBuilder.build();
    }

    private void toggleDrawer() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
        } else {
            drawer.openDrawer();
        }
    }

    private void initFragment() {
        mainFragment = new MainFragment();
        musicFragment = new MusicFragment(fragment_main, fragment_music, bottom_music, bottom_music_front, bottom_music_back);
        fragmentManager = getSupportFragmentManager();
    }

    protected void addFragment(Fragment fragment, boolean addToBackStack, int containerId) {
        String backStackName = fragment.getClass().getName();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate(backStackName, 0);
        if (!fragmentPopped) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.add(containerId, fragment, backStackName);
            if (addToBackStack)
                transaction.addToBackStack(backStackName);
            transaction.commit();
        }

    }

    private void findView() {
        fragment_music = (RelativeLayout) findViewById(R.id.fragment_music);
        fragment_main = (RelativeLayout) findViewById(R.id.fragment_main);
        fragment_container = (FrameLayout) findViewById(R.id.fragment_container);
        bottom_music = (RelativeLayout) findViewById(R.id.bottom_music);
        bottom_music_front = (ProgressLayout) findViewById(R.id.bottom_music_front);
        bottom_music_back = (RelativeLayout) findViewById(R.id.bottom_music_back);
        bottom_music_progress_text = (TextView) findViewById(R.id.bottom_music_progress_text);
        bottom_music_profile = (CircularImageView) findViewById(R.id.bottom_music_profile);
        bottom_music_title = (TextView) findViewById(R.id.bottom_music_title);
        bottom_music_artist = (TextView) findViewById(R.id.bottom_music_artist);
        bottom_music_play = (RelativeLayout) findViewById(R.id.bottom_music_play);
        bottom_music_play_icon = (ImageView) findViewById(R.id.bottom_music_play_icon);
        bottom_music_playlist = (RelativeLayout) findViewById(R.id.bottom_music_playlist);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if (fragment_music.getVisibility() == View.VISIBLE) {
            handlerMusicFragment.sendEmptyMessage(MusicFragment.OPEN_MENU);
        } else if (MainFragment.localMusicFragment.isVisible()) {
            handlerLocalMusicFragment.sendEmptyMessage(LocalMusicFragment.OPEN_MENU);
        } else if (mainFragment.isVisible()) {
            toggleDrawer();
        }
//        return super.onMenuOpened(featureId, menu);
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //-------------------操作音乐播放界面-----------------------
            if (fragment_music.getVisibility() == View.VISIBLE) {
                if (MusicFragment.mMenuDialogFragment != null && MusicFragment.mMenuDialogFragment.isAdded()) {
                    handlerMusicFragment.sendEmptyMessage(MusicFragment.CLOSE_MENU);
                } else {
                    handlerMusicFragment.sendEmptyMessage(MusicFragment.HIDE_MUSIC_FRAGMENT);
//                    handlerMusicFragment.sendEmptyMessage(MusicFragment.STOP_UPDATE_PROGRESS);
                    return false;
                }
            } else {//------------------------------操作主界面-------------------------
                //优先关闭播放列表
                if (playlistDialog != null && playlistDialog.isShowing()) {
                    playlistDialog.dismiss();
                    return true;
                }
                //----------------------------操作本地音乐------------------------
                //如果本地音乐处于排序音乐状态，则关闭排序状态，不执行返回
                if (LocalMusicFragment.nav_menu_finish != null && LocalMusicFragment.nav_menu_finish.getVisibility() == View.VISIBLE) {
                    handlerLocalMusicFragment.sendEmptyMessage(LocalMusicFragment.END_DRAG_SORT_STATUS);
                    return true;
                }

                //TODO:操作其他页面
                //----------------------------操作歌单页面----------------------------
                if (MusicMenuFragment.mUnfoldableView != null && MusicMenuFragment.mUnfoldableView.isUnfolded() || MusicMenuFragment.mUnfoldableView.isUnfolding()) {
                    handlerMusicMenuFragment.sendEmptyMessage(MusicMenuFragment.FOLD_Card);
                    return true;
                }

                //主页面fragment的返回(优先级最低，应让其他操作做完最后才执行此操作)
                handlerMainFragment.sendEmptyMessage(MainFragment.BACK);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public static DialogPlus playlistDialog;

    TextView tv_title_and_num;//播放列表的标题和数量
    RelativeLayout clear_all;//清空播放列表的按钮
    private void initPlaylist() {
        LinearLayout linearLayout = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(params);
        //设置headerView
        View headerView = LayoutInflater.from(this).inflate(R.layout.include_playlist_header, null);
        tv_title_and_num = (TextView) headerView.findViewById(R.id.tv_title_and_num);
        tv_title_and_num.setText("播放列表(" + PlayList.musics.size() + ")");
        clear_all = (RelativeLayout) headerView.findViewById(R.id.clear_all);
        //设置播放列表
        playList = new ListView(this);
        if (adapter_playlist == null){
            adapter_playlist = new PlayListAdapter(this, PlayList.musics);
            playList.setAdapter(adapter_playlist);
            playList.setSelection(PlayList.position);
            adapter_playlist.notifyDataSetInvalidated();
            Log.e("ContainerActivity", "初始化播放列表并定位到当前播放位置");
        }

        linearLayout.addView(headerView);
        linearLayout.addView(playList);

        playlistDialog = DialogPlus.newDialog(this)
                .setBackgroundColorResourceId(R.color.black_transparent)
                .setContentHolder(new ViewHolder(linearLayout))
                .setContentHeight(App.sScreenHeight / 2 + headerView.getHeight())
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                    }
                })
                .setExpanded(false)
                .setOutMostMargin(0, 0, 0, (int) getResources().getDimension(R.dimen.bottom_music_height))
                .create();
        playlistDialog.show();

        //这个点击事件必须放到对话框显示后才设置，设置对话框时对view进行了重绘，点击事件设置了会失效
        clear_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.clearPlayList(ContainerActivity.this, Dialogs.PLAY_LIST_BOTTOM_MUSIC);
            }
        });
    }

    private void togglePlayList() {
        if (playlistDialog == null){
            initPlaylist();
        }else{
            if (playlistDialog.isShowing()){
                playlistDialog.dismiss();
            }else {
                playlistDialog.show();

                if (tv_title_and_num == null) {
                    System.out.println("tv_title_and_num是空的");
                } else {
                    System.out.println("tv_title_and_num不是空的");
                }
                tv_title_and_num.setText("播放列表(" + PlayList.musics.size() + ")");
                playList.setSelection(PlayList.position);
                adapter_playlist.notifyDataSetInvalidated();
                Log.e("ContainerActivity", "更新播放列表并定位到当前播放位置");
            }
        }
    }

    /**
     * 控制播放或者暂停
     */
    private void playOrPause() {
        if (PlayList.isEmpty()) {//音乐列表为空
            Toast.makeText(this, "当前没有可播放的歌曲", Toast.LENGTH_SHORT).show();
        }else{
            MusicService.playOrPause();
        }
    }


    @Override
    public void onMenuItemClick(View clickedView, int position) {
        if (fragment_music.getVisibility() == View.VISIBLE) {
            handlerMusicFragment.sendEmptyMessage(position);
        } else if (!MainFragment.localMusicFragment.isHidden()) {
            handlerLocalMusicFragment.sendEmptyMessage(position);
        }
    }

    @Override
    public void onMenuItemLongClick(View clickedView, int position) {
        Toast.makeText(this, "长按了主页:" + position, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        //保存当前播放列表和播放位置（不能清空播放列表，否则播放下一首时会报错）
        MusicUtils.savePlayList();
        MusicUtils.savePlayPosition();
        //保存当前播放歌曲的进度和时长
        MusicUtils.saveProgressDuration();

        Log.e("ContainerActivity", "所有fragment和用到的静态成员变量都需要在这个方法里置为null，" +
                "否则下次启动页面内容为空，类的静态成员变量不会随着实例被销毁");
        /**
         * 类的实例被销毁了，但类的静态成员变量和方法还存在
         * 比如静态的handler就要随着实例销毁时置空，因为handler指向的是类的实例的handler
         * （TODO:还有许多类似的情况没有处理,需要对内存进行优化）
         */
        //所有讲台fragment回收
        mainFragment = null;
        musicFragment = null;
        MainFragment.localMusicFragment = null;
        MainFragment.musicMenuFragment = null;

        //将所有静态handler回收
        MainFragment.staticHandler = null;
        LocalMusicFragment.staticHandler = null;
        MusicMenuFragment.staticHandler = null;
        MusicFragment.staticHandler = null;
        SongFragment.staticHandler = null;
        LrcView.staticHandler = null;

        //回收播放列表
        MusicFragment.adapter_playlist = null;
        adapter_playlist = null;
        playlistDialog = null;

        //TODO:需要先判断是否关闭音乐服务，如果关闭，则播放列表要清空

        super.onDestroy();
    }

    /**
     * 双击退出函数
     */
    private static Boolean isExit = false;
    public void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true; // 准备退出
            Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // 取消退出
                }
            }, 2000); // 如果2秒钟内没有按下返回键，则启动定时器取消掉刚才执行的任务
        } else {
            finish();
//            System.exit(0);
        }
    }
}
