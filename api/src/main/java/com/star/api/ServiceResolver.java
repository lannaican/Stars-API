package com.star.api;

import com.star.api.adapter.CallBack;

import io.reactivex.Observable;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/5/31 21:47
 */
public interface ServiceResolver<T> {

    /**
     * 处理返回结果
     */
    void resolver(CallBack<T> callBack, T t);

    /**
     * 发生错误
     */
    void error(CallBack<T> callBack, Throwable e);

}
