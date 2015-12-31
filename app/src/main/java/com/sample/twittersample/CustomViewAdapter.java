package com.sample.twittersample;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class CustomViewAdapter extends RecyclerView.Adapter<CustomViewAdapter.TweetHolder> {
    Context context;
    private List<RowItem> mRowItems;

    public CustomViewAdapter(List<RowItem> items) {
        mRowItems = items;
    }

    @Override
    public TweetHolder onCreateViewHolder(ViewGroup parent, int pos) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new TweetHolder(view);
    }

    @Override
    public void onBindViewHolder(TweetHolder holder, int pos) {
        RowItem rowItem = mRowItems.get(pos);
        holder.bindTweet(rowItem);
    }

    @Override
    public int getItemCount() {
        return mRowItems.size();
    }

    class TweetHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView tweetText;
        private TextView author;
        private TextView favcount;

        public TweetHolder(View itemView) {
            super(itemView);
            tweetText = (TextView) itemView.findViewById(R.id.title);
            imageView = (ImageView) itemView.findViewById(R.id.icon);
            author = (TextView) itemView.findViewById(R.id.author);
            favcount = (TextView) itemView.findViewById(R.id.favcount);
        }

        public void bindTweet(RowItem rowItem) {
            tweetText.setText(rowItem.getTweetText());
            author.setText("By " + rowItem.getUserName());
            favcount.setText("Fav #" + Integer.toString(rowItem.getFavoriteCount()));
            UrlImageViewHelper.setUrlDrawable(imageView, rowItem.getUrl(),
                    R.drawable.icon_home);
        }
    }

    /*
    public View getView(int position, View convertView, ViewGroup parent) {
        RecyclerView.ViewHolder holder = null;
        RowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new RecyclerView.ViewHolder();
            convertView.setTag(holder);
        } else
            holder = (RecyclerView.ViewHolder) convertView.getTag();
        return convertView;
    }
    */
}
