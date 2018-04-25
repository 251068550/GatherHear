package com.nicmic.gatherhear.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.adapter.SongListAdapter;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.Dialogs;
import com.nicmic.gatherhear.utils.SongUtils;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment {

    private static SongFragment songFragment;//单例

    private SongListAdapter adapter;
    private SwipeMenuListView lv;
    private SwipeMenuCreator swipeMenu;
    public String title;
    public static List<Music> musics;

    //用于更新当前界面的数据
    public int TAG = -1;
    public static final int TAG_ARTIST = 0;
    public static final int TAG_ALBUM = 1;
    public static final int TAG_FILE = 2;
    public static final int TAG_MY_LIKE = 3;
    public static final int TAG_RECENT_PLAY = 4;
    public static final int TAG_LOCAL_MUSIC = 5;

    public static final int UPDATE_UI = 10;
    public static final int RESET_UI = 11;
    public static Handler staticHandler;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_UI){
                updateUI();
            }
            if (msg.what == RESET_UI) {
                if (msg.arg1 >= 0) {//删除歌曲传回来的被删除歌曲的位置
                    musics.remove(msg.arg1);
                    if (msg.arg1 < PlayList.position){//删除的歌曲在正在播放歌曲的前面
                        PlayList.position = PlayList.position - 1;
                    }
                }
                resetUI();
            }
        }
    };

    private void resetUI() {
        adapter = new SongListAdapter(getActivity(), musics);
        lv.setAdapter(adapter);
    }

    private void updateUI() {
        if (adapter != null){
            adapter.notifyDataSetChanged();
        }
    }

    public SongFragment() {
        staticHandler = handler;
    }

    public static SongFragment getInstance(){
        if (songFragment == null){
            songFragment = new SongFragment();
        }
        return songFragment;
    }

    public int getTAG() {
        return TAG;
    }

    public void setTAG(int tag){
        this.TAG = tag;
    }

    public void setData(int tag, String title, List<Music> musics){
        TAG = tag;
        this.title = title;
        this.musics = musics;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        //刷新数据
        if (!hidden){
            switch (TAG){
                case TAG_ARTIST:
                case TAG_ALBUM:
                case TAG_FILE:
                    refresh();
                    break;
                case TAG_MY_LIKE:
                    refresh();
                    break;
                case TAG_RECENT_PLAY:
                    refresh();
                    break;
                case TAG_LOCAL_MUSIC:
                    refresh();
                    break;
            }
        }
    }

    /**
     * 刷新数据
     */
    private void refresh() {
        if (TAG == TAG_MY_LIKE || TAG == TAG_RECENT_PLAY) {
            btn_menu.setVisibility(View.VISIBLE);
            btn_menu.setBackgroundResource(R.drawable.btn_delete_white);
            btn_menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (TAG == TAG_MY_LIKE) {
                        Dialogs.clearMyLike(getActivity());
                    }
                    if (TAG == TAG_RECENT_PLAY) {
                        Dialogs.clearRecentPlay(getActivity());
                    }
                }
            });
        }
        else {
            btn_menu.setVisibility(View.INVISIBLE);
        }
        tv_navbar_title.setText(title);
        adapter = new SongListAdapter(getActivity(), musics);
        lv.setAdapter(adapter);

        //listview进入动画
        AnimUtil.listviewEnterAnim(lv, AnimUtil.DELAY);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initHeaderView(view);
        findView();
        initListView();

    }

    private void initListView() {
        lv.setMenuCreator(SongUtils.getSwipeMenu(getActivity()));
        lv.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        addOrRemoveMyLike(position);
                        break;
                    case 1:
                        Dialogs.add2MusicMenuDialog(getActivity(), musics.get(position));
                        break;
                    case 2:
                        Dialogs.musicInfoDialog(getActivity(), musics.get(position));
                        break;
                    case 3:
                        Dialogs.currentUI = MusicService.UI_SONG_FRAGMENT;
                        Dialogs.deleteMusicDialog(getActivity(), musics.get(position), position);
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SongUtils.songClickListener(view, position, musics);
            }
        });
    }

    /**
     * 添加或移除我喜欢的
     * @param position
     */
    private void addOrRemoveMyLike(int position){
        int myLike = Dialogs.add2LikeDialog(getActivity(), musics.get(position));
        musics.get(position).setMyLike(myLike);
        //如果播放列表有这首歌曲，则也更新喜欢状态
        for (int i = 0; i < PlayList.musics.size(); i++) {
            if (PlayList.musics.get(i).getId().equals(musics.get(position).getId())){
                PlayList.musics.get(i).setMyLike(myLike);
            }
        }
        if (TAG == TAG_MY_LIKE) {//如果是我喜欢的界面，则需要移除该歌曲
            musics.remove(position);
        }
        //TODO:刷新菜单状态
        int firstVisiblePosition = lv.getFirstVisiblePosition();
        adapter = new SongListAdapter(getActivity(), musics);
        lv.setAdapter(adapter);
        lv.setSelection(firstVisiblePosition);
        adapter.notifyDataSetInvalidated();
    };

    private void findView() {
        lv = (SwipeMenuListView) getActivity().findViewById(R.id.lv_mylike);
    }

    private ImageButton btn_back;
    private ImageButton btn_menu;
    private TextView tv_navbar_title;
    private void initHeaderView(View view) {
        //必须传入参数view，getActivity获取的不一定是当前fragment（原因不明）
        btn_back = (ImageButton) view.findViewById(R.id.btn_back);
        btn_menu = (ImageButton) view.findViewById(R.id.btn_menu);
        tv_navbar_title = (TextView) view.findViewById(R.id.tv_navbar_title);

        tv_navbar_title.setText("我喜欢的");
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainFragment.back();
            }
        });
        btn_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TAG == TAG_MY_LIKE) {
                    Dialogs.clearMyLike(getActivity());
                }
                if (TAG == TAG_RECENT_PLAY) {
                    Dialogs.clearRecentPlay(getActivity());
                }
            }
        });
    }

}
