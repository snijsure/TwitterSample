package com.snijsure.twittersample;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CustomViewAdapter extends RecyclerView.Adapter<CustomViewAdapter.TweetHolder> {
    private final List<RowItem> mRowItems;

    public CustomViewAdapter(List<RowItem> items) {
        mRowItems = items;
    }


    @Override
    public TweetHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        ButterKnife.bind(view);
        return new TweetHolder(view);
    }

    @Override
    public void onBindViewHolder(final TweetHolder holder, int pos) {
        RowItem rowItem = mRowItems.get(pos);
        holder.bindTweet(rowItem);
    }

    @Override
    public int getItemCount() {
        return mRowItems.size();
    }

    public List<RowItem> getRowItems() {
        return mRowItems;
    }
    public void onItemDismiss(int position) {
        mRowItems.remove(position);
        notifyItemRemoved(position);
    }

    class TweetHolder extends RecyclerView.ViewHolder {
        final View mHolderView;
        @Bind(R.id.title) TextView tweetText;
        @Bind(R.id.icon) ImageView imageView;
        @Bind(R.id.author) TextView author;
        @Bind(R.id.favcount) TextView favcount;
        @Bind(R.id.geolocation) TextView geoLocation;


        public TweetHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(itemView);

            mHolderView = itemView;
            tweetText = (TextView) itemView.findViewById(R.id.title);
            imageView = (ImageView) itemView.findViewById(R.id.icon);
            author = (TextView) itemView.findViewById(R.id.author);
            favcount = (TextView) itemView.findViewById(R.id.favcount);
            geoLocation = (TextView)itemView.findViewById(R.id.geolocation);
        }

        public void bindTweet(RowItem rowItem) {
            tweetText.setText(rowItem.getTweetText());
            author.setText("By " + rowItem.getUserName());
            favcount.setText("Fav #" + Integer.toString(rowItem.getFavoriteCount()));
            if ( geoLocation != null ) {
                String s = rowItem.getGeoLocationString();
                if ( s.isEmpty() )
                    geoLocation.setText("Geo Location N/A");
                else
                    geoLocation.setText("Geo Location " + s);

            }
            UrlImageViewHelper.setUrlDrawable(imageView, rowItem.getUrl(),
                    android.R.drawable.gallery_thumb);
        }
        public void onItemSelected() {
            mHolderView.setBackgroundColor(Color.LTGRAY);
        }

        public void onItemClear() {
            mHolderView.setBackgroundColor(0);
        }

    }
}
