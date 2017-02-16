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
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.budiyev.rssreader.model.data.Feed;
import com.budiyev.rssreader.model.data.Message;

import java.util.List;

public final class PreferencesHelper {
    public static final int NOT_DEFINED = -1;
    private static final String PREFERENCES_NAME = "rss_reader_main";
    private static final String PREFIX_URL = "url_";
    private static final String PREFIX_FEED = "feed_";
    private static final String PREFIX_GUID = "guid_";
    private static final String PREFIX_POSITION = "position_";
    private static final String PREFIX_UPDATE_INTERVAL = "update_interval_";
    private static final String PREFIX_UPDATE_TIME = "update_time_";
    private static final String NULL_STRING = "null";
    private static final char SEPARATOR = ',';
    private static final int COLUMN_TITLE = 0;
    private static final int COLUMN_DESCRIPTION = 1;
    private static final int COLUMN_LINK = 2;
    private static final int COLUMN_LANGUAGE = 3;
    private static final int COLUMN_COPYRIGHT = 4;
    private static final int COLUMN_PUBLISH_DATE = 5;
    private static final int COLUMN_AUTHOR = 3;
    private static final int COLUMN_GUID = 4;
    private static final int INFO_COLUMNS = 6;
    private static final int MESSAGE_COLUMNS = 5;

    @NonNull
    private static SharedPreferences getPreferences(@NonNull Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    private static String getUrlKey(int widgetId) {
        return PREFIX_URL + widgetId;
    }

    @NonNull
    private static String getFeedKey(int widgetId) {
        return PREFIX_FEED + widgetId;
    }

    @NonNull
    private static String getGuidKey(int widgetId) {
        return PREFIX_GUID + widgetId;
    }

    @NonNull
    private static String getPositionKey(int widgetId) {
        return PREFIX_POSITION + widgetId;
    }

    @NonNull
    private static String getUpdateIntervalKey(int widgetId) {
        return PREFIX_UPDATE_INTERVAL + widgetId;
    }

    @NonNull
    private static String getUpdateTimeKey(int widgetId) {
        return PREFIX_UPDATE_TIME + widgetId;
    }

    public static void removeUrl(@NonNull Context context, int widgetId) {
        getPreferences(context).edit().remove(getUrlKey(widgetId)).apply();
    }

    public static void setUrl(@NonNull Context context, int widgetId, @Nullable String url) {
        getPreferences(context).edit().putString(getUrlKey(widgetId), url).apply();
    }

    @Nullable
    public static String getUrl(@NonNull Context context, int widgetId) {
        return getPreferences(context).getString(getUrlKey(widgetId), null);
    }

    public static void removeFeed(@NonNull Context context, int widgetId) {
        getPreferences(context).edit().remove(getFeedKey(widgetId)).apply();
    }

    public static void setFeed(@NonNull Context context, int widgetId, @NonNull Feed feed) {
        StringTable table = new StringTable();
        table.add(feed.getTitle(), feed.getDescription(), feed.getLink(), feed.getLanguage(),
                feed.getCopyright(), feed.getPublishDate());
        for (Message message : feed.getMessages()) {
            table.add(message.getTitle(), message.getDescription(), message.getLink(),
                    message.getAuthor(), message.getGuid());
        }
        getPreferences(context).edit()
                .putString(getFeedKey(widgetId), CsvParser.encode(table, SEPARATOR)).apply();
    }

    @Nullable
    public static Feed getFeed(@NonNull Context context, int widgetId) {
        String feedString = getPreferences(context).getString(getFeedKey(widgetId), null);
        if (feedString == null) {
            return null;
        }
        StringTable table = CsvParser.parse(feedString, SEPARATOR);
        StringRow infoRow = table.row(0);
        if (infoRow.size() != INFO_COLUMNS) {
            return null;
        }
        Feed feed = new Feed(getCell(infoRow, COLUMN_TITLE), getCell(infoRow, COLUMN_DESCRIPTION),
                getCell(infoRow, COLUMN_LINK), getCell(infoRow, COLUMN_LANGUAGE),
                getCell(infoRow, COLUMN_COPYRIGHT), getCell(infoRow, COLUMN_PUBLISH_DATE));
        List<Message> messages = feed.getMessages();
        for (int i = 1, s = table.size(); i < s; i++) {
            StringRow messageRow = table.row(i);
            if (messageRow.size() != MESSAGE_COLUMNS) {
                continue;
            }
            messages.add(new Message(getCell(messageRow, COLUMN_TITLE),
                    getCell(messageRow, COLUMN_DESCRIPTION), getCell(messageRow, COLUMN_LINK),
                    getCell(messageRow, COLUMN_AUTHOR), getCell(messageRow, COLUMN_GUID)));
        }
        return feed;
    }

    public static void removeGuid(@NonNull Context context, int widgetId) {
        getPreferences(context).edit().remove(getGuidKey(widgetId)).apply();
    }

    public static void setGuid(@NonNull Context context, int widgetId, @Nullable String guid) {
        getPreferences(context).edit().putString(getGuidKey(widgetId), guid).apply();
    }

    @Nullable
    public static String getGuid(@NonNull Context context, int widgetId) {
        return getPreferences(context).getString(getGuidKey(widgetId), null);
    }

    public static void removePosition(@NonNull Context context, int widgetId) {
        getPreferences(context).edit().remove(getPositionKey(widgetId)).apply();
    }

    public static void setPosition(@NonNull Context context, int widgetId, int position) {
        getPreferences(context).edit().putInt(getPositionKey(widgetId), position).apply();
    }

    public static int getPosition(@NonNull Context context, int widgetId) {
        return getPreferences(context).getInt(getPositionKey(widgetId), -1);
    }

    public static void removeUpdateInterval(@NonNull Context context, int widgetId) {
        getPreferences(context).edit().remove(getUpdateIntervalKey(widgetId)).apply();
    }

    public static void setUpdateInterval(@NonNull Context context, int widgetId, int index) {
        getPreferences(context).edit().putInt(getUpdateIntervalKey(widgetId), index).apply();
    }

    public static int getUpdateInterval(@NonNull Context context, int widgetId) {
        return getPreferences(context)
                .getInt(getUpdateIntervalKey(widgetId), UpdateIntervalHelper.DEFAULT_INTERVAL);
    }

    public static void removeUpdateTime(@NonNull Context context, int widgetId) {
        getPreferences(context).edit().remove(getUpdateTimeKey(widgetId)).apply();
    }

    public static void setUpdateTime(@NonNull Context context, int widgetId, long time) {
        getPreferences(context).edit().putLong(getUpdateTimeKey(widgetId), time).apply();
    }

    public static long getUpdateTime(@NonNull Context context, int widgetId) {
        return getPreferences(context).getLong(getUpdateTimeKey(widgetId), NOT_DEFINED);
    }

    @Nullable
    private static String getCell(@NonNull StringRow row, int column) {
        return validate(row.cell(column));
    }

    @Nullable
    private static String validate(@Nullable String string) {
        if (string == null || string.equalsIgnoreCase(NULL_STRING)) {
            return null;
        } else {
            return string;
        }
    }
}
