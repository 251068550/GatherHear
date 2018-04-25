package com.nicmic.gatherhear.widget;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.util.Log;
import android.widget.RemoteViews;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.R;
import com.nicmic.gatherhear.bean.Music;
import com.nicmic.gatherhear.bean.PlayList;
import com.nicmic.gatherhear.fragment.ContainerActivity;
import com.nicmic.gatherhear.receiver.MusicReceiver;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.utils.ImageUtils;

/**
 * Created by Administrator on 2015/10/19.
 */
public class MusicNotification {

    public static int NOTIFICATION_TAG;
    public static final int NOTIFICATION_COMMON = 0;
    public static final int NOTIFICATION_BIG = 1;

    private static NotificationManager manager;
    private static NotificationCompat.Builder builder;

    /**
     * 初始化歌曲通知，需要在应用启动的时候启用
     */
    public static void initNotification() {
        if (manager == null) {
            manager = (NotificationManager) App.sContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (builder == null) {
            builder = new NotificationCompat.Builder(App.sContext);
        }
    }

    /**
     * 显示歌曲通知
     */
    public static void showNotification() {
//        NOTIFICATION_TAG = NOTIFICATION_COMMON;
//        showCommonNotification();
        NOTIFICATION_TAG = NOTIFICATION_BIG;
        showBigNotification();
    }

    /**
     * 显示普通的歌曲播放通知
     */
    public static void showCommonNotification() {

        RemoteViews remoteViews = new RemoteViews(App.sContext.getPackageName(), R.layout.notification_common);
        //设置数据
        if (PlayList.getPlayingMusic() == null) {
            remoteViews.setTextViewText(R.id.tv_title, App.sContext.getString(R.string.default_music_title));
            remoteViews.setTextViewText(R.id.tv_artist, App.sContext.getString(R.string.default_music_artist));
        } else {
            remoteViews.setTextViewText(R.id.tv_title, PlayList.getPlayingMusic().getTitle());
            remoteViews.setTextViewText(R.id.tv_artist, PlayList.getPlayingMusic().getArtist());
        }
        //上一首按钮点击事件
        Intent lastIntent = new Intent(App.sContext, MusicReceiver.class);
        lastIntent.putExtra("ACTION", MusicReceiver.LAST);
        PendingIntent lastPendingIntent = PendingIntent.getBroadcast(App.sContext, 0, lastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_last, lastPendingIntent);
        //下一首按钮点击事件
        Intent nextIntent = new Intent(App.sContext, MusicReceiver.class);
        nextIntent.putExtra("ACTION", MusicReceiver.NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(App.sContext, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_next, nextPendingIntent);
        //播放按钮点击事件
        Intent playIntent = new Intent(App.sContext, MusicReceiver.class);
        playIntent.putExtra("ACTION", MusicReceiver.PLAY_PAUSE);
        PendingIntent playPendingIntent = PendingIntent.getBroadcast(App.sContext, 2, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_play, playPendingIntent);
        if (MusicService.player != null && MusicService.player.isPlaying()) {
            Bitmap bitmap = BitmapFactory.decodeResource(App.sContext.getResources(), R.drawable.ic_pause_circle_fill_white);
            remoteViews.setImageViewBitmap(R.id.btn_play, bitmap);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(App.sContext.getResources(), R.drawable.ic_play_circle_fill_white);
            remoteViews.setImageViewBitmap(R.id.btn_play, bitmap);
        }

        //关闭按钮点击事件
        Intent closeIntent = new Intent(App.sContext, MusicReceiver.class);
        closeIntent.putExtra("ACTION", MusicReceiver.CLOSE);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(App.sContext, 3, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.btn_close, closePendingIntent);
        //设置点击通知跳转的页面
        Intent intent = new Intent(App.sContext, ContainerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent pendingIntent = PendingIntent.getActivity(App.sContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        builder.setSmallIcon(R.drawable.default_cd_cover);
        if (PlayList.getPlayingMusic() == null) {
            builder.setTicker(App.sContext.getString(R.string.default_music_title));
        } else {
            builder.setTicker("正在播放：" + PlayList.getPlayingMusic().getTitle());
        }

        builder.setContent(remoteViews);
        builder.setContentIntent(pendingIntent);
        builder.setOngoing(true);
        builder.setOnlyAlertOnce(true);
        builder.setAutoCancel(false);
        Notification notification = builder.build();

        manager.notify(NOTIFICATION_COMMON, notification);
        Log.e("MusicNotification", "显示了通知showCommonNotification");

    }

    /**
     * 显示大的歌曲通知
     */
    public static void showBigNotification() {
        //获取正在播放的音乐
        Music music = PlayList.getPlayingMusic();

        MediaStyle mediaStyle = new MediaStyle();
        //设置小视图显示的按钮
        int[] actions = new int[]{1,2};//数字代表action中按钮的下标
        mediaStyle.setShowActionsInCompactView(actions);
        //关闭按钮点击事件
        mediaStyle.setShowCancelButton(true);
        Intent closeIntent = new Intent(App.sContext, MusicReceiver.class);
        closeIntent.putExtra("ACTION", MusicReceiver.CLOSE);
        PendingIntent closePendingIntent = PendingIntent.getBroadcast(App.sContext, MusicReceiver.CLOSE, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaStyle.setCancelButtonIntent(closePendingIntent);
        //设置点击通知跳转的页面
        Intent intent = new Intent(App.sContext, ContainerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent contentIntent = PendingIntent.getActivity(App.sContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(contentIntent);
        //设置通知的小图片
        builder.setSmallIcon(R.drawable.default_cd_cover);
        //设置通知的大图片
        if (music != null) {
            Bitmap icon = ImageUtils.getArtwork(App.sContext, music.getSongId(), music.getAlbumId(), true);
            builder.setLargeIcon(icon);
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(App.sContext.getResources(), R.drawable.default_cd_cover);
            builder.setLargeIcon(bitmap);
        }

        //设置通知提示文字
        if (PlayList.getPlayingMusic() == null) {
            builder.setTicker(App.sContext.getString(R.string.default_music_title));
        } else {
            builder.setTicker("正在播放：" + music.getTitle());
        }
        //设置歌名歌手
        if (PlayList.getPlayingMusic() == null) {
            builder.setContentTitle(App.sContext.getString(R.string.default_music_title));
            builder.setContentText(App.sContext.getString(R.string.default_music_artist));
        } else {
            builder.setContentTitle(music.getTitle());
            builder.setContentText(music.getArtist());
        }
        //设置不显示时间
        builder.setShowWhen(false);
        builder.setWhen(0);
        //设置其他参数
        builder.setOngoing(true);
        builder.setOnlyAlertOnce(true);
        builder.setAutoCancel(false);
        builder.setStyle(mediaStyle);

        if (builder.mActions.size() != 3) {//第一次设置Action需要添加
            //上一首点击事件
            builder.addAction(generateAction(R.drawable.ic_skip_previous_grey600_36dp, "上一首", MusicReceiver.LAST));
            //根据音乐是否播放设置播放或暂停图片
            if (MusicService.player != null && MusicService.player.isPlaying()) {
                builder.addAction(generateAction(R.drawable.ic_pause_circle_outline_grey600_36dp, "暂停", MusicReceiver.PLAY_PAUSE));
            } else {
                builder.addAction(generateAction(R.drawable.ic_play_circle_outline_grey600_36dp, "播放", MusicReceiver.PLAY_PAUSE));
            }
            //下一首点击事件
            builder.addAction(generateAction(R.drawable.ic_skip_next_grey600_36dp, "下一首", MusicReceiver.NEXT));
        } else {
            //根据音乐是否播放设置播放或暂停图片
            if (MusicService.player != null && MusicService.player.isPlaying()) {
                builder.mActions.set(1, generateAction(R.drawable.ic_pause_circle_outline_grey600_36dp, "暂停", MusicReceiver.PLAY_PAUSE));
            } else {
                builder.mActions.set(1, generateAction(R.drawable.ic_play_circle_outline_grey600_36dp, "播放", MusicReceiver.PLAY_PAUSE));
            }

        }

        manager.notify(NOTIFICATION_BIG, builder.build());
    }

    /**
     * 生成按钮动作（点击事件）
     *
     * @param icon
     * @param title
     * @param action
     * @return
     */
    private static NotificationCompat.Action generateAction(int icon, String title, int action) {
        Intent intent = new Intent(App.sContext, MusicReceiver.class);
        intent.putExtra("ACTION", action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(App.sContext, action, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new NotificationCompat.Action(icon, title, pendingIntent);
    }

    /**
     * 取消普通的歌曲播放通知
     */
    public static void cancelNotification() {
        builder.setAutoCancel(true);
        manager.cancel(NOTIFICATION_TAG);
    }
}
