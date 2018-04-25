package com.nicmic.gatherhear.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.SoundEffect;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;
import java.util.List;

public class EqualizerActivity extends BaseActivity {

    //均衡器相关
    private TextView freq1, freq2, freq3, freq4, freq5;
    private TextView band_max, band_min;
    private SeekBar seekbar1, seekbar2, seekbar3, seekbar4, seekbar5;
    private List<SeekBar> seekBars = new ArrayList<>();
    private List<TextView> freqs = new ArrayList<>();

    //重低音相关
    private SeekBar seekbar_bass_boost;

    //环绕音相关
    private SeekBar seekbar_virtualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer);
        Slidr.attach(this);

        findView();
        // 初始化navbar
        initHead();
        // 初始化均衡控制器
        setupEqualizer();
        // 初始化重低音控制器
        setupBassBoost();
        // 初始化环绕音控制器
        setupVirtualizer();
    }

    private void initHead() {
        ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
        TextView tv_navbar_title = (TextView) findViewById(R.id.tv_navbar_title);
        ImageButton btn_menu = (ImageButton) findViewById(R.id.btn_menu);
        btn_menu.setVisibility(View.INVISIBLE);
        tv_navbar_title.setText("自定义");
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setupVirtualizer() {
        // 环绕音的范围为0～1000
        seekbar_virtualizer.setMax(1000);
        //获得保存的环绕音的值
        Short strength = SoundEffect.getVirtualizer(this);
        //设置进度条的值
        seekbar_virtualizer.setProgress(strength);
        //设置重低音的强度
        SoundEffect.mVirtualizer.setStrength(strength);
        // 为SeekBar的拖动事件设置事件监听器
        seekbar_virtualizer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 设置重低音的强度
                if (SoundEffect.mVirtualizer.getStrengthSupported()) {
                    SoundEffect.mVirtualizer.setStrength((short) progress);
                    SoundEffect.saveVirtualizer(EqualizerActivity.this, (short) progress);
                    Log.e("EqualizerActivity", "设置环绕音强度为" + progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setupBassBoost() {
        // 重低音的范围为0～1000
        seekbar_bass_boost.setMax(1000);
        //获得保存的重低音的值
        Short strength = SoundEffect.getBassBoost(this);
        //设置进度条的值
        seekbar_bass_boost.setProgress(strength);
        //设置重低音的强度
        SoundEffect.mBass.setStrength(strength);
        // 为SeekBar的拖动事件设置事件监听器
        seekbar_bass_boost.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 设置重低音的强度
                if (SoundEffect.mBass.getStrengthSupported()) {
                    SoundEffect.mBass.setStrength((short) progress);
                    SoundEffect.saveBassBoost(EqualizerActivity.this, (short) progress);
                    Log.e("EqualizerActivity", "设置重低音强度为" + progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void setupEqualizer() {
        seekBars.add(seekbar1);
        seekBars.add(seekbar2);
        seekBars.add(seekbar3);
        seekBars.add(seekbar4);
        seekBars.add(seekbar5);

        freqs.add(freq1);
        freqs.add(freq2);
        freqs.add(freq3);
        freqs.add(freq4);
        freqs.add(freq5);
        //设置均衡控制器的最小值
        band_min.setText((SoundEffect.minEQLevel / 100) + "db");
        //设置均衡控制器的最大值
        band_max.setText((SoundEffect.maxEQLevel / 100) + "db");

        for (short i = 0; i < SoundEffect.freqNames.size(); i++) {
            if (i == 5) {
                // TODO: 2016/4/26 MIUI7有6个声道，这里暂时这样处理
                break;
            }

            final short band = i;
            //设置每个seekbar底部的频率名称
            freqs.get(i).setText(SoundEffect.freqNames.get(i));
            //设置seekbar最大值
            seekBars.get(i).setMax(SoundEffect.maxEQLevel - SoundEffect.minEQLevel);
            //获取保存的band的值
            Short level = SoundEffect.getBandLevel(this, band);
            //设置该频率的值
            SoundEffect.mEqualizer.setBandLevel(band, level);
            //设置seekbar进度值
            seekBars.get(i).setProgress(level + SoundEffect.maxEQLevel);
            //为seekbar的拖动事件设置时间监听器
            seekBars.get(i).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    // 设置该频率的均衡值
                    SoundEffect.mEqualizer.setBandLevel(band, (short) (progress + SoundEffect.minEQLevel));
                    // 保存该频率的均衡值
                    SoundEffect.saveBandLevel(EqualizerActivity.this, band, (short) (progress + SoundEffect.minEQLevel));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    private void findView() {
        freq1 = (TextView) findViewById(R.id.freq1);
        freq2 = (TextView) findViewById(R.id.freq2);
        freq3 = (TextView) findViewById(R.id.freq3);
        freq4 = (TextView) findViewById(R.id.freq4);
        freq5 = (TextView) findViewById(R.id.freq5);

        band_max = (TextView) findViewById(R.id.band_max);
        band_min = (TextView) findViewById(R.id.band_min);

        seekbar1 = (SeekBar) findViewById(R.id.seekBar1);
        seekbar2 = (SeekBar) findViewById(R.id.seekBar2);
        seekbar3 = (SeekBar) findViewById(R.id.seekBar3);
        seekbar4 = (SeekBar) findViewById(R.id.seekBar4);
        seekbar5 = (SeekBar) findViewById(R.id.seekBar5);

        seekbar_bass_boost = (SeekBar) findViewById(R.id.seekbar_bass_boost);

        seekbar_virtualizer = (SeekBar) findViewById(R.id.seekbar_virtualizer);
    }

}
