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

    public APIManager addService(Class cls, @NonNull Object service, @NonNull ServiceResolver resolver) {
        services.put(cls, service);
        resolvers.put(cls, resolver);
        return this;
    }

    @NonNull
    public Object getService(Class cls) {
        return services.get(cls);
    }

    public void removeService(Class cls) {
        services.remove(cls);
        resolvers.remove(cls);
    }

    public ServiceResolver getResolver(Class cls) {
        return resolvers.get(cls);
    }

    public APIManager setFailDefault(Fail response) {
        failDefault = response;
        return this;
    }

    public APIManager setListenerDefault(DefaultListener defaultListener) {
        this.defaultListener = defaultListener;
        return this;
    }

    public Fail getFailDefault() {
        return failDefault;
    }

    public Listener getListenerDefault() {
        return defaultListener.newInstance();
    }
}
