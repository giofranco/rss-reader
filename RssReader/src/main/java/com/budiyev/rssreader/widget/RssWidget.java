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
package com.budiyev.rssreader.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.activity.SettingsActivity;
import com.budiyev.rssreader.helper.CollectionsHelper;
import com.budiyev.rssreader.helper.ConnectivityHelper;
import com.budiyev.rssreader.helper.PreferencesHelper;
import com.budiyev.rssreader.helper.ReaderHelper;
import com.budiyev.rssreader.helper.UpdateIntervalHelper;
import com.budiyev.rssreader.helper.UrlHelper;
import com.budiyev.rssreader.helper.WakeLockHelper;
import com.budiyev.rssreader.model.Feed;
import com.budiyev.rssreader.model.Message;

import java.util.List;

public class RssWidget extends AppWidgetProvider {
    public static final String ACTION_UPDATE_WIDGET =
            "com.yotatest.budiyev.rssreader.widget.ACTION_UPDATE_WIDGET";
    public static final String ACTION_SETTINGS_CHANGED =
            "com.yotatest.budiyev.rssreader.widget.ACTION_SETTINGS_CHANGED";
    public static final String EXTRA_USE_WAKE_LOCK = "use_wake_lock";
    public static final String EXTRA_URL_CHANGED = "url_changed";
    public static final String EXTRA_UPDATE_INTERVAL_CHANGED = "update_interval_changed";
    private static final String WAKE_LOCK_TAG = "RssWidget";
    private static final String ACTION_UPDATE_DATA =
            "com.yotatest.budiyev.rssreader.widget.ACTION_UPDATE_DATA";
    private static final String ACTION_NEXT = "com.yotatest.budiyev.rssreader.widget.ACTION_NEXT";
    private static final String ACTION_PREVIOUS =
            "com.yotatest.budiyev.rssreader.widget.ACTION_PREVIOUS";
    private static final String EM_DASH = "\u2014";
    private static final int RC_UPDATE_DATA = 100;
    private static final int RC_SETTINGS = 101;
    private static final int RC_LINK = 102;
    private static final int RC_PREVIOUS = 103;
    private static final int RC_NEXT = 104;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager.WakeLock wakeLock = null;
        if (intent.getBooleanExtra(EXTRA_USE_WAKE_LOCK, false)) {
            wakeLock = WakeLockHelper.acquireWakeLock(context, WAKE_LOCK_TAG);
        }
        String action = intent.getAction();
        int widgetId = getWidgetId(intent);
        if (ACTION_SETTINGS_CHANGED.equals(action)) {
            boolean interval = intent.getBooleanExtra(EXTRA_UPDATE_INTERVAL_CHANGED, false);
            if (interval) {
                cancelUpdateDataAlarm(context, widgetId);
            }
            if (intent.getBooleanExtra(EXTRA_URL_CHANGED, false)) {
                ReaderHelper.updateFeed(context, widgetId);
            }
            if (interval) {
                setUpdateDataAlarm(context, widgetId);
            }
        } else if (ACTION_UPDATE_DATA.equals(action)) {
            ReaderHelper.updateFeed(context, widgetId, true);
            setUpdateDataAlarm(context, widgetId);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            int[] appwidgetIds = getAppwidgetIds(context);
            for (int id : appwidgetIds) {
                cancelUpdateDataAlarm(context, id);
            }
            if (ConnectivityHelper.isConnectedToNetwork(context)) {
                for (int id : appwidgetIds) {
                    ReaderHelper.updateFeed(context, id, true);
                    setUpdateDataAlarm(context, id);
                }
            } else {
                AppWidgetManager instance = AppWidgetManager.getInstance(context);
                for (int id : appwidgetIds) {
                    updateWidget(context, instance, id);
                }
            }
        } else if (ACTION_UPDATE_WIDGET.equals(action)) {
            if (validateWidgetId(context, widgetId)) {
                updateWidget(context, AppWidgetManager.getInstance(context), widgetId);
            }
        } else if (ACTION_NEXT.equals(action)) {
            if (validateWidgetId(context, widgetId)) {
                int position = PreferencesHelper.getPosition(context, widgetId);
                if (position == PreferencesHelper.NO_POSITION || position == Integer.MAX_VALUE) {
                    position = 0;
                } else {
                    position++;
                }
                PreferencesHelper.setPosition(context, widgetId, position);
                updateWidget(context, AppWidgetManager.getInstance(context), widgetId);
            }
        } else if (ACTION_PREVIOUS.equals(action)) {
            if (validateWidgetId(context, widgetId)) {
                int position = PreferencesHelper.getPosition(context, widgetId);
                if (position <= 1) {
                    position = 0;
                } else {
                    position--;
                }
                PreferencesHelper.setPosition(context, widgetId, position);
                updateWidget(context, AppWidgetManager.getInstance(context), widgetId);
            }
        } else {
            super.onReceive(context, intent);
        }
        WakeLockHelper.releaseWakeLock(wakeLock);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            PreferencesHelper.removeUrl(context, widgetId);
            PreferencesHelper.removeFeed(context, widgetId);
            PreferencesHelper.removeGuid(context, widgetId);
            PreferencesHelper.removePosition(context, widgetId);
            PreferencesHelper.removeUpdateInterval(context, widgetId);
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        for (int i = 0; i < oldWidgetIds.length; i++) {
            int oldWidgetId = oldWidgetIds[i];
            int newWidgetId = newWidgetIds[i];
            PreferencesHelper
                    .setUrl(context, newWidgetId, PreferencesHelper.getUrl(context, oldWidgetId));
            Feed feed = PreferencesHelper.getFeed(context, oldWidgetId);
            if (feed != null) {
                PreferencesHelper.setFeed(context, newWidgetId, feed);
            }
            PreferencesHelper
                    .setGuid(context, newWidgetId, PreferencesHelper.getGuid(context, oldWidgetId));
            PreferencesHelper.setPosition(context, newWidgetId,
                    PreferencesHelper.getPosition(context, oldWidgetId));
            PreferencesHelper.setUpdateInterval(context, newWidgetId,
                    PreferencesHelper.getUpdateInterval(context, oldWidgetId));
            PreferencesHelper.removeUrl(context, oldWidgetId);
            PreferencesHelper.removeFeed(context, oldWidgetId);
            PreferencesHelper.removeGuid(context, oldWidgetId);
            PreferencesHelper.removePosition(context, oldWidgetId);
            PreferencesHelper.removeUpdateInterval(context, oldWidgetId);
        }
    }

    private static void updateWidget(@NonNull Context context,
            @NonNull AppWidgetManager appWidgetManager, int widgetId) {
        appWidgetManager.updateAppWidget(widgetId, getRemoteViews(context, widgetId));
    }

    @NonNull
    private static RemoteViews getRemoteViews(@NonNull Context context, int widgetId) {
        RemoteViews remoteViews;
        String url = PreferencesHelper.getUrl(context, widgetId);
        if (TextUtils.isEmpty(url)) {
            remoteViews = getErrorRemoteViews(context);
            remoteViews.setTextViewText(R.id.text, context.getText(R.string.url_is_not_specified));
        } else if (!UrlHelper.validate(url)) {
            remoteViews = getErrorRemoteViews(context);
            remoteViews.setTextViewText(R.id.text, context.getText(R.string.invalid_url));
        } else {
            Feed feed = PreferencesHelper.getFeed(context, widgetId);
            if (feed == null || feed.getMessages().isEmpty()) {
                remoteViews = getErrorRemoteViews(context);
                if (!ConnectivityHelper.isConnectedToNetwork(context)) {
                    remoteViews.setTextViewText(R.id.text, context.getText(R.string.no_connection));
                } else {
                    remoteViews.setTextViewText(R.id.text, context.getText(R.string.no_data));
                }
            } else {
                int position = PreferencesHelper.getPosition(context, widgetId);
                if (position < 0) {
                    position = 0;
                }
                remoteViews = getDefaultRemoteViews(context);
                if (position <= 0) {
                    remoteViews.setViewVisibility(R.id.previous, View.GONE);
                } else {
                    remoteViews.setViewVisibility(R.id.previous, View.VISIBLE);
                    setPreviousOnClick(context, remoteViews, widgetId);
                }
                List<Message> messages = feed.getMessages();
                if (position >= messages.size() - 1) {
                    remoteViews.setViewVisibility(R.id.next, View.GONE);
                } else {
                    remoteViews.setViewVisibility(R.id.next, View.VISIBLE);
                    setNextOnClick(context, remoteViews, widgetId);
                }
                Message message = messages.get(position);
                PreferencesHelper.setGuid(context, widgetId, message.getGuid());
                setTextViewText(remoteViews, R.id.header, message.getTitle());
                setTextViewText(remoteViews, R.id.text, message.getDescription());
                String link = message.getLink();
                if (TextUtils.isEmpty(link)) {
                    remoteViews.setViewVisibility(R.id.link, View.GONE);
                } else {
                    remoteViews.setViewVisibility(R.id.link, View.VISIBLE);
                    setLinkOnClick(context, remoteViews, R.id.link, link, widgetId);
                }
            }
        }
        setSettingsOnClick(context, remoteViews, widgetId);
        return remoteViews;
    }

    @NonNull
    public static Intent buildIntent(@NonNull Context context, int widgetId,
            @Nullable String action) {
        Intent intent = new Intent(context, RssWidget.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    @NonNull
    private static RemoteViews getDefaultRemoteViews(@NonNull Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.widget_rss_default);
    }

    @NonNull
    private static RemoteViews getErrorRemoteViews(@NonNull Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.widget_rss_error);
    }

    private static void setSettingsOnClick(@NonNull Context context,
            @NonNull RemoteViews remoteViews, int widgetId) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        remoteViews.setOnClickPendingIntent(R.id.settings, PendingIntent
                .getActivity(context, getRequestCode(RC_SETTINGS, widgetId), intent,
                        PendingIntent.FLAG_CANCEL_CURRENT));
    }

    private static void setLinkOnClick(@NonNull Context context, @NonNull RemoteViews remoteViews,
            @IdRes int viewId, @NonNull String link, int widgetId) {
        if (!link.contains("://")) {
            link = "http://" + link;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        remoteViews.setOnClickPendingIntent(viewId, PendingIntent
                .getActivity(context, getRequestCode(RC_LINK, widgetId), intent,
                        PendingIntent.FLAG_CANCEL_CURRENT));
    }

    private static void setNextOnClick(@NonNull Context context, @NonNull RemoteViews remoteViews,
            int widgetId) {
        Intent intent = buildIntent(context, widgetId, ACTION_NEXT);
        remoteViews.setOnClickPendingIntent(R.id.next, PendingIntent
                .getBroadcast(context, getRequestCode(RC_NEXT, widgetId), intent,
                        PendingIntent.FLAG_CANCEL_CURRENT));
    }

    private static void setPreviousOnClick(@NonNull Context context,
            @NonNull RemoteViews remoteViews, int widgetId) {
        Intent intent = buildIntent(context, widgetId, ACTION_PREVIOUS);
        remoteViews.setOnClickPendingIntent(R.id.previous, PendingIntent
                .getBroadcast(context, getRequestCode(RC_PREVIOUS, widgetId), intent,
                        PendingIntent.FLAG_CANCEL_CURRENT));
    }

    private static int[] getAppwidgetIds(@NonNull Context context) {
        return AppWidgetManager.getInstance(context)
                .getAppWidgetIds(new ComponentName(context, RssWidget.class));
    }

    private static boolean validateWidgetId(@NonNull Context context, int widgetId) {
        return CollectionsHelper.contains(getAppwidgetIds(context), widgetId);
    }

    private static int getWidgetId(@NonNull Intent intent) {
        return intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    private static void setTextViewText(@NonNull RemoteViews remoteViews, @IdRes int viewId,
            @Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            remoteViews.setTextViewText(viewId, EM_DASH);
        } else {
            remoteViews.setTextViewText(viewId, Html.fromHtml(text));
        }
    }

    private static void setUpdateDataAlarm(@NonNull Context context, int widgetId) {
        getAlarmManager(context).set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + UpdateIntervalHelper.INTERVALS[PreferencesHelper
                        .getUpdateInterval(context, widgetId)],
                getUpdateDataPendingIntent(context, widgetId));
    }

    private static void cancelUpdateDataAlarm(@NonNull Context context, int widgetId) {
        getAlarmManager(context).cancel(getUpdateDataPendingIntent(context, widgetId));
    }

    @NonNull
    private static AlarmManager getAlarmManager(@NonNull Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @NonNull
    private static PendingIntent getUpdateDataPendingIntent(@NonNull Context context,
            int widgetId) {
        return PendingIntent.getBroadcast(context, getRequestCode(RC_UPDATE_DATA, widgetId),
                new Intent(context, RssWidget.class).setAction(ACTION_UPDATE_DATA)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId),
                PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static int getRequestCode(int base, int widgetId) {
        return base + widgetId + 10000;
    }
}
