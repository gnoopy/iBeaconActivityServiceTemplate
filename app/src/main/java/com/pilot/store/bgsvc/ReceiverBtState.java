package com.pilot.store.bgsvc;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.pilot.store.MainService;
import com.pilot.store.util.AndroUtil;

public class ReceiverBtState extends BroadcastReceiver {

    public ReceiverBtState() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        AndroUtil.popLog("ReceiverBtState", intent.getAction());
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
            AndroUtil.popLog("ReceiverBtState", action);
            MainService.mScanning=false;

        } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
            MainService.mScanning=true;
            AndroUtil.popLog("ReceiverBtState", action);
        }

    }
}
