package com.nicmic.gatherhear.fragment;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.activity.ScanMusicActivity;
import com.nicmic.gatherhear.activity.SearchLocalMusicActivity;
import com.nicmic.gatherhear.adapter.AlbumListAdapter;
import com.nicmic.gatherhear.adapter.ArtistListAdapter;
import com.nicmic.gatherhear.adapter.FileListAdapter;
import com.nicmic.gatherhear.adapter.SongListAdapter;
import com.nicmic.gatherhear.adapter.SongListDragAdapter;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.Dialogs;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.nicmic.gatherhear.utils.SongUtils;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.TouchViewDraggableManager;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocalMusicFragment extends Fragment implements View.OnClickListener, OnMenuItemClickListener {

    private SwipeMenuListView lv_song = null;
    private ListView lv_artist = null;
    private ListView lv_album = null;
    private ListView lv_file = null;
    private DynamicListView lv_dynamic;//用于动态排序歌曲，仅在单曲模式有效

    private SongListAdapter adapter_song;
    private ArtistListAdapter adapter_artist;
    private AlbumListAdapter adapter_album;
    private FileListAdapter adapter_file;
    //单曲音乐集合
    public static List<Music> songs = new ArrayList<>();

    //navbar相关
    public static TextView nav_menu_finish;
    private LinearLayout nav_menu_buttons;
    private TextView btn_song;//0
    private TextView btn_artist;//1
    private TextView btn_album;//2
    private TextView btn_file;//3

    public LocalMusicFragment() {
        // Required empty public constructor
    }

    public static final int SCAN_MUSIC = 1;//扫描音乐
    public static final int SEARCH_MUSIC = 2;//搜索音乐
    public static final int PLAY_ORDER = 3;//播放顺序
    public static final int OPEN_MENU = 4;//打开菜单
    public static final int CLOSE_MENU = 5;//关闭菜单
    public static final int END_DRAG_SORT_STATUS = 6;//结束拖拽排序的界面
    public static final int UPDATE_UI_SONG = 7;//更新单曲界面的UI
    public static final int RESET_UI = 8;//更新单曲界面的UI

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case OPEN_MENU:
                    openMenu();
                    break;
                case CLOSE_MENU:

                    break;
                case END_DRAG_SORT_STATUS:
                    endDragSortStatus();
                    break;
                case SCAN_MUSIC:
                    Intent intent = new Intent(getActivity(), ScanMusicActivity.class);
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.shrink_out);
                    break;
                case SEARCH_MUSIC:
                    Intent intent1 = new Intent(getActivity(), SearchLocalMusicActivity.class);
                    startActivity(intent1);
                    getActivity().overridePendingTransition(R.anim.slide_right_in, R.anim.shrink_out);
                    break;
                case PLAY_ORDER:
                    Dialogs.showOrder(getActivity());
                    break;
                case UPDATE_UI_SONG://更新单曲界面
                    updateUI();
                    break;
                case RESET_UI:
                    if (msg.arg1 >= 0) {//删除歌曲传回来的被删除歌曲的位置
                        songs.remove(msg.arg1);
                        if (msg.arg1 < PlayList.position) {//删除的歌曲在正在播放歌曲的前面
                            PlayList.position = PlayList.position - 1;
                        }
                    }
                    resetUI();
                    break;
            }
        }
    };

    public static Handler staticHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContainerActivity.handlerLocalMusicFragment = handler;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_music, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initHeaderView(view);
        findView(view);
        initTabListener();
        selectTab(0);
