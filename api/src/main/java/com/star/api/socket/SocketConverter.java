package com.star.api.socket;

import java.util.Map;

/**
 * 说明：数据转换器
 * 时间：2020/6/28 22:26
 */
public interface SocketConverter {

    /**
     * 发送时
     */
    String convertSend(Map<String, Object> params);

    /**
     * 接收时
     */
    Map<String, Object> convertReceive(String message);

}
