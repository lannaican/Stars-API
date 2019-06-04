package com.star.api.socket;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/4 9:10
 */
public interface SocketBody {

    void url(String url);

    void addField(String key, Object value);

    String toMessage();
}
