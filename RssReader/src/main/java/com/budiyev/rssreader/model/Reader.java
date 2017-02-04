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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class Reader {
    private static final String LOG_TAG = "Reader";
    private static final String RSS = "rss";
    private static final String CHANNEL = "channel";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String LANGUAGE = "language";
    private static final String COPYRIGHT = "copyright";
    private static final String LINK = "link";
    private static final String AUTHOR = "author";
    private static final String ITEM = "item";
    private static final String PUBLISH_DATE = "pubDate";
    private static final String GUID = "guid";
    private final URL mUrl;

    private Reader(@NonNull String url) {
        if (!url.contains("://")) {
            url = "http://" + url;
        }
        URL tmp;
        try {
            tmp = new URL(url);
        } catch (MalformedURLException e) {
            Log.w(LOG_TAG, "Malformed URL", e);
            tmp = null;
        }
        mUrl = tmp;
    }

    @Nullable
    private Feed read() {
        if (mUrl == null) {
            return null;
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) mUrl.openConnection();
            try (InputStream input = connection.getInputStream()) {
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(input, connection.getContentEncoding());
                parser.nextTag();
                return readMessages(parser, readFeedInfo(parser));
            }
        } catch (XmlPullParserException | IOException e) {
            Log.w(LOG_TAG, "Unable to read RSS feed", e);
            return null;
        }
    }

    @NonNull
    private Feed readFeedInfo(@NonNull XmlPullParser parser) throws IOException,
            XmlPullParserException {
        requireTag(parser, RSS);
        parser.nextTag();
        requireTag(parser, CHANNEL);
        String title = null;
        String description = null;
        String link = null;
        String language = null;
        String copyright = null;
        String publishDate = null;
        for (String name; ; ) {
            if (parser.next() == XmlPullParser.END_DOCUMENT) {
                break;
            }
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            name = parser.getName();
            if (ITEM.equals(name)) {
                break;
            }
            if (TITLE.equals(name)) {
                title = readText(parser);
            } else if (DESCRIPTION.equals(name)) {
                description = readText(parser);
            } else if (LINK.equals(name)) {
                link = readText(parser);
            } else if (LANGUAGE.equals(name)) {
                language = readText(parser);
            } else if (COPYRIGHT.equals(name)) {
                copyright = readText(parser);
            } else if (PUBLISH_DATE.equals(name)) {
                publishDate = readText(parser);
            }
        }
        return new Feed(title, description, link, language, copyright, publishDate);
    }

    @NonNull
    private Feed readMessages(@NonNull XmlPullParser parser, @NonNull Feed feed) throws IOException,
            XmlPullParserException {
        List<Message> messages = feed.getMessages();
        for (; ; ) {
            int eventType = parser.getEventType();
            if (eventType == XmlPullParser.END_DOCUMENT ||
                    CHANNEL.equals(parser.getName()) && eventType == XmlPullParser.END_TAG) {
                break;
            }
            if (eventType != XmlPullParser.START_TAG) {
                parser.next();
                continue;
            }
            messages.add(readMessage(parser));
        }
        return feed;
    }

    @NonNull
    private Message readMessage(@NonNull XmlPullParser parser) throws IOException,
            XmlPullParserException {
        requireTag(parser, ITEM);
        String title = null;
        String description = null;
        String link = null;
        String author = null;
        String guid = null;
        for (String name; ; ) {
            if (parser.next() == XmlPullParser.END_DOCUMENT) {
                break;
            }
            name = parser.getName();
            if (ITEM.equals(name) && parser.getEventType() == XmlPullParser.END_TAG) {
                break;
            }
            if (TITLE.equals(name)) {
                title = readText(parser);
            } else if (DESCRIPTION.equals(name)) {
                description = readText(parser);
            } else if (LINK.equals(name)) {
                link = readText(parser);
            } else if (AUTHOR.equals(name)) {
                author = readText(parser);
            } else if (GUID.equals(name)) {
                guid = readText(parser);
            }
        }
        return new Message(title, description, link, author, guid);
    }

    @Nullable
    private String readText(@NonNull XmlPullParser parser) throws IOException,
            XmlPullParserException {
        String result = null;
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void requireTag(@NonNull XmlPullParser parser, @Nullable String name) throws
            IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, name);
    }

    /**
     * Read RSS feed by specified URL-address
     *
     * @param url URL-address of the feed
     * @return Feed or {@code null} if the feed can't be read from the specified URL-address
     */
    @Nullable
    public static Feed read(@NonNull String url) {
        return new Reader(url).read();
    }
}
