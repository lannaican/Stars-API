package com.star.api.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.annotations.Nullable;
import retrofit2.Retrofit;

public final class CallBackCallAdapterFactory extends retrofit2.CallAdapter.Factory {

    public static CallBackCallAdapterFactory create() {
        return new CallBackCallAdapterFactory();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    @Nullable
    public retrofit2.CallAdapter get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        Class<?> rawType = getRawType(returnType);
        if (rawType == CallBack.class && returnType instanceof ParameterizedType) {
            Type type = getParameterUpperBound(0, (ParameterizedType) returnType);
            return new CallBackCallAdapter(type);
        }
        return null;
    }
}

