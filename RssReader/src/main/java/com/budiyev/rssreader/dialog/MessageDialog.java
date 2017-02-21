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
package com.budiyev.rssreader.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.TextView;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.dialog.base.BaseDialog;

public class MessageDialog extends BaseDialog {
    private final TextView mHeaderView;
    private final TextView mTextView;

    public MessageDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.dialog_message);
        mHeaderView = (TextView) findViewById(R.id.header);
        mTextView = (TextView) findViewById(R.id.text);
        findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        setCancelable(true);
    }

    @NonNull
    public MessageDialog setHeader(@Nullable CharSequence header) {
        mHeaderView.setText(header);
        return this;
    }

    @NonNull
    public MessageDialog setHeader(@StringRes int headerId) {
        mHeaderView.setText(headerId);
        return this;
    }

    @NonNull
    public MessageDialog setText(@Nullable CharSequence text) {
        mTextView.setText(text);
        return this;
    }

    @NonNull
    public MessageDialog setText(@StringRes int textId) {
        mTextView.setText(textId);
        return this;
    }

    public void show(@Nullable CharSequence header, @Nullable CharSequence text) {
        mHeaderView.setText(header);
        mTextView.setText(text);
        show();
    }

    public void show(@StringRes int headerId, @StringRes int textId) {
        mHeaderView.setText(headerId);
        mTextView.setText(textId);
        show();
    }
}
