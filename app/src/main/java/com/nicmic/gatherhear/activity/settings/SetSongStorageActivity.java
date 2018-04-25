package com.nicmic.gatherhear.activity.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.activity.BaseActivity;
import com.nicmic.gatherhear.utils.FileUtils;
import com.r0adkll.slidr.Slidr;
import com.rey.material.widget.CheckBox;

public class SetSongStorageActivity extends BaseActivity {

    private LinearLayout inner_sd_card;
    private TextView tv_inner_total_size, tv_inner_available_size;
    private CheckBox checkbox_inner;

    private LinearLayout ext_sd_card;
    private TextView tv_ext_total_size, tv_ext_available_size;
    private CheckBox checkbox_ext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_song_storage);
        Slidr.attach(this);

        findView();
        initHead();
        //初始化数据
        setData();
        //设置监听事件
        setListener();
    }

    private void setListener() {
        checkbox_inner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox_inner.setChecked(true);
                checkbox_ext.setChecked(false);
            }
        });
        checkbox_ext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox_ext.setChecked(true);
                checkbox_inner.setChecked(false);
            }
        });
        inner_sd_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox_inner.setChecked(true);
                checkbox_ext.setChecked(false);
            }
        });
        ext_sd_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkbox_ext.setChecked(true);
                checkbox_inner.setChecked(false);
            }
        });
        checkbox_inner.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FileUtils.saveAppFilePath(FileUtils.SD_CARD_INNER);
                }
            }
        });
        checkbox_ext.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    FileUtils.saveAppFilePath(FileUtils.SD_CARD_EXT);
                }
            }
        });
    }

    private void findView() {
        inner_sd_card = (LinearLayout) findViewById(R.id.inner_sd_card);
        tv_inner_total_size = (TextView) findViewById(R.id.tv_inner_total_size);
        tv_inner_available_size = (TextView) findViewById(R.id.tv_inner_available_size);
        checkbox_inner = (CheckBox) findViewById(R.id.checkbox_inner);
        ext_sd_card = (LinearLayout) findViewById(R.id.ext_sd_card);
        tv_ext_total_size = (TextView) findViewById(R.id.tv_ext_total_size);
        tv_ext_available_size = (TextView) findViewById(R.id.tv_ext_available_size);
        checkbox_ext = (CheckBox) findViewById(R.id.checkbox_ext);
    }

    private void setData() {
        //内置SD卡存在
        if (FileUtils.isSDCardMounted()) {
            inner_sd_card.setVisibility(View.VISIBLE);
            String SDTotalSize = FileUtils.getSDTotalSize();
            String SDAvailableSize = FileUtils.getSDAvailableSize();
            tv_inner_total_size.setText(SDTotalSize);
            tv_inner_available_size.setText(SDAvailableSize);
            Log.e("SetSongStorageActivity", "内置SD卡存在");
            Log.e("SetSongStorageActivity", "SDTotalSize = " + SDTotalSize);
            Log.e("SetSongStorageActivity", "SDAvailableSize = " + SDAvailableSize);
        }
        //存储路径在内置SD卡上
        if (FileUtils.getAppFilePath() == FileUtils.SD_CARD_INNER) {
            checkbox_inner.setChecked(true);
        }

        //外置SD卡存在
        if (FileUtils.isExtSDCardMounted()) {
            ext_sd_card.setVisibility(View.VISIBLE);
            String extSDTotalSize = FileUtils.getExtSDTotalSize();
            String extSDAvailableSize = FileUtils.getExtSDAvailableSize();
            tv_ext_total_size.setText(extSDTotalSize);
            tv_ext_available_size.setText(extSDAvailableSize);
            Log.e("SetSongStorageActivity", "外置SD卡存在");
            Log.e("SetSongStorageActivity", "extSDTotalSize = " + extSDTotalSize);
            Log.e("SetSongStorageActivity", "extSDAvailableSize = " + extSDAvailableSize);
        }
        //存储路径在外置SD卡上
        if (FileUtils.getAppFilePath() == FileUtils.SD_CARD_EXT) {
            checkbox_ext.setChecked(true);
        }
    }

    private void initHead() {
        ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
        TextView tv_navbar_title = (TextView) findViewById(R.id.tv_navbar_title);
        ImageButton btn_menu = (ImageButton) findViewById(R.id.btn_menu);
        btn_menu.setVisibility(View.INVISIBLE);
        tv_navbar_title.setText("歌曲存放目录");
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
