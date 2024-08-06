package com.charlyghislain.openopenradio.service.util;

public interface RequestCallback<T> {
    void onSuccess(T value);

    void onError(Throwable error);
}
