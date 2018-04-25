package com.nicmic.gatherhear.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.nicmic.gatherhear.App;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        App.sActivitys.add(this);//把当前Activity放入集合中
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        App.sActivitys.remove(this);//把当前Activity从集合中移除
    }
}
