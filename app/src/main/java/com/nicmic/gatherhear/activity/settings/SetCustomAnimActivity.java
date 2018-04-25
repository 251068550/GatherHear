package com.nicmic.gatherhear.activity.settings;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.activity.BaseActivity;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.r0adkll.slidr.Slidr;
import com.rey.material.widget.Switch;

import java.util.ArrayList;
import java.util.List;

public class SetCustomAnimActivity extends BaseActivity implements View.OnClickListener {

    private Switch btn1;
    private Switch btn2;
    private Switch btn3;
    private Switch btn4;
    private Switch btn5;
    private Switch btn6;
    private Switch btn7;
    private Switch btn8;

    List<Switch> switches;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_custom_anim);
        Slidr.attach(this);

        initHead();
        findView();
        initList();
        setListener();

        initData();
    }

    private void initList() {
        switches = new ArrayList<>();
        switches.add(btn1);
        switches.add(btn2);
        switches.add(btn3);
        switches.add(btn4);
        switches.add(btn5);
        switches.add(btn6);
        switches.add(btn7);
        switches.add(btn8);
    }

    private void initData() {
        for (int i = 0; i < switches.size(); i++) {
            boolean b = AnimUtil.getCustomAnimSwitch(AnimUtil.animNames.get(i));
            switches.get(i).setChecked(b);
        }
    }

    private void setListener() {
        for (int i = 0; i < switches.size(); i++) {
            switches.get(i).setOnClickListener(this);
        }
    }

    private void initHead() {
        ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
        TextView tv_navbar_title = (TextView) findViewById(R.id.tv_navbar_title);
        ImageButton btn_menu = (ImageButton) findViewById(R.id.btn_menu);
        btn_menu.setVisibility(View.INVISIBLE);
        tv_navbar_title.setText("自定义动画");
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void findView() {
        btn1 = (Switch) findViewById(R.id.btn1);
        btn2 = (Switch) findViewById(R.id.btn2);
        btn3 = (Switch) findViewById(R.id.btn3);
        btn4 = (Switch) findViewById(R.id.btn4);
        btn5 = (Switch) findViewById(R.id.btn5);
        btn6 = (Switch) findViewById(R.id.btn6);
        btn7 = (Switch) findViewById(R.id.btn7);
        btn8 = (Switch) findViewById(R.id.btn8);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                AnimUtil.saveCustomAnimSwitch(AnimUtil.animNames.get(0), btn1.isChecked());
                break;
            case R.id.btn2:
                AnimUtil.saveCustomAnimSwitch(AnimUtil.animNames.get(1), btn2.isChecked());
                break;
            case R.id.btn3:
                AnimUtil.saveCustomAnimSwitch(AnimUtil.animNames.get(2), btn3.isChecked());
                break;
            case R.id.btn4:
                AnimUtil.saveCustomAnimSwitch(AnimUtil.animNames.get(3), btn4.isChecked());
                break;
            case R.id.btn5:
                AnimUtil.saveCustomAnimSwitch(AnimUtil.animNames.get(4), btn5.isChecked());
                break;
            case R.id.btn6:
                AnimUtil.saveCustomAnimSwitch(AnimUtil.animNames.get(5), btn6.isChecked());
                break;
            case R.id.btn7:
                AnimUtil.saveCustomAnimSwitch(AnimUtil.animNames.get(6), btn7.isChecked());
                break;
            case R.id.btn8:
                AnimUtil.saveCustomAnimSwitch(AnimUtil.animNames.get(7), btn8.isChecked());
                break;
        }
    }
}
