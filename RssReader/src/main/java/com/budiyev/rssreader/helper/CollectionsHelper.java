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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

public final class CollectionsHelper {
    private CollectionsHelper() {
    }

    /**
     * Whether if specified {@code array} contains {@code element}
     *
     * @param array   an array to be checked
     * @param element element
     * @return {@code true} if {@code array} contains {@code element}, {@code false} otherwise
     */
    public static boolean contains(@NonNull int[] array, int element) {
        for (int e : array) {
            if (e == element) {
                return true;
            }
        }
        return false;
    }

    /**
     * Search {@code item} in {@code list}, starting from specified {@code position}
     * in both directions
     * <br>
     * Such approach is effective if approximate position of an element in the list is known
     * <br>
     * Starting from {@code position} it checks {@code step} of elements on the left
     * and at the right, if {@code item} is not found among them, another {@code step} of
     * elements on the left and at the right, and so on while {@code item} found or list ended
     *
     * @param list      List of items
     * @param item      Item to search for
     * @param condition Condition searching item to satisfy to
     * @param position  Search start position
     * @param step      Search step
     * @return Position of {@code item} in {@code list} or {@code -1} if {@code item} is not found
     */
    public static <T> int search(@NonNull List<T> list, @Nullable T item,
            @NonNull Condition<T> condition, int position, int step) {
        if (position < 0 || step < 1) {
            throw new IllegalArgumentException();
        }
        if (condition.test(item, list.get(position))) {
            return position;
        } else {
            int listSize = list.size();
            int currentOffset = step;
            int previousOffset = 0;
            for (; ; ) {
                int start = position - currentOffset;
                if (start < 0) {
                    start = 0;
                }
                int end = position + currentOffset + 1;
                if (end > listSize) {
                    end = listSize;
                }
                int startOffset = position - previousOffset - 1;
                for (int i = startOffset; i >= start; i--) {
                    if (condition.test(item, list.get(i))) {
                        return i;
                    }
                }
                int endOffset = position + previousOffset + 1;
                for (int i = endOffset; i < end; i++) {
                    if (condition.test(item, list.get(i))) {
                        return i;
                    }
                }
                previousOffset = currentOffset;
                currentOffset += step;
                if (start == 0 && end == listSize) {
                    return -1;
                }
            }
        }
    }

    public interface Condition<T> {
        boolean test(T item, T candidate);
    }
}
