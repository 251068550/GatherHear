package com.nicmic.gatherhear.activity;

import android.os.Bundle;

import com.nicmic.gatherhear.R;
import com.r0adkll.slidr.Slidr;

public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        Slidr.attach(this);

    }

}
