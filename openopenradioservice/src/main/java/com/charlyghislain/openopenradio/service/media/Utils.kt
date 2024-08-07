package com.charlyghislain.openopenradio.service.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.common.util.concurrent.JdkFutureAdapters
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume


fun <T> LiveData<T>.asListenableFuture(): ListenableFuture<T> {
    val future = SettableFuture.create<T>()
    val observer = object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            future.set(value)
        }
    }
    observeForever(observer)
    return future
}
