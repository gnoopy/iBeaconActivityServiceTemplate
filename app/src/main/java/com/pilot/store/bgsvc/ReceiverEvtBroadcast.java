package com.pilot.store.bgsvc;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.pilot.store.MainActivity;
import com.pilot.store.MainService;
import com.pilot.store.beacon.IBeacon;


public class ReceiverEvtBroadcast extends WakefulBroadcastReceiver {


    final public static String ONE_TIME = "onetime";
    final public static String ONE_TIME_CMD = "command";
    final public static String EXTRA_FOUND_BEACON="beacon";
    final public static String EXTRA_FROM_WHERE = "from";

    final public static int NEW_COUPON_ACTIVITY_WAKEUP=1;

    public static void setOnetimeCouponActivityLaunchTimer(Context context, IBeacon device){
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReceiverEvtBroadcast.class);
        intent.putExtra(ONE_TIME, Boolean.TRUE);
        intent.putExtra(ONE_TIME_CMD, NEW_COUPON_ACTIVITY_WAKEUP);
        intent.putExtra(EXTRA_FOUND_BEACON, device);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pi);
        Log.i("@@@@@@", "\n\n\n ------------- setOnetimeTimer called 1---------------\n\n\n");
    }


    @SuppressLint("Wakelock")
    @Override
    public void onReceive(Context context, Intent intent) {

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())){
            Intent i = new Intent(context, MainService.class);
            context.startService(i);
            return;
        }

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,"MAG_SCAN");

        Bundle extras = intent.getExtras();

        if(extras != null && extras.getBoolean(ONE_TIME, Boolean.FALSE)){
            Log.i("ALARM_RECEIVER","#"+extras.getInt(ONE_TIME_CMD,0));
            Log.i("ALARM_RECEIVER","# starting command..."+extras.getInt(ONE_TIME_CMD,0));
            Log.i("ALARM_RECEIVER","#"+extras.getInt(ONE_TIME_CMD,0));
            switch (extras.getInt(ONE_TIME_CMD, 0)) {
                case NEW_COUPON_ACTIVITY_WAKEUP: // new activity wakeup
                    wl.acquire();
                    Intent coupon = new Intent(context, MainActivity.class);
                    coupon.putExtra(EXTRA_FOUND_BEACON, extras.getSerializable(EXTRA_FOUND_BEACON));
                    coupon.putExtra(ReceiverEvtBroadcast.EXTRA_FROM_WHERE, "mainService");
                    coupon.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(coupon);
                    wl.release();
                    Log.i("ALARM_RECEIVER", "\n\n\n\n\n\n--- ScanActivity executed ---\n\n\n\n\n\n");
                    break;
            }

        }
    }
}
