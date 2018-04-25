package com.nicmic.gatherhear.fragment;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alexvasilkov.foldablelayout.UnfoldableView;
import com.alexvasilkov.foldablelayout.shading.GlanceFoldShading;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.adapter.MusicMenuListAdapter;
import com.nicmic.gatherhear.adapter.SongListAdapter;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.bean.MusicMenu;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.Dialogs;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.nicmic.gatherhear.utils.SongUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MusicMenuFragment extends Fragment {


    private ListView mListView;
    private MusicMenuListAdapter adapter;
    private List<MusicMenu> musicMenus;

    private View mListTouchInterceptor;
    private View mDetailsLayout;
    public static UnfoldableView mUnfoldableView;

    public static final int FOLD_Card = 0;
    public static final int UPDATE_UI = 1;
    public static final int RESET_UI = 2;

    public static Handler staticHandler;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case FOLD_Card:
                    mUnfoldableView.foldBack();
                    break;
                case UPDATE_UI:
                    updateUI();
                    break;
                case RESET_UI:
                    if (msg.arg1 >= 0) {//删除歌曲传回来的被删除歌曲的位置
                        musicMenu.getMusics().remove(msg.arg1);
                        if (msg.arg1 < PlayList.position) {//删除的歌曲在正在播放歌曲的前面
                            PlayList.position = PlayList.position - 1;
                        }
                    }

                    updateUI();
                    resetUI();
                    break;
            }
        }
    };

    private void resetUI() {
        adapter_song = new SongListAdapter(getActivity(), musicMenu.getMusics());
        details_lv.setAdapter(adapter_song);
    }

    private void updateUI() {
        musicMenus = MusicUtils.getAllMusicMenu(getActivity());
        adapter = new MusicMenuListAdapter(getActivity(), musicMenus);
        mListView.setAdapter(adapter);

        //更新歌单展开后歌曲列表的状态
        if (adapter_song != null){
            adapter_song.notifyDataSetChanged();
        }
    }

    public MusicMenuFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_music_menu, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ContainerActivity.handlerMusicMenuFragment = handler;
        staticHandler = handler;

        initHeaderView(view);
        initFoldableView(view);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            updateUI();
            Log.e("MusicMenuFragment", "显示了当前界面，刷新歌单信息");
        }
    }

    private void initFoldableView(View view) {

        mListView = (ListView) view.findViewById(R.id.list_view);
        musicMenus = MusicUtils.getAllMusicMenu(getActivity());
        adapter = new MusicMenuListAdapter(getActivity(), musicMenus);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View view_cover = view.findViewById(R.id.music_menu_cover);
                //传入的第一个参数一定不能是整个item，必须是item中的某个部件，详情见引用文档页面
                openDetails(view_cover, (MusicMenu) view_cover.getTag());
            }
        });

        mListTouchInterceptor = view.findViewById(R.id.touch_interceptor_view);
        mListTouchInterceptor.setClickable(false);

        mDetailsLayout = view.findViewById(R.id.details_layout);
        mDetailsLayout.setVisibility(View.INVISIBLE);

        mUnfoldableView = (UnfoldableView) view.findViewById(R.id.unfoldable_view);

        Bitmap glance = BitmapFactory.decodeResource(getResources(), R.drawable.unfold_glance);
        mUnfoldableView.setFoldShading(new GlanceFoldShading(getActivity(), glance));

        mUnfoldableView.setOnFoldingListener(new UnfoldableView.SimpleFoldingListener() {
            @Override
            public void onUnfolding(UnfoldableView unfoldableView) {
                mListTouchInterceptor.setClickable(true);
                mDetailsLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUnfolded(UnfoldableView unfoldableView) {
                mListTouchInterceptor.setClickable(false);
            }

            @Override
            public void onFoldingBack(UnfoldableView unfoldableView) {
                mListTouchInterceptor.setClickable(true);
            }

            @Override
            public void onFoldedBack(UnfoldableView unfoldableView) {
                mListTouchInterceptor.setClickable(false);
                mDetailsLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    private ImageButton btn_back;
    private ImageButton btn_menu;
    private TextView tv_navbar_title;
    private void initHeaderView(View view) {
        //必须传入参数view，getActivity获取的不一定是当前fragment（原因不明）
        btn_back = (ImageButton) view.findViewById(R.id.btn_back);
        btn_menu = (ImageButton) view.findViewById(R.id.btn_menu);
        btn_menu.setBackgroundResource(R.drawable.btn_add_white);
        tv_navbar_title = (TextView) view.findViewById(R.id.tv_navbar_title);

        tv_navbar_title.setText("我的歌单");
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.back();
            }
        });
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.createNewMusicMenuDialog(getActivity());
            }
        });
    }

    public void openDetails(View coverView, final MusicMenu musicMenu) {
        //通过第二个传入的参数为详情页面设置数据（歌单的歌曲也在这里设置适配器）
        ImageView details_corver = (ImageView) mDetailsLayout.findViewById(R.id.details_corver);
        TextView details_title = (TextView) mDetailsLayout.findViewById(R.id.details_title);
        TextView details_desc = (TextView) mDetailsLayout.findViewById(R.id.details_desc);
        TextView details_num = (TextView) mDetailsLayout.findViewById(R.id.details_num);
        ImageView details_edit = (ImageView) mDetailsLayout.findViewById(R.id.details_edit);
        ImageView details_add = (ImageView) mDetailsLayout.findViewById(R.id.details_add);
        ImageView details_delete = (ImageView) mDetailsLayout.findViewById(R.id.details_delete);
        details_lv = (SwipeMenuListView) mDetailsLayout.findViewById(R.id.details_lv);

        details_title.setText(musicMenu.getTitle());
        details_desc.setText(musicMenu.getDesc());
        details_num.setText(musicMenu.getMusics().size() + "");
        setupListView(details_lv, musicMenu);

        details_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.modifyMusicMenuDialog(getActivity(), musicMenu);
                handler.sendEmptyMessage(FOLD_Card);
            }
        });

        details_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.chooseMusicDialog(getActivity(), musicMenu);
            }
        });

        details_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2015/9/30 如果播放列表是当前歌单，需要清空播放列表
                if (MusicService.CURRENT_UI == MusicService.UI_MUSIC_MENU) {
                    //清空播放列表
                    PlayList.clearPlayList(getActivity());
                    //停止播放
                    PlayList.playStatus = MusicService.PLAYING;
                    MusicService.stop();
                }
                //删除当前歌单
                Dialogs.deleteMusicMenu(getActivity(), musicMenu);
                handler.sendEmptyMessage(FOLD_Card);
            }
        });

        mUnfoldableView.unfold(coverView, mDetailsLayout);

        //listview进入动画
        AnimUtil.listviewEnterAnim(details_lv, AnimUtil.DELAY);
    }

    private SongListAdapter adapter_song;//歌单展开后歌曲列表的适配器
    private SwipeMenuListView details_lv;//歌单展开后的列表
    private MusicMenu musicMenu;//所展开的歌单
    private void setupListView(final SwipeMenuListView details_lv, MusicMenu musicMenu1) {
        this.musicMenu = musicMenu1;
        adapter_song = new SongListAdapter(getActivity(), musicMenu.getMusics());

        details_lv.setAdapter(adapter_song);
        details_lv.setMenuCreator(SongUtils.getMusicMenuSwipeMenu(getActivity()));
        details_lv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        addOrRemoveMyLike(musicMenu ,position, details_lv, adapter_song);
                        break;
                    case 1:
                        Dialogs.add2MusicMenuDialog(getActivity(), musicMenu.getMusics().get(position));
                        break;
                    case 2:
                        Dialogs.musicInfoDialog(getActivity(), musicMenu.getMusics().get(position));
                        break;
                    case 3:
                        Dialogs.currentUI = MusicService.UI_MUSIC_MENU;
                        Dialogs.removeMusicDialog(getActivity(), musicMenu, position);
                        break;
                    case 4:
                        Dialogs.currentUI = MusicService.UI_MUSIC_MENU;
                        Dialogs.deleteMusicDialog(getActivity(), musicMenu.getMusics().get(position), position);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        details_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SongUtils.songClickListener(view, position, musicMenu.getMusics());
            }
        });
    }

    /**
     * 添加或移除我喜欢的
     * @param musicMenu
     * @param position
     * @param details_lv
     * @param adapter
     */
    private void addOrRemoveMyLike(MusicMenu musicMenu, int position, SwipeMenuListView details_lv, SongListAdapter adapter){
        int myLike = Dialogs.add2LikeDialog(getActivity(), musicMenu.getMusics().get(position));
        musicMenu.getMusics().get(position).setMyLike(myLike);
        //如果播放列表有这首歌曲，则也更新喜欢状态
        for (int i = 0; i < PlayList.musics.size(); i++) {
            if (PlayList.musics.get(i).getId().equals(musicMenu.getMusics().get(position).getId())){
                PlayList.musics.get(i).setMyLike(myLike);
            }
        }
        //TODO:刷新菜单状态
        int firstVisiblePosition = details_lv.getFirstVisiblePosition();
        adapter = new SongListAdapter(getActivity(), musicMenu.getMusics());
        details_lv.setAdapter(adapter);
        details_lv.setSelection(firstVisiblePosition);
        adapter.notifyDataSetInvalidated();
    };

}
