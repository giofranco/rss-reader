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
package com.budiyev.rssreader.viewholder.factory;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.budiyev.rssreader.model.view.ItemType;
import com.budiyev.rssreader.viewholder.MessageItemViewHolder;
import com.budiyev.rssreader.viewholder.SettingsItemViewHolder;
import com.budiyev.rssreader.viewholder.base.ItemViewHolder;

public final class ItemViewHolderFactory {
    private ItemViewHolderFactory() {
    }

    @NonNull
    public static ItemViewHolder newInstance(int viewType, @NonNull Activity activity,
            @NonNull ViewGroup container) {
        if (viewType == ItemType.SETTINGS.viewType) {
            return newInstance(ItemType.SETTINGS, activity, container);
        } else if (viewType == ItemType.MESSAGE.viewType) {
            return newInstance(ItemType.MESSAGE, activity, container);
        } else {
            throw new IllegalArgumentException("Illegal view type");
        }
    }

    @NonNull
    public static ItemViewHolder newInstance(@NonNull ItemType itemType, @NonNull Activity activity,
            @NonNull ViewGroup container) {
        switch (itemType) {
            case SETTINGS: {
                return new SettingsItemViewHolder(activity, container);
            }
            case MESSAGE: {
                return new MessageItemViewHolder(activity, container);
            }
            default: {
                throw new IllegalArgumentException("Illegal item type.");
            }
        }
    }
}
