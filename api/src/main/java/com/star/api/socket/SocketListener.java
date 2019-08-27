package com.star.api.socket;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.star.annotation.Field;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/5 10:15
 */
public abstract class SocketListener extends WebSocketListener {

    private Object receiver;
    private Handler handler;

    public SocketListener(Object receiver) {
        super();
        this.receiver = receiver;
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * 服务端返回错误码
     */
    public abstract void onResultFail(int code, String message);

    /**
     * 消息结构
     * url: String
     * code: int
     * message: String
     * 其他: 自定义字段
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onMessage(WebSocket webSocket, String text) {
        final Map<String, Object> result = JSON.parseObject(text, Map.class);
        //错误处理
        final int code = (int)result.get("code");
        if (code != 200) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onResultFail(code, (String) result.get("message"));
                }
            });
            return;
        }
        //正常反射
        for (final Method method : receiver.getClass().getMethods()) {
            SocketReceiver sr = method.getAnnotation(SocketReceiver.class);
            if (sr != null && sr.value().equals(result.get("url"))) {
                Annotation[][] annotations = method.getParameterAnnotations();
                final Object[] params = new Object[annotations.length];
                for (int i=0; i<annotations.length; i++) {
                    Annotation[] annotation = annotations[i];
                    if (annotation.length > 0 && annotation[0] instanceof Field) {
                        String key = ((Field)annotation[0]).value();
                        Object o = result.get(key);
                        if (o == null) {
                            params[i] = null;
                            continue;
                        }
                        Type type = method.getGenericParameterTypes()[i];
                        if (o instanceof JSONObject) {
                            o = ((JSONObject) o).toJavaObject(type);
                        }
                        if (o instanceof JSONArray) {
                            o = JSON.parseObject(((JSONArray) o).toJSONString(), type);
                        }
                        switch (type.toString()) {
                            case "float":
                                params[i] = ((BigDecimal)o).floatValue();
                                break;
                            case "double":
                                params[i] = ((BigDecimal)o).doubleValue();
                                break;
                            default:
                                params[i] = o;
                                break;
                        }
                    }
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            method.invoke(receiver, params);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return;
            }
        }
    }
}
