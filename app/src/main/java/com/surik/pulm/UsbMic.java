package com.surik.pulm;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.content.BroadcastReceiver;
/**
 * Created by User on 26.06.2017.
 */

public class UsbMic extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Test", "USB Mic");
    }
}