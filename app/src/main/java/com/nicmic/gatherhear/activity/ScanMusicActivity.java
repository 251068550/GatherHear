package com.nicmic.gatherhear.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.dd.CircularProgressButton;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.r0adkll.slidr.Slidr;
import com.rey.material.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ScanMusicActivity extends BaseActivity {

    private ImageView btn_back;

    private TextView tv_num;
    private TextView tv_title;
    private TextView tv_content;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private List<TextView> textViews = new ArrayList<>();

    private CheckBox checkBox;
    private CircularProgressButton btn_scanner;

    List<Map<String, String>> unFilterMusics;//全部音乐
    List<Map<String, String>> filterMusics;//过滤60秒的音乐
    List<Map<String, String>> musics;//最终要保存的音乐

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_music);
        Slidr.attach(this);

        findView();
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int progress = btn_scanner.getProgress();
                if (progress == 0) {//开始扫描
                    scanMusic();
                } else if (progress == 100) {//扫描完成
                    saveMusic();
                } else if (progress > 0 && progress < 100) {//扫描中
                    Toast.makeText(ScanMusicActivity.this, "请耐心等待一会喔——", 0).show();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.boost_in, R.anim.slide_left_out);
    }

    private void findView() {
        btn_back = (ImageView) findViewById(R.id.btn_back);
        tv_num = (TextView) findViewById(R.id.tv_num);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        btn_scanner = (CircularProgressButton) findViewById(R.id.btn_scanner);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_content = (TextView) findViewById(R.id.tv_content);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
        tv3 = (TextView) findViewById(R.id.tv3);
        textViews.add(tv1);
        textViews.add(tv2);
        textViews.add(tv3);
    }

    private void saveMusic() {
        MusicUtils.saveScanedMusic(this, musics);
        MusicService.updateUI();
        finish();
    }

    int index = 0;
    private void scanMusic() {
        //设置进度条滚动
        btn_scanner.setIndeterminateProgressMode(true);
        btn_scanner.setProgress(50);
        checkBox.setClickable(false);
        tv_title.setText("扫描中...");
        tv_content.setVisibility(View.GONE);

        //扫描音乐
        unFilterMusics = MusicUtils.scanMusic(ScanMusicActivity.this, 0);
        filterMusics = MusicUtils.scanMusic(ScanMusicActivity.this, 60);

        if (checkBox.isChecked()){
            musics = filterMusics;
        }else{
            musics = unFilterMusics;
        }

        //播放动画
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (index < musics.size()) {
                    handler.sendEmptyMessage(SCANING);
                }else{
                    timer.cancel();
                    handler.sendEmptyMessage(FINISHED);
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, duration);
    }

    public static final int SCANING = 0;
    public static final int FINISHED = 1;
    private static int duration = 100;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SCANING:
                    TextView tv = textViews.get(index % 3);
                    tv.setText(musics.get(index).get("title"));
                    YoYo.with(Techniques.SlideInUp).duration(duration).playOn(tv);
                    YoYo.with(Techniques.SlideOutUp).duration(duration).delay(duration).playOn(tv);
                    index++;
                    tv_num.setText(index + "");
//                    YoYo.with(Techniques.FadeIn).duration(duration).playOn(tv_num);

                    break;
                case FINISHED:
                    tv_title.setText("扫描结束");
                    YoYo.with(Techniques.Pulse).duration(1000).playOn(tv_title);
                    tv1.setText("共扫描到" + musics.size() + "首歌曲");
                    tv1.setTextColor(getResources().getColor(R.color.red));
                    YoYo.with(Techniques.DropOut).duration(1000).playOn(tv1);
                    if (musics == filterMusics){
                        tv2.setText("有" + (unFilterMusics.size() - filterMusics.size()) + "首音频文件被过滤掉");
                        YoYo.with(Techniques.StandUp).duration(1000).playOn(tv2);
                    }

                    YoYo.with(Techniques.Flash).duration(1000).playOn(tv_num);
                    btn_scanner.setProgress(100);
                    break;
            }

        }
    };

}
