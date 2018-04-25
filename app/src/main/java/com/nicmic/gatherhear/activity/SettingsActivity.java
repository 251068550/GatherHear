package com.nicmic.gatherhear.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.activity.settings.SetCustomAnimActivity;
import com.nicmic.gatherhear.activity.settings.SetLrcStorageActivity;
import com.nicmic.gatherhear.activity.settings.SetSongStorageActivity;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.constant.Constant;
import com.nicmic.gatherhear.utils.FileUtils;
import com.r0adkll.slidr.Slidr;
import com.rey.material.widget.RadioButton;
import com.rey.material.widget.Switch;

public class SettingsActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private ImageView btn_back;
    /**歌曲*/
    private LinearLayout btn_song_storage_location;
    private TextView tv_song_storage_location;
    private RelativeLayout btn_soon_to_play;
    private Switch switch_soon_to_play;
    /**歌词*/
    private LinearLayout btn_lrc_storage_location;
    private TextView tv_lrc_storage_location;
    /**动画*/
    private RadioButton btn_low_grade;
    private RadioButton btn_middle_grade;
    private RadioButton btn_high_grade;
    private RadioButton btn_custom_grade;
    private RelativeLayout btn_custom_grade_settings;
    /**关于*/
    private RelativeLayout btn_contact_me, btn_check_upgrade, btn_feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Slidr.attach(this);

        findView();
        setListener();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //重新加载歌曲存放位置
        setupSongStorageLocation();
        //加载即将播放歌曲的通知
        setupSoonToPlay();
        //加载歌词存放位置
        setupLrcStorageLocation();
        //加载动画等级数据
        setupAnimGrade();
    }

    private void setupSoonToPlay() {
        boolean b = AnimUtil.getNextMusicNotification();
        switch_soon_to_play.setChecked(b);
    }

    private void setupSongStorageLocation() {
        if (FileUtils.getAppFilePath() == FileUtils.SD_CARD_INNER) {
            tv_song_storage_location.setText(FileUtils.INNER_SD_CARD_PATH + Constant.FILE_SONG);
        }else if (FileUtils.getAppFilePath() == FileUtils.SD_CARD_EXT) {
            tv_song_storage_location.setText(FileUtils.EXT_SD_CARD_PATH + Constant.FILE_SONG);
        }
    }

    private void setupLrcStorageLocation() {
        if (FileUtils.getAppFilePath() == FileUtils.SD_CARD_INNER) {
            tv_lrc_storage_location.setText(FileUtils.INNER_SD_CARD_PATH + Constant.FILE_LRC);
        }else if (FileUtils.getAppFilePath() == FileUtils.SD_CARD_EXT) {
            tv_lrc_storage_location.setText(FileUtils.EXT_SD_CARD_PATH + Constant.FILE_LRC);
        }
    }

    private void setupAnimGrade() {
        int grade = AnimUtil.getAnimGrade();
        if (grade == AnimUtil.ANIM_GRADE_LOW) {
            changeChecked(btn_low_grade);
        }
        if (grade == AnimUtil.ANIM_GRADE_MIDDLE) {
            changeChecked(btn_middle_grade);
        }
        if (grade == AnimUtil.ANIM_GRADE_HIGH) {
            changeChecked(btn_high_grade);
        }
        if (grade == AnimUtil.ANIM_GRADE_CUSTOM) {
            changeChecked(btn_custom_grade);
        }
    }

    private void setListener() {
        btn_back.setOnClickListener(this);
        btn_song_storage_location.setOnClickListener(this);
        btn_lrc_storage_location.setOnClickListener(this);
        btn_custom_grade_settings.setOnClickListener(this);
        btn_low_grade.setOnClickListener(this);
        btn_middle_grade.setOnClickListener(this);
        btn_high_grade.setOnClickListener(this);
        btn_custom_grade.setOnClickListener(this);
        btn_soon_to_play.setOnClickListener(this);
        btn_contact_me.setOnClickListener(this);
        btn_check_upgrade.setOnClickListener(this);
        btn_feedback.setOnClickListener(this);

        btn_low_grade.setOnCheckedChangeListener(this);
        btn_middle_grade.setOnCheckedChangeListener(this);
        btn_high_grade.setOnCheckedChangeListener(this);
        btn_custom_grade.setOnCheckedChangeListener(this);

        switch_soon_to_play.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                AnimUtil.saveNextMusicNotification(checked);
            }
        });
    }

    private void findView() {
        btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_song_storage_location = (LinearLayout) findViewById(R.id.btn_song_storage_location);
        tv_song_storage_location = (TextView) findViewById(R.id.tv_song_storage_location);
        btn_soon_to_play = (RelativeLayout) findViewById(R.id.btn_soon_to_play);
        switch_soon_to_play = (Switch) findViewById(R.id.switch_soon_to_play);
        btn_lrc_storage_location = (LinearLayout) findViewById(R.id.btn_lrc_storage_location);
        tv_lrc_storage_location = (TextView) findViewById(R.id.tv_lrc_storage_location);
        btn_low_grade = (RadioButton) findViewById(R.id.btn_low_grade);
        btn_middle_grade = (RadioButton) findViewById(R.id.btn_middle_grade);
        btn_high_grade = (RadioButton) findViewById(R.id.btn_high_grade);
        btn_custom_grade = (RadioButton) findViewById(R.id.btn_custom_grade);
        btn_custom_grade_settings = (RelativeLayout) findViewById(R.id.btn_custom_grade_settings);
        btn_contact_me = (RelativeLayout) findViewById(R.id.btn_contact_me);
        btn_check_upgrade = (RelativeLayout) findViewById(R.id.btn_check_upgrade);
        btn_feedback = (RelativeLayout) findViewById(R.id.btn_feedback);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_song_storage_location:
                intent = new Intent(this, SetSongStorageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.shrink_out);
                break;
            case R.id.btn_lrc_storage_location:
                intent = new Intent(this, SetLrcStorageActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.shrink_out);
                break;
            case R.id.btn_custom_grade_settings:
                intent = new Intent(this, SetCustomAnimActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_right_in, R.anim.shrink_out);
                break;
            case R.id.btn_low_grade:
                changeChecked(btn_low_grade);
                break;
            case R.id.btn_middle_grade:
                changeChecked(btn_middle_grade);
                break;
            case R.id.btn_high_grade:
                changeChecked(btn_high_grade);
                break;
            case R.id.btn_custom_grade:
                changeChecked(btn_custom_grade);
                break;
            case R.id.btn_soon_to_play:
                if (switch_soon_to_play.isChecked()) {
                    switch_soon_to_play.setChecked(false);
                } else {
                    switch_soon_to_play.setChecked(true);
                }
                break;
            case R.id.btn_contact_me:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_check_upgrade:
                Toast.makeText(this, "请下载聚片应用获取最新版本", Toast.LENGTH_SHORT).show();
                break;
            case R.id.btn_feedback:
                Toast.makeText(this, "请到联系我进行反馈", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void changeChecked(View view) {
        btn_low_grade.setChecked(false);
        btn_middle_grade.setChecked(false);
        btn_high_grade.setChecked(false);
        btn_custom_grade.setChecked(false);

        if (btn_low_grade.equals(view)) {
            btn_low_grade.setChecked(true);
            AnimUtil.setLowGradeAnim(AnimUtil.ANIM_GRADE_LOW);
        }
        if (btn_middle_grade.equals(view)) {
            btn_middle_grade.setChecked(true);
            AnimUtil.setMiddleGradeAnim(AnimUtil.ANIM_GRADE_MIDDLE);
        }
        if (btn_high_grade.equals(view)) {
            btn_high_grade.setChecked(true);
            AnimUtil.setHighGradeAnim(AnimUtil.ANIM_GRADE_HIGH);
        }
        if (btn_custom_grade.equals(view)) {
            btn_custom_grade.setChecked(true);
            AnimUtil.setCustomGradeAnim(AnimUtil.ANIM_GRADE_CUSTOM);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            switch (buttonView.getId()) {
                case R.id.btn_low_grade:
                    AnimUtil.setLowGradeAnim(AnimUtil.ANIM_GRADE_LOW);
                    break;
                case R.id.btn_middle_grade:
                    AnimUtil.setMiddleGradeAnim(AnimUtil.ANIM_GRADE_MIDDLE);
                    break;
                case R.id.btn_high_grade:
                    AnimUtil.setHighGradeAnim(AnimUtil.ANIM_GRADE_HIGH);
                    break;
                case R.id.btn_custom_grade:
                    AnimUtil.setCustomGradeAnim(AnimUtil.ANIM_GRADE_CUSTOM);
                    break;
            }
        }
    }
}