//        initMusicMenu();
        initSongListView(view);

        //设置单曲列表
        setupSongList();

        //将静态变量指向当前对象的成员变量，供外界调用
        staticHandler = handler;

    }

    @Override
    public void onResume() {
        super.onResume();
        //当其他Activity覆盖当前页面时会调用onPause和onStop，返回后会调用onResume
        setupSongList();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setupSongList();
            //提示去扫描音乐
            if (songs.size() == 0) {
                Dialogs.tipToScanMusic(getActivity());
            }
            //listview进入动画
            if (lv_song.getVisibility() == View.VISIBLE) {
                AnimUtil.listviewEnterAnim(lv_song, AnimUtil.DELAY);
            }
            if (lv_artist.getVisibility() == View.VISIBLE) {
                AnimUtil.listviewEnterAnim(lv_artist, AnimUtil.DELAY);
            }
            if (lv_album.getVisibility() == View.VISIBLE) {
                AnimUtil.listviewEnterAnim(lv_album, AnimUtil.DELAY);
            }
            if (lv_file.getVisibility() == View.VISIBLE) {
                AnimUtil.listviewEnterAnim(lv_file, AnimUtil.DELAY);
            }
        }
    }

    /**
     * 更新UI，在播放歌曲的位置发生改变时调用
     */
    private void updateUI() {
        adapter_song.notifyDataSetChanged();
    }

    /**
     * 重新设置UI，只有当删除歌曲时调用
     */
    private void resetUI() {
        adapter_song = new SongListAdapter(getActivity(), songs);
        lv_song.setAdapter(adapter_song);
    }

    /**
     * 设置单曲列表
     */
    private void setupSongList() {
        songs = MusicUtils.getMusic(getActivity());
        adapter_song = new SongListAdapter(getActivity(), songs);

        //每次显示本地音乐界面都重新设置适配器，防止音乐列表更新后没有刷新界面
        lv_song.setAdapter(adapter_song);
        lv_song.setSelection(PlayList.position - 3);//让正在播放的view显示在中间
        adapter_song.notifyDataSetInvalidated();
        adapter_song.notifyDataSetChanged();
        handler.sendEmptyMessage(UPDATE_UI_SONG);
    }

    /**
     * 设置歌手列表
     */
    private Map<String, List<Music>> mapArtist;

    private void setupArtistList() {
        mapArtist = MusicUtils.getMusicArtist(getActivity());

        adapter_artist = new ArtistListAdapter(getActivity(), mapArtist);
        lv_artist.setAdapter(adapter_artist);
        lv_artist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                String title = tv_title.getText().toString();
                MainFragment.toSongFragment(SongFragment.TAG_ARTIST, title, mapArtist.get(title));
            }
        });

        adapter_artist.notifyDataSetChanged();
        adapter_artist.notifyDataSetInvalidated();

    }

    /**
     * 设置专辑列表
     */
    private void setupAlbumList() {

        final Map<String, List<Music>> map = MusicUtils.getMusicAlbum(getActivity());
        adapter_album = new AlbumListAdapter(getActivity(), map);
        lv_album.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                String title = tv_title.getText().toString();
                MainFragment.toSongFragment(SongFragment.TAG_ALBUM, title, map.get(title));
            }
        });

        lv_album.setAdapter(adapter_album);
        adapter_album.notifyDataSetChanged();

    }

    /**
     * 设置文件夹列表
     */
    private void setupFileList() {

        final Map<String, List<Music>> map = MusicUtils.getMusicFile(getActivity());
        adapter_file = new FileListAdapter(getActivity(), map);
        lv_file.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
                String title = tv_title.getText().toString();
                TextView tv_path = (TextView) view.findViewById(R.id.tv_path);
                String path = tv_path.getText().toString();
                MainFragment.toSongFragment(SongFragment.TAG_FILE, title, map.get(path));
            }
        });

        lv_file.setAdapter(adapter_file);
        adapter_file.notifyDataSetChanged();

    }

    /**
     * 结束拖拽排序的状态
     */
    private void endDragSortStatus() {
        if (nav_menu_finish.getVisibility() == View.VISIBLE) {
            lv_song.setVisibility(View.VISIBLE);
            lv_dynamic.setVisibility(View.GONE);
            nav_menu_buttons.setVisibility(View.VISIBLE);
            nav_menu_finish.setVisibility(View.GONE);
            //TODO:处理改变顺序后lv_song的数据setDataChanged
            songs = MusicUtils.getMusic(getActivity());
            adapter_song = new SongListAdapter(getActivity(), songs);
            lv_song.setAdapter(adapter_song);

            lv_song.setSelection(PlayList.position - 3);//让正在播放的view显示在中间
            adapter_song.notifyDataSetInvalidated();
            adapter_song.notifyDataSetChanged();
        }
    }

    //-----------------------------navbar菜单相关begin-----------------------------//

    public static DialogFragment mMenuDialogFragment;
    private FragmentManager fragmentManager;

    /**
     * 初始化右上角菜单
     */
    private void initMusicMenu() {
        //step1
        MenuObject close = new MenuObject();
        close.setResource(R.drawable.ic_close_white_small);
        close.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
        close.setDividerColor(Color.rgb(255, 255, 255));

        MenuObject send1 = new MenuObject("扫描歌曲");
        send1.setResource(R.drawable.ic_scanner_white_24dp);
        send1.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
        send1.setDividerColor(Color.rgb(255, 255, 255));

        MenuObject send2 = new MenuObject("搜索歌曲");
        send2.setResource(R.drawable.ic_search_white_small);
        send2.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
        send2.setDividerColor(Color.rgb(255, 255, 255));

        MenuObject send3 = new MenuObject("播放顺序");
        if (MusicUtils.ORDER == MusicUtils.ORDER_SEQUENTIAL) {
            send3.setResource(R.drawable.ic_repeat_white_small);
        }
        if (MusicUtils.ORDER == MusicUtils.ORDER_LOOP) {
            send3.setResource(R.drawable.ic_loop_white_small);
        }
        if (MusicUtils.ORDER == MusicUtils.ORDER_CYCLE) {
            send3.setResource(R.drawable.ic_repeat_one_white_small);
        }
        if (MusicUtils.ORDER == MusicUtils.ORDER_RANDOM) {
            send3.setResource(R.drawable.ic_shuffle_white_small);
        }
        send3.setBgColor(getActivity().getResources().getColor(R.color.custom_color));
        send3.setDividerColor(Color.rgb(255, 255, 255));

        List<MenuObject> menuObjects = new ArrayList<>();
        menuObjects.add(close);
        menuObjects.add(send1);
        menuObjects.add(send2);
        menuObjects.add(send3);

        //step2
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.navbar_height));
        menuParams.setMenuObjects(menuObjects);
