package com.nicmic.gatherhear.widget;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.utils.Dialogs;
import com.nicmic.gatherhear.utils.ImageUtils;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.flyco.dialog.utils.CornerUtils;
import com.flyco.dialog.widget.base.BaseDialog;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by Administrator on 2015/10/3.
 */
public class MusicInfoDialog extends BaseDialog {
    /**
     * method execute order:
     * show:constrouctor---show---oncreate---onStart---onAttachToWindow
     * dismiss:dismiss---onDetachedFromWindow---onStop
     *
     * @param context
     */
    public MusicInfoDialog(Context context) {
        super(context);
    }

    private Music music;
    private CircularImageView iv_profile;
    private TextView tv_title;
    private TextView tv_artist;
    private TextView tv_album;
    private TextView tv_duration;
    private TextView tv_size;
    private TextView tv_path;

    private ImageView btn_edit;

    public MusicInfoDialog(Context context, Music music){
        super(context);
        this.music = music;
    }

    @Override
    public View onCreateView() {
        View view = View.inflate(context, R.layout.dialog_music_info, null);
        iv_profile = (CircularImageView) view.findViewById(R.id.iv_profile);
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_artist = (TextView) view.findViewById(R.id.tv_artist);
        tv_album = (TextView) view.findViewById(R.id.tv_album);
        tv_duration = (TextView) view.findViewById(R.id.tv_duration);
        tv_size = (TextView) view.findViewById(R.id.tv_size);
        tv_path = (TextView) view.findViewById(R.id.tv_path);
        btn_edit = (ImageView) view.findViewById(R.id.btn_edit);
        view.setBackgroundDrawable(CornerUtils.cornerDrawable(Color.parseColor("#ffffff"), dp2px(10)));
        return view;
    }

    @Override
    public boolean setUiBeforShow() {
        String uri = ImageUtils.getArtworkUri(context, music.getSongId(), music.getAlbumId(), true);
        ImageLoader.getInstance().displayImage(uri, iv_profile);

        tv_title.setText(music.getTitle());
        tv_artist.setText("歌手：" + music.getArtist());
        tv_album.setText("专辑：" + music.getAlbum());
        tv_duration.setText("时长：" + MusicUtils.getTimeString(Long.parseLong(music.getDuration())));
        tv_size.setText("大小：" + MusicUtils.convertFileSize(Long.parseLong(music.getSize())));
        tv_path.setText("路径：" + music.getPath());
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialogs.modifyMusicInfoDialog(context, music);
                dismiss();
            }
        });
        return true;
    }


}
