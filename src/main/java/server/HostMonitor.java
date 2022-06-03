package server;

import kit.utilities.MysqlUtilities;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * 这是 host 主机用来监听新的连接的方法, 由于前端是另一个进程所以后端进程只需要一个守护进程,
 * 它负责持有一个 socket map, 每个创建出来的 socket thread 负责更新 UI thread 中的 log data, 日志是并行更新的, 需要一把锁.
 */
class HostMonitor
{
    private final int PORT = 5432; // 服务器监听端口

    public HostMonitor()
    {
        try( ServerSocket server_socket = new ServerSocket(PORT) ){

            // 服务器有一个数据库账号 (提供给 database 访问类)
            MysqlUtilities manager = new MysqlUtilities();

            // 服务器持有一个对 socket 编号的 hashmap
            Map< Integer, Socket > socketMap = new HashMap<>();

            // 已经建立了的 socket thread 数量, 每个 socket thread 编号不断增长
            int count = 0;

            JServer.update(JServer.MAIN, "服务器开始运行");

            // 开始监听
            while( true ){

                // 等待接受连接并且将通信转移到新的 service socket 上
                Socket service_socket = server_socket.accept();
                ServerSocketThread socket_thread = new ServerSocketThread(service_socket, manager, socketMap);
                socket_thread.setThreadID(count);
                count++;

                // 运行 socket thread
                socket_thread.start();

            }
        } catch(IOException e) {

            // 无法建立网络服务器 server socket
            JServer.update(JServer.MAIN, "无法建立网络服务器");

        }
    }
}