package com.star.api;

import com.star.annotation.APIService;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/3 19:58
 */
@APIService("SocketAPI")
public interface Service {

    void login(String uid);

    void logit();

}
