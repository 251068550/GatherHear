package com.nicmic.gatherhear.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.nicmic.gatherhear.service.MusicService;

public class PhoneBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "PhoneReceiver";
    private static boolean mIncomingFlag = false;
    private static String mIncomingNumber = null;
    private static boolean isPlaying = false;

    public PhoneBroadcastReceiver() {
        Log.e(TAG, "开启了电话广播");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果是拨打电话
        if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            mIncomingFlag = false;
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            Log.e(TAG, "拨出电话:" + phoneNumber);
            //将正在播放的音乐暂停
            if (MusicService.player != null && MusicService.player.isPlaying()) {
                isPlaying = true;
                MusicService.playOrPause();
            }
        } else {
            // 如果是来电
            TelephonyManager tManager = (TelephonyManager) context
                    .getSystemService(Service.TELEPHONY_SERVICE);
            switch (tManager.getCallState()) {

                case TelephonyManager.CALL_STATE_RINGING:
                    mIncomingNumber = intent.getStringExtra("incoming_number");
                    Log.e(TAG, "来电 :" + mIncomingNumber);
                    //将正在播放的音乐暂停
                    if (MusicService.player != null && MusicService.player.isPlaying()) {
                        isPlaying = true;
                        MusicService.playOrPause();
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if (mIncomingFlag) {
                        Log.e(TAG, "接通电话 :" + mIncomingNumber);
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (mIncomingFlag) {
                        Log.e(TAG, "incoming IDLE（挂断电话）");
                        //将正在播放的音乐暂停
                        if (isPlaying) {
                            isPlaying = false;
                            MusicService.playOrPause();
                        }
                    }
                    break;
            }
        }
    }
}
