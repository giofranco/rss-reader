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

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.budiyev.rssreader.model.Feed;
import com.budiyev.rssreader.model.Message;
import com.budiyev.rssreader.model.Reader;
import com.budiyev.rssreader.widget.RssWidget;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ReaderHelper {
    private static final String WAKE_LOCK_TAG = "ReaderHelper";

    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private static final CollectionsHelper.Condition<Message> GUID_CONDITION =
            new CollectionsHelper.Condition<Message>() {
                @Override
                public boolean test(Message item, Message candidate) {
                    return item != null && candidate != null &&
                            Objects.equals(item.getGuid(), candidate.getGuid());
                }
            };

    private ReaderHelper() {
    }

    /**
     * Update feed for specified widget ID
     *
     * @param context  Context
     * @param widgetId Widget ID
     */
    public static void updateFeed(@NonNull Context context, int widgetId) {
        updateFeed(context, widgetId, false);
    }

    /**
     * Update feed for specified widget ID
     *
     * @param context     Context
     * @param widgetId    Widget ID
     * @param useWakeLock Use wake lock during update process
     */
    public static void updateFeed(@NonNull Context context, int widgetId, boolean useWakeLock) {
        EXECUTOR.execute(new ReadAction(context, widgetId, useWakeLock));
    }

    private static void sendUpdateIntent(@NonNull Context context, int widgetId,
            boolean needWakeLock) {
        context.sendBroadcast(
                RssWidget.buildIntent(context, widgetId, RssWidget.ACTION_UPDATE_WIDGET)
                        .putExtra(RssWidget.EXTRA_USE_WAKE_LOCK, needWakeLock));
    }

    private static void setFirstMessage(@NonNull Context context, int widgetId,
            @NonNull Feed feed) {
        PreferencesHelper.setFeed(context, widgetId, feed);
        PreferencesHelper.setPosition(context, widgetId, 0);
        PreferencesHelper.setGuid(context, widgetId, feed.getMessages().get(0).getGuid());
    }

    private static void clearData(@NonNull Context context, int widgetId) {
        PreferencesHelper.removeFeed(context, widgetId);
        PreferencesHelper.removeGuid(context, widgetId);
        PreferencesHelper.removePosition(context, widgetId);
    }

    private static class ReadAction implements Runnable {
        private final Context mContext;
        private final int mWidgetId;
        private final boolean mUseWakeLock;

        private ReadAction(@NonNull Context context, int widgetId, boolean useWakeLock) {
            mContext = context;
            mWidgetId = widgetId;
            mUseWakeLock = useWakeLock;
        }

        @Override
        public void run() {
            PowerManager.WakeLock wakeLock = null;
            if (mUseWakeLock) {
                wakeLock = WakeLockHelper.acquireWakeLock(mContext, WAKE_LOCK_TAG);
            }
            String url = PreferencesHelper.getUrl(mContext, mWidgetId);
            if (TextUtils.isEmpty(url) || !UrlHelper.validate(url)) {
                sendUpdateIntent(mContext, mWidgetId, mUseWakeLock);
                return;
            }
            Feed feed = PreferencesHelper.getFeed(mContext, mWidgetId);
            String guid = PreferencesHelper.getGuid(mContext, mWidgetId);
            if (feed == null || guid == null) {
                feed = Reader.read(url);
                if (feed != null && !feed.getMessages().isEmpty()) {
                    setFirstMessage(mContext, mWidgetId, feed);
                } else {
                    clearData(mContext, mWidgetId);
                }
            } else {
                int position = PreferencesHelper.getPosition(mContext, mWidgetId);
                Feed updateFeed = Reader.read(url);
                if (updateFeed == null) {
                    clearData(mContext, mWidgetId);
                } else {
                    if (position == PreferencesHelper.NO_POSITION || position == 0) {
                        setFirstMessage(mContext, mWidgetId, updateFeed);
                    } else {
                        int updatePosition = CollectionsHelper
                                .search(updateFeed.getMessages(), feed.getMessages().get(position),
                                        GUID_CONDITION, position, 4);
                        if (updatePosition < 0) {
                            setFirstMessage(mContext, mWidgetId, updateFeed);
                        } else {
                            PreferencesHelper.setFeed(mContext, mWidgetId, updateFeed);
                            PreferencesHelper.setPosition(mContext, mWidgetId, updatePosition);
                            PreferencesHelper.setGuid(mContext, mWidgetId,
                                    updateFeed.getMessages().get(updatePosition).getGuid());
                        }
                    }
                }
            }
            sendUpdateIntent(mContext, mWidgetId, mUseWakeLock);
            WakeLockHelper.releaseWakeLock(wakeLock);
        }
    }
}
