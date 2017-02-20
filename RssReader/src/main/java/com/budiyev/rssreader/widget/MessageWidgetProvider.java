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
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.activity.SettingsActivity;
import com.budiyev.rssreader.helper.CollectionsHelper;
import com.budiyev.rssreader.helper.ConnectivityHelper;
import com.budiyev.rssreader.helper.TextHelper;
import com.budiyev.rssreader.helper.UpdateIntervalHelper;
import com.budiyev.rssreader.helper.UrlHelper;
import com.budiyev.rssreader.helper.WakeLockHelper;
import com.budiyev.rssreader.model.Provider;
import com.budiyev.rssreader.model.Repository;
import com.budiyev.rssreader.model.data.Feed;
import com.budiyev.rssreader.model.data.Message;
import com.budiyev.rssreader.model.preferences.Constants;
import com.budiyev.rssreader.model.preferences.Preferences;

import java.util.List;

public class MessageWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_UPDATE_WIDGET =
            "com.yotatest.budiyev.rssreader.widget.ACTION_UPDATE_WIDGET";
    public static final String ACTION_SETTINGS_CHANGED =
            "com.yotatest.budiyev.rssreader.widget.ACTION_SETTINGS_CHANGED";
    public static final String EXTRA_USE_WAKE_LOCK = "use_wake_lock";
    public static final String EXTRA_URL_CHANGED = "url_changed";
    public static final String EXTRA_UPDATE_INTERVAL_CHANGED = "update_interval_changed";
    private static final String WAKE_LOCK_TAG = MessageWidgetProvider.class.getName();
    private static final String ACTION_UPDATE_DATA =
            "com.yotatest.budiyev.rssreader.widget.ACTION_UPDATE_DATA";
    private static final String ACTION_NEXT = "com.yotatest.budiyev.rssreader.widget.ACTION_NEXT";
    private static final String ACTION_PREVIOUS =
            "com.yotatest.budiyev.rssreader.widget.ACTION_PREVIOUS";
    private static final int RC_UPDATE_DATA = 100;
    private static final int RC_SETTINGS = 101;
    private static final int RC_LINK = 102;
    private static final int RC_PREVIOUS = 103;
    private static final int RC_NEXT = 104;
    private static final int RC_REFRESH = 105;

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager.WakeLock wakeLock = null;
        boolean useWakeLock = intent.getBooleanExtra(EXTRA_USE_WAKE_LOCK, false);
        if (useWakeLock) {
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
                clearFeed(context, widgetId);
                Provider.updateFeed(context, widgetId, useWakeLock);
            }
            if (interval) {
                setUpdateDataAlarm(context, widgetId);
            }
        } else if (ACTION_UPDATE_DATA.equals(action)) {
            cancelUpdateDataAlarm(context, widgetId);
            Provider.updateFeed(context, widgetId, useWakeLock);
            setUpdateDataAlarm(context, widgetId);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] widgetIds = getAppwidgetIds(context, manager);
            cancelUpdateDataAlarm(context, widgetIds);
            if (ConnectivityHelper.isConnectedToNetwork(context)) {
                for (int id : widgetIds) {
                    setUpdateDataAlarmRemainingAndUpdateIfNeeded(context, id);
                    updateWidget(context, manager, id);
                }
            } else {
                for (int id : widgetIds) {
                    updateWidget(context, manager, id);
                }
            }
        } else if (ACTION_UPDATE_WIDGET.equals(action)) {
            if (validateWidgetId(context, widgetId)) {
                updateWidget(context, AppWidgetManager.getInstance(context), widgetId);
            }
        } else if (ACTION_NEXT.equals(action)) {
            if (validateWidgetId(context, widgetId)) {
                int position = Preferences.getPosition(context, widgetId);
                if (position == Constants.NOT_DEFINED || position == Integer.MAX_VALUE) {
                    position = 0;
                } else {
                    position++;
                }
                Preferences.setPosition(context, widgetId, position);
                updateWidget(context, AppWidgetManager.getInstance(context), widgetId);
            }
        } else if (ACTION_PREVIOUS.equals(action)) {
            if (validateWidgetId(context, widgetId)) {
                int position = Preferences.getPosition(context, widgetId);
                if (position <= 1) {
                    position = 0;
                } else {
                    position--;
                }
                Preferences.setPosition(context, widgetId, position);
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
            cancelUpdateDataAlarm(context, widgetId);
            setUpdateDataAlarmRemainingAndUpdateIfNeeded(context, widgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            cancelUpdateDataAlarm(context, widgetId);
            clearPreferences(context, widgetId);
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        for (int i = 0; i < oldWidgetIds.length; i++) {
            int oldWidgetId = oldWidgetIds[i];
            int newWidgetId = newWidgetIds[i];
            Preferences.setUrl(context, newWidgetId, Preferences.getUrl(context, oldWidgetId));
            Preferences.setGuid(context, newWidgetId, Preferences.getGuid(context, oldWidgetId));
            Preferences.setPosition(context, newWidgetId,
                    Preferences.getPosition(context, oldWidgetId));
            Preferences.setUpdateInterval(context, newWidgetId,
                    Preferences.getUpdateInterval(context, oldWidgetId));
            Preferences.setUpdateTime(context, newWidgetId,
                    Preferences.getUpdateTime(context, oldWidgetId));
            clearPreferences(context, oldWidgetId);
        }
    }

    private static void updateWidget(@NonNull Context context, @NonNull AppWidgetManager manager,
            int widgetId) {
        manager.updateAppWidget(widgetId, getRemoteViews(context, widgetId));
    }

    @NonNull
    private static RemoteViews getRemoteViews(@NonNull Context context, int widgetId) {
        RemoteViews remoteViews;
        String url = Preferences.getUrl(context, widgetId);
        if (TextUtils.isEmpty(url)) {
            remoteViews = getErrorRemoteViews(context);
            remoteViews.setTextViewText(R.id.text, context.getText(R.string.url_is_not_specified));
            remoteViews.setViewVisibility(R.id.refresh, View.GONE);
        } else if (!UrlHelper.validate(url)) {
            remoteViews = getErrorRemoteViews(context);
            remoteViews.setTextViewText(R.id.text, context.getText(R.string.invalid_url));
            remoteViews.setViewVisibility(R.id.refresh, View.GONE);
        } else {
            Feed feed = Repository.getFeed(context, url);
            if (feed == null || feed.getMessages().isEmpty()) {
                remoteViews = getErrorRemoteViews(context);
                if (!ConnectivityHelper.isConnectedToNetwork(context)) {
                    remoteViews.setTextViewText(R.id.text, context.getText(R.string.no_connection));
                    remoteViews.setViewVisibility(R.id.refresh, View.GONE);
                } else {
                    remoteViews.setTextViewText(R.id.text, context.getText(R.string.no_data));
                    remoteViews.setViewVisibility(R.id.refresh, View.VISIBLE);
                    setRefreshOnClick(context, remoteViews, widgetId);
                }
            } else {
                int position = Preferences.getPosition(context, widgetId);
                if (position < 0) {
                    position = 0;
                }
                remoteViews = getDefaultRemoteViews(context);
                if (position <= 0) {
                    remoteViews.setViewVisibility(R.id.previous, View.GONE);
                    if (ConnectivityHelper.isConnectedToNetwork(context)) {
                        remoteViews.setViewVisibility(R.id.refresh, View.VISIBLE);
                        setRefreshOnClick(context, remoteViews, widgetId);
                    } else {
                        remoteViews.setViewVisibility(R.id.refresh, View.GONE);
                    }
                } else {
                    remoteViews.setViewVisibility(R.id.refresh, View.GONE);
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
                Preferences.setGuid(context, widgetId, message.getGuid());
                TextHelper.setTextViewHtml(remoteViews, R.id.header, message.getTitle());
                TextHelper.setTextViewHtml(remoteViews, R.id.text, message.getDescription());
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
        Intent intent = new Intent(context, MessageWidgetProvider.class);
        intent.setAction(action);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return intent;
    }

    @NonNull
    private static RemoteViews getDefaultRemoteViews(@NonNull Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.widget_message_default);
    }

    @NonNull
    private static RemoteViews getErrorRemoteViews(@NonNull Context context) {
        return new RemoteViews(context.getPackageName(), R.layout.widget_message_error);
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
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.validateScheme(link)));
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

    private static void setRefreshOnClick(@NonNull Context context,
            @NonNull RemoteViews remoteViews, int widgetId) {
        Intent intent = buildIntent(context, widgetId, ACTION_UPDATE_DATA);
        remoteViews.setOnClickPendingIntent(R.id.refresh, PendingIntent
                .getBroadcast(context, getRequestCode(RC_REFRESH, widgetId), intent,
                        PendingIntent.FLAG_CANCEL_CURRENT));
    }

    private static int[] getAppwidgetIds(@NonNull Context context) {
        return getAppwidgetIds(context, AppWidgetManager.getInstance(context));
    }

    private static int[] getAppwidgetIds(@NonNull Context context,
            @NonNull AppWidgetManager manager) {
        return manager.getAppWidgetIds(new ComponentName(context, MessageWidgetProvider.class));
    }

    private static boolean validateWidgetId(@NonNull Context context, int widgetId) {
        return CollectionsHelper.contains(getAppwidgetIds(context), widgetId);
    }

    private static int getWidgetId(@NonNull Intent intent) {
        return intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    private static void setUpdateDataAlarm(@NonNull Context context, int widgetId) {
        setUpdateDataAlarm(context, widgetId, SystemClock.elapsedRealtime() + UpdateIntervalHelper
                .getIntervalMillis(Preferences.getUpdateInterval(context, widgetId)));
    }

    private static void setUpdateDataAlarm(@NonNull Context context, int widgetId, long time) {
        getAlarmManager(context).set(AlarmManager.ELAPSED_REALTIME_WAKEUP, time,
                getUpdateDataPendingIntent(context, widgetId));
    }

    private void setUpdateDataAlarmRemainingAndUpdateIfNeeded(@NonNull Context context,
            int widgetId) {
        long updateInterval = UpdateIntervalHelper
                .getIntervalMillis(Preferences.getUpdateInterval(context, widgetId));
        long updateTime = Preferences.getUpdateTime(context, widgetId);
        long elapsedTime = System.currentTimeMillis() - updateTime;
        if (updateTime == Constants.NOT_DEFINED || updateInterval <= elapsedTime) {
            Provider.updateFeed(context, widgetId);
            setUpdateDataAlarm(context, widgetId);
        } else {
            setUpdateDataAlarm(context, widgetId,
                    SystemClock.elapsedRealtime() + updateInterval - elapsedTime);
        }
    }

    private static void cancelUpdateDataAlarm(@NonNull Context context, int widgetId) {
        getAlarmManager(context).cancel(getUpdateDataPendingIntent(context, widgetId));
    }

    private void cancelUpdateDataAlarm(@NonNull Context context, int[] widgetIds) {
        for (int id : widgetIds) {
            cancelUpdateDataAlarm(context, id);
        }
    }

    @NonNull
    private static AlarmManager getAlarmManager(@NonNull Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @NonNull
    private static PendingIntent getUpdateDataPendingIntent(@NonNull Context context,
            int widgetId) {
        return PendingIntent.getBroadcast(context, getRequestCode(RC_UPDATE_DATA, widgetId),
                buildIntent(context, widgetId, ACTION_UPDATE_DATA)
                        .putExtra(EXTRA_USE_WAKE_LOCK, true), PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static void clearPreferences(@NonNull Context context, int widgetId) {
        Preferences.removeUrl(context, widgetId);
        Preferences.removeUpdateInterval(context, widgetId);
        Preferences.removeUpdateTime(context, widgetId);
        clearFeed(context, widgetId);
    }

    private static void clearFeed(@NonNull Context context, int widgetId) {
        Preferences.removeGuid(context, widgetId);
        Preferences.removePosition(context, widgetId);
    }

    private static int getRequestCode(int base, int widgetId) {
        return base + widgetId + 10000;
    }
}
