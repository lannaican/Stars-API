package com.star.api.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

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
    private Handler handler;

    public SocketListener(Object receiver) {
        super();
        this.receiver = receiver;
        this.resultType = new TypeToken<Map<String, Object>>(){}.getType();
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        Map<String, String> result = GsonUtil.fromJson(text, resultType);
        for (final Method method : receiver.getClass().getMethods()) {
            SocketReceiver sr = method.getAnnotation(SocketReceiver.class);
            if (sr != null && sr.value().equals(result.get("url"))) {
                Annotation[][] annotations = method.getParameterAnnotations();
                final Object[] params = new Object[annotations.length];
                for (int i=0; i<annotations.length; i++) {
                    Annotation[] annotation = annotations[i];
                    if (annotation.length > 0 && annotation[0] instanceof Field) {
                        String key = ((Field)annotation[0]).value();
                        String param = result.get(key);
                        Type type = method.getParameterTypes()[i];
                        params[i] = GsonUtil.fromJson(param, type);
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
