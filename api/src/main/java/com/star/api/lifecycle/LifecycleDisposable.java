package com.star.api.lifecycle;

import io.reactivex.disposables.Disposable;

/**
 * 说明：监听Disposable周期
 * 时间：2021/8/18 12:37
 */
public class LifecycleDisposable implements Disposable {

    private Disposable disposable;

    private OnDisposeListener onDisposeListener;

    public LifecycleDisposable(Disposable disposable) {
        this.disposable = disposable;
    }

    public void setOnDisposeListener(OnDisposeListener onDisposeListener) {
        this.onDisposeListener = onDisposeListener;
    }

    @Override
    public void dispose() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            if (onDisposeListener != null) {
                onDisposeListener.onDispose();
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return disposable != null && disposable.isDisposed();
    }

    public interface OnDisposeListener {
        void onDispose();
    }
}
