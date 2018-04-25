package com.nicmic.gatherhear.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.service.MusicService;

import java.util.List;

import io.gresse.hugo.vumeterlibrary.VuMeterView;

/**
 * Created by Administrator on 2015/9/26.
 * 使用步骤：
 * 1.设置单曲的点击事件，调用songClickListener(),传入被点击的view，被点击的view的position，该UI中的歌曲集合，UI的标识
 * 2.在handler中更新单曲界面，由MusicService的updateUI判断并发送handler
    SongUtils.updatePlayingView(adapter_song, lv_song);
    SongUtils.switchEqualizerStatus();
 */
public class SongUtils {

    /**
     * 切换均衡器状态，播放状态或暂停状态
     */
    public static void switchEqualizerStatus(View view){
        VuMeterView iv_equalizer = (VuMeterView) view.findViewById(R.id.iv_equalizer);
        if (MusicService.player.isPlaying()){
            iv_equalizer.resume(true);
        }else{
            iv_equalizer.pause();
        }
    }

    /**
     * 点击单曲的事件
     * @param view 被点击的view
     * @param position 被点击的view的位置
     * @param songs 调用此方法UI中的歌曲列表
     */
    public static void songClickListener(View view, int position, List<Music> songs) {

        String musicId = (String) view.findViewById(R.id.tv_id).getTag();
        Music playingMusic = PlayList.getPlayingMusic();
        //2.1点击的是正在播放的音乐
        if (playingMusic != null && playingMusic.getId().equals(musicId)) {
            MusicService.playOrPause();
            switchEqualizerStatus(view);
        }else {//2.2点击的不是正在播放的音乐
            PlayList.setPlayList(songs, position);
            MusicService.play();
        }

    }

    /**
     * 获得单个歌曲的滑动菜单
     * @param context
     * @return
     */
    public static SwipeMenuCreator getSwipeMenu(final Context context) {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem likeItem = new SwipeMenuItem(context);
                likeItem.setBackground(R.color.menu_color1);
                likeItem.setWidth(AnimUtil.dp2px(context, 80));
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

                SwipeMenuItem musicMenuItem = new SwipeMenuItem(context);
                musicMenuItem.setBackground(R.color.menu_color2);
                musicMenuItem.setWidth(AnimUtil.dp2px(context, 80));
                musicMenuItem.setTitle("加入歌单");
                musicMenuItem.setIcon(R.drawable.ic_playlist_add_white_small);
                musicMenuItem.setTitleSize(12);
                musicMenuItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(musicMenuItem);

                SwipeMenuItem infoItem = new SwipeMenuItem(context);
                infoItem.setBackground(R.color.menu_color3);
                infoItem.setWidth(AnimUtil.dp2px(context, 80));
                infoItem.setTitle("歌曲信息");
                infoItem.setIcon(R.drawable.ic_info_outline_white_small);
                infoItem.setTitleSize(12);
                infoItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(infoItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(context);
                deleteItem.setBackground(R.color.menu_color4);
                deleteItem.setWidth(AnimUtil.dp2px(context, 80));
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
     * 获得歌单中歌曲的滑动菜单
     * 多了一个移出歌单
     * @param context
     * @return
     */
    public static SwipeMenuCreator getMusicMenuSwipeMenu(final Context context) {
        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem likeItem = new SwipeMenuItem(context);
                likeItem.setBackground(R.color.menu_color1);
                likeItem.setWidth(AnimUtil.dp2px(context, 80));
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

                SwipeMenuItem musicMenuItem = new SwipeMenuItem(context);
                musicMenuItem.setBackground(R.color.menu_color2);
                musicMenuItem.setWidth(AnimUtil.dp2px(context, 80));
                musicMenuItem.setTitle("加入歌单");
                musicMenuItem.setIcon(R.drawable.ic_playlist_add_white_small);
                musicMenuItem.setTitleSize(12);
                musicMenuItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(musicMenuItem);

                SwipeMenuItem infoItem = new SwipeMenuItem(context);
                infoItem.setBackground(R.color.menu_color3);
                infoItem.setWidth(AnimUtil.dp2px(context, 80));
                infoItem.setTitle("歌曲信息");
                infoItem.setIcon(R.drawable.ic_info_outline_white_small);
                infoItem.setTitleSize(12);
                infoItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(infoItem);

                SwipeMenuItem removeItem = new SwipeMenuItem(context);
                removeItem.setBackground(R.color.gray);
                removeItem.setWidth(AnimUtil.dp2px(context, 80));
                removeItem.setTitle("移出歌单");
                removeItem.setIcon(R.drawable.ic_delete_white_small);
                removeItem.setTitleSize(12);
                removeItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(removeItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(context);
                deleteItem.setBackground(R.color.menu_color4);
                deleteItem.setWidth(AnimUtil.dp2px(context, 80));
                deleteItem.setTitle("删除");
                deleteItem.setIcon(R.drawable.ic_delete_white_small);
                deleteItem.setTitleSize(12);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);
            }
        };

        return creator;
    }

}
