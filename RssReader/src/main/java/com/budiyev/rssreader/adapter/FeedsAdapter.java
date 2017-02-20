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

import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.budiyev.rssreader.adapter.base.Adapter;
import com.budiyev.rssreader.common.InfoUndoCallback;
import com.budiyev.rssreader.helper.SnackbarHelper;
import com.budiyev.rssreader.helper.ThreadHelper;
import com.budiyev.rssreader.model.Provider;
import com.budiyev.rssreader.model.callback.InfoAddCallback;
import com.budiyev.rssreader.model.callback.InfoDeleteCallback;
import com.budiyev.rssreader.model.data.FeedInfo;
import com.budiyev.rssreader.viewholder.FeedViewHolder;

import java.util.Collections;
import java.util.List;

public class FeedsAdapter extends Adapter<FeedInfo, FeedViewHolder>
        implements InfoAddCallback, InfoDeleteCallback {
    private boolean mItemsChanged;

    public FeedsAdapter(@NonNull Context context, @NonNull RecyclerView recyclerView) {
        super(context, recyclerView);
        new ItemTouchHelper(new TouchCallback()).attachToRecyclerView(recyclerView);
        recyclerView.setOnTouchListener(new TouchListener());
    }

    public void add(@NonNull FeedInfo info) {
        Provider.insertInfo(getContext(), info, 0, this);
    }

    @Override
    public FeedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FeedViewHolder(getContext(), this, parent);
    }

    @Override
    public void onInfoInserted(@NonNull List<FeedInfo> infoList, @NonNull FeedInfo info,
            int position) {
        ThreadHelper.runOnMainThread(new InsertAction(infoList, position));
    }

    @Override
    public void onInfoDeleted(@NonNull Provider.InfoDeleteDelegate deleteDelegate,
            @NonNull List<FeedInfo> infoList, @NonNull FeedInfo info, int position) {
        ThreadHelper.runOnMainThread(new DeleteAction(deleteDelegate, infoList, info, position));

    }

    @Override
    public void onInfoRestored(@NonNull List<FeedInfo> infoList, @NonNull FeedInfo info,
            int position) {
        ThreadHelper.runOnMainThread(new InsertAction(infoList, position));
    }

    private class TouchCallback extends ItemTouchHelper.Callback {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                    ItemTouchHelper.START | ItemTouchHelper.END);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                RecyclerView.ViewHolder target) {
            List<FeedInfo> items = getItems();
            if (items == null) {
                return false;
            }
            mItemsChanged = true;
            int from = viewHolder.getAdapterPosition();
            int to = target.getAdapterPosition();
            Collections.swap(items, from, to);
            notifyItemMoved(from, to);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            List<FeedInfo> items = getItems();
            if (items == null || position >= items.size() || position < 0) {
                return;
            }
            Provider.deleteInfo(getContext(), items.get(position), position, FeedsAdapter.this);
        }
    }

    private class TouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) &&
                    mItemsChanged) {
                mItemsChanged = false;
                List<FeedInfo> items = getItems();
                if (items != null) {
                    Provider.saveInfoList(getContext(), items);
                }
            }
            return false;
        }
    }

    private class DeleteAction implements Runnable {
        private final Provider.InfoDeleteDelegate mDeleteDelegate;
        private final List<FeedInfo> mInfoList;
        private final FeedInfo mInfo;
        private final int mPosition;

        private DeleteAction(@NonNull Provider.InfoDeleteDelegate deleteDelegate,
                @NonNull List<FeedInfo> infoList, @NonNull FeedInfo info, int position) {
            mDeleteDelegate = deleteDelegate;
            mInfoList = infoList;
            mInfo = info;
            mPosition = position;
        }

        @Override
        @MainThread
        public void run() {
            setItems(mInfoList);
            notifyItemRemoved(mPosition);
            SnackbarHelper.showDeleteInfoSnackbar(getRecyclerView(), mInfo, new InfoUndoCallback() {
                @Override
                public void onUndo() {
                    mDeleteDelegate.undo();
                }
            });
        }
    }

    private class InsertAction implements Runnable {
        private final List<FeedInfo> mInfoList;
        private final int mPosition;

        private InsertAction(@NonNull List<FeedInfo> infoList, int position) {
            mInfoList = infoList;
            mPosition = position;
        }

        @Override
        @MainThread
        public void run() {
            setItems(mInfoList);
            notifyItemInserted(mPosition);
            getRecyclerView().smoothScrollToPosition(mPosition);
        }
    }
}