//        menuParams.setClosableOutside(true);
        // set other settings to meet your needs
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        fragmentManager = getActivity().getSupportFragmentManager();
    }

    public void openMenu() {
        initMusicMenu();
        mMenuDialogFragment.show(fragmentManager, ContextMenuDialogFragment.TAG);
    }
    //-----------------------------navbar菜单相关end-----------------------------//

    private ImageButton btn_back;
    private ImageButton btn_menu;
    private TextView tv_navbar_title;

    private void initHeaderView(View view) {
        btn_back = (ImageButton) view.findViewById(R.id.btn_back);
        btn_menu = (ImageButton) view.findViewById(R.id.btn_menu);
        tv_navbar_title = (TextView) view.findViewById(R.id.tv_navbar_title);
        tv_navbar_title.setText("本地音乐");
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.back();
            }
        });
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenu();
            }
        });
    }

    private void initTabListener() {
        btn_song.setOnClickListener(this);
        btn_artist.setOnClickListener(this);
        btn_album.setOnClickListener(this);
        btn_file.setOnClickListener(this);
    }

    private void findView(View view) {
        nav_menu_finish = (TextView) view.findViewById(R.id.nav_menu_finish);
        nav_menu_buttons = (LinearLayout) view.findViewById(R.id.nav_menu_buttons);
        btn_song = (TextView) view.findViewById(R.id.btn_song);
        btn_artist = (TextView) view.findViewById(R.id.btn_artist);
        btn_album = (TextView) view.findViewById(R.id.btn_album);
        btn_file = (TextView) view.findViewById(R.id.btn_file);

        lv_song = (SwipeMenuListView) view.findViewById(R.id.lv_song);
        lv_artist = (ListView) view.findViewById(R.id.lv_artist);
        lv_album = (ListView) view.findViewById(R.id.lv_album);
        lv_file = (ListView) view.findViewById(R.id.lv_file);
        lv_dynamic = (DynamicListView) view.findViewById(R.id.lv_dynamic);
    }

    /**
     * 选择（单曲，歌手，专辑，文件夹）
     *
     * @param pos
     */
    private void selectTab(int pos) {
        if (pos == 0) {
            btn_song.setBackgroundResource(R.color.custom_color);
            btn_artist.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_album.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_file.setBackgroundResource(R.drawable.bg_stroke_custom_color);

            btn_artist.setTextColor(getResources().getColor(R.color.custom_color));
            btn_album.setTextColor(getResources().getColor(R.color.custom_color));
            btn_file.setTextColor(getResources().getColor(R.color.custom_color));
            btn_song.setTextColor(getResources().getColor(R.color.white));

            lv_song.setVisibility(View.VISIBLE);
            lv_artist.setVisibility(View.GONE);
            lv_album.setVisibility(View.GONE);
            lv_file.setVisibility(View.GONE);

            setupSongList();
            //listview进入动画
            AnimUtil.listviewEnterAnim(lv_song, 0);
        }
        if (pos == 1) {
            btn_song.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_artist.setBackgroundResource(R.color.custom_color);
            btn_album.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_file.setBackgroundResource(R.drawable.bg_stroke_custom_color);

            btn_song.setTextColor(getResources().getColor(R.color.custom_color));
            btn_album.setTextColor(getResources().getColor(R.color.custom_color));
            btn_file.setTextColor(getResources().getColor(R.color.custom_color));
            btn_artist.setTextColor(getResources().getColor(R.color.white));

            lv_song.setVisibility(View.GONE);
            lv_artist.setVisibility(View.VISIBLE);
            lv_album.setVisibility(View.GONE);
            lv_file.setVisibility(View.GONE);

            setupArtistList();
            //listview进入动画
            AnimUtil.listviewEnterAnim(lv_artist, 0);
        }
        if (pos == 2) {
            btn_song.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_artist.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_album.setBackgroundResource(R.color.custom_color);
            btn_file.setBackgroundResource(R.drawable.bg_stroke_custom_color);

            btn_song.setTextColor(getResources().getColor(R.color.custom_color));
            btn_artist.setTextColor(getResources().getColor(R.color.custom_color));
            btn_file.setTextColor(getResources().getColor(R.color.custom_color));
            btn_album.setTextColor(getResources().getColor(R.color.white));

            lv_song.setVisibility(View.GONE);
            lv_artist.setVisibility(View.GONE);
            lv_album.setVisibility(View.VISIBLE);
            lv_file.setVisibility(View.GONE);

            setupAlbumList();
            //listview进入动画
            AnimUtil.listviewEnterAnim(lv_album, 0);
        }
        if (pos == 3) {
            btn_song.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_artist.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_album.setBackgroundResource(R.drawable.bg_stroke_custom_color);
            btn_file.setBackgroundResource(R.color.custom_color);

            btn_song.setTextColor(getResources().getColor(R.color.custom_color));
            btn_artist.setTextColor(getResources().getColor(R.color.custom_color));
            btn_album.setTextColor(getResources().getColor(R.color.custom_color));
            btn_file.setTextColor(getResources().getColor(R.color.white));

            lv_song.setVisibility(View.GONE);
            lv_artist.setVisibility(View.GONE);
            lv_album.setVisibility(View.GONE);
            lv_file.setVisibility(View.VISIBLE);

            setupFileList();
            //listview进入动画
            AnimUtil.listviewEnterAnim(lv_file, 0);
        }
    }

    /**
     * 初始化单曲列表子菜单及事件
     *
     * @param view
     */
    private void initSongListView(View view) {
        //初始化歌曲的子菜单
        final SwipeMenuCreator creator = getSwipeMenu();
        lv_song.setMenuCreator(creator);
        //歌曲的点击事件
        lv_song.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                MaterialRippleLayout.on(view)
//                        .rippleColor(R.color.custom_color)
//                        .rippleInAdapter(true)
//                        .create();
                SongUtils.songClickListener(view, position, songs);
            }
        });
        //歌曲的长按事件
        lv_song.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                songLongClickListener(lv_song.getFirstVisiblePosition());
                return true;//如果返回false则点击事件也会触发
            }
        });
        //歌曲的子菜单点击事件
        lv_song.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        addOrRemoveMyLike(position);
                        break;
                    case 1:
                        Dialogs.add2MusicMenuDialog(getActivity(), songs.get(position));
                        break;
                    case 2:
                        Dialogs.musicInfoDialog(getActivity(), songs.get(position));
                        break;
                    case 3:
                        Dialogs.currentUI = MusicService.UI_LOCALMUSIC_SONG;
                        Dialogs.deleteMusicDialog(getActivity(), songs.get(position), position);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });

        // 子菜单显示的滑动方向
        lv_song.setSwipeDirection(SwipeMenuListView.DIRECTION_LEFT);
    }

    /**
     * 长按单曲的事件
     *
     * @param position
     */
    private void songLongClickListener(int position) {
        initDragView(position);
        lv_song.setVisibility(View.GONE);
        lv_dynamic.setVisibility(View.VISIBLE);
        nav_menu_buttons.setVisibility(View.GONE);
        nav_menu_finish.setVisibility(View.VISIBLE);
        nav_menu_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(END_DRAG_SORT_STATUS);
            }
        });
    }

    /**
     * 初始化单曲拖拽排序的功能
     *
     * @param position
     */
    private void initDragView(int position) {
        SongListDragAdapter adapter_drag = new SongListDragAdapter(getActivity(), songs);
        /* Setup the adapter */
        SimpleSwipeUndoAdapter simpleSwipeUndoAdapter = new SimpleSwipeUndoAdapter(adapter_drag, getActivity(), null);
        AlphaInAnimationAdapter animAdapter = new AlphaInAnimationAdapter(simpleSwipeUndoAdapter);
        animAdapter.setAbsListView(lv_dynamic);
        assert animAdapter.getViewAnimator() != null;
        animAdapter.getViewAnimator().setInitialDelayMillis(300);
        lv_dynamic.setAdapter(animAdapter);

        lv_dynamic.enableDragAndDrop();
        lv_dynamic.setDraggableManager(new TouchViewDraggableManager(R.id.drag_touchview));
        lv_dynamic.setOnItemMovedListener(new MyOnItemMovedListener(adapter_drag));
        lv_dynamic.setOnItemLongClickListener(new MyOnItemLongClickListener(lv_dynamic));

        //定位到点击的item
        lv_dynamic.setSelection(position);
        adapter_drag.notifyDataSetInvalidated();
        adapter_drag.notifyDataSetChanged();
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        Toast.makeText(getActivity(), "点击了菜单位置：" + position, 0).show();
    }

    private class MyOnItemLongClickListener implements AdapterView.OnItemLongClickListener {

        private final DynamicListView mListView;

        MyOnItemLongClickListener(final DynamicListView listView) {
            mListView = listView;
        }

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            if (mListView != null) {
                mListView.startDragging(position - mListView.getHeaderViewsCount());
            }
            return true;
        }
    }

    private class MyOnItemMovedListener implements OnItemMovedListener {

        private final SongListDragAdapter mAdapter;

        MyOnItemMovedListener(final SongListDragAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onItemMoved(final int originalPosition, final int newPosition) {
            songs = MusicUtils.exchangeMusicPosition(getActivity(), songs, originalPosition, newPosition);
            mAdapter.notifyDataSetChanged();
        }

    }

    private SwipeMenuCreator getSwipeMenu() {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem likeItem = new SwipeMenuItem(getActivity());
                likeItem.setBackground(R.color.menu_color1);
                likeItem.setWidth(AnimUtil.dp2px(getActivity(), 80));
                switch (menu.getViewType()) {
                    case 0:
                        likeItem.setTitle("喜欢");
                        likeItem.setIcon(R.drawable.ic_favorite_outline_white_small);
                        break;
                    case 1:
                        likeItem.setTitle("取消喜欢");
                        likeItem.setIcon(R.drawable.ic_favorite_white_small);
                        break;
                }
                likeItem.setTitleSize(12);
                likeItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(likeItem);

                SwipeMenuItem musicMenuItem = new SwipeMenuItem(getActivity());
                musicMenuItem.setBackground(R.color.menu_color2);
                musicMenuItem.setWidth(AnimUtil.dp2px(getActivity(), 80));
                musicMenuItem.setTitle("加入歌单");
                musicMenuItem.setIcon(R.drawable.ic_playlist_add_white_small);
                musicMenuItem.setTitleSize(12);
                musicMenuItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(musicMenuItem);

                SwipeMenuItem infoItem = new SwipeMenuItem(getActivity());
                infoItem.setBackground(R.color.menu_color3);
                infoItem.setWidth(AnimUtil.dp2px(getActivity(), 80));
                infoItem.setTitle("歌曲信息");
                infoItem.setIcon(R.drawable.ic_info_outline_white_small);
                infoItem.setTitleSize(12);
                infoItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(infoItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
                deleteItem.setBackground(R.color.menu_color4);
                deleteItem.setWidth(AnimUtil.dp2px(getActivity(), 80));
                deleteItem.setTitle("删除");
                deleteItem.setIcon(R.drawable.ic_delete_white_small);
                deleteItem.setTitleSize(12);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };

        return creator;
    }

    /**
     * 添加或移除我喜欢的
     *
     * @param position
     */
    private void addOrRemoveMyLike(int position) {
        int myLike = Dialogs.add2LikeDialog(getActivity(), songs.get(position));
        songs.get(position).setMyLike(myLike);
        //如果播放列表有这首歌曲，则也更新喜欢状态
        for (int i = 0; i < PlayList.musics.size(); i++) {
            if (PlayList.musics.get(i).getId().equals(songs.get(position).getId())) {
                PlayList.musics.get(i).setMyLike(myLike);
            }
        }
        //TODO:刷新菜单状态
        int firstVisiblePosition = lv_song.getFirstVisiblePosition();
        adapter_song = new SongListAdapter(getActivity(), songs);
        lv_song.setAdapter(adapter_song);
        lv_song.setSelection(firstVisiblePosition);
        adapter_song.notifyDataSetInvalidated();
    }

    ;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_song:
                selectTab(0);
                break;
            case R.id.btn_artist:
                selectTab(1);
                break;
            case R.id.btn_album:
                selectTab(2);
                break;
            case R.id.btn_file:
                selectTab(3);
                break;
        }
    }
}
