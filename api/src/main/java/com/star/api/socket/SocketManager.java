package com.star.api.socket;

import java.util.HashMap;
import java.util.Map;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/4 9:17
 */
public class SocketManager {

    private static SocketManager instance;

    private Map<Class, Socket> sockets = new HashMap<>();

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    /**
     * 注册
     */
    public void register(Class service, SocketOption option) {
        sockets.put(service, new Socket(service, option));
    }

    /**
     * 解除注册
     */
    public void unregister(Class service) {
        Socket socket = sockets.remove(service);
        if (socket != null) {
            socket.close();
        }
    }

    public Socket getSocket(Class cls) {
        return sockets.get(cls);
    }
}
