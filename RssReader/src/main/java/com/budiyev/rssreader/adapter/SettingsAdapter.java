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
package com.budiyev.rssreader.adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.budiyev.rssreader.model.data.Feed;
import com.budiyev.rssreader.model.data.Message;
import com.budiyev.rssreader.model.view.Item;
import com.budiyev.rssreader.model.view.ItemType;
import com.budiyev.rssreader.viewholder.base.ItemViewHolder;
import com.budiyev.rssreader.viewholder.factory.ItemViewHolderFactory;

import java.util.ArrayList;
import java.util.List;

public class SettingsAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private final Activity mActivity;
    private volatile List<Item> mItems;

    public SettingsAdapter(@NonNull Activity activity) {
        mActivity = activity;
    }

    public void refresh(@Nullable Feed feed) {
        List<Item> items = new ArrayList<>();
        items.add(new Item(ItemType.SETTINGS));
        if (feed != null) {
            for (Message message : feed) {
                items.add(new Item(ItemType.MESSAGE, message));
            }
        }
        List<Item> oldItems = mItems;
        mItems = items;
        notifyDataSetChanged();
        oldItems.clear();
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return ItemViewHolderFactory.newInstance(viewType, mActivity, parent);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        List<Item> items = mItems;
        if (position != RecyclerView.NO_POSITION && items != null) {
            holder.bind(items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        List<Item> items = mItems;
        return items == null ? 0 : items.size();
    }

    @Override
    public int getItemViewType(int position) {
        List<Item> items = mItems;
        if (position == RecyclerView.NO_POSITION || items == null) {
            return 0;
        } else {
            return items.get(position).getViewType();
        }
    }
}
