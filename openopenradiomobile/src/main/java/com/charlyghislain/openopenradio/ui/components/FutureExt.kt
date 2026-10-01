package com.charlyghislain.openopenradio.ui.components

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Suspends until the future completes, without blocking the calling thread. */
suspend fun <T> ListenableFuture<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addListener({
        try {
            continuation.resume(get())
        } catch (e: ExecutionException) {
            continuation.resumeWithException(e.cause ?: e)
        } catch (e: Exception) {
            continuation.resumeWithException(e)
        }
    }, MoreExecutors.directExecutor())
}
