package com.nicmic.gatherhear.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.Music;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/9/22.
 */
public class FileListAdapter extends BaseAdapter {

    private Context context;
    private Map<String, List<Music>> map;
    private List<String> files;

    public FileListAdapter(Context context, Map<String, List<Music>> map){
        this.context = context;
        this.map = map;
        //将map中的所有键放入集合中
        files = new ArrayList<>();
        for (Map.Entry entry : map.entrySet()) {
            String album = (String) entry.getKey();
            files.add(album);
        }
    }

    @Override
    public int getCount() {
        return map.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_file, null);
            holder.iv_profile = (ImageView) convertView.findViewById(R.id.iv_profile);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tv_path = (TextView) convertView.findViewById(R.id.tv_path);
            holder.tv_num = (TextView) convertView.findViewById(R.id.tv_num);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        String file = files.get(position);
        List<Music> musics = map.get(file);
        if (file.contains("kgmusic/download/")){
            holder.tv_title.setText("酷狗音乐");
        }else if (file.contains("ttpod/song/")) {
            holder.tv_title.setText("天天动听");
        }else if (file.contains("qqmusic/song/")) {
            holder.tv_title.setText("QQ音乐");
        }else {
            String[] dirs = file.split("/");
            String title = dirs[dirs.length - 1];
            holder.tv_title.setText(title);
        }
        holder.tv_path.setText(file);
        holder.tv_num.setText(musics.size() + "首");

        return convertView;
    }

    class ViewHolder{
        ImageView iv_profile;
        TextView tv_title;
        TextView tv_path;
        TextView tv_num;
    }

}
