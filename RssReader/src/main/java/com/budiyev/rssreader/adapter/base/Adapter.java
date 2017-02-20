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
package com.budiyev.rssreader.adapter.base;

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.budiyev.rssreader.viewholder.base.ViewHolder;

import java.util.List;

public abstract class Adapter<T, V extends ViewHolder<T>> extends RecyclerView.Adapter<V> {
    private final Context mContext;
    private final RecyclerView mRecyclerView;
    private volatile List<T> mItems;

    public Adapter(@NonNull Context context, @NonNull RecyclerView recyclerView) {
        mContext = context;
        mRecyclerView = recyclerView;
    }

    @MainThread
    public void refresh(@Nullable List<T> items, int position) {
        mItems = items;
        notifyDataSetChanged();
        if (position != RecyclerView.NO_POSITION && items != null && position < items.size()) {
            mRecyclerView.scrollToPosition(position);
        }
    }

    @Override
    public void onBindViewHolder(V holder, int position) {
        List<T> items = mItems;
        if (items == null || holder == null || position == RecyclerView.NO_POSITION ||
                position >= items.size()) {
            return;
        }
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        List<T> items = mItems;
        return items == null ? 0 : items.size();
    }

    @Nullable
    public List<T> getItems() {
        return mItems;
    }

    public void setItems(@Nullable List<T> items) {
        mItems = items;
    }

    @NonNull
    protected Context getContext() {
        return mContext;
    }

    @NonNull
    protected RecyclerView getRecyclerView() {
        return mRecyclerView;
    }
}
