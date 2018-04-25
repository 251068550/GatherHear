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
import com.nicmic.gatherhear.utils.ImageUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/9/22.
 */
public class AlbumListAdapter extends BaseAdapter {

    private Context context;
    private Map<String, List<Music>> map;
    private List<String> albums;

    public AlbumListAdapter(Context context, Map<String, List<Music>> map){
        this.context = context;
        this.map = map;
        //将map中的所有键放入集合中
        albums = new ArrayList<>();
        for (Map.Entry entry : map.entrySet()) {
            String album = (String) entry.getKey();
            albums.add(album);
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_album, null);
            holder.iv_profile = (ImageView) convertView.findViewById(R.id.iv_profile);
            holder.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tv_num = (TextView) convertView.findViewById(R.id.tv_num);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        String album = albums.get(position);
        List<Music> musics = map.get(album);

        holder.tv_title.setText(album);
        holder.tv_num.setText(musics.size() + "首");

        String uri = ImageUtils.getArtworkUri(context, musics.get(0).getSongId(), musics.get(0).getAlbumId(), true);
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.displayImage(uri, holder.iv_profile);

        return convertView;
    }

    class ViewHolder{
        ImageView iv_profile;
        TextView tv_title;
        TextView tv_num;
    }

}
