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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.budiyev.rssreader.helper.CsvParser;
import com.budiyev.rssreader.helper.HashHelper;
import com.budiyev.rssreader.helper.StringRow;
import com.budiyev.rssreader.helper.StringTable;
import com.budiyev.rssreader.helper.UrlHelper;
import com.budiyev.rssreader.model.data.Feed;
import com.budiyev.rssreader.model.data.FeedInfo;
import com.budiyev.rssreader.model.data.Message;
import com.budiyev.rssreader.model.preferences.Constants;
import com.budiyev.rssreader.model.preferences.Preferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Repository {
    private static final String FEED_FILE_PREFIX = "feed_";
    private static final Lock FEED_LOCK = new ReentrantLock();
    private static final Lock INFO_LOCK = new ReentrantLock();
    private static volatile Map<String, Feed> sFeedMap;
    private static volatile Reference<List<FeedInfo>> sInfoList;

    private Repository() {
    }

    @Nullable
    public static Feed getFeed(@NonNull Context context, @NonNull String url) {
        Map<String, Feed> feedMap = getFeedMap();
        url = UrlHelper.validateScheme(url);
        Feed feed;
        FEED_LOCK.lock();
        try {
            feed = feedMap.get(url);
        } finally {
            FEED_LOCK.unlock();
        }
        if (feed == null) {
            FEED_LOCK.lock();
            try {
                feed = feedMap.get(url);
                if (feed == null) {
                    feed = loadFeed(context, url);
                    if (feed != null) {
                        feedMap.put(url, feed);
                    }
                }
            } finally {
                FEED_LOCK.unlock();
            }
        }
        return feed;
    }

    public static void setFeed(@NonNull Context context, @NonNull String url, @NonNull Feed feed) {
        Map<String, Feed> feedMap = getFeedMap();
        url = UrlHelper.validateScheme(url);
        FEED_LOCK.lock();
        try {
            saveFeed(context, url, feed);
            feedMap.put(url, feed);
        } finally {
            FEED_LOCK.unlock();
        }
    }

    @Nullable
    public static List<FeedInfo> getInfoList(@NonNull Context context) {
        Reference<List<FeedInfo>> reference = sInfoList;
        if (reference == null) {
            INFO_LOCK.lock();
            try {
                reference = sInfoList;
                if (reference == null) {
                    reference = new WeakReference<>(Preferences.getInfoList(context));
                    sInfoList = reference;
                }
            } finally {
                INFO_LOCK.unlock();
            }
        }
        List<FeedInfo> infoList = reference.get();
        if (infoList == null) {
            INFO_LOCK.lock();
            try {
                reference = sInfoList;
                if (reference == null || (infoList = reference.get()) == null) {
                    infoList = Preferences.getInfoList(context);
                    sInfoList = new WeakReference<>(infoList);
                }
            } finally {
                INFO_LOCK.unlock();
            }
        }
        return infoList;
    }

    public static void setInfoList(@NonNull Context context, @NonNull List<FeedInfo> infoList) {
        INFO_LOCK.lock();
        try {
            Preferences.setInfoList(context, infoList);
            sInfoList = new WeakReference<>(infoList);
        } finally {
            INFO_LOCK.unlock();
        }
    }

    @Nullable
    public static List<FeedInfo> deleteInfo(@NonNull Context context, @NonNull FeedInfo info,
            int position) {
        INFO_LOCK.lock();
        try {
            List<FeedInfo> infoList = getInfoList(context);
            if (infoList == null) {
                return null;
            }
            if (!Objects.equals(info, infoList.get(position))) {
                return null;
            }
            infoList.remove(position);
            setInfoList(context, infoList);
            return infoList;
        } finally {
            INFO_LOCK.unlock();
        }
    }

    @NonNull
    public static List<FeedInfo> insertInfo(@NonNull Context context, @NonNull FeedInfo info,
            int position) {
        INFO_LOCK.lock();
        try {
            List<FeedInfo> infoList = getInfoList(context);
            if (infoList == null) {
                infoList = new ArrayList<>();
            }
            if (position == -1) {
                infoList.add(info);
            } else {
                infoList.add(position, info);
            }
            setInfoList(context, infoList);
            return infoList;
        } finally {
            INFO_LOCK.unlock();
        }
    }

    @NonNull
    private static Map<String, Feed> getFeedMap() {
        Map<String, Feed> map = sFeedMap;
        if (map == null) {
            FEED_LOCK.lock();
            try {
                map = sFeedMap;
                if (map == null) {
                    map = new WeakHashMap<>();
                    sFeedMap = map;
                }
            } finally {
                FEED_LOCK.unlock();
            }
        }
        return map;
    }

    @Nullable
    private static Feed loadFeed(@NonNull Context context, @NonNull String url) {
        File cacheFile = getCacheFile(context, url);
        if (!cacheFile.exists()) {
            return null;
        }
        try (InputStream inputStream = new FileInputStream(cacheFile)) {
            return decodeFeed(inputStream);
        } catch (IOException e) {
            return null;
        }
    }

    private static void saveFeed(@NonNull Context context, @NonNull String url,
            @NonNull Feed feed) {
        File cacheFile = getCacheFile(context, url);
        if (cacheFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cacheFile.delete();
        }
        try (OutputStream outputStream = new FileOutputStream(cacheFile)) {
            encodeFeed(outputStream, feed);
        } catch (IOException ignored) {
        }
    }

    @NonNull
    private static File getCacheDirectory(@NonNull Context context) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            cacheDir = context.getCacheDir();
        }
        return cacheDir;
    }

    @NonNull
    private static File getCacheFile(@NonNull Context context, @NonNull String url) {
        return new File(getCacheDirectory(context),
                FEED_FILE_PREFIX + HashHelper.generateSHA256(url));
    }

    private static void encodeFeed(@NonNull OutputStream outputStream, @NonNull Feed feed) {
        StringTable table = new StringTable();
        table.add(feed.getAddress(), feed.getTitle(), feed.getDescription(), feed.getLink(),
                feed.getLanguage(), feed.getCopyright(), feed.getPublishDate());
        for (Message message : feed.getMessages()) {
            table.add(message.getTitle(), message.getDescription(), message.getLink(),
                    message.getAuthor(), message.getGuid());
        }
        CsvParser.encode(table, outputStream, Constants.CSV_SEPARATOR, Constants.CSV_CHARSET);
    }

    @Nullable
    private static Feed decodeFeed(@NonNull InputStream inputStream) {
        StringTable table =
                CsvParser.parse(inputStream, Constants.CSV_SEPARATOR, Constants.CSV_CHARSET);
        if (table == null) {
            return null;
        }
        StringRow infoRow = table.row(0);
        if (infoRow.size() != Constants.COLUMNS_COUNT_INFO) {
            return null;
        }
        Feed feed = new Feed(Preferences.getCell(infoRow, Constants.COLUMN_INFO_ADDRESS),
                Preferences.getCell(infoRow, Constants.COLUMN_INFO_TITLE),
                Preferences.getCell(infoRow, Constants.COLUMN_INFO_DESCRIPTION),
                Preferences.getCell(infoRow, Constants.COLUMN_INFO_LINK),
                Preferences.getCell(infoRow, Constants.COLUMN_INFO_LANGUAGE),
                Preferences.getCell(infoRow, Constants.COLUMN_INFO_COPYRIGHT),
                Preferences.getCell(infoRow, Constants.COLUMN_INFO_PUBLISH_DATE));
        List<Message> messages = feed.getMessages();
        for (int i = 1, s = table.size(); i < s; i++) {
            StringRow messageRow = table.row(i);
            if (messageRow.size() != Constants.COLUMNS_COUNT_MESSAGE) {
                continue;
            }
            messages.add(
                    new Message(Preferences.getCell(messageRow, Constants.COLUMN_MESSAGE_TITLE),
                            Preferences.getCell(messageRow, Constants.COLUMN_MESSAGE_DESCRIPTION),
                            Preferences.getCell(messageRow, Constants.COLUMN_MESSAGE_LINK),
                            Preferences.getCell(messageRow, Constants.COLUMN_MESSAGE_AUTHOR),
                            Preferences.getCell(messageRow, Constants.COLUMN_MESSAGE_GUID)));
        }
        return feed;
    }
}
