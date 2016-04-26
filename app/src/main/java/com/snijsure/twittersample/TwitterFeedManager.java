package com.snijsure.twittersample;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;


class TwitterFeedManager {
    private static final String TAG = "TwitterActivity";
    private static Query mTwitterQuery;
    private final Twitter mTwitter;
    private long lowestTweetId = Long.MAX_VALUE;
    private int TWEET_COUNT = 25;
    private List<twitter4j.Status> statuses;

    public TwitterFeedManager(TwitterApp app) {
        statuses = new ArrayList<>();
        mTwitter = getTwitterHandle(app);
    }


    private Twitter getTwitterHandle(TwitterApp app) {
        Twitter twitterFactoryInstace;
        twitterFactoryInstace = new TwitterFactory().getInstance();

        twitterFactoryInstace.setOAuthConsumer(
                app.getResources().getString(R.string.consumer_key),
                app.getResources().getString(R.string.consumer_secret));
        AccessToken a = new AccessToken(
                app.getResources().getString(
                R.string.access_token),
                app.getResources().getString(
                        R.string.access_token_secret));
        twitterFactoryInstace.setOAuthAccessToken(a);

        try {
            return twitterFactoryInstace;
        } catch (Exception e) {
            Log.d(TAG, "Exception while getting Twitter instance = " + e);
        }
        return null;
    }

    public static Observable<List<Status>> fetchTweets(final String queryString,
                                                       final TwitterFeedManager task) {

        return Observable.create(new Observable.OnSubscribe<List<Status>>() {
            @Override
            public void call(Subscriber<? super List<Status>> subscriber) {
                List<twitter4j.Status> s = task.fetchTweetsAboutTopic(queryString);
                subscriber.onNext(s);
                subscriber.onCompleted();
            }
        });
    }

    private List<twitter4j.Status> fetchTweetsAboutTopic(String queryString) {
        try {
            QueryResult mQueryResults;
            if (queryString.equals("next")) {
                    mTwitterQuery.setMaxId(0);
                    //lowestTweetId = onUpdate(statuses, lowestTweetId);
                    mTwitterQuery.setMaxId(lowestTweetId);
                    mQueryResults = mTwitter.search(TwitterFeedManager.mTwitterQuery);
                    statuses = mQueryResults.getTweets();
                    for (twitter4j.Status s : statuses) {
                        if (s.getId() < lowestTweetId) {
                            lowestTweetId = s.getId();
                    }
                }
            } else {
                Log.d(TAG, "Sending query " + queryString + " to twitter");
                TwitterFeedManager.mTwitterQuery = new Query(queryString);
                TwitterFeedManager.mTwitterQuery.setSince("2015-01-01");
                TwitterFeedManager.mTwitterQuery.setCount(TWEET_COUNT);
                // Query result object as described in
                // http://twitter4j.org/javadoc/twitter4j/QueryResult.html
                mQueryResults = mTwitter.search(TwitterFeedManager.mTwitterQuery);
                statuses = mQueryResults.getTweets();
                for (twitter4j.Status s : statuses) {
                    if (s.getId() < lowestTweetId) {
                        lowestTweetId = s.getId();
                    }
                }
            }
            return statuses;
        } catch (Exception e) {
            Log.d(TAG, "Twitter query failed - " + e.toString());
            return null;
        }
    }
}
