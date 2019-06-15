package com.star.api.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/6/2 15:38
 */
public class LifecycleManager implements Application.ActivityLifecycleCallbacks {

    private Map<Activity, CompositeDisposable> map = new HashMap<>();
    private CompositeDisposable disposable;

    private static LifecycleManager instance;

    public static void register(Application application) {
        instance = new LifecycleManager();
        application.registerActivityLifecycleCallbacks(instance);
    }

    public static LifecycleManager getInstance() {
        return instance;
    }

    public void add(Disposable disposable) {
        if (this.disposable != null) {
            this.disposable.add(disposable);
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        CompositeDisposable disposable = new CompositeDisposable();
        map.put(activity, disposable);
        this.disposable = disposable;
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        disposable = map.get(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity.isFinishing()) {
            remove(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        remove(activity);
    }

    private void remove(Activity activity) {
        CompositeDisposable disposable = map.remove(activity);
        if (disposable != null) {
            disposable.clear();
        }
        if (this.disposable == disposable) {
            this.disposable = null;
        }
    }

}
