package com.nicmic.gatherhear.adapter;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.fragment.LocalMusicFragment;
import com.nicmic.gatherhear.fragment.MusicMenuFragment;
import com.nicmic.gatherhear.fragment.SongFragment;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.Dialogs;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.nicmic.gatherhear.utils.SongUtils;

import java.util.List;

import io.gresse.hugo.vumeterlibrary.VuMeterView;

/**
 * Created by Administrator on 2015/9/19.
 */
public class PlayListAdapter extends BaseAdapter {

    private Context context;
    private List<Music> list;

    public PlayListAdapter(Context context, List list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Music getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_playlist, null);
            holder.item_music = (LinearLayout) convertView.findViewById(R.id.item_music);
            holder.iv_equalizer = (VuMeterView) convertView.findViewById(R.id.iv_equalizer);
            holder.tv_id = (TextView) convertView.findViewById(R.id.tv_id);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tv_artist = (TextView) convertView.findViewById(R.id.tv_artist);
            holder.delete_view = (RelativeLayout) convertView.findViewById(R.id.delete_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Music music = getItem(position);

        holder.tv_id.setText((position + 1) + "");
        holder.tv_id.setTag(music.getId());
        holder.tv_title.setText(music.getTitle());
        holder.tv_artist.setText(music.getArtist());
        holder.delete_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //从播放列表中移除该歌曲
                MusicUtils.removePlayListMusic(context, music);
                //更新播放列表的UI
                updatePlayListUI(position);
                //更新UI(本地音乐，歌单，歌曲界面)
                updateOtherUI(position);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SongUtils.songClickListener(v, position, PlayList.musics);
            }
        });

        Music playingMusic = PlayList.getPlayingMusic();
        //2.有正在播放的音乐，判断当前播放的音乐是否在当前列表中，是的话把当前item设置为播放状态
        if (playingMusic != null && playingMusic.getId().equals(music.getId())) {
            Log.e("SongListAdapter", "正在播放的item是：" + music.getTitle());
//            holder.item_music.setBackgroundResource(R.color.custom_color);
            holder.iv_equalizer.setVisibility(View.VISIBLE);
            holder.tv_id.setVisibility(View.GONE);

            //刷新均衡器状态
            if (MusicService.player.isPlaying()) {
                holder.iv_equalizer.resume(true);
            } else {
                holder.iv_equalizer.pause();
            }
        } else {
//            holder.item_music.setBackgroundResource(R.color.transparent);
            holder.iv_equalizer.setVisibility(View.GONE);
            holder.tv_id.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    class ViewHolder {
        LinearLayout item_music;
        VuMeterView iv_equalizer;
        TextView tv_id;
        TextView tv_title;
        TextView tv_artist;
        RelativeLayout delete_view;
    }

    private void updatePlayListUI(int position) {
        if (position < PlayList.position) {//删除的歌曲在正在播放歌曲的前面
            PlayList.position = PlayList.position - 1;
        }
        notifyDataSetChanged();
    }

    public static void updateOtherUI(int position) {
        if (Dialogs.currentUI == MusicService.UI_LOCALMUSIC_SONG) {
            Message msg = new Message();
            msg.what = LocalMusicFragment.RESET_UI;
            msg.arg1 = position;
            LocalMusicFragment.staticHandler.sendMessage(msg);
        }
        if (Dialogs.currentUI == MusicService.UI_SONG_FRAGMENT) {
            Message msg = new Message();
            msg.what = SongFragment.RESET_UI;
            msg.arg1 = position;
            SongFragment.staticHandler.sendMessage(msg);
        }
        if (Dialogs.currentUI == MusicService.UI_MUSIC_MENU) {
            Message msg = new Message();
            msg.what = MusicMenuFragment.RESET_UI;
            msg.arg1 = position;
            MusicMenuFragment.staticHandler.sendMessage(msg);
        }
    }

}
