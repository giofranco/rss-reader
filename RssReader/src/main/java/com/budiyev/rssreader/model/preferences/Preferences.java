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
package com.budiyev.rssreader.model.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.budiyev.rssreader.helper.CsvParser;
import com.budiyev.rssreader.helper.StringRow;
import com.budiyev.rssreader.helper.StringTable;
import com.budiyev.rssreader.helper.UpdateIntervalHelper;
import com.budiyev.rssreader.model.data.FeedInfo;

import java.util.ArrayList;
import java.util.List;

public final class Preferences {
    private static final String PREFERENCES_NAME = "rss_reader_main";
    private static final String PREFIX_URL = "url_";
    private static final String PREFIX_FEED = "feed_";
    private static final String PREFIX_GUID = "guid_";
    private static final String PREFIX_POSITION = "position_";
    private static final String PREFIX_UPDATE_INTERVAL = "update_interval_";
    private static final String PREFIX_UPDATE_TIME = "update_time_";
    private static final String KEY_INFO_LIST = "info_list";
    private static final String NULL_STRING = "null";

    @NonNull
    private static SharedPreferences getPreferences(@NonNull Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @NonNull
    private static String getUrlKey(int widgetId) {
        return PREFIX_URL + widgetId;
    }

    @NonNull
    private static String getFeedKey(@NonNull String url) {
        return PREFIX_FEED + url;
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

    public static void removeInfoList(@NonNull Context context) {
        getPreferences(context).edit().remove(KEY_INFO_LIST).apply();
    }

    public static void setInfoList(@NonNull Context context, @NonNull List<FeedInfo> feedInfoList) {
        getPreferences(context).edit().putString(KEY_INFO_LIST, encodeInfoList(feedInfoList))
                .apply();
    }

    @Nullable
    public static List<FeedInfo> getInfoList(@NonNull Context context) {
        String infoString = getPreferences(context).getString(KEY_INFO_LIST, null);
        if (infoString == null) {
            return null;
        }
        return decodeInfoList(infoString);
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
        return getPreferences(context).getLong(getUpdateTimeKey(widgetId), Constants.NOT_DEFINED);
    }

    @Nullable
    public static String getCell(@NonNull StringRow row, int column) {
        return validate(row.cell(column));
    }

    @NonNull
    private static String encodeInfoList(@NonNull List<FeedInfo> feedInfoList) {
        StringTable table = new StringTable();
        for (FeedInfo info : feedInfoList) {
            table.add(info.getAddress(), info.getTitle(), info.getDescription(), info.getLink(),
                    info.getLanguage(), info.getCopyright(), info.getPublishDate());
        }
        return CsvParser.encode(table, Constants.CSV_SEPARATOR);
    }

    @Nullable
    private static List<FeedInfo> decodeInfoList(@NonNull String infoString) {
        StringTable table = CsvParser.parse(infoString, Constants.CSV_SEPARATOR);
        List<FeedInfo> feedInfoList = new ArrayList<>(table.size());
        for (StringRow row : table) {
            if (row.size() != Constants.COLUMNS_COUNT_INFO) {
                continue;
            }
            feedInfoList.add(new FeedInfo(getCell(row, Constants.COLUMN_INFO_ADDRESS),
                    getCell(row, Constants.COLUMN_INFO_TITLE),
                    getCell(row, Constants.COLUMN_INFO_DESCRIPTION),
                    getCell(row, Constants.COLUMN_INFO_LINK),
                    getCell(row, Constants.COLUMN_INFO_LANGUAGE),
                    getCell(row, Constants.COLUMN_INFO_COPYRIGHT),
                    getCell(row, Constants.COLUMN_INFO_PUBLISH_DATE)));
        }
        return feedInfoList.isEmpty() ? null : feedInfoList;
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
