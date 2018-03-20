package com.pilot.store.util;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;

public class AndroUtil extends QuickUtil {

    private static BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

    public static void stopBtLeScan(BluetoothAdapter.LeScanCallback callbak) {
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.stopLeScan(callbak);
    }

    public static void startBtDevice() {
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        if (!mBtAdapter.isEnabled())
            mBtAdapter.disable();
    }

    public static void startBtLeScan(BluetoothAdapter.LeScanCallback callbak) {
        if (!mBtAdapter.isEnabled())
            mBtAdapter.enable();
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startLeScan(callbak);
    }


    static public boolean isPlugConnected(Context context) {
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
    }

    public static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    public static String mIMEI;
    public static String mMDNfromTelephMgr = null;
    public static String mWifiMacAddress = null;
    public static Context mContext = null;

    public static void getUniqValues(Context context) {
        TelephonyManager tMgr = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMDNfromTelephMgr = tMgr.getLine1Number();
        mIMEI = tMgr.getDeviceId();
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        mWifiMacAddress = info.getMacAddress();
        mContext=context;
    }

    static public double getBatteryCapacity(Context context) {
        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            double batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            return batteryCapacity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    static public void longLog(String tag, String log) {
        if(log.length() > 4000) {
            for(int i=0;i<log.length();i+=4000){
                if(i+4000<log.length())
                    Log.i("rescounter" + i, log.substring(i, i + 4000));
                else
                    Log.i("rescounter"+i,log.substring(i, log.length()));
            }
        } else
            Log.i("resinfo",log);
    }


    public static String popLog(String log) {
        String line="\n\n#\n#"+log+"\n#";
        Log.i("popLog",line);
        return line;
    }
    public static String popLog(String[] logs) {
        String line="\n\n#\n";
        for (int i = 0; i < logs.length; i++) {
            line+="#"+logs[i]+"\n";
        }
        line+="#";
        Log.i("popLog",line);
        return line;
    }
    public static String popLog(Object... args){
        String line="\n\n#\n";
        for (int i = 0; i < args.length; i++) {
            if (args[i]==null)
                line+="# args["+i+"] is null\n";
            else
                line+="#"+args[i].toString()+"\n";
        }
        line+="#\n";
        Log.i("popLog",line);
        return line;
    }
    public static boolean isMyServiceRunning(Context ctx, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static byte [] b64ToByteArr(String b64Encoded){ //b64 to DB
        byte[] decoded = Base64.decode(b64Encoded, Base64.DEFAULT);
        return decoded;
    }
    public static Bitmap b64ToBitmap(String b64Encoded) { // b64 to Image
        byte[] decoded = Base64.decode(b64Encoded, Base64.DEFAULT);
        Bitmap imgByte = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        return imgByte;
    }
    public static Bitmap byteToBitmap(byte[] decoded) { //DB to Image
        Bitmap imgByte = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
        return imgByte;
    }

    public static ProgressDialog getPrgDlg(Context ctx) {
        ProgressDialog prgDlg = new ProgressDialog(ctx);
        prgDlg.setMessage("Please wait...");
        prgDlg.setIndeterminate(true);
        prgDlg.setCancelable(true);
        prgDlg.setInverseBackgroundForced(false);
        prgDlg.setCanceledOnTouchOutside(true);
        return prgDlg;
    }


}
