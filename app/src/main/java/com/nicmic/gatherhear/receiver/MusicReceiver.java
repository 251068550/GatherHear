package com.nicmic.gatherhear.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.nicmic.gatherhear.App;
import com.nicmic.gatherhear.service.MusicService;
import com.nicmic.gatherhear.widget.MusicNotification;

import java.util.List;

public class MusicReceiver extends BroadcastReceiver {

    public static final int CLOSE = 0;
    public static final int LAST = 1;
    public static final int NEXT = 2;
    public static final int PLAY_PAUSE = 3;

    public MusicReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            Toast.makeText(context, "未接收到任何数据", Toast.LENGTH_SHORT).show();
        } else {
            int action = bundle.getInt("ACTION", CLOSE);
            if (action == LAST) {
                MusicService.playLast();
            }
            if (action == NEXT) {
                MusicService.playNext();
            }
            if (action == PLAY_PAUSE) {
//                if (ContainerActivity.musicFragment == null) {//可能应用被清理了，但是通知栏音乐还在，点击播放按钮先开启应用
//                    Intent intent1 = new Intent(context, ContainerActivity.class);
//                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//                    context.startActivity(intent1);
//                }
                MusicService.playOrPause();
            }if (action == CLOSE) {
                List<Activity> activities = App.sActivitys;
                for (Activity activity : activities) {
                    activity.finish();
                }
//                android.os.Process.killProcess(android.os.Process.myPid());
                MusicNotification.cancelNotification();
                System.exit(0);
            }

        }
    }
}
