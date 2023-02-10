package com.example.socketbroadcasttesttool.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

public class UdpService extends Service {
    private static final String TAG = "UdpService";
    Provider provider;
    private WifiManager.MulticastLock multicastLock;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        if (provider==null){
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            multicastLock = wifi.createMulticastLock("ServiceLock");
            provider= new Provider(multicastLock);
        }
        provider.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (provider!=null){
            provider.exit();
        }
        super.onDestroy();
    }
}
