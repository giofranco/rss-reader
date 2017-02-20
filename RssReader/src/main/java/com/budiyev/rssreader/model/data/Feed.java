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
package com.budiyev.rssreader.model.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Feed extends FeedInfo implements Iterable<Message> {
    private final List<Message> mMessages = new ArrayList<>();

    public Feed(@NonNull FeedInfo feedInfo) {
        super(feedInfo.getAddress(), feedInfo.getTitle(), feedInfo.getDescription(),
                feedInfo.getLink(), feedInfo.getLanguage(), feedInfo.getCopyright(),
                feedInfo.getPublishDate());
    }

    public Feed(@Nullable String address, @Nullable String title, @Nullable String description,
            @Nullable String link, @Nullable String language, @Nullable String copyright,
            @Nullable String publishDate) {
        super(address, title, description, link, language, copyright, publishDate);
    }

    @NonNull
    public List<Message> getMessages() {
        return mMessages;
    }

    @Override
    public Iterator<Message> iterator() {
        return mMessages.iterator();
    }

    @Override
    public String toString() {
        return "Feed [title = " + getTitle() + ", description = " + getDescription() + ", link = " +
                getLink() + ", language = " + getLanguage() + ", copyright = " + getCopyright() +
                ", publish date = " + getPublishDate() + ", messages number = " + mMessages.size() +
                "]";
    }
}
