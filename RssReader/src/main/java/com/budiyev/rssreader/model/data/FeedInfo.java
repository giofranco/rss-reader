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

public class FeedInfo {
    private final String mAddress;
    private final String mTitle;
    private final String mDescription;
    private final String mLink;
    private final String mLanguage;
    private final String mCopyright;
    private final String mPublishDate;
    private final Spanned mTitleSpanned;
    private final Spanned mDescriptionSpanned;

    public FeedInfo(@Nullable String address, @Nullable String title, @Nullable String description,
            @Nullable String link, @Nullable String language, @Nullable String copyright,
            @Nullable String publishDate) {
        mAddress = address;
        mTitle = title;
        mDescription = description;
        mLink = link;
        mLanguage = language;
        mCopyright = copyright;
        mPublishDate = publishDate;
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
    public String getAddress() {
        return mAddress;
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
    public String getLanguage() {
        return mLanguage;
    }

    @Nullable
    public String getCopyright() {
        return mCopyright;
    }

    @Nullable
    public String getPublishDate() {
        return mPublishDate;
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
        if (mAddress == null) {
            return 0;
        } else {
            return mAddress.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
                obj instanceof FeedInfo && Objects.equals(mAddress, ((FeedInfo) obj).mAddress);
    }

    @Override
    public String toString() {
        return "Feed [address = " + mAddress + ", title = " + mTitle + ", description = " +
                mDescription + ", link = " + mLink + ", language = " + mLanguage +
                ", copyright = " + mCopyright + ", publish date = " + mPublishDate + "]";
    }
}
