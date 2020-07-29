package com.star.api.socket;

import com.alibaba.fastjson.JSON;

import java.util.Map;

public class GSONConverter implements SocketConverter {
    @Override
    public String convertSend(Map<String, Object> params) {
        return JSON.toJSONString(params);
    }

    @Override
    public Map<String, Object> convertReceive(String message) {
        return JSON.parseObject(message);
    }
}
