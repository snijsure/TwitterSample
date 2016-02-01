package com.snijsure.twittersample;


import android.util.Log;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;

class TwitterConnectionTask extends Observable<List<twitter4j.Status>> {

    private static Query mTwitterQuery;
    private static long lowestTweetId = Long.MAX_VALUE;
    private static Twitter mTwitter;
    public static TwitterConnectionTask create(Twitter t, String queryString) {
        OnSubscribe<List<twitter4j.Status>> onSubscribe = new OnSubscribe<List<twitter4j.Status>>() {
            @Override
            public void call(Subscriber<? super List<twitter4j.Status>> t1) {
                try {
                    QueryResult mQueryResults;
                    if (TwitterConnectionTask.lowestTweetId != 0) {
                        TwitterConnectionTask.mTwitterQuery = new Query("#travel");
                        TwitterConnectionTask.mTwitterQuery.setSince("2015-01-01");
                        TwitterConnectionTask.mTwitterQuery.setCount(50);
                        mQueryResults = mTwitter.search(TwitterConnectionTask.mTwitterQuery);
                    } else {
                        TwitterConnectionTask.mTwitterQuery = new Query("#travel");
                        TwitterConnectionTask.mTwitterQuery.setSince("2015-01-01");
                        TwitterConnectionTask.mTwitterQuery.setCount(50);
                        mQueryResults = mTwitter.search(TwitterConnectionTask.mTwitterQuery);
                    }
                    List<twitter4j.Status> statuses = mQueryResults.getTweets();
                    for (twitter4j.Status s : statuses) {
                        Log.d("TwitterTask" , " status id = " + s.getText());
                        if (s.getId() < lowestTweetId) {
                            lowestTweetId = s.getId();
                        }
                    }
                    t1.onNext(statuses);
                    t1.onCompleted();
                } catch (Exception e) {
                    Log.d("TwitterTask" , "Exception " + e);
                    t1.onError(e);
                }
            }
        };
        return new TwitterConnectionTask(onSubscribe, t);
    }

    private TwitterConnectionTask(OnSubscribe<List<twitter4j.Status>> onSubscribe, Twitter t) {
        super(onSubscribe);
        this.mTwitter = t;
    }
}
