package com.star.api.socket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.star.annotation.Action;
import com.star.annotation.Field;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.WebSocket;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/4 9:04
 */
public class Socket {

    private WebSocket socket;
    private Object service;

    public Socket(Class service, SocketOption option) {
        OkHttpClient client = option.getClient();
        this.socket = client.newWebSocket(option.getRequest(), option.getListener());
        this.service = getProxy(service);
    }

    public Object getService() {
        return service;
    }

    @SuppressWarnings("unchecked")
    private <T>T getProxy(Class<T> service) {
        return (T)Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[]{service}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Action action = method.getAnnotation(Action.class);
                        String url = action.value();
                        Annotation[][] fields = method.getParameterAnnotations();
                        Map<String, Object> params = new HashMap<>();
                        params.put("url", url);
                        for (int i=0; i<fields.length; i++) {
                            for (Annotation annotation : fields[i]) {
                                if (annotation instanceof Field) {
                                    String name = ((Field) annotation).value();
                                    params.put(name, args[i]);
                                    break;
                                }
                            }
                        }
                        send(params);
                        return null;
                    }
                });
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (socket != null) {
            socket.close(1000, "Close");
            socket = null;
        }
    }

    /**
     * 发送消息
     */
    private void send(Map<String, Object> params) {
        if (socket != null) {
            socket.send(JSON.toJSONString(params));
        }
    }

}
