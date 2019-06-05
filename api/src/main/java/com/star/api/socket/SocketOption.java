package com.star.api.socket;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/4 9:21
 */
public interface SocketOption {

    OkHttpClient getClient();

    Request getRequest();

    SocketListener getListener();

}
