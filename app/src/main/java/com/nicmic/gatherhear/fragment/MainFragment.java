package com.nicmic.gatherhear.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.ImageUtils;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements View.OnClickListener {

    @Bind(R.id.btn_menu_main)
    ImageButton btnMenuMain;
    @Bind(R.id.tv_logo_title)
    TextView tvLogoTitle;
    @Bind(R.id.btn_play_main)
    CircleImageView btnPlayMain;
    @Bind(R.id.btn_last_main)
    CircleImageView btnLastMain;
    @Bind(R.id.btn_next_main)
    CircleImageView btnNextMain;
    @Bind(R.id.tv_title_main)
    TextView tvTitleMain;
    @Bind(R.id.tv_artist_main)
    TextView tvArtistMain;
    @Bind(R.id.tv_lrc_last)
    TextView tvLrcLast;
    @Bind(R.id.tv_lrc_next)
    TextView tvLrcNext;
    @Bind(R.id.lrc_card)
    CardView lrcCard;
    @Bind(R.id.tv_local_music_num)
    TextView tvLocalMusicNum;
    @Bind(R.id.local_music_card)
    CardView localMusicCard;
    @Bind(R.id.tv_recent_play_num)
    TextView tvRecentPlayNum;
    @Bind(R.id.recent_play_card)
    CardView recentPlayCard;
    @Bind(R.id.tv_my_like_num)
    TextView tvMyLikeNum;
    @Bind(R.id.my_like_card)
    CardView myLikeCard;
    @Bind(R.id.tv_music_menu_num)
    TextView tvMusicMenuNum;
    @Bind(R.id.my_music_menu_card)
    CardView myMusicMenuCard;
    @Bind(R.id.fragment_main_container)
    FrameLayout fragmentMainContainer;
    @Bind(R.id.icon_play_main)
    ImageView icon_play_main;

    public static FragmentManager fragmentManager;

    public static LocalMusicFragment localMusicFragment;
    public static MusicMenuFragment musicMenuFragment;

    public static final int BACK = 0;
    public static final int UPDATE_UI = 1;
    public static final int PLAY_LAST_ANIM = 2;
    public static final int PLAY_NEXT_ANIM = 3;
    public static final int UPDATE_LRC = 4;
    public static Handler staticHandler;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case BACK:
                    back();
                    break;
                case UPDATE_UI:
                    updateUI();
                    break;
                case PLAY_LAST_ANIM:
                    tvLrcLast.setText(R.string.default_music_title);
                    tvLrcNext.setText(R.string.default_music_artist);
                    AnimUtil.mainFragmentSwitchMusicTitleAnim(tvTitleMain, "playLast");
                    AnimUtil.mainFragmentSwitchMusicArtistAnim(tvArtistMain, "playLast");
                    break;
                case PLAY_NEXT_ANIM:
                    tvLrcLast.setText(R.string.default_music_title);
                    tvLrcNext.setText(R.string.default_music_artist);
                    AnimUtil.mainFragmentSwitchMusicTitleAnim(tvTitleMain, "playNext");
                    AnimUtil.mainFragmentSwitchMusicArtistAnim(tvArtistMain, "playNext");
                    break;
                case UPDATE_LRC:
                    Bundle bundle = msg.getData();
                    int highlight = bundle.getInt("highlight");
                    String lrc1 = bundle.getString("lrc1", App.sContext.getResources().getString(R.string.default_music_title));
                    String lrc2 = bundle.getString("lrc2", App.sContext.getResources().getString(R.string.default_music_artist));
                    updateLrc(highlight, lrc1, lrc2);
                    break;
            }
        }
    };

    public MainFragment() {
        ContainerActivity.handlerMainFragment = handler;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        staticHandler = handler;
        fragmentManager = getFragmentManager();

        ButterKnife.bind(this, view);
        setClickListener();
        initFragment();
        updateUI();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden){
            updateUI();
        }
    }

    private void initFragment() {
        if (localMusicFragment == null){
            localMusicFragment = new LocalMusicFragment();
        }
        if (musicMenuFragment == null){
            musicMenuFragment = new MusicMenuFragment();
        }

        fragmentManager.beginTransaction()
                .add(R.id.fragment_main, localMusicFragment).hide(localMusicFragment)
                .add(R.id.fragment_main, musicMenuFragment).hide(musicMenuFragment)
                .add(R.id.fragment_main, SongFragment.getInstance()).hide(SongFragment.getInstance())
                .commit();
    }

    private void updateUI() {
        Music music = PlayList.getPlayingMusic();
        if (music == null) {
            tvTitleMain.setText(R.string.default_music_title);
            tvArtistMain.setText(R.string.default_music_artist);
        }else{
            tvTitleMain.setText(music.getTitle());
            tvArtistMain.setText(music.getArtist());
        }

        if (MusicService.player != null && MusicService.player.isPlaying()){
            icon_play_main.setImageResource(R.drawable.btn_pause_white);
        }else {
            icon_play_main.setImageResource(R.drawable.btn_play_arrow_white);
        }

        //更新专辑图片
        if (music != null) {
            String uri = ImageUtils.getArtworkUri(App.sContext, music.getSongId(), music.getAlbumId(), true);
            ImageLoader.getInstance().displayImage(uri, btnPlayMain);
        }

        //更新本地音乐，我喜欢的，我的歌单，最近播放的歌曲数目
        int localMusicNum = MusicUtils.getMusic(getActivity()).size();
        int myLikeNum = MusicUtils.getMyLikeMusic(getActivity()).size();
        int musicMenuNum = MusicUtils.getAllMusicMenu(getActivity()).size();
        int recentPlayNum = MusicUtils.getRecentPlay(getActivity()).size();

        tvLocalMusicNum.setText(localMusicNum + "首");
        tvMyLikeNum.setText(myLikeNum + "首");
        tvMusicMenuNum.setText(musicMenuNum + "张");
        tvRecentPlayNum.setText(recentPlayNum + "\n首");

    }

    private void updateLrc(int highlight, String lrc1, String lrc2) {
        if (highlight == 1) {//高亮歌词在上面
            tvLrcLast.setText(lrc1);
            tvLrcLast.setTextColor(getActivity().getResources().getColor(R.color.custom_color));
            tvLrcNext.setText(lrc2);
            tvLrcNext.setTextColor(Color.BLACK);
//            YoYo.with(Techniques.FadeIn).duration(1000).playOn(tvLrcNext);
        } else {//高亮歌词在下面
            tvLrcNext.setText(lrc1);
            tvLrcNext.setTextColor(App.sContext.getResources().getColor(R.color.custom_color));
            tvLrcLast.setText(lrc2);
            tvLrcLast.setTextColor(Color.BLACK);
//            YoYo.with(Techniques.FadeIn).duration(1000).playOn(tvLrcLast);
        }
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

    private void setClickListener() {
        btnMenuMain.setOnClickListener(this);
        btnLastMain.setOnClickListener(this);
        btnPlayMain.setOnClickListener(this);
        btnNextMain.setOnClickListener(this);

        lrcCard.setOnClickListener(this);
        localMusicCard.setOnClickListener(this);
        recentPlayCard.setOnClickListener(this);
        myLikeCard.setOnClickListener(this);
        myMusicMenuCard.setOnClickListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(getActivity());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_menu_main:
                toggleDrawer();
                break;
            case R.id.btn_last_main:
                playLast();
                break;
            case R.id.btn_play_main:
                playOrPause();
                break;
            case R.id.btn_next_main:
                playNext();
                break;

            case R.id.lrc_card:

                break;
            case R.id.local_music_card:
                toLocalMusic();
                break;
            case R.id.recent_play_card:
                List<Music> recentPlay = MusicUtils.getRecentPlay(getActivity());
                toSongFragment(SongFragment.TAG_RECENT_PLAY, "最近播放", recentPlay);
                break;
            case R.id.my_like_card:
                List<Music> musics = MusicUtils.getMyLikeMusic(getActivity());
                toSongFragment(SongFragment.TAG_MY_LIKE, "我喜欢的", musics);
                break;
            case R.id.my_music_menu_card:
                toMusicMenu();
                break;

        }
    }

    /**
     * 播放下一首
     */
    private void playNext() {
        //先判断播放列表是否有音乐
        if (PlayList.isEmpty()){
            Toast.makeText(getActivity(), "没有可播放的音乐", Toast.LENGTH_SHORT).show();
        }else{
            MusicService.playNext();
            //播放MainFragment切换下一首的动画效果
            handler.sendEmptyMessage(PLAY_NEXT_ANIM);
        }
    }

    /**
     * 播放上一首
     */
    private void playLast() {
        //先判断播放列表是否有音乐
        if (PlayList.isEmpty()){
            Toast.makeText(getActivity(), "没有可播放的音乐", Toast.LENGTH_SHORT).show();
        }else{
            MusicService.playLast();
            //播放MainFragment切换上一首的动画效果
            handler.sendEmptyMessage(PLAY_LAST_ANIM);

        }
    }

    /**
     * 播放或暂停
     */
    private void playOrPause(){

        if (PlayList.isEmpty()) {//音乐列表为空
            btnPlayMain.setImageResource(R.drawable.default_cd_cover);
            Toast.makeText(getActivity(), "当前没有可播放的音乐", Toast.LENGTH_SHORT).show();
            Log.e("MainFragment", "播放列表没有音乐");
        }else {
            MusicService.playOrPause();
            if (MusicService.player.isPlaying()){
                icon_play_main.setImageResource(R.drawable.btn_pause_white);
                Log.e("MainFragment", "停止播放");
            }else {
                icon_play_main.setImageResource(R.drawable.btn_play_arrow_white);
                Log.e("MainFragment", "继续播放");
            }
            //TODO:先去加载音乐是否有歌手图片，没有的话使用默认的图片(联网实现，未实现)
        }
    }


    private void toggleDrawer() {
        boolean isOpen = ContainerActivity.drawer.isDrawerOpen();
        if (isOpen){
            ContainerActivity.drawer.closeDrawer();
        }else{
            ContainerActivity.drawer.openDrawer();
        }
    }

    private void toMusicMenu() {
        hidedFragment = ContainerActivity.mainFragment;
        showedFragment = musicMenuFragment;

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_FRAGMENT_ENTER_EXIT) {
            ft.setCustomAnimations(R.anim.slide_right_in, R.anim.shrink_out,
                    R.anim.boost_in, R.anim.slide_left_out);
        }
        ft.hide(hidedFragment).show(showedFragment).commit();
    }

    private void toLocalMusic() {
        hidedFragment = ContainerActivity.mainFragment;
        showedFragment = localMusicFragment;

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_FRAGMENT_ENTER_EXIT) {
            ft.setCustomAnimations(R.anim.slide_right_in, R.anim.shrink_out,
                    R.anim.boost_in, R.anim.slide_left_out);
        }
        ft.hide(hidedFragment).show(showedFragment).commit();
    }

    /**
     * 跳转到SongFragment
     * @param tag
     * @param title
     * @param musics
     */
    public static void toSongFragment(int tag, String title, List<Music> musics) {
        SongFragment.getInstance().setData(tag, title, musics);

        if (tag == SongFragment.TAG_ARTIST || tag == SongFragment.TAG_ALBUM || tag == SongFragment.TAG_FILE){
            hidedFragment = localMusicFragment;
            showedFragment = SongFragment.getInstance();
        }else if (tag == SongFragment.TAG_MY_LIKE || tag == SongFragment.TAG_RECENT_PLAY) {
            hidedFragment = ContainerActivity.mainFragment;
            showedFragment = SongFragment.getInstance();
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_FRAGMENT_ENTER_EXIT) {
            ft.setCustomAnimations(R.anim.slide_right_in, R.anim.shrink_out,
                    R.anim.boost_in, R.anim.slide_left_out);
        }
        ft.hide(hidedFragment).show(showedFragment).commit();
    }

    public static void back(){

        //没有可回退的fragment，直接退出界面
        if (hidedFragment == null && showedFragment == null){
            ContainerActivity activity = (ContainerActivity) ContainerActivity.mainFragment.getActivity();
            activity.exitBy2Click();
            return;
        }

        FragmentTransaction ft = fragmentManager.beginTransaction();
        if (AnimUtil.ANIM_OPENED && AnimUtil.ANIM_FRAGMENT_ENTER_EXIT) {
            ft.setCustomAnimations(R.anim.boost_in, R.anim.slide_left_out);
        }
        ft.hide(showedFragment).show(hidedFragment).commit();

        //如果是歌手，专辑，文件夹这3个回退，则回退到LocalMusicFragment
        if (SongFragment.getInstance().getTAG() == SongFragment.TAG_ARTIST ||
                SongFragment.getInstance().getTAG() == SongFragment.TAG_ALBUM ||
                SongFragment.getInstance().getTAG() == SongFragment.TAG_FILE){
            showedFragment = localMusicFragment;
            hidedFragment = ContainerActivity.mainFragment;
            SongFragment.getInstance().setTAG(-1);
        }else{
            showedFragment = null;
            hidedFragment = null;
        }
        Log.e("MainFragment", "执行返回方法");

    }

    public static Fragment hidedFragment = null;//被隐藏的fragment
    public static Fragment showedFragment = null;//被显示的fragment

}
