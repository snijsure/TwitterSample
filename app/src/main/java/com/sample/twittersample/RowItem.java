package com.sample.twittersample;

import java.util.Date;
import java.util.Comparator;


public class RowItem implements Comparable<RowItem> {
    private String url;
    private String tweetText;
    private Date tweetDate;
    private String userName;
    int favoriteCount;

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

    public Date getDate() {
        return tweetDate;
    }

    public int compareTo(RowItem compareFruit) {

        String str = ((RowItem) compareFruit).getTweetText();

        // ascending order
        return this.tweetText.compareTo(str);


    }

    public static class OrderByText implements Comparator<RowItem> {

        @Override
        public int compare(RowItem o1, RowItem o2) {
            String title1 = o1.getTweetText();
            String title2 = o2.getTweetText();

            int k = title1.compareTo(title2);
            return k;
        }
    }

    public static class OrderByDate implements Comparator<RowItem> {
        @Override
        public int compare(RowItem o1, RowItem o2) {
            Date date1 = o1.getDate();
            Date date2 = o2.getDate();

            int k = date1.compareTo(date2);
            return k;
        }
    }
}
