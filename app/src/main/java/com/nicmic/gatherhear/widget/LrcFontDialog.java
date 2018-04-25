package com.nicmic.gatherhear.widget;

import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.lrc.LrcView;
import com.nicmic.gatherhear.utils.LrcUtils;
import com.flyco.dialog.widget.base.BaseDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/10/29.
 */
public class LrcFontDialog extends BaseDialog implements View.OnClickListener{
    /**
     * method execute order:
     * show:constrouctor---show---oncreate---onStart---onAttachToWindow
     * dismiss:dismiss---onDetachedFromWindow---onStop
     *
     * @param context
     */
    public LrcFontDialog(Context context) {
        super(context);
    }

    private SeekBar seekbar_font_size;
    private ImageView pink_dark;
    private ImageView pink_light;
    private ImageView green_dark;
    private ImageView green_light;
    private ImageView orange_dark;
    private ImageView orange_light;
    private ImageView yellow_dark;
    private ImageView yellow_light;
    private ImageView purple_dark;
    private ImageView purple_light;
    private ImageView blue_dark;
    private ImageView blue_light;
    private ImageView grey_dark;
    private ImageView grey_light;
    private ImageView black;
    List<ImageView> colors;

    @Override
    public View onCreateView() {
        View view = View.inflate(context, R.layout.dialog_lrc_font, null);
        seekbar_font_size = (SeekBar) view.findViewById(R.id.seekbar_font_size);
        pink_dark = (ImageView) view.findViewById(R.id.pink_dark);
        pink_light = (ImageView) view.findViewById(R.id.pink_light);
        green_dark = (ImageView) view.findViewById(R.id.green_dark);
        green_light = (ImageView) view.findViewById(R.id.green_light);
        orange_dark = (ImageView) view.findViewById(R.id.orange_dark);
        orange_light = (ImageView) view.findViewById(R.id.orange_light);
        yellow_dark = (ImageView) view.findViewById(R.id.yellow_dark);
        yellow_light = (ImageView) view.findViewById(R.id.yellow_light);
        purple_dark = (ImageView) view.findViewById(R.id.purple_dark);
        purple_light = (ImageView) view.findViewById(R.id.purple_light);
        blue_dark = (ImageView) view.findViewById(R.id.blue_dark);
        blue_light = (ImageView) view.findViewById(R.id.blue_light);
        grey_dark = (ImageView) view.findViewById(R.id.grey_dark);
        grey_light = (ImageView) view.findViewById(R.id.grey_light);
        black = (ImageView) view.findViewById(R.id.black);

        colors = new ArrayList<>();
        colors.add(pink_dark);
        colors.add(pink_light);
        colors.add(green_dark);
        colors.add(green_light);
        colors.add(orange_dark);
        colors.add(orange_light);
        colors.add(yellow_dark);
        colors.add(yellow_light);
        colors.add(purple_dark);
        colors.add(purple_light);
        colors.add(blue_dark);
        colors.add(blue_light);
        colors.add(grey_dark);
        colors.add(grey_light);
        colors.add(black);

        return view;
    }

    @Override
    public boolean setUiBeforShow() {
        //设置当前歌词的字体大小
        int size = LrcUtils.getFontSize();
        Message message1 = Message.obtain();
        message1.what = LrcView.UPDATE_FONT_SIZE;
        message1.arg1 = size;
        if (LrcView.staticHandler != null) {
            LrcView.staticHandler.sendMessage(message1);
        }
        //设置当前歌词的颜色
        int color = LrcUtils.getFontColor();
        Message message2 = Message.obtain();
        message2.what = LrcView.UPDATE_FONT_COLOR;
        message2.arg1 = color;
        if (LrcView.staticHandler != null) {
            LrcView.staticHandler.sendMessage(message2);
        }
        //设置seekbar的滑动监听事件
        seekbar_font_size.setMax(20);
        seekbar_font_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = progress + 15;
                Message message1 = Message.obtain();
                message1.what = LrcView.UPDATE_FONT_SIZE;
                message1.arg1 = size;
                if (LrcView.staticHandler != null) {
                    LrcView.staticHandler.sendMessage(message1);
                }
                //保存歌词字体大小的值
                LrcUtils.saveFontSize(size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //设置当前进度条的值
        seekbar_font_size.setProgress(size - 15);
        //设置每个颜色面板的点击事件
        for (int i = 0; i < colors.size(); i++) {
            colors.get(i).setOnClickListener(this);
            //设置当前歌词颜色选中的面板
            if (Color.parseColor((String) colors.get(i).getTag()) == color) {
                colors.get(i).setImageResource(R.drawable.ic_done_white);
            }else {
                colors.get(i).setImageResource(R.color.transparent);
            }
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        //以下方法并没有重绘dialog，因为dialog已经创建了
        ImageView color_view = (ImageView) v;
        for (int i = 0; i < colors.size(); i++) {
            if (color_view.getTag() == colors.get(i).getTag()){
                color_view.setImageResource(R.drawable.ic_done_white);
            }else {
                color_view.setImageResource(R.color.transparent);
            }
        }
        //保存歌词颜色值
        LrcUtils.saveFontColor(Color.parseColor((String) color_view.getTag()));
        //设置当前歌词的颜色打勾
        int color = LrcUtils.getFontColor();
        Message message2 = Message.obtain();
        message2.what = LrcView.UPDATE_FONT_COLOR;
        message2.arg1 = color;
        if (LrcView.staticHandler != null) {
            LrcView.staticHandler.sendMessage(message2);
        }

        dismiss();
    }
}
