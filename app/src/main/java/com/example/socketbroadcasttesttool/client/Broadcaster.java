package com.example.socketbroadcasttesttool.client;

import static android.content.Context.CONNECTIVITY_SERVICE;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.example.socketbroadcasttesttool.utils.LogUtils;


public class Broadcaster {
    private static final String TAG = "Broadcaster";

    private Context mContext;
    private int mDestPort = 0;
    private DatagramSocket mSocket;
    private WifiManager.MulticastLock multicastLock;
    private volatile boolean isOpened = false;


    public Broadcaster(Context context) {
        mContext = context;
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        multicastLock = wifi.createMulticastLock("multicastLock");
    }

    public boolean open(int localPort, int destPort) {
        isOpened = true;
        mDestPort = destPort;
        try {
            mSocket = new DatagramSocket(localPort);
            mSocket.setBroadcast(true);
            mSocket.setReuseAddress(true);
            return true;
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean close() {
        if (mSocket != null && !mSocket.isClosed()) {
            mSocket.close();
            mSocket = null;
        }
        isOpened = false;
        return true;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public boolean sendPacket(byte[] buffer) {
        try {
//            InetAddress addr = getBroadcastAddress(mContext);
            InetAddress addr = getBroadcast(getIpAddress());
            LogUtils.d(TAG, "sendPacket: broadcast address=" + addr.getHostAddress());
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            packet.setAddress(addr);
            packet.setPort(mDestPort);
            mSocket.send(packet);
            return true;
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public DatagramPacket recvPacket(byte[] buffer) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            mSocket.receive(packet);
            return packet;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public InetAddress getIpAddress() {
        InetAddress inetAddress = null;
        InetAddress myAddr = null;

        try {
            for (Enumeration<NetworkInterface> networkInterface = NetworkInterface
                    .getNetworkInterfaces(); networkInterface.hasMoreElements(); ) {

                NetworkInterface singleInterface = networkInterface.nextElement();

                for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses
                        .hasMoreElements(); ) {
                    inetAddress = IpAddresses.nextElement();
                    Log.d(TAG, "getIpAddress: singleInterface=" + singleInterface.getDisplayName());
                    if (!inetAddress.isLoopbackAddress() && (singleInterface.getDisplayName()
                            .contains("wlan0") ||
                            singleInterface.getDisplayName().contains("eth0") ||
                            singleInterface.getDisplayName().contains("ap0"))) {
                        myAddr = inetAddress;
                    }
                }
            }

        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return myAddr;
    }

    public InetAddress getBroadcast(InetAddress inetAddr) {
        NetworkInterface temp;
        InetAddress iAddr = null;
        try {
            temp = NetworkInterface.getByInetAddress(inetAddr);
            List<InterfaceAddress> addresses = temp.getInterfaceAddresses();
            for (InterfaceAddress inetAddress : addresses) {
                iAddr = inetAddress.getBroadcast();
                Log.d(TAG, "getBroadcast iAddr=" + iAddr);
            }
            return iAddr;

        } catch (SocketException e) {
            e.printStackTrace();
            Log.d(TAG, "getBroadcast" + e.getMessage());
        }
        return null;
    }

    public static InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
        if (isWifiApEnabled(context)) {
            return InetAddress.getByName("192.168.43.255");
        }
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        }
        return InetAddress.getByAddress(quads);
    }

    private boolean isWifiApOpened(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        Method[] wmMethods = wifi.getClass().getDeclaredMethods();
        for (Method method : wmMethods) {
            if (method.getName().equals("isWifiApEnabled")) {

                try {
                    if ((Boolean) method.invoke(wifi)) {
                        Log.d(TAG, "WifiTether on");
                        return true;
                    } else {
                        Log.d(TAG, "WifiTether off");
                    }
                } catch (IllegalArgumentException | InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

            }
        }
        return false;
    }

    protected static Boolean isWifiApEnabled(Context context) {
        boolean wifiAp = false;
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getMethod("isWifiApEnabled");
            wifiAp = (Boolean) method.invoke(manager);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (!wifiAp) {
                wifiAp = wifi.isConnected();
            }
        }
        return wifiAp;
    }


    public void setWiFiMulticastLock(boolean enable) {
        if (enable) {
            if (multicastLock.isHeld()) {
                Log.w(TAG, "WiFi multicast lock already acquired");
            } else {
                Log.i(TAG, "WiFi multicast lock acquired");
                multicastLock.acquire();
            }
        } else {
            if (multicastLock.isHeld()) {
                Log.i(TAG, "WiFi multicast lock released");
                multicastLock.release();
            } else {
                Log.w(TAG, "WiFi multicast lock already released");
            }
        }
    }


}