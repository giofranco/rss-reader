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

import java.util.Objects;

public class Message {
    private final String mTitle;
    private final String mDescription;
    private final String mLink;
    private final String mAuthor;
    private final String mGuid;

    public Message(@Nullable String title, @Nullable String description, @Nullable String link,
            @Nullable String author, @Nullable String guid) {
        mTitle = title;
        mDescription = description;
        mLink = link;
        mAuthor = author;
        mGuid = guid;
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

    @Override
    public int hashCode() {
        return Objects.hash(mTitle, mDescription, mLink, mAuthor, mGuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof Message) {
            Message other = (Message) obj;
            return Objects.equals(mTitle, other.mTitle) &&
                    Objects.equals(mDescription, other.mDescription) &&
                    Objects.equals(mLink, other.mLink) && Objects.equals(mAuthor, other.mAuthor) &&
                    Objects.equals(mGuid, other.mGuid);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "Message [title = " + mTitle + ", description = " + mDescription + ", link = " +
                mLink + ", author = " + mAuthor + ", GUID = " + mGuid + "]";
    }
}
