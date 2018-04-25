package com.nicmic.gatherhear.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.adapter.PlayListAdapter;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.bean.SoundEffect;
import com.nicmic.gatherhear.fragment.ContainerActivity;
import com.nicmic.gatherhear.fragment.LocalMusicFragment;
import com.nicmic.gatherhear.fragment.MainFragment;
import com.nicmic.gatherhear.fragment.MusicFragment;
import com.nicmic.gatherhear.fragment.MusicMenuFragment;
import com.nicmic.gatherhear.fragment.SongFragment;
import com.nicmic.gatherhear.utils.MusicUtils;
import com.nicmic.gatherhear.widget.MusicNotification;

public class MusicService extends Service {

    public static MediaPlayer player;

    //3种音乐播放状态
    public static final int INITIAL = 0;//初始状态
    public static final int PLAYING = 1;//正在播放状态
    public static final int PAUSE = 2;//暂停状态

    /**
     * 用于标识当前播放列表处于哪个界面，以便更新UI的时候选择适当的界面更新
     * 防止由于不是当前播放列表的界面更新UI时出现数组越界的异常
     */
    public static int CURRENT_UI = -1;
    public static final int UI_LOCALMUSIC_SONG = 0;//本地歌曲的单曲界面
    public static final int UI_SONG_FRAGMENT = 1;//所有SONG_FRAGMENT界面
    public static final int UI_MUSIC_MENU = 2;//所有SONG_FRAGMENT界面

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.e("MusicService","onCreate()");
        if(player == null){
            player = new MediaPlayer();

            //播放完成后调用
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.reset();
                    PlayList.playStatus = INITIAL;
                    //播放下一首
                    playNext();
                    Log.e("MusicService", "播放完成，自动播放下一首");
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (what == 1){//音乐文件不存在
                        Music music = PlayList.getPlayingMusic();
                        //当前是网络音乐
                        if (music.getPath().startsWith("http")) {
                            return true;
                        }
                        //从数据库中删除该歌曲记录
                        MusicUtils.deleteMusic(App.sContext, music);
                        //更新UI(本地音乐，歌单，歌曲界面)
                        PlayListAdapter.updateOtherUI(PlayList.position);
                        //从播放列表中移除该歌曲
                        MusicUtils.removePlayListMusic(App.sContext, music);
                        Toast.makeText(App.sContext, "歌名：" + music.getTitle() + "\n歌手：" + music.getArtist() +
                                "\n原因：歌曲文件不存在" + "\n操作：删除该歌曲记录", Toast.LENGTH_LONG).show();
                    }
                    Log.e("音乐出错了", mp.getAudioSessionId() + "\nwhat = " + what + " , " + extra);
                    return true;
                }
            });
            // TODO: 2015/10/30 如果有保存歌曲播放的进度，则恢复进度
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //启动音效（如果有开启音效）
        SoundEffect.start();

        //获得音乐播放顺序
        MusicUtils.getPlayOrder(this);
        if (player != null && player.isPlaying()) {
            //更新歌词界面(用于退出应用再进入时候加载当前播放歌曲的歌词)
            if (MusicFragment.staticHandler != null) {
                MusicFragment.staticHandler.sendEmptyMessage(MusicFragment.BEGIN_LRC_PLAY);
            }
        } else {
            //初始化player的数据
            try {
                player.reset();
                player.setDataSource(PlayList.musics.get(PlayList.position).getPath());
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        player.seekTo(MusicUtils.getPlayListProgress(getApplicationContext()));
                        PlayList.playStatus = PAUSE;
                        Log.e("ContainerActivity", "初始化player的数据");
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.e("MusicService","onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 播放音乐
     */
    public static void play() {
        if (PlayList.playStatus == INITIAL && !PlayList.isEmpty() && PlayList.position != -1){
            try {
                player.reset();
                player.setDataSource(PlayList.musics.get(PlayList.position).getPath());
                player.prepareAsync();
                player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        //停止更新歌词界面
                        if (MusicFragment.staticHandler != null) {
                            MusicFragment.staticHandler.sendEmptyMessage(MusicFragment.STOP_LRC_PLAY);
                        }
                        //重置状态值
                        PlayList.playStatus = PLAYING;
                        //开始播放
                        player.start();
                        //更新歌词界面
                        if (MusicFragment.staticHandler != null) {
                            MusicFragment.staticHandler.sendEmptyMessage(MusicFragment.BEGIN_LRC_PLAY);
                        }
                        //更新相关界面
                        updateUI();
                        //保存最近播放的时间
                        MusicUtils.savePlayTime();
                        //显示通知
                        MusicNotification.showNotification();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 播放或暂停
     */
    public static void playOrPause(){
        Log.e("MusicService", "播放状态:" + PlayList.playStatus);
        if (PlayList.playStatus == INITIAL){
            MusicService.play();
        }else if (PlayList.playStatus == PLAYING) {
            MusicService.pause();
            MusicNotification.showNotification();
        } else if (PlayList.playStatus == PAUSE){
            MusicService.goOn();
            MusicNotification.showNotification();
        }
    }

    /**
     * 继续播放
     */
    public static void goOn() {
        if (PlayList.playStatus == PAUSE){
            PlayList.playStatus = PLAYING;
            player.start();
            updateUI();
        }
    }

    /**
     * 暂停
     */
    public static void pause(){
        if (PlayList.playStatus == PLAYING){
            PlayList.playStatus = PAUSE;
            player.pause();
            updateUI();
        }
    }

    /**
     * 停止
     */
    public static void stop(){
        if (PlayList.playStatus == PLAYING){
            PlayList.playStatus = INITIAL;
            player.stop();
        }
    }

    /**
     * 获得下一首音乐的下标
     */
    public static int getNextMusicPosition() {

        //顺序播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_SEQUENTIAL){
            if (PlayList.position == PlayList.musics.size() - 1){//如果是最后一首，则暂停播放
                PlayList.playStatus = PAUSE;
                Toast.makeText(App.sContext, "没有可播放的下一首。\n若要播放下一首，请切换为循环播放", Toast.LENGTH_SHORT).show();
            }else{
                PlayList.position = PlayList.position < PlayList.musics.size() - 1 ? PlayList.position + 1 : 0;
            }
            return PlayList.position;
        }

        //循环播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_LOOP){
            PlayList.position = PlayList.position < PlayList.musics.size() - 1 ? PlayList.position + 1 : 0;
            return PlayList.position;
        }

        //单曲循环
        if (MusicUtils.ORDER == MusicUtils.ORDER_CYCLE){
            return PlayList.position;
        }

        //随机播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_RANDOM){
            int num = PlayList.musics.size();
            int random = (int) (Math.random() * num);
            PlayList.position = random;
            return PlayList.position;
        }

        //如果MusicUtils.Order被回收，则默认调用顺序播放
        PlayList.position = PlayList.position < PlayList.musics.size() - 1 ? PlayList.position + 1 : 0;
        return PlayList.position;
    }

    /**
     * 获得上一首音乐的下标
     */
    public static int getLastMusicPosition(){

        //顺序播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_SEQUENTIAL){
            if (PlayList.position == 0){//如果是第一首，则暂停播放
                PlayList.playStatus = PAUSE;
                Toast.makeText(App.sContext, "没有可播放的上一首。\n若要播放上一首，请切换为循环播放", Toast.LENGTH_SHORT).show();
            }else {
                PlayList.position = PlayList.position == 0 ? PlayList.musics.size() - 1 : PlayList.position - 1;
            }
            return PlayList.position;
        }

        //循环播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_LOOP){
            PlayList.position = PlayList.position == 0 ? PlayList.musics.size() - 1 : PlayList.position - 1;
            return PlayList.position;
        }

        //单曲循环
        if (MusicUtils.ORDER == MusicUtils.ORDER_CYCLE){
            return PlayList.position;
        }

        //随机播放
        if (MusicUtils.ORDER == MusicUtils.ORDER_RANDOM){
            int num = PlayList.musics.size();
            int random = (int) (Math.random() * num);
            PlayList.position = random;
            return PlayList.position;
        }

        //如果MusicUtils.Order被回收，则默认调用顺序播放
        PlayList.position = PlayList.position == 0 ? PlayList.musics.size() - 1 : PlayList.position - 1;
        return PlayList.position;
    }

    /**
     * 播放下一首
     */
    public static void playNext(){
        PlayList.playStatus = INITIAL;
        getNextMusicPosition();
        play();
        //保存一下播放列表的状态，防止退出应用返回时获得是退出时的状态
        MusicUtils.savePlayPosition();
        //保存最近播放的时间
        MusicUtils.savePlayTime();
        Log.e("MusicService", "下一首，保存了播放列表的状态和最近播放时间");
    }

    /**
     * 播放上一首
     */
    public static void playLast(){
        PlayList.playStatus = INITIAL;
        getLastMusicPosition();
        play();
        //保存一下播放列表的状态，防止退出应用返回时获得是退出时的状态
        MusicUtils.savePlayPosition();
        //保存最近播放的时间
        MusicUtils.savePlayTime();
        Log.e("MusicService", "上一首，保存了播放列表的状态和最近播放时间");
    }

    /**
     * 播放第一首
     */
    public static void playFirst(){
        PlayList.position = 0;
        PlayList.playStatus = MusicService.INITIAL;
        MusicService.play();
    }

    /**
     * 播放最后一首
     */
    public static void playEnd(){
        PlayList.position = PlayList.musics.size() - 1;
        PlayList.playStatus = MusicService.INITIAL;
        MusicService.play();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //保存播放进度
        MusicUtils.saveProgressDuration();
        stop();
        player.release();
        player = null;
        PlayList.playStatus = INITIAL;
        updateUI();
    }

    /**
     * 更新其他所有界面的UI
     */
    public static void updateUI(){
        //所有操作前必须先判断对象是否存在

        //更新MainFragment的UI
        if (MainFragment.staticHandler != null) {
            MainFragment.staticHandler.sendEmptyMessage(MainFragment.UPDATE_UI);
            Log.e("MusicService", "更新主页面的UI");
        }

        //更新底部音乐的UI
        if(ContainerActivity.staticHandler != null){
            ContainerActivity.staticHandler.sendEmptyMessage(ContainerActivity.UPDATE_BOTTOM_MUSIC);
            Log.e("MusicService", "更新底部音乐信息");
        }

        //更新音乐播放界面的UI
        if(MusicFragment.staticHandler != null){
            MusicFragment.staticHandler.sendEmptyMessage(MusicFragment.UPDATE_UI);
            Log.e("MusicService", "更新音乐播放界面");
        }

        //以下所有界面的UI更新前都要先判断播放的列表是否属于当前界面

        //更新本地音乐的UI
        if(MainFragment.localMusicFragment.staticHandler != null){
            MainFragment.localMusicFragment.staticHandler.sendEmptyMessage(LocalMusicFragment.UPDATE_UI_SONG);
            Log.e("MusicService", "更新本地音乐单曲界面");
        }

        //更新SongFragment的UI
        if(SongFragment.staticHandler != null){
            SongFragment.staticHandler.sendEmptyMessage(SongFragment.UPDATE_UI);
            Log.e("MusicService", "更新SongFragment界面");
        }

        //更新MusicMenuFragment的UI
        if(MusicMenuFragment.staticHandler != null){
            MusicMenuFragment.staticHandler.sendEmptyMessage(MusicMenuFragment.UPDATE_UI);
            Log.e("MusicService", "更新MusicMenuFragment界面");
        }

        //更新音乐播放界面的播放列表
        if (MusicFragment.adapter_playlist != null){
            MusicFragment.adapter_playlist.notifyDataSetChanged();
        }

        //更新底部音乐的播放列表
        if (ContainerActivity.adapter_playlist != null){
            ContainerActivity.adapter_playlist.notifyDataSetChanged();
        }

    }
}
