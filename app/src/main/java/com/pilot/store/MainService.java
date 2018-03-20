package com.pilot.store;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import com.pilot.store.bgsvc.ReceiverBtState;
import com.pilot.store.bgsvc.ReceiverEvtBroadcast;
import com.pilot.store.util.AndroUtil;
import com.pilot.store.util.QuickUtil;

import com.pilot.store.beacon.IBeacon;

public class MainService extends IntentService {

    public static final int MSG_REGISTER_CLIENT = 1000 ;
    public static final int MSG_UNREGISTER_CLIENT =1001 ;
    public static final int MSG_SET_PAGE = 1;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    private NotificationManager mNotificationManager;

    static public boolean mScanning=false;
    //TODO: 3. Configure proper scan interval and period for your project
    public static long SCAN_PERIOD = 1200;
    public static long SCAN_INTERVAL = 3000;

    private Handler mHandler = new Handler();
    private BluetoothAdapter mBluetoothAdapter ;
    private BroadcastReceiver mBtStateReceiver=new ReceiverBtState();

    private BluetoothAdapter.LeScanCallback mLeScanCallback =  new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    IBeacon iBeacon = IBeacon.fromScanData(scanRecord, rssi);
                    if (iBeacon==null)
                        return ;

                    String uuidStr = iBeacon.getProximityUuid();
                    if (IBeacon.isGateBeacon(uuidStr)) {
                        AndroUtil.popLog("iBeacon Found ==>"+iBeacon);
                        IBeacon.insertBeacon(iBeacon);
                        //TODO: 2. Append what to do if iBeacons are detected by messaging between service to activity
                        if (iBeacon.getMajor() == 3 && iBeacon.getMinor() == 403)
                            MainService.this.sendMessageToUI(1);
                        if (iBeacon.getMajor() == 2 && iBeacon.getMinor() == 2)
                            MainService.this.sendMessageToUI(2);

                        ReceiverEvtBroadcast.setOnetimeCouponActivityLaunchTimer(mContext,iBeacon);
                    }
                }
            };
            performOnBackgroundThread(r);
        }
    };

    private Looper mServiceLooper;

    private void repeatScanLeDevice() {
        Runnable r = new Runnable(){
            @Override
            public void run() {
                while(true) {
                    if (mScanning)
                        continue;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            mScanning = false;
                        }
                    }, SCAN_PERIOD);
                    mScanning = true;
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    Log.i("@@@@@","Scan Started-------------------------------------");
                    QuickUtil.giveMeAsec(SCAN_INTERVAL);

                }
            }
        };
        performOnBackgroundThread(r);
    }

    public MainService(String name) {
        super(name);
    }
    public MainService() {
        super("MainService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("@@@@@","onHandleIntent "+intent);
    }


    static private int ONGOING_NOTIFICATION_ID=100;
    static private boolean isRunning = false;
    NotificationCompat.Builder mBuilder ;
    Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("SVC", "oncreateed-----------------------");
        mContext=this;
        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(com.pilot.store.R.drawable.ic_sale).setContentTitle(getString(com.pilot.store.R.string.notibar_title)).setContentText(getString(com.pilot.store.R.string.notibar_msg));


        Intent nextIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(nextIntent);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mBuilder.setOngoing(true);
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, mBuilder.getNotification() );


        pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,"mytag");

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        AndroUtil.getUniqValues(this);
        isRunning=true;

    }
    PowerManager pm ;
    PowerManager.WakeLock wl ;

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i("@@@@@","onStart "+intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // return super.onStartCommand(intent, flags, startId);
        startForeground(ONGOING_NOTIFICATION_ID, mBuilder.getNotification() );

        Toast.makeText(this, "service starting", Toast.LENGTH_LONG).show();
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        registerReceiver(mBtStateReceiver, makeBtSmartScanIntentFilter());

        repeatScanLeDevice();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        mNotificationManager.cancel(ONGOING_NOTIFICATION_ID);
        isRunning = false;


    }

    private static IntentFilter makeBtSmartScanIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        return intentFilter;
    }

    /*
    public void showToastInIntentService(final String sText) {
        final Context MyContext = this;
        new Handler(Looper.getMainLooper()).post(new Runnable()
        {  @Override public void run()
            {  Toast toast1 = Toast.makeText(MyContext, sText, Toast.LENGTH_LONG);
                toast1.show();
            }
        });
    }
    */
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }

// Messaging with Activity
//-----------------------------------------------------------------------


    private final IBinder mIBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {

        public MainService getService() {
            return MainService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        return mMessenger.getBinder();
    }


    ArrayList<Messenger> mClients = new ArrayList<>();

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
//                case MSG_SET_INT_VALUE:
//                    incrementby = msg.arg1;
//                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToUI(int intvaluetosend) {
        Log.i("SERVICE ##","sendMessageToUI called!");
        for (int i=mClients.size()-1; i>=0; i--) {
            try {
                // Send data as an Integer
                mClients.get(i).send(Message.obtain(null, MSG_SET_PAGE, intvaluetosend, 0));

                //Send data as a String
//                Bundle b = new Bundle();
//                b.putString("str1", "ab" + intvaluetosend + "cd");
//                Message msg = Message.obtain(null, MSG_SET_STRING_VALUE);
//                msg.setData(b);
//                mClients.get(i).send(msg);

            }
            catch (RemoteException e) {
                mClients.remove(i);
            }
        }
    }

//-----------------------------------------------------------------------



}
