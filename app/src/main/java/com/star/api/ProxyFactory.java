package com.star.api;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/3 20:00
 */
public class ProxyFactory {

    @SuppressWarnings("unchecked")
    public static <T>T getProxy(Class<T> service) {
        return (T)Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class<?>[] { service }, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        log(method.getName());
                        return proxy;
                    }
                });
    }

    public static void log(String method) {
        Log.e("1", method);
    }

}
