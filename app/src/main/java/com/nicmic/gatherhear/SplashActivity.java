package com.nicmic.gatherhear;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;

import com.nicmic.gatherhear.activity.BaseActivity;
import com.nicmic.gatherhear.fragment.ContainerActivity;

public class SplashActivity extends BaseActivity {

    private int to;
    private static int TO_GUIDE = 1;//跳转到引导页面
    private static int TO_HOME = 2;//跳转到主页面

    private static final long DELAY_TIME = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initView();
    }

    private void initView() {
        /*1、首次运行，跳转到引导页面
        * 2、跳转到主页面*/
        boolean isFirstRun = false;
        if (isFirstRun) {
            to = 1;
        }else {
            to = 2;
        }

        //开启延迟的子线程
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                switch (to) {
                    case 1://跳转到引导页面

                        break;
                    case 2://跳转到主页面
                        intent = new Intent(SplashActivity.this, ContainerActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                }
            }
        }, DELAY_TIME);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//防止闪屏页面被退出
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


}
