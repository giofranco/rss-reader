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
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.helper.UrlHelper;

public class AddUrlDialog extends AppCompatDialog {
    private final EditText mUrlEditText;
    private String mUrl;
    private Callback mCallback;
    private boolean mResultOk;
    private boolean mUrlCorrect;

    public AddUrlDialog(@NonNull Context context) {
        super(context, getDialogTheme(context));
        setContentView(R.layout.dialog_add_url);
        setCancelable(true);
        mUrlEditText = findValidViewById(R.id.url);
        final TextView addButton = findValidViewById(R.id.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultOk = true;
                dismiss();
            }
        });
        TextView cancelButton = findValidViewById(R.id.cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResultOk = false;
                dismiss();
            }
        });
        final CharSequence invalidUrlErrorText = context.getText(R.string.invalid_url);
        mUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = s.toString();
                mUrl = address;
                if (TextUtils.isEmpty(address)) {
                    mUrlCorrect = false;
                    mUrlEditText.setError(null);
                    addButton.setEnabled(false);
                } else if (UrlHelper.validate(address)) {
                    mUrlCorrect = true;
                    mUrlEditText.setError(null);
                    addButton.setEnabled(true);
                } else {
                    mUrlCorrect = false;
                    mUrlEditText.setError(invalidUrlErrorText);
                    addButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mUrlEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE && mUrlCorrect) {
                    mResultOk = true;
                    dismiss();
                    return true;
                } else {
                    return false;
                }
            }
        });
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Callback callback = mCallback;
                if (callback != null) {
                    callback.onDialogResult(mUrl, mResultOk);
                }
            }
        });
        setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mResultOk = false;
                Callback callback = mCallback;
                if (callback != null) {
                    callback.onDialogResult(mUrl, false);
                }
            }
        });
    }

    @NonNull
    public AddUrlDialog setCallback(@Nullable Callback callback) {
        mCallback = callback;
        return this;
    }

    @Nullable
    public String getUrl() {
        return mUrl;
    }

    public boolean isResultOk() {
        return mResultOk;
    }

    @Override
    public void show() {
        show(null);
    }

    public void show(@Nullable String url) {
        mResultOk = false;
        mUrl = url;
        mUrlEditText.setText(url);
        super.show();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private <T extends View> T findValidViewById(@IdRes int viewId) {
        View view = findViewById(viewId);
        if (view == null) {
            throw new IllegalStateException("Invalid layout.");
        }
        return (T) view;
    }

    private static int getDialogTheme(@NonNull Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme()
                .resolveAttribute(android.support.v7.appcompat.R.attr.alertDialogTheme, typedValue,
                        true);
        return typedValue.resourceId;
    }

    public interface Callback {
        void onDialogResult(@Nullable String url, boolean resultOk);
    }
}
