package com.star.api.socket;

import java.util.Map;

/**
 * 说明：
 * 时间：2020/6/28 22:26
 */
public interface SocketConvert {

    String convertSend(Map<String, Object> params);

    Map<String, Object> convertReceive(String message);
}
