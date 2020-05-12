package com.star.api;

import android.app.Application;

import com.star.api.adapter.DefaultListener;
import com.star.api.adapter.Listener;
import com.star.api.adapter.callback.Fail;
import com.star.api.environment.Environment;
import com.star.api.lifecycle.LifecycleManager;
import com.star.api.resolver.ServiceResolver;
import com.star.api.service.Service;
import com.star.api.service.ServiceProvider;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.annotations.NonNull;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/4/26 10:24
 */
public class APIManager {

    private Environment environment;

    private Map<Class, Service> services;

    private Fail failDefault;
    private DefaultListener defaultListener;
    
    private static APIManager instance;

    /**
     * Application初始化
     */
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
    }

    /**
     * 设置环境
     */
    public void setEnvironment(@NonNull Environment environment, @NonNull ServiceProvider provider) {
        clear();
        this.environment = environment;
        for (Service service : provider.getServices(environment)) {
            services.put(service.getService().getClass(), service);
        }
    }

    public Environment getEnvironment() {
        return environment;
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

    public void removeService(Class cls) {
        services.remove(cls);
    }

    /**
     * 清除所有设置
     */
    public void clear() {
        services.clear();
        failDefault = null;
        defaultListener = null;
    }


    //Auto Call
    public final ServiceResolver getResolver(Class cls) {
        return services.get(cls).getResolver();
    }


    //Auto Call
    @NonNull
    public final Object getService(Class cls) {
        return services.get(cls).getService();
    }
}
