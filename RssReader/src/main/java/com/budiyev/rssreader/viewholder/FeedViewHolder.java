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
package com.budiyev.rssreader.viewholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.budiyev.rssreader.R;
import com.budiyev.rssreader.activity.MessagesActivity;
import com.budiyev.rssreader.adapter.FeedsAdapter;
import com.budiyev.rssreader.helper.TextHelper;
import com.budiyev.rssreader.helper.UrlHelper;
import com.budiyev.rssreader.model.data.FeedInfo;
import com.budiyev.rssreader.viewholder.base.ViewHolder;

public class FeedViewHolder extends ViewHolder<FeedInfo> {
    private final TextView mHeader;
    private final TextView mText;
    private final ImageView mLink;
    private FeedInfo mFeedInfo;

    public FeedViewHolder(@NonNull Context context, @NonNull final FeedsAdapter adapter,
            @NonNull ViewGroup parent) {
        super(context, LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false));
        mHeader = (TextView) itemView.findViewById(R.id.header);
        mText = (TextView) itemView.findViewById(R.id.text);
        mLink = (ImageView) itemView.findViewById(R.id.link);
        mLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedInfo feedInfo = mFeedInfo;
                if (feedInfo == null) {
                    return;
                }
                String link = feedInfo.getLink();
                if (link == null) {
                    return;
                }
                getContext().startActivity(
                        new Intent(Intent.ACTION_VIEW, Uri.parse(UrlHelper.validateScheme(link))));
            }
        });
        itemView.findViewById(R.id.view_full).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FeedInfo feedInfo = mFeedInfo;
                if (feedInfo == null) {
                    return;
                }
                String address = feedInfo.getAddress();
                if (address == null) {
                    return;
                }
                Context context = getContext();
                context.startActivity(new Intent(context, MessagesActivity.class)
                        .putExtra(MessagesActivity.EXTRA_FEED_URL, address));
            }
        });
    }

    @Override
    public void bind(@NonNull FeedInfo feedInfo) {
        mFeedInfo = feedInfo;
        TextHelper.setTextViewText(mHeader, feedInfo.getTitleSpanned());
        TextHelper.setTextViewText(mText, feedInfo.getDescriptionSpanned());
        if (TextUtils.isEmpty(feedInfo.getLink())) {
            mLink.setVisibility(View.GONE);
        } else {
            mLink.setVisibility(View.VISIBLE);
        }
    }
}
