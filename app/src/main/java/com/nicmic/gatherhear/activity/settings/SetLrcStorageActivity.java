package com.nicmic.gatherhear.activity.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.activity.BaseActivity;
import com.nicmic.gatherhear.constant.Constant;
import com.nicmic.gatherhear.utils.FileUtils;
import com.r0adkll.slidr.Slidr;

public class SetLrcStorageActivity extends BaseActivity {

    private TextView tv_lrc_filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_lrc_storage);
        Slidr.attach(this);

        initHead();
        tv_lrc_filepath = (TextView) findViewById(R.id.tv_lrc_filepath);
        setupLrcStorageLocation();
    }

    private void initHead() {
        ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
        TextView tv_navbar_title = (TextView) findViewById(R.id.tv_navbar_title);
        ImageButton btn_menu = (ImageButton) findViewById(R.id.btn_menu);
        btn_menu.setVisibility(View.INVISIBLE);
        tv_navbar_title.setText("歌词存放目录");
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupLrcStorageLocation() {
        if (FileUtils.getAppFilePath() == FileUtils.SD_CARD_INNER) {
            tv_lrc_filepath.setText(FileUtils.INNER_SD_CARD_PATH + Constant.FILE_LRC);
        }else if (FileUtils.getAppFilePath() == FileUtils.SD_CARD_EXT) {
            tv_lrc_filepath.setText(FileUtils.EXT_SD_CARD_PATH + Constant.FILE_LRC);
        }
    }

}
