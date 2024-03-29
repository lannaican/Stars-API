package com.star.api.adapter;

import android.app.Activity;

import com.star.api.APIManager;
import com.star.api.adapter.callback.Cancel;
import com.star.api.adapter.callback.Complete;
import com.star.api.adapter.callback.Fail;
import com.star.api.adapter.callback.Success;
import com.star.api.lifecycle.LifecycleDisposable;
import com.star.api.lifecycle.LifecycleManager;
import com.star.api.resolver.ServiceResolver;

import java.lang.ref.WeakReference;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Detail：
 * Author：Stars
 * Create Time：2019/5/31 21:32
 */
public class CallBack<T> implements Observer<T> {

    private Observable<T> observable;
    private ServiceResolver<T> resolver;

    private Success<?> success;
    private Fail fail;
    private Cancel cancel;
    private Complete complete;

    private Object tag;

    private Listener listener;

    private Disposable disposable;

    //不绑定生命周期
    private boolean unbindLife;

    private WeakReference<Activity> bindActivity;

    public CallBack(Observable<T> observable) {
        this.observable = observable;
    }

    public Observable<T> getObservable() {
        return observable;
    }

    /**
     * 设置监听器
     */
    public CallBack<T> listener(Listener listener) {
        this.listener = listener;
        return this;
    }

    public CallBack<T> listenerDefault() {
        listener(APIManager.getInstance().getListenerDefault());
        return this;
    }

    /**
     * 成功回调
     */
    public CallBack<T> success(Success<?> response) {
        this.success = response;
        return this;
    }

    /**
     * 失败回调
     */
    public CallBack<T> fail(Fail response) {
        this.fail = response;
        return this;
    }

    public CallBack<T> failDefault() {
        fail(APIManager.getInstance().getFailDefault());
        return this;
    }

    /**
     * 取消回调
     */
    public CallBack<T> cancel(Cancel response) {
        this.cancel = response;
        return this;
    }

    /**
     * 完成回调
     */
    public CallBack<T> complete(Complete response) {
        this.complete = response;
        return this;
    }

    /**
     * 设置解析器
     */
    public CallBack<T> setResolver(ServiceResolver<T> resolver) {
        this.resolver = resolver;
        return this;
    }

    /**
     * 绑定Activity生命周期
     */
    public CallBack<T> setBindActivity(Activity activity) {
        this.bindActivity = new WeakReference<>(activity);
        return this;
    }

    /**
     * 独立生命周期
     */
    public CallBack<T> setUnbindLife(boolean unbindLife) {
        this.unbindLife = unbindLife;
        return this;
    }

    public CallBack<T> setTag(Object tag) {
        this.tag = tag;
        return this;
    }

    /**
     * 执行
     */
    public void go() {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this);
    }

    /**
     * 同步执行
     */
    public T execute() {
        return observable.blockingFirst();
    }

    /**
     * 获取回调
     */
    public Success getSuccess() {
        return success;
    }

    public Fail getFail() {
        return fail;
    }

    public Cancel getCancel() {
        return cancel;
    }

    public Complete getComplete() {
        return complete;
    }

    public Object getTag() {
        return tag;
    }

    /**
     * 取消请求
     */
    public void cancel() {
        if (disposable != null){
            disposable.dispose();
        }
        if (cancel != null) {
            cancel.onCancel();
        }
        callComplete();
    }

    @Override
    public void onSubscribe(Disposable d) {
        LifecycleDisposable lifecycleDisposable = new LifecycleDisposable(d);
        lifecycleDisposable.setOnDisposeListener(new LifecycleDisposable.OnDisposeListener() {
            @Override
            public void onDispose() {
                callComplete();
            }
        });
        disposable = lifecycleDisposable;
        if (!unbindLife) {
            if (bindActivity != null && bindActivity.get() != null) {
                LifecycleManager.getInstance().add(bindActivity.get(), disposable);
                bindActivity = null;
            } else {
                LifecycleManager.getInstance().add(disposable);
            }
        }
        if (listener != null) {
            listener.onStart(this);
        }
    }

    @Override
    public void onNext(T t) {
        resolver.resolver(this, t);
        callComplete();
    }

    public void callComplete() {
        if (complete != null) {
            complete.onComplete(this);
            complete = null;
        }
        if (listener != null) {
            listener.onComplete(this);
            listener = null;
        }
    }

    @Override
    public void onError(Throwable e) {
        resolver.error(this, e);
        callComplete();
    }

    @Override
    public void onComplete() {

    }
}
