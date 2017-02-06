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

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.format.DateUtils;

import com.budiyev.rssreader.R;

public final class UpdateIntervalHelper {
    public static final int DEFAULT_INTERVAL = 2;

    // @formatter:off

    private static final long[] INTERVALS = {
            30 * DateUtils.SECOND_IN_MILLIS, // 30 seconds
            DateUtils.MINUTE_IN_MILLIS,      // 1 minute
            5 * DateUtils.MINUTE_IN_MILLIS,  // 5 minutes
            15 * DateUtils.MINUTE_IN_MILLIS, // 15 minutes
            30 * DateUtils.MINUTE_IN_MILLIS, // 30 minutes
            DateUtils.HOUR_IN_MILLIS,        // Two hours
            2 * DateUtils.HOUR_IN_MILLIS,    // Two hours
            DateUtils.DAY_IN_MILLIS,         // One day
            2 * DateUtils.DAY_IN_MILLIS      // Two days
    };

    // @formatter:on

    private UpdateIntervalHelper() {
    }

    public static int getIntervalsCount() {
        return INTERVALS.length;
    }

    public static long getIntervalMillis(int index) {
        return INTERVALS[index];
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, int index) {
        return getDisplayName(context, getIntervalMillis(index));
    }

    @NonNull
    public static String getDisplayName(@NonNull Context context, long millis) {
        Resources resources = context.getResources();
        if (millis < DateUtils.MINUTE_IN_MILLIS) {
            int seconds = (int) (millis / DateUtils.SECOND_IN_MILLIS);
            return resources.getQuantityString(R.plurals.seconds, seconds, seconds);
        } else if (millis < DateUtils.HOUR_IN_MILLIS) {
            int minutes = (int) (millis / DateUtils.MINUTE_IN_MILLIS);
            return resources.getQuantityString(R.plurals.minutes, minutes, minutes);
        } else if (millis < DateUtils.DAY_IN_MILLIS) {
            int hours = (int) (millis / DateUtils.HOUR_IN_MILLIS);
            return resources.getQuantityString(R.plurals.hours, hours, hours);
        } else {
            int days = (int) (millis / DateUtils.DAY_IN_MILLIS);
            return resources.getQuantityString(R.plurals.days, days, days);
        }
    }
}
