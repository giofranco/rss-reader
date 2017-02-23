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

import android.support.annotation.Nullable;
import android.text.Spanned;

import com.budiyev.rssreader.helper.TextHelper;

import java.util.Objects;

public class Message {
    private final String mTitle;
    private final String mDescription;
    private final String mLink;
    private final String mAuthor;
    private final String mGuid;
    private final Spanned mTitleSpanned;
    private final Spanned mDescriptionSpanned;

    public Message(@Nullable String title, @Nullable String description, @Nullable String link,
            @Nullable String author, @Nullable String guid) {
        mTitle = title;
        mDescription = description;
        mLink = link;
        mAuthor = author;
        mGuid = guid;
        if (title == null) {
            mTitleSpanned = null;
        } else {
            mTitleSpanned = TextHelper.parseHtml(title);
        }
        if (description == null) {
            mDescriptionSpanned = null;
        } else {
            mDescriptionSpanned = TextHelper.parseHtml(description);
        }
    }

    @Nullable
    public String getTitle() {
        return mTitle;
    }

    @Nullable
    public String getDescription() {
        return mDescription;
    }

    @Nullable
    public String getLink() {
        return mLink;
    }

    @Nullable
    public String getAuthor() {
        return mAuthor;
    }

    @Nullable
    public String getGuid() {
        return mGuid;
    }

    @Nullable
    public Spanned getTitleSpanned() {
        return mTitleSpanned;
    }

    @Nullable
    public Spanned getDescriptionSpanned() {
        return mDescriptionSpanned;
    }

    @Override
    public int hashCode() {
        if (mGuid == null) {
            return 0;
        } else {
            return mGuid.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
                obj instanceof Message && Objects.equals(mGuid, ((Message) obj).mGuid);
    }

    @Override
    public String toString() {
        return "Message [title = " + mTitle + ", description = " + mDescription + ", link = " +
                mLink + ", author = " + mAuthor + ", GUID = " + mGuid + "]";
    }
}
