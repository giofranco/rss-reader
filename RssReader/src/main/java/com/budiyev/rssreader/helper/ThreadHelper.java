/*
 * MIT License
 *
 * Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.rssreader.helper;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ThreadHelper {
    private static final Lock WORKER_THREAD_EXECUTOR_LOCK = new ReentrantLock();
    private static final Lock MAIN_THREAD_HANDLER_LOCK = new ReentrantLock();
    private static volatile ExecutorService sWorkerThreadExecutor;
    private static volatile Handler sMainThreadHandler;

    private ThreadHelper() {
    }

    @AnyThread
    public static void runOnWorkerThread(@NonNull Runnable action) {
        getWorkerThreadExecutor().submit(action);
    }

    @AnyThread
    public static void runOnMainThread(@NonNull Runnable action) {
        getMainThreadHandler().post(action);
    }

    @NonNull
    private static ExecutorService getWorkerThreadExecutor() {
        ExecutorService executor = sWorkerThreadExecutor;
        if (executor == null) {
            WORKER_THREAD_EXECUTOR_LOCK.lock();
            try {
                executor = sWorkerThreadExecutor;
                if (executor == null) {
                    int threads = Runtime.getRuntime().availableProcessors();
                    if (threads == 1) {
                        threads = 2;
                    } else {
                        threads = Math.round(threads * 1.5f);
                    }
                    executor = new ThreadPoolExecutor(threads, threads, 0L, TimeUnit.NANOSECONDS,
                            new LinkedBlockingQueue<Runnable>(), new AppThreadFactory()) {
                        @Override
                        protected void afterExecute(Runnable r, Throwable t) {
                            ThreadHelper.throwExecutionExceptionIfNeeded(r, t);
                        }
                    };
                    sWorkerThreadExecutor = executor;
                }
            } finally {
                WORKER_THREAD_EXECUTOR_LOCK.unlock();
            }
        }
        return executor;
    }

    @NonNull
    private static Handler getMainThreadHandler() {
        Handler handler = sMainThreadHandler;
        if (handler == null) {
            MAIN_THREAD_HANDLER_LOCK.lock();
            try {
                handler = sMainThreadHandler;
                if (handler == null) {
                    handler = new Handler(Looper.getMainLooper());
                    sMainThreadHandler = handler;
                }
            } finally {
                MAIN_THREAD_HANDLER_LOCK.unlock();
            }
        }
        return handler;
    }

    private static void throwExecutionExceptionIfNeeded(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?> && ((Future<?>) r).isDone()) {
            try {
                ((Future<?>) r).get();
            } catch (InterruptedException | CancellationException ignored) {
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private static final class AppThreadFactory implements ThreadFactory {
        private static final AtomicInteger COUNTER = new AtomicInteger();
        private static final String NAME_PREFIX = "Simply RSS worker thread #";

        @Override
        public Thread newThread(@NonNull Runnable runnable) {
            COUNTER.compareAndSet(Integer.MAX_VALUE, 0);
            Thread thread = new Thread(runnable, NAME_PREFIX + COUNTER.incrementAndGet());
            if (thread.getPriority() != Thread.MIN_PRIORITY) {
                thread.setPriority(Thread.MIN_PRIORITY);
            }
            if (thread.isDaemon()) {
                thread.setDaemon(false);
            }
            return thread;
        }
    }
}
