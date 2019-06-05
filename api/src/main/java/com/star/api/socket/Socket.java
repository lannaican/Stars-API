package com.star.api.socket;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.star.annotation.Action;
import com.star.annotation.Field;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Map;

import okhttp3.OkHttpClient;
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

    private Gson gson = new Gson();

    public Socket(Class service, SocketOption option) {
        this.option = option;
        OkHttpClient client = option.getClient();
        this.socket = client.newWebSocket(option.getRequest(), getListenerProxy(option.getListener()));
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

    @SuppressWarnings("unchecked")
    private <T extends WebSocketListener> T getListenerProxy(final T listener) {
        final Class cls = listener.getClass();
        final Type resultType = new TypeToken<Map<String, Object>>(){}.getType();
        return (T)Proxy.newProxyInstance(
                cls.getClassLoader(),
                new Class<?>[]{cls}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("onMessage")) {     //消息分发
                            if (args.length == 2 && args[1] instanceof String) {    //文本消息
                                Map<String, Object> result = gson.fromJson((String) args[1], resultType);
                                Method[] methods = cls.getMethods();
                                for (Method m : methods) {
                                    SocketReceiver receiver = m.getAnnotation(SocketReceiver.class);
                                    if (receiver != null && receiver.value().equals(result.get("url"))) {
                                        Annotation[][] annotations = m.getParameterAnnotations();
                                        Object[] params = new Object[annotations.length];
                                        for (int i=0; i<annotations.length; i++) {
                                            Annotation[] annotation = annotations[i];
                                            if (annotation[0] instanceof Field) {
                                                String key = ((Field)annotation[0]).value();
                                                params[i] = result.get(key);
                                            }
                                        }
                                        return m.invoke(listener, params);
                                    }
                                }
                            }
                        } else {
                            return method.invoke(proxy, args);
                        }
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
            socket.close(2000, "");
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
