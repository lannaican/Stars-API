package com.star.api.socket;

import java.util.Map;

/**
 * 拦截器
 */
public interface SocketInterceptor {

    boolean onInterceptReceive(Map<String, Object> params);

}
