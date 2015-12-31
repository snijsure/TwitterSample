package com.sample.twittersample;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.Comparator;


class RowItem implements Comparable<RowItem> {
    private final String url;
    private final String tweetText;
    private final Date tweetDate;
    private final String userName;
    private final int favoriteCount;

    public RowItem(String url, String tweetText, Date tweetDate,
                   String userName, int favoriteCount) {
        this.url = url;
        this.tweetText = tweetText;
        this.tweetDate = tweetDate;
        this.userName = userName;
        this.favoriteCount = favoriteCount;

    }

    public String getUserName() {
        return userName;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public String getUrl() {
        return url;
    }

    public String getTweetText() {
        return tweetText;
    }

    @Override
    public String toString() {
        return tweetText + "\n" + tweetText;
    }

    private Date getDate() {
        return tweetDate;
    }

    public int compareTo(@NonNull RowItem r) {
        Date date1 = r.getDate();

        return date1.compareTo(tweetDate);
    }

    public static class OrderByText implements Comparator<RowItem> {

        @Override
        public int compare(RowItem o1, RowItem o2) {
            String title1 = o1.getTweetText();
            String title2 = o2.getTweetText();

            return title1.compareTo(title2);
        }
    }

    public static class OrderByFavCount implements Comparator<RowItem> {

        @Override
        public int compare(RowItem o1, RowItem o2) {
            int favoriteCount1 = o1.getFavoriteCount();
            int favoriteCount2 = o2.getFavoriteCount();
            if (favoriteCount1 < favoriteCount2)
                return 1;
            else if (favoriteCount1 > favoriteCount2)
                return -1;
            else
                return 0;
        }
    }

    public static class OrderByDate implements Comparator<RowItem> {
        @Override
        public int compare(RowItem o1, RowItem o2) {
            Date date1 = o1.getDate();
            Date date2 = o2.getDate();

            return date1.compareTo(date2);
        }
    }
}
