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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.adapter.MessagesAdapter;
import com.budiyev.rssreader.helper.ThreadHelper;
import com.budiyev.rssreader.model.Provider;
import com.budiyev.rssreader.model.callback.FeedCallback;
import com.budiyev.rssreader.model.data.Feed;

public class MessagesActivity extends AppCompatActivity implements FeedCallback {
    private static final String EXTRA_FEED_POSITION = "feed_position";
    public static final String EXTRA_FEED_URL = "feed_url";
    private String mUrl;
    private LinearLayoutManager mItemsLayoutManager;
    private MessagesAdapter mAdapter;
    private ActionBar mActionBar;
    private int mLastPosition = RecyclerView.NO_POSITION;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        String url = null;
        if (savedInstanceState != null) {
            url = savedInstanceState.getString(EXTRA_FEED_URL);
            mLastPosition =
                    savedInstanceState.getInt(EXTRA_FEED_POSITION, RecyclerView.NO_POSITION);
        }
        if (url == null) {
            url = getIntent().getStringExtra(EXTRA_FEED_URL);
        }
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("No feed URL-address specified.");
        }
        mUrl = url;
        RecyclerView itemsView = (RecyclerView) findViewById(R.id.list);
        mItemsLayoutManager = new LinearLayoutManager(this);
        itemsView.setLayoutManager(mItemsLayoutManager);
        mAdapter = new MessagesAdapter(this, itemsView);
        itemsView.setAdapter(mAdapter);
        Provider.loadFeed(this, url, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Provider.updateFeed(this, mUrl, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_FEED_URL, mUrl);
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
    public void onFeedLoaded(@Nullable Feed feed) {
        if (feed == null) {
            return;
        }
        ThreadHelper.runOnMainThread(new RefreshAction(feed, mLastPosition));
    }

    @Override
    public void onFeedUpdated(@Nullable Feed feed) {
        if (feed == null) {
            return;
        }
        ThreadHelper.runOnMainThread(new RefreshAction(feed, RecyclerView.NO_POSITION));
    }

    private class RefreshAction implements Runnable {
        private final Feed mFeed;
        private final int mPosition;

        private RefreshAction(@NonNull Feed feed, int position) {
            mFeed = feed;
            mPosition = position;
        }

        @Override
        public void run() {
            ActionBar actionBar = mActionBar;
            if (actionBar != null) {
                actionBar.setTitle(mFeed.getTitle());
            }
            mAdapter.refresh(mFeed, mPosition);
        }
    }
}
