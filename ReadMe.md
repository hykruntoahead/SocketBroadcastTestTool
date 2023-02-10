
### 安装apk并启动:
adb install broadcastTool.apk
am start -n com.example.socketbroadcasttesttool/com.example.socketbroadcasttesttool.MainActivity

### 命令配置UDP Socket 客户端/服务端

1.Udp客户端port设置,重启客户端生效.默认端口号为7879，可不设置:

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --ei cPort 7778
    
2.Udp服务器端port设置,重启服务端生效.默认端口号为7878，可不设置:

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --ei sPort 7779
    
3.打开/关闭Udp客户端:

3.1 打开:

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --ei cOpen 1
    
3.2 关闭:

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --ei cOpen 0
    
4.打开/关闭Udp服务端:

3.1 打开:

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --ei sOpen 1
    
3.2 关闭:

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --ei sOpen 0
    
5.客户端发送广播消息，发送之前请确保客户端打开

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --es cMsg "this is a message from client.."
    
6.multicastLock acquire/release

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --ei acq 1/0
    
7.服务端receive 前后支持multicastLock acquire/release

    am broadcast -a gs.action.wifi.mul -f 0x01000000 --ei acqEnable 1/0
