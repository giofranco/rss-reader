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
package com.budiyev.rssreader.activity;

import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.adapter.FeedsAdapter;
import com.budiyev.rssreader.dialog.AddUrlDialog;
import com.budiyev.rssreader.helper.ThreadHelper;
import com.budiyev.rssreader.model.Provider;
import com.budiyev.rssreader.model.callback.InfoListCallback;
import com.budiyev.rssreader.model.callback.InfoLoadCallback;
import com.budiyev.rssreader.model.data.FeedInfo;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements InfoLoadCallback, InfoListCallback, AddUrlDialog.Callback {
    private static final String EXTRA_DIALOG_VISIBLE = "dialog_visible";
    private static final String EXTRA_DIALOG_URL = "dialog_url";
    private static final String EXTRA_FEED_POSITION = "feed_position";
    private AddUrlDialog mAddUrlDialog;
    private LinearLayoutManager mItemsLayoutManager;
    private FeedsAdapter mAdapter;
    private boolean mDialogVisible;
    private volatile int mLastPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mAddUrlDialog = new AddUrlDialog(this).setCallback(this);
        FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialogVisible = true;
                mAddUrlDialog.show();
            }
        });
        RecyclerView itemsView = (RecyclerView) findViewById(R.id.list);
        mItemsLayoutManager = new LinearLayoutManager(this);
        itemsView.setLayoutManager(mItemsLayoutManager);
        mAdapter = new FeedsAdapter(this, itemsView);
        itemsView.setAdapter(mAdapter);
        if (savedInstanceState != null) {
            mLastPosition =
                    savedInstanceState.getInt(EXTRA_FEED_POSITION, RecyclerView.NO_POSITION);
            if (savedInstanceState.getBoolean(EXTRA_DIALOG_VISIBLE)) {
                mDialogVisible = true;
                mAddUrlDialog.show(savedInstanceState.getString(EXTRA_DIALOG_URL));
            }
        }
        Provider.loadInfoList(this, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(EXTRA_DIALOG_VISIBLE, mDialogVisible);
        outState.putString(EXTRA_DIALOG_URL, mAddUrlDialog.getUrl());
        outState.putInt(EXTRA_FEED_POSITION,
                mItemsLayoutManager.findFirstCompletelyVisibleItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInfoLoaded(@NonNull String url, @Nullable FeedInfo info) {
        FeedsAdapter adapter = mAdapter;
        if (info == null || adapter == null) {
            return;
        }
        adapter.add(info);
    }

    @Override
    public void onInfoListLoaded(@Nullable List<FeedInfo> infoList) {
        if (infoList == null) {
            return;
        }
        ThreadHelper.runOnMainThread(new RefreshAction(infoList, mLastPosition));
    }

    @Override
    public void onDialogResult(@Nullable String url, boolean resultOk) {
        mDialogVisible = false;
        if (!resultOk || TextUtils.isEmpty(url)) {
            return;
        }
        Provider.loadInfo(url, this);
    }

    private class RefreshAction implements Runnable {
        private final List<FeedInfo> mInfoList;
        private final int mPosition;

        private RefreshAction(@NonNull List<FeedInfo> infoList, int position) {
            mInfoList = infoList;
            mPosition = position;
        }

        @Override
        @MainThread
        public void run() {
            FeedsAdapter adapter = mAdapter;
            if (adapter == null) {
                return;
            }
            adapter.refresh(mInfoList, mPosition);
        }
    }
}
