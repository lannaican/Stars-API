package com.star.api.socket;

import com.google.gson.reflect.TypeToken;
import com.star.annotation.Field;
import com.star.api.GsonUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
    private Type resultType;

    public SocketListener(Object receiver) {
        super();
        this.receiver = receiver;
        this.resultType = new TypeToken<Map<String, Object>>(){}.getType();
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Map<String, Object> result = GsonUtil.fromJson(text, resultType);
        for (Method method : receiver.getClass().getMethods()) {
            SocketReceiver receiver = method.getAnnotation(SocketReceiver.class);
            if (receiver != null && receiver.value().equals(result.get("url"))) {
                Annotation[][] annotations = method.getParameterAnnotations();
                Object[] params = new Object[annotations.length];
                for (int i=0; i<annotations.length; i++) {
                    Annotation[] annotation = annotations[i];
                    if (annotation[0] instanceof Field) {
                        String key = ((Field)annotation[0]).value();
                        params[i] = result.get(key);
                    }
                }
                try {
                    method.invoke(receiver, params);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
