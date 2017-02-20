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
package com.budiyev.rssreader.model;

import android.content.Context;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.budiyev.rssreader.helper.CollectionsHelper;
import com.budiyev.rssreader.helper.ThreadHelper;
import com.budiyev.rssreader.helper.UrlHelper;
import com.budiyev.rssreader.helper.WakeLockHelper;
import com.budiyev.rssreader.model.callback.FeedCallback;
import com.budiyev.rssreader.model.callback.InfoAddCallback;
import com.budiyev.rssreader.model.callback.InfoDeleteCallback;
import com.budiyev.rssreader.model.callback.InfoListCallback;
import com.budiyev.rssreader.model.callback.InfoLoadCallback;
import com.budiyev.rssreader.model.data.Feed;
import com.budiyev.rssreader.model.data.FeedInfo;
import com.budiyev.rssreader.model.data.Message;
import com.budiyev.rssreader.model.preferences.Constants;
import com.budiyev.rssreader.model.preferences.Preferences;
import com.budiyev.rssreader.widget.MessageWidgetProvider;

import java.util.List;
import java.util.Objects;

public final class Provider {
    private static final String WAKE_LOCK_TAG = Provider.class.getName();

    private static final CollectionsHelper.SearchCondition<Message> MESSAGE_SEARCH_CONDITION =
            new CollectionsHelper.SearchCondition<Message>() {
                @Override
                public boolean test(Message item, Message candidate) {
                    return Objects.equals(item, candidate);
                }
            };

    private Provider() {
    }

    public static void loadFeed(@NonNull Context context, @NonNull String url,
            @NonNull FeedCallback callback) {
        ThreadHelper.runOnWorkerThread(new LoadFeedAction(context, url, callback));
    }

    public static void updateFeed(@NonNull Context context, @NonNull String url,
            @NonNull FeedCallback callback) {
        ThreadHelper.runOnWorkerThread(new UpdateFeedAction(context, url, callback));
    }

    public static void updateFeed(@NonNull Context context, int widgetId) {
        ThreadHelper.runOnWorkerThread(new WidgetFeedUpdateAction(context, widgetId, false));
    }

    public static void updateFeed(@NonNull Context context, int widgetId, boolean useWakeLock) {
        ThreadHelper.runOnWorkerThread(new WidgetFeedUpdateAction(context, widgetId, useWakeLock));
    }

    public static void loadInfo(@NonNull String url, @NonNull InfoLoadCallback callback) {
        ThreadHelper.runOnWorkerThread(new LoadInfoAction(url, callback));
    }

    public static void insertInfo(@NonNull Context context, @NonNull FeedInfo info, int position,
            @NonNull InfoAddCallback callback) {
        ThreadHelper.runOnWorkerThread(new InsertInfoAction(context, info, position, callback));
    }

    @NonNull
    public static InfoDeleteDelegate deleteInfo(@NonNull Context context, @NonNull FeedInfo info,
            int position, @NonNull InfoDeleteCallback callback) {
        DeleteInfoAction deleteAction = new DeleteInfoAction(context, info, position, callback);
        ThreadHelper.runOnWorkerThread(deleteAction);
        return deleteAction.mDelegate;
    }

    public static void loadInfoList(@NonNull Context context, @NonNull InfoListCallback callback) {
        ThreadHelper.runOnWorkerThread(new LoadInfoListAction(context, callback));
    }

    public static void saveInfoList(@NonNull Context context, @NonNull List<FeedInfo> infoList) {
        ThreadHelper.runOnWorkerThread(new SaveInfoListAction(context, infoList));
    }

    public static class InfoDeleteDelegate {
        private final DeleteInfoAction mAction;

        private InfoDeleteDelegate(@NonNull DeleteInfoAction action) {
            mAction = action;
        }

        public boolean undo() {
            if (mAction.mDeleted) {
                ThreadHelper.runOnWorkerThread(new UndoDeleteInfoAction(mAction));
                return true;
            } else {
                return false;
            }
        }
    }

    private static class LoadFeedAction implements Runnable {
        private final Context mContext;
        private final String mUrl;
        private final FeedCallback mCallback;

        private LoadFeedAction(@NonNull Context context, @NonNull String url,
                @NonNull FeedCallback callback) {
            mContext = context;
            mUrl = url;
            mCallback = callback;
        }

        @Override
        public void run() {
            mCallback.onFeedLoaded(Repository.getFeed(mContext, mUrl));
        }
    }

    private static class UpdateFeedAction implements Runnable {
        private final Context mContext;
        private final String mUrl;
        private final FeedCallback mCallback;

        private UpdateFeedAction(@NonNull Context context, @NonNull String url,
                @NonNull FeedCallback callback) {
            mContext = context;
            mUrl = url;
            mCallback = callback;
        }

        @Override
        public void run() {
            Feed feed = Loader.loadFeed(mUrl);
            if (feed != null) {
                Repository.setFeed(mContext, mUrl, feed);
            }
            mCallback.onFeedUpdated(feed);
        }
    }

    private static class WidgetFeedUpdateAction implements Runnable {
        private final Context mContext;
        private final int mWidgetId;
        private final boolean mUseWakeLock;

