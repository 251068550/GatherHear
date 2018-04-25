package com.nicmic.gatherhear.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.MusicMenu;
import com.nicmic.gatherhear.utils.Dialogs;

import java.util.List;

/**
 * Created by Administrator on 2015/9/19.
 */
public class MusicMenuListAdapter extends BaseAdapter{

    private Context context;
    private List<MusicMenu> list;

    public MusicMenuListAdapter(Context context, List list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public MusicMenu getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode() ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_music_menu, null);
            holder.music_menu_cover = (ImageView) convertView.findViewById(R.id.music_menu_cover);
            holder.music_menu_title = (TextView) convertView.findViewById(R.id.music_menu_title);
            holder.music_menu_desc = (TextView) convertView.findViewById(R.id.music_menu_desc);
            holder.music_menu_num = (TextView) convertView.findViewById(R.id.music_menu_num);
            holder.music_menu_edit = (ImageView) convertView.findViewById(R.id.music_menu_edit);
            holder.music_menu_add = (ImageView) convertView.findViewById(R.id.music_menu_add);
            holder.music_menu_delete = (ImageView) convertView.findViewById(R.id.music_menu_delete);

            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final MusicMenu musicMenu = getItem(position);
        //用于传递给详细页面数据
        holder.music_menu_cover.setTag(musicMenu);

        holder.music_menu_cover.setImageResource(R.drawable.lollipop2);

        holder.music_menu_title.setText(musicMenu.getTitle());
        holder.music_menu_desc.setText(musicMenu.getDesc());
        holder.music_menu_num.setText(musicMenu.getMusics().size() + "");
        holder.music_menu_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.modifyMusicMenuDialog(context, musicMenu);
            }
        });
        holder.music_menu_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.chooseMusicDialog(context, musicMenu);
            }
        });
        holder.music_menu_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.deleteMusicMenu(context, musicMenu);
            }
        });

        return convertView;
    }

    class ViewHolder{
        ImageView music_menu_cover;
        TextView music_menu_title;
        TextView music_menu_desc;
        TextView music_menu_num;
        ImageView music_menu_edit;
        ImageView music_menu_add;
        ImageView music_menu_delete;
    }

}
