package com.star.api.socket;

import com.star.annotation.Action;
import com.star.annotation.Field;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/4 9:04
 */
public class Socket {

    private WebSocket socket;
    private SocketOption option;
    private Object service;

    public Socket(Class service, SocketOption option) {
        this.option = option;
        OkHttpClient client = option.createClient();
        this.socket = client.newWebSocket(option.createRequest(), option.createListener());
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
                        SocketBody call = option.createBody();
                        call.url(url);
                        for (int i=0; i<fields.length; i++) {
                            for (Annotation annotation : fields[i]) {
                                if (annotation instanceof Field) {
                                    String name = ((Field) annotation).value();
                                    call.addField(name, args[i]);
                                    break;
                                }
                            }
                        }
                        send(call);
                        return null;
                    }
                });
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (socket != null) {
            socket.cancel();
            socket.close(0, null);
            socket = null;
        }
    }

    /**
     * 发送消息
     */
    private void send(SocketBody body) {
        if (socket != null) {
            socket.send(body.toMessage());
        }
    }

}
