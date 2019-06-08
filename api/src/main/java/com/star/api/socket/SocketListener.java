package com.star.api.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.gson.reflect.TypeToken;
import com.star.annotation.Field;
import com.star.api.GsonUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
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
                        Type parameterType = method.getGenericParameterTypes()[i];
                        if (parameterType instanceof ParameterizedType) {
                            Type type = getParameterUpperBound(0, (ParameterizedType) parameterType);
                            params[i] = GsonUtil.fromJson(param, type);
                        } else {
                            params[i] = GsonUtil.fromJson(param, parameterType);
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

    private Class<?> getRawType(Type type) {
        if (type instanceof Class<?>) {
            // Type is a normal class.
            return (Class<?>) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
            // suspects some pathological case related to nested classes exists.
            Type rawType = parameterizedType.getRawType();
            if (!(rawType instanceof Class)) throw new IllegalArgumentException();
            return (Class<?>) rawType;
        }
        if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable) {
            // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
            // type that's more general than necessary is okay.
            return Object.class;
        }
        if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        }
        throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
                + "GenericArrayType, but <" + type + "> is of type " + type.getClass().getName());
    }

    private Type getParameterUpperBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) {
            throw new IllegalArgumentException(
                    "Index " + index + " not in range [0," + types.length + ") for " + type);
        }
        Type paramType = types[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }
}
