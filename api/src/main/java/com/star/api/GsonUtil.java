package com.star.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

/**
 * Detail：
 * Author：Star
 * Create Time：2018/11/13 13:44
 */
public class GsonUtil {

    private static Gson gson;

    private static Gson create() {
        GsonBuilder builder = new GsonBuilder();
        return builder.create();
    }

    public static String toJson(Object o) {
        if (gson == null) {
            gson = create();
        }
        return gson.toJson(o);
    }

    public static <T>T fromJson(String json, Type type) {
        if (gson == null) {
            gson = create();
        }
        return gson.fromJson(json, type);
    }
}
