package com.nicmic.gatherhear.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.animation.AnimUtil;
import com.nicmic.gatherhear.bean.SoundEffect;
import com.r0adkll.slidr.Slidr;
import com.rey.material.widget.Switch;

import net.wujingchao.android.view.SimpleTagImageView;

import java.util.List;

public class SoundEffectActivity extends BaseActivity {

    private ImageView btn_back;
    private TextView navbar_title;
    private Switch btn_switch;
    private RecyclerView recyclerView;

    public static final int UPDATE_VIEW = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case UPDATE_VIEW:
                    initRecyclerView();
                    initHead();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_effect);
        Slidr.attach(this);

        findView();

        //TODO:有一个总开关，用来启用或关闭音效
        boolean b = SoundEffect.init();
        if (b) {
            //初始化预设音场列表
            initRecyclerView();
            initHead();
        }else {
            Toast.makeText(this, "音乐服务还未启动，启动音效控制器失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimUtil.listviewGridEnterAnim(recyclerView, AnimUtil.DELAY);
    }

    private void initHead() {
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        navbar_title.setText("音效");
        btn_switch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(Switch view, boolean checked) {
                if (checked) {
                    if (SoundEffect.getPresetReverb(SoundEffectActivity.this) == -1){
                        Toast.makeText(SoundEffectActivity.this, "请选择一种音乐场景以开启音效", Toast.LENGTH_SHORT).show();
                    }
                    SoundEffect.mBass.setEnabled(true);
                    SoundEffect.mPresetReverb.setEnabled(true);
//                    SoundEffect.mEqualizer.setEnabled(true);
                }else {//关闭音效
                    SoundEffect.savePresetReverb(SoundEffectActivity.this, (short) -1);
                    SoundEffect.mBass.setEnabled(false);
                    SoundEffect.mPresetReverb.setEnabled(false);
//                    SoundEffect.mEqualizer..setEnabled(false);
                }

                handler.sendEmptyMessage(UPDATE_VIEW);
            }
        });
        if (SoundEffect.getPresetReverb(this) == -1){
            btn_switch.setChecked(false);
        }else{
            btn_switch.setChecked(true);
        }
    }

    private void initRecyclerView() {
        MyAdapter adapter = new MyAdapter(SoundEffect.presetNames);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void findView() {
        btn_back = (ImageView) findViewById(R.id.btn_back);
        navbar_title = (TextView) findViewById(R.id.navbar_title);
        btn_switch = (Switch) findViewById(R.id.btn_switch);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        private List<String> mPresetNames;
        private int[] resIds = new int[]{R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4, R.drawable.pic5,
                R.drawable.pic6, R.drawable.pic7, R.drawable.pic8, R.drawable.pic9, R.drawable.pic10,
                R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4, R.drawable.pic5};

        public MyAdapter(List<String> presetNames){
            mPresetNames = presetNames;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            SimpleTagImageView stiv;
            RelativeLayout in_use;

            public ViewHolder(View itemView) {
                super(itemView);
                stiv = (SimpleTagImageView) itemView.findViewById(R.id.stiv);
                in_use = (RelativeLayout) itemView.findViewById(R.id.in_use);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preset_reverb, parent, false);
            ViewHolder vh = new ViewHolder(view);
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            if (position == mPresetNames.size()) {//自定义（跳转到均衡器界面）
                if (SoundEffect.getPresetReverb(getApplicationContext()) == -2) {//-2是自定义的标识
                    holder.in_use.setVisibility(View.VISIBLE);
                }
                holder.stiv.setTagText("自定义");
                holder.stiv.setBackgroundResource(R.drawable.bg_add_custom_color_outline);
                holder.stiv.setImageResource(R.drawable.default_cd_cover);
                holder.stiv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //保存预设音场的值为-2，-2是自定义的标识，不能使用
                        SoundEffect.savePresetReverb(getApplicationContext(), (short) -2);
                        handler.sendEmptyMessage(UPDATE_VIEW);

                        Intent intent = new Intent(SoundEffectActivity.this, EqualizerActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_right_in, R.anim.popup_exit);
                    }
                });
            } else {//预设音场
                holder.stiv.setTagText(mPresetNames.get(position));
                holder.stiv.setImageResource(resIds[position]);

                if (SoundEffect.getPresetReverb(getApplicationContext()) == position) {
                    holder.in_use.setVisibility(View.VISIBLE);
                } else {
                    holder.in_use.setVisibility(View.GONE);
                }
                holder.stiv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //遇到一个问题，刷新item的时候，图片和item会对应不上，不知道view是怎么回收的
                        //所以只能先用handler来setAdapter
                        int preset = SoundEffect.getPresetReverb(getApplicationContext());
                        if (preset == position) {
                            SoundEffect.savePresetReverb(getApplicationContext(), (short) -1);
                            SoundEffect.mPresetReverb.setEnabled(false);
//                        notifyItemChanged(position);
                        } else {
//                        notifyItemChanged(preset);
                            SoundEffect.savePresetReverb(getApplicationContext(), (short) position);
                            SoundEffect.mPresetReverb.setEnabled(true);
                            SoundEffect.mEqualizer.usePreset((short) position);
//                        notifyItemChanged(position);
                        }
                        handler.sendEmptyMessage(UPDATE_VIEW);
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return mPresetNames.size() + 1;
        }

    }

}
