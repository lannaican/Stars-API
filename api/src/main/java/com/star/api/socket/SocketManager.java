package com.star.api.socket;

import java.util.HashMap;
import java.util.Map;

/**
 *
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
    public void register(Socket socket) {
        sockets.put(socket.getServiceClass(), socket);
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
