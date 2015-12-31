package com.sample.twittersample;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class CustomListViewAdapter extends ArrayAdapter<RowItem> {
    Context context;

    public CustomListViewAdapter(Context context, int resourceId,
                                 List<RowItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
        ImageView imageView;
        TextView tweetText;
        TextView author;
        TextView favcount;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.tweetText = (TextView) convertView.findViewById(R.id.title);
            holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
            holder.author = (TextView) convertView.findViewById(R.id.author);
            holder.favcount = (TextView) convertView.findViewById(R.id.favcount);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        holder.tweetText.setText(rowItem.getTweetText());
        holder.author.setText("By " + rowItem.getUserName());
        holder.favcount.setText("Fav #" + Integer.toString(rowItem.getFavoriteCount()));

        UrlImageViewHelper.setUrlDrawable(holder.imageView, rowItem.getUrl(),
                R.drawable.icon_home);

        return convertView;
    }
}
