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

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.common.InfoUndoCallback;
import com.budiyev.rssreader.model.data.FeedInfo;

public final class SnackbarHelper {
    private SnackbarHelper() {
    }

    public static void showDeleteInfoSnackbar(@NonNull View view, @NonNull FeedInfo info,
            @NonNull final InfoUndoCallback callback) {
        Spanned text = TextHelper.parseHtml(view.getContext()
                .getString(R.string.info_deleted, TextHelper.validateEmpty(info.getTitle())));
        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.BLACK);
        TextView textView =
                (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f);
        TextView actionView =
                (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_action);
        actionView.setTextColor(Color.WHITE);
        actionView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        actionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onUndo();
            }
        });
        snackbar.show();
    }
}
