package com.nicmic.gatherhear.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.Music;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.GripView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import java.util.List;

/**
 * Created by Administrator on 2015/9/20.
 */
public class SongListDragAdapter extends ArrayAdapter<Music> implements UndoAdapter {

    private Context context;

    public SongListDragAdapter(Context context, List<Music> list) {
        this.context = context;

        for (int i = 0; i < list.size(); i++){
            add(i, list.get(i));
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_music, null);
            holder.drag_touchview = (GripView) convertView.findViewById(R.id.drag_touchview);
            holder.tv_id = (TextView) convertView.findViewById(R.id.tv_id);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tv_artist = (TextView) convertView.findViewById(R.id.tv_artist);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.drag_touchview.setVisibility(View.VISIBLE);

        Music music = getItem(position);

        holder.tv_id.setText(position + 1 + "");
        holder.tv_title.setText(music.getTitle());
        holder.tv_artist.setText(music.getArtist());

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

    class ViewHolder{
        GripView drag_touchview;
        TextView tv_id;
        TextView tv_title;
        TextView tv_artist;
    }

}
