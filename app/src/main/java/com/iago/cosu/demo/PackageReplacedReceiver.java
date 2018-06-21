package com.iago.cosu.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by ankurkumar on 6/15/18.
 */

public class PackageReplacedReceiver extends BroadcastReceiver {

    private static final String TAG = "PackageReplacedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "intent: " + intent.getAction());

        Intent intent1 = new Intent(context, KioskActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);

    }
}
