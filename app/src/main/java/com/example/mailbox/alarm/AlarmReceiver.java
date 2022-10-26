package com.example.mailbox.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.mailbox.util.UserUtil;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String NOTIFICATION_CHANNEL_ID = "10002";
    private static final String TAG = "Alarm Receiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Alarm recieved");

        UserUtil.downloadUserData(context,false, null, true);

    }


}
