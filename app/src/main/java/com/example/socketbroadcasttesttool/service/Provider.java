package com.example.socketbroadcasttesttool.service;

import android.net.wifi.WifiManager;
import android.os.Build;

import com.example.socketbroadcasttesttool.utils.Constants;
import com.example.socketbroadcasttesttool.utils.LogUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

class Provider extends Thread {
    private static final String TAG = "UdpService_Provider";
    private DatagramSocket socket;
    private boolean isFinish = false;
    private WifiManager.MulticastLock multicastLock;

    Provider(WifiManager.MulticastLock multicastLock) {
        this.multicastLock = multicastLock;
    }

    @Override
    public void run() {
        super.run();
        LogUtils.d(TAG, "UDP 服务端已经启动,监听端口:" + Constants.serverPort);
        try {
            //1.获取 datagramSocket 实例,并监听某个端口
            socket = new DatagramSocket(Constants.serverPort);
            while (!isFinish) {
                //2.创建一个 udp 的数据包
                byte[] buf = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                if (Constants.acqEnable) {
                    setWiFiMulticastLock(multicastLock, true);
                }
                //3.开始阻塞获取udp数据包
                socket.receive(packet);
                //拿到发送端的一些信息
                String ip = packet.getAddress().getHostAddress();
                int port = packet.getPort();
                int length = packet.getLength();

                String msg = new String(buf, 0, length);
                LogUtils.d(TAG, "客户端: " + ip + "\tport: " + port + "\t信息: " + msg);

                if (Constants.acqEnable) {
                    setWiFiMulticastLock(multicastLock, false);
                }

                /**
                 * 给客户端发送消息
                 */
                byte[] receiveMsg = ("我是服务端:" + getModel() + ";current timeMill:" + System.currentTimeMillis()).getBytes();

                DatagramPacket receivePacket = new DatagramPacket(receiveMsg,
                        receiveMsg.length,
                        packet.getAddress(), //目标地址
                        port);      //广播端口

                socket.send(receivePacket);
            }
            //关闭资源
            socket.close();
            if (Constants.acqEnable && multicastLock != null) {
                setWiFiMulticastLock(multicastLock, false);
            }
            System.out.println("结束");
        } catch (IOException e) {
            //  e.printStackTrace();
            //忽略错误
        } finally {
            exit();
        }
    }

    public void exit() {
        if (socket != null) {
            socket.close();
            socket = null;
        }
        isFinish = true;
    }

    /**
     * 获取手机Model型号
     */
    public static String getModel() {
        return Build.MANUFACTURER + " " + Build.MODEL;
    }

    private void setWiFiMulticastLock(WifiManager.MulticastLock multicastLock, boolean enable) {
        if (enable) {
            if (multicastLock.isHeld()) {
                LogUtils.w(TAG, "WiFi multicast lock already acquired");
            } else {
                LogUtils.i(TAG, "WiFi multicast lock acquired");
                multicastLock.acquire();
            }
        } else {
            if (multicastLock.isHeld()) {
                LogUtils.i(TAG, "WiFi multicast lock released");
                multicastLock.release();
            } else {
                LogUtils.w(TAG, "WiFi multicast lock already released");
            }
        }
    }
}

