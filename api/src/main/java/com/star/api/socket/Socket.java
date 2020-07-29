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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Socket主类
 */
public class Socket extends WebSocketListener {

    private Handler handler;

    private OkHttpClient client;
    private WebSocket socket;

    private Class serviceClass;
    private Object service; //用于发送消息Service

    private SocketState state = SocketState.None;   //连接状态

    private List<Object> receivers = new ArrayList<>(); //接收器
    private SocketConverter convert = new GSONConverter();  //消息转换器
    private SocketInterceptor interceptor;  //拦截器
    private List<SocketStateListener> socketStateListeners = new ArrayList<>();

    private long reconnectDelay = 3 * 1000;    //重连间隔

    public Socket(Class serviceClass, OkHttpClient client) {
        this.serviceClass = serviceClass;
        this.client = client;
        this.service = getProxy(serviceClass);
        handler = new Handler(Looper.getMainLooper());
        SocketManager.getInstance().register(this);
    }

    /**
     * 开始连接
     */
    public void connect(String url) {
        onStateChanged(SocketState.Connecting);
        this.socket = client.newWebSocket(new Request.Builder().url(url).build(), this);
    }

    public void setConvert(SocketConverter convert) {
        this.convert = convert;
    }

    public void setInterceptor(SocketInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void addSocketStateListener(SocketStateListener listener) {
        this.socketStateListeners.add(listener);
    }

    public void removeSocketStateListener(SocketStateListener listener){
        socketStateListeners.remove(listener);
    }

    public void clearSocketStateListener() {
        socketStateListeners.clear();
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

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        onStateChanged(SocketState.Connected);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        onStateChanged(SocketState.Disconnect);
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        onStateChanged(SocketState.ConnectFail);
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

    private void onStateChanged(SocketState state) {
        if (state == SocketState.ConnectFail) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    reconnect();
                }
            }, reconnectDelay);
        }
        this.state = state;
        for (SocketStateListener listener : socketStateListeners) {
            listener.onStateChanged(state);
        }
    }

    /**
     * 重新连接
     */
    private void reconnect() {
        if (socket != null && state != SocketState.Reconnecting) {
            onStateChanged(SocketState.Reconnecting);
            socket = client.newWebSocket(socket.request(), this);
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
        onStateChanged(SocketState.Close);
        socketStateListeners.clear();
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
