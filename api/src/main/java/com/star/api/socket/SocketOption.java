package com.star.api.socket;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocketListener;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/4 9:21
 */
public interface SocketOption {

    SocketBody createBody();

    OkHttpClient createClient();

    Request createRequest();

    WebSocketListener createListener();

}
