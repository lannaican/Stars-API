package com.star.api.socket;

import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.star.annotation.Action;
import com.star.annotation.Field;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Socket主类
 */
public class Socket extends WebSocketListener {

    private Handler handler;

    private WebSocket socket;

    private Class serviceClass;
    private Object service; //用于发送消息Service

    private List<Object> receivers = new ArrayList<>(); //接收器
    private SocketConverter convert = new GSONConverter();  //消息转换器
    private SocketInterceptor interceptor;  //拦截器

    public Socket(Class serviceClass, WebSocket webSocket) {
        this.serviceClass = serviceClass;
        this.socket = webSocket;
        this.service = getProxy(serviceClass);
        handler = new Handler(Looper.getMainLooper());
        SocketManager.getInstance().register(this);
    }

    public void setConvert(SocketConverter convert) {
        this.convert = convert;
    }

    public void setInterceptor(SocketInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void addReceiver(Object receiver) {
        receivers.add(receiver);
    }

    public void removeReceiver(Object receiver) {
        receivers.remove(receiver);
    }

    public void clearReceiver() {
        receivers.clear();
    }

    public Class getServiceClass() {
        return serviceClass;
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
     * 消息结构
     * url: String
     * code: int
     * message: String
     * 其他: 自定义字段
     */
    @Override
    public void onMessage(final WebSocket webSocket, String text) {
        final Map<String, Object> result = convert.convertReceive(text);
        handler.post(new Runnable() {
            @Override
            public void run() {
                onMessage(webSocket, result);
            }
        });
    }

    /**
     * 主线程回调
     */
    private void onMessage(WebSocket webSocket, Map<String, Object> result) {
        //拦截器
        if (interceptor != null) {
            if (interceptor.onInterceptReceive(result)) {
                return;
            }
        }
        //正常反射
        for (final Object receiver : receivers) {
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

    /**
     * 关闭连接
     */
    public void close() {
        if (socket != null) {
            socket.close(1000, "Close");
            socket = null;
        }
        SocketManager.getInstance().unregister(getServiceClass());
    }

    /**
     * 发送消息
     */
    private void send(Map<String, Object> params) {
        if (socket != null) {
            socket.send(convert.convertSend(params));
        }
    }

}
