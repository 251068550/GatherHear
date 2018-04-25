package com.nicmic.gatherhear.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.adapter.SongListAdapter;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.nicmic.gatherhear.utils.SongUtils;
import com.r0adkll.slidr.Slidr;

import java.util.List;

public class SearchLocalMusicActivity extends BaseActivity {

    private ImageView btn_back;
    private EditText et_content;

    private ListView listview;
    private SongListAdapter adapter;
    private List<Music> musics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_local_music);
        Slidr.attach(this);

        findView();

        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setupInputListener();
    }

    private void setupInputListener() {
        et_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                musics = MusicUtils.searchMusicByContent(SearchLocalMusicActivity.this, content);
                adapter = new SongListAdapter(SearchLocalMusicActivity.this, musics);
                listview.setAdapter(adapter);
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        SongUtils.songClickListener(view, position, musics);
                        finish();
                    }
                });
            }
        });
    }

    private void findView() {
        btn_back = (ImageView) findViewById(R.id.btn_back);
        et_content = (EditText) findViewById(R.id.et_content);
        listview = (ListView) findViewById(R.id.listview);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // TODO: 2015/10/15 以下方法没用
        overridePendingTransition(R.anim.boost_in, R.anim.slide_left_out);
    }

}
