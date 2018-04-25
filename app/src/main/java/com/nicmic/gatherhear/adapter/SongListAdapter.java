package com.nicmic.gatherhear.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.service.MusicService;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import java.util.List;

import io.gresse.hugo.vumeterlibrary.VuMeterView;

/**
 * Created by Administrator on 2015/9/19.
 */
public class SongListAdapter extends BaseAdapter implements UndoAdapter{

    private Context context;
    private List<Music> list;

    public SongListAdapter(Context context, List list){
        this.context = context;
        this.list = list;
    }

    @Override
    public boolean hasStableIds() {
        return true;
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
        return getItem(position).hashCode() ;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_music, null);
            holder.item_music = (LinearLayout) convertView.findViewById(R.id.item_music);
            holder.iv_equalizer = (VuMeterView) convertView.findViewById(R.id.iv_equalizer);
            holder.tv_id = (TextView) convertView.findViewById(R.id.tv_id);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tv_artist = (TextView) convertView.findViewById(R.id.tv_artist);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final Music music = getItem(position);
        //设置音乐信息

        holder.tv_id.setText((position + 1) + "");
        holder.tv_id.setTag(music.getId());//设置当前音乐的真实id
        holder.tv_title.setText(music.getTitle());
        holder.tv_artist.setText(music.getArtist());

        Music playingMusic = PlayList.getPlayingMusic();
        //2.有正在播放的音乐，判断当前播放的音乐是否在当前列表中，是的话把当前item设置为播放状态
        if (playingMusic != null && playingMusic.getId() != null && playingMusic.getId().equals(music.getId())){
            Log.e("SongListAdapter", "正在播放的item是：" + music.getTitle());
//            holder.item_music.setBackgroundResource(R.color.custom_color);
            holder.iv_equalizer.setVisibility(View.VISIBLE);
            holder.tv_id.setVisibility(View.GONE);

            //刷新均衡器状态
            if (MusicService.player.isPlaying()){
                holder.iv_equalizer.resume(true);
            }else{
                holder.iv_equalizer.pause();
            }
        }else{
//            holder.item_music.setBackgroundResource(R.color.white);
            holder.iv_equalizer.setVisibility(View.GONE);
            holder.tv_id.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    @NonNull
    @Override
    public View getUndoView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.undo_row, parent, false);
        }
        return view;
    }

    @NonNull
    @Override
    public View getUndoClickView(@NonNull View view) {
        return view.findViewById(R.id.undo_row_undobutton);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getMyLike();
    }

    class ViewHolder{
        LinearLayout item_music;
        VuMeterView iv_equalizer;
        TextView tv_id;
        TextView tv_title;
        TextView tv_artist;
    }

}