        private WidgetFeedUpdateAction(@NonNull Context context, int widgetId,
                boolean useWakeLock) {
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
            String url = Preferences.getUrl(mContext, mWidgetId);
            if (TextUtils.isEmpty(url) || !UrlHelper.validate(url)) {
                sendUpdateIntent();
                return;
            }
            Feed feed = Repository.getFeed(mContext, url);
            String guid = Preferences.getGuid(mContext, mWidgetId);
            if (feed == null || guid == null) {
                feed = Loader.loadFeed(url);
                if (feed != null && !feed.getMessages().isEmpty()) {
                    setFirstMessage(feed, url);
                }
            } else {
                int position = Preferences.getPosition(mContext, mWidgetId);
                Feed updateFeed = Loader.loadFeed(url);
                if (updateFeed != null) {
                    if (position == Constants.NOT_DEFINED || position == 0) {
                        setFirstMessage(updateFeed, url);
                    } else {
                        int updatePosition = CollectionsHelper
                                .search(updateFeed.getMessages(), feed.getMessages().get(position),
                                        MESSAGE_SEARCH_CONDITION, position, 4, -1);
                        if (updatePosition < 0) {
                            setFirstMessage(updateFeed, url);
                        } else {
                            Repository.setFeed(mContext, url, updateFeed);
                            Preferences.setPosition(mContext, mWidgetId, updatePosition);
                            Preferences.setGuid(mContext, mWidgetId,
                                    updateFeed.getMessages().get(updatePosition).getGuid());
                        }
                    }
                }
            }
            Preferences.setUpdateTime(mContext, mWidgetId, System.currentTimeMillis());
            sendUpdateIntent();
            WakeLockHelper.releaseWakeLock(wakeLock);
        }

        private void setFirstMessage(@NonNull Feed feed, @NonNull String url) {
            Repository.setFeed(mContext, url, feed);
            Preferences.setPosition(mContext, mWidgetId, 0);
            Preferences.setGuid(mContext, mWidgetId, feed.getMessages().get(0).getGuid());
        }

        private void sendUpdateIntent() {
            mContext.sendBroadcast(MessageWidgetProvider
                    .buildIntent(mContext, mWidgetId, MessageWidgetProvider.ACTION_UPDATE_WIDGET)
                    .putExtra(MessageWidgetProvider.EXTRA_USE_WAKE_LOCK, mUseWakeLock));
        }
    }

    private static class LoadInfoAction implements Runnable {
        private final String mUrl;
        private final InfoLoadCallback mCallback;

        private LoadInfoAction(@NonNull String url, @NonNull InfoLoadCallback callback) {
            mUrl = url;
            mCallback = callback;
        }

        @Override
        public void run() {
            mCallback.onInfoLoaded(mUrl, Loader.loadInfo(mUrl));
        }
    }

    private static class InsertInfoAction implements Runnable {
        private final Context mContext;
        private final FeedInfo mInfo;
        private final int mPosition;
        private final InfoAddCallback mCallback;

        private InsertInfoAction(@NonNull Context context, @NonNull FeedInfo info, int position,
                @NonNull InfoAddCallback callback) {
            mContext = context;
            mInfo = info;
            mPosition = position;
            mCallback = callback;
        }

        @Override
        public void run() {
            mCallback.onInfoInserted(Repository.insertInfo(mContext, mInfo, mPosition), mInfo,
                    mPosition);
        }
    }

    private static class DeleteInfoAction implements Runnable {
        private final Context mContext;
        private final FeedInfo mInfo;
        private final int mPosition;
        private final InfoDeleteCallback mCallback;
        private final InfoDeleteDelegate mDelegate;
        private volatile boolean mDeleted;

        private DeleteInfoAction(@NonNull Context context, @NonNull FeedInfo info, int position,
                @NonNull InfoDeleteCallback callback) {
            mContext = context;
            mInfo = info;
            mPosition = position;
            mCallback = callback;
            mDelegate = new InfoDeleteDelegate(this);
        }

        @Override
        public void run() {
            List<FeedInfo> infoList = Repository.deleteInfo(mContext, mInfo, mPosition);
            if (infoList != null) {
                mDeleted = true;
                mCallback.onInfoDeleted(mDelegate, infoList, mInfo, mPosition);
            }
        }
    }

    public static class UndoDeleteInfoAction implements Runnable {
        private final DeleteInfoAction mAction;

        public UndoDeleteInfoAction(@NonNull DeleteInfoAction action) {
            mAction = action;
        }

        @Override
        public void run() {
            mAction.mCallback.onInfoRestored(
                    Repository.insertInfo(mAction.mContext, mAction.mInfo, mAction.mPosition),
                    mAction.mInfo, mAction.mPosition);
        }
    }

    private static class LoadInfoListAction implements Runnable {
        private final Context mContext;
        private final InfoListCallback mCallback;

        private LoadInfoListAction(@NonNull Context context, @NonNull InfoListCallback callback) {
            mContext = context;
            mCallback = callback;
        }

        @Override
        public void run() {
            mCallback.onInfoListLoaded(Repository.getInfoList(mContext));
        }
    }

    private static class SaveInfoListAction implements Runnable {
        private final Context mContext;
        private final List<FeedInfo> mInfoList;

        private SaveInfoListAction(@NonNull Context context, @NonNull List<FeedInfo> infoList) {
            mContext = context;
            mInfoList = infoList;
        }

        @Override
        public void run() {
            Repository.setInfoList(mContext, mInfoList);
        }
    }
}
