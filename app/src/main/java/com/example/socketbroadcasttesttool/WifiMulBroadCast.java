package com.example.socketbroadcasttesttool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.socketbroadcasttesttool.client.Broadcaster;
import com.example.socketbroadcasttesttool.service.UdpService;
import com.example.socketbroadcasttesttool.utils.Constants;
import com.example.socketbroadcasttesttool.utils.LogUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;

public class WifiMulBroadCast extends BroadcastReceiver {
    private static final String TAG = "WifiMulBroadCast";
    public static final String KEY_ACQ = "acq";

    public static final String KEY_ACQ_ENABLE = "acqEnable";

    public static final String KEY_SERVER_PORT = "sPort";
    public static final String KEY_CLIENT_PORT = "cPort";
    public static final String KEY_CLIENT_MSG = "cMsg";
    public static final String KEY_CLIENT_OPEN = "cOpen";

    public static final String KEY_SERVER_OPEN = "sOpen";

    private Broadcaster broadcaster;
    private ResponseListener responseListener;

    private  Intent serviceIntent;

    WifiMulBroadCast(){

    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (broadcaster==null){
            broadcaster = new Broadcaster(context.getApplicationContext());
        }

        Log.d(TAG, "onReceive: "+intent.getAction());
        if (TextUtils.equals(intent.getAction(),MainActivity.ACTION_WIFI_MUL_BROADCAST)) {
            int acq = intent.getIntExtra(KEY_ACQ,-1);
            if (acq!=-1) {
                Log.d(TAG, "onReceive: available acq="+acq);
                broadcaster.setWiFiMulticastLock(acq == 1);
            }

            int sPort = intent.getIntExtra(KEY_SERVER_PORT,-1);
            int cPort = intent.getIntExtra(KEY_CLIENT_PORT,-1);
            if (sPort!=-1){
                Log.d(TAG, "onReceive: available serverPort="+sPort);
                Constants.serverPort = sPort;
            }
            if (cPort!=-1){
                Log.d(TAG, "onReceive: available clientPort="+cPort);
                Constants.clientPort = cPort;
            }


            String msg = intent.getStringExtra(KEY_CLIENT_MSG);
            if (!TextUtils.isEmpty(msg)){
                LogUtils.d(TAG, "onReceive: client send msg="+msg);
                if (!broadcaster.isOpened()){
                    Log.w(TAG, "onReceive: please open client socket first");
                }else {
                    broadcaster.sendPacket(msg.getBytes(StandardCharsets.UTF_8));
                }
            }

            if (intent.hasExtra(KEY_CLIENT_OPEN)){
                boolean opened = (intent.getIntExtra(KEY_CLIENT_OPEN,-1) == 1);
                Log.d(TAG, "onReceive:client "+(opened ?"open":"close"));
                if (opened && broadcaster.isOpened()){
                    Log.w(TAG, "onReceive: already opened client socket please close it first");
                }else if (!opened &&!broadcaster.isOpened()){
                    Log.w(TAG, "onReceive: already closed client socket please open it first" );
                }else {
                    if (opened){
                        LogUtils.d(TAG, "onReceive: client open port:"+Constants.clientPort);
                        responseListener = new ResponseListener(broadcaster);
                        responseListener.start();
                    }else {
                        if (responseListener!=null){
                            responseListener.close();
                        }
                    }
                }
            }

            if (intent.hasExtra(KEY_SERVER_OPEN)){
                int sOpen = intent.getIntExtra(KEY_SERVER_OPEN,-1);
                Log.d(TAG, "onReceive: server open ="+sOpen);
                if (sOpen==1) {
                    if (serviceIntent == null) {
                        serviceIntent = new Intent(context, UdpService.class);
                    }
                    context.startService(serviceIntent);
                }else {
                    context.stopService(serviceIntent);
                    serviceIntent=null;
                }
            }

            if (intent.hasExtra(KEY_ACQ_ENABLE)){
                int acqEnable = intent.getIntExtra(KEY_ACQ_ENABLE,-1);
                Constants.acqEnable = (acqEnable==1);
            }
        }
    }


    private static class ResponseListener extends Thread{
        private static final String TAG = "ClientListener";
        private boolean isFinish = false;
        private final Broadcaster broadcaster;
        private ResponseListener(Broadcaster broadcaster){
            this.broadcaster = broadcaster;
            this.broadcaster.open(Constants.clientPort,Constants.serverPort);
        }

        @Override
        public void run() {
            super.run();
            LogUtils.d(TAG, "run: client listener...");
            try {
                while(!isFinish) {
                    //监听回送端口
                    byte[] buf = new byte[1024];
                    //拿数据
                    DatagramPacket packet = broadcaster.recvPacket(buf);
                    if (packet==null){
                        LogUtils.w(TAG, "run: packet is null");
                        return;
                    }

                    //拿到发送端的一些信息
                    String ip = packet.getAddress().getHostAddress();
                    int port = packet.getPort();
                    int length = packet.getLength();
                    String msg = new String(buf, 0, length);
                    LogUtils.d(TAG, "监听到: " + ip + "\tport: " + port + "\t信息: " + msg);
                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }


        public void close(){
            Log.d(TAG, "close: ");
            if (broadcaster!=null){
                broadcaster.close();
            }
            isFinish = true;
        }
    }



}
