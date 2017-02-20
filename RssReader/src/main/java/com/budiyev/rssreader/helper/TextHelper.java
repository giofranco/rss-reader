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

import android.os.Build;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.TextView;

public final class TextHelper {
    public static final String EM_DASH = "\u2014";

    private TextHelper() {
    }

    public static void setTextViewHtml(@NonNull TextView textView, @Nullable String htmlString) {
        if (TextUtils.isEmpty(htmlString)) {
            textView.setText(EM_DASH);
        } else {
            textView.setText(parseHtml(htmlString));
        }
    }

    public static void setTextViewHtml(@NonNull RemoteViews remoteViews, @IdRes int viewId,
            @Nullable String htmlString) {
        if (TextUtils.isEmpty(htmlString)) {
            remoteViews.setTextViewText(viewId, EM_DASH);
        } else {
            remoteViews.setTextViewText(viewId, parseHtml(htmlString));
        }
    }

    @NonNull
    public static String validateEmpty(@Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            return EM_DASH;
        } else {
            return text;
        }
    }

    @NonNull
    public static Spanned parseHtml(@NonNull String htmlString) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(htmlString);
        }
    }
}
