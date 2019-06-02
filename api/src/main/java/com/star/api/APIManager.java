package com.star.api;

import android.app.Application;

import com.star.api.adapter.DefaultListener;
import com.star.api.adapter.Listener;
import com.star.api.adapter.callback.Fail;
import com.star.api.lifecycle.LifecycleManager;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.annotations.NonNull;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/4/26 10:24
 */
public class APIManager {

    private Map<Class, Object> services;
    private Map<Class, ServiceResolver> resolvers;

    private Fail failDefault;
    private DefaultListener defaultListener;
    
    private static APIManager instance;

    public static void init(Application application) {
        LifecycleManager.register(application);
    }

    public static APIManager getInstance() {
        if (instance == null) {
            instance = new APIManager();
        }
        return instance;
    }

    private APIManager() {
        services = new HashMap<>();
        resolvers = new HashMap<>();
    }

    public void addService(Class cls, @NonNull Object service, @NonNull ServiceResolver resolver) {
        services.put(cls, service);
        resolvers.put(cls, resolver);
    }

    @NonNull
    public Object getService(Class cls) {
        return services.get(cls);
    }

    public ServiceResolver getResolver(Class cls) {
        return resolvers.get(cls);
    }

    public void setFailDefault(Fail response) {
        failDefault = response;
    }

    public void setListenerDefault(DefaultListener defaultListener) {
        this.defaultListener = defaultListener;
    }

    public Fail getFailDefault() {
        return failDefault;
    }

    public Listener getListenerDefault() {
        return defaultListener.newInstance();
    }
}
