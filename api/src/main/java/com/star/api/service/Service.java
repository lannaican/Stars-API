package com.star.api.service;

import androidx.annotation.NonNull;

import com.star.api.resolver.ServiceResolver;

/**
 * 说明：
 * 时间：2020/5/12 14:46
 */
public class Service {

    private Object service;
    private Class cls;
    private ServiceResolver resolver;

    public Service(@NonNull Class cls, @NonNull Object service, @NonNull ServiceResolver resolver) {
        this.cls = cls;
        this.service = service;
        this.resolver = resolver;
    }

    public Class getCls() {
        return cls;
    }

    public void setCls(Class cls) {
        this.cls = cls;
    }

    public Object getService() {
        return service;
    }

    public void setService(@NonNull Object service) {
        this.service = service;
    }

    public ServiceResolver getResolver() {
        return resolver;
    }

    public void setResolver(@NonNull ServiceResolver resolver) {
        this.resolver = resolver;
    }
}
