package com.nicmic.gatherhear.activity;

import android.content.Intent;
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
import com.r0adkll.slidr.Slidr;

import java.util.List;

public class SelectMusicActivity extends BaseActivity {

    private ImageView btn_back;
    private EditText et_content;

    private ListView listview;
    private SongListAdapter adapter;
    private List<Music> musics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_music);
        Slidr.attach(this);
        findView();
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setupInputListener();

        setupListView();
        if (musics.size() == 0) {//检查到音乐列表没有数据，提示去扫描音乐
            Intent intent = new Intent(this, ScanMusicActivity.class);
            startActivity(intent);
        }
    }

    private void setupListView() {
        musics = MusicUtils.getMusic(this);
        adapter = new SongListAdapter(SelectMusicActivity.this, musics);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                select(musics.get(position));
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        //可能此时跳转到扫描音乐界面又回来，需要重新刷新列表
        musics.clear();
        musics.addAll(MusicUtils.getMusic(this));
        adapter.notifyDataSetChanged();
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
                musics.clear();
                musics.addAll(MusicUtils.searchMusicByContent(SelectMusicActivity.this, content));
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void select(Music music) {
        Intent intent = new Intent();
        intent.putExtra("title", music.getTitle());
        intent.putExtra("artist", music.getArtist());
        intent.putExtra("duration", MusicUtils.getTimeString(Long.parseLong(music.getDuration())));
        intent.putExtra("path", music.getPath());
        setResult(99, intent);
        finish();
        overridePendingTransition(R.anim.boost_in, R.anim.slide_left_out);
    }

    private void findView() {
        btn_back = (ImageView) findViewById(R.id.btn_back);
        et_content = (EditText) findViewById(R.id.et_content);
        listview = (ListView) findViewById(R.id.listview);
    }

}
