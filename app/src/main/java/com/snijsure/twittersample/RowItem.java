package com.snijsure.twittersample;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Comparator;
import java.util.Date;

import twitter4j.GeoLocation;


class RowItem implements Comparable<RowItem>, Parcelable {
    private final String url;
    private final String tweetText;
    private final Date tweetDate;
    private final String userName;
    private final int favoriteCount;
    private final GeoLocation geoLocation;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.tweetText);
        dest.writeSerializable(this.tweetDate);
        dest.writeString(this.userName);
        dest.writeInt(this.favoriteCount);
        dest.writeSerializable(this.geoLocation);
    }

    protected RowItem(Parcel in) {
        this.url = in.readString();
        this.tweetText = in.readString();
        this.tweetDate = (java.util.Date) in.readSerializable();
        this.userName = in.readString();
        this.favoriteCount = in.readInt();
        this.geoLocation = (GeoLocation) in.readSerializable();
    }

    public static final Parcelable.Creator<RowItem> CREATOR = new Parcelable.Creator<RowItem>() {
        public RowItem createFromParcel(Parcel source) {
            return new RowItem(source);
        }

        public RowItem[] newArray(int size) {
            return new RowItem[size];
        }
    };

    public RowItem(String url, String tweetText, Date tweetDate,
                   String userName, int favoriteCount, GeoLocation location) {
        this.url = url;
        this.tweetText = tweetText;
        this.tweetDate = tweetDate;
        this.userName = userName;
        this.favoriteCount = favoriteCount;
        this.geoLocation = location;

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

    public String getGeoLocationString() {
        if ( geoLocation != null )
            return geoLocation.toString();
        else
            return "";
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
