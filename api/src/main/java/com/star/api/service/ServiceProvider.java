package com.star.api.service;

import com.star.api.environment.Environment;

/**
 * 说明：
 * 时间：2020/5/12 14:46
 */
public interface ServiceProvider {

    Service[] getServices(Environment environment);

}
