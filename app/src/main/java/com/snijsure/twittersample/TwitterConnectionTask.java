package com.snijsure.twittersample;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;


class TwitterConnectionTask extends
        AsyncTask<String, String, String> {

    private final Context mContext;
    private final TwitterActivity mActivity;

    private static Query mTwitterQuery;
    private Twitter mTwitter;
    private int pastVisiblesItems;
    private int visibleItemCount;
    private int totalItemCount;
    private long lowestTweetId = Long.MAX_VALUE;

    public boolean isFlag_loading() {
        return flag_loading;
    }

    public void setFlag_loading(boolean v) {
        this.flag_loading = v;
    }

    private boolean flag_loading = false;

    private final String TAG = "TwitterActivity";

    private List<twitter4j.Status> statuses;

    public TwitterConnectionTask(TwitterActivity activity) {
        mActivity = activity;
        mContext = mActivity.getApplicationContext();
        statuses = new ArrayList<>();
    }

    private Twitter getTwitterHandle() {
        Log.d(TAG, "In method getTwitterHandle");
        Twitter twitterFactorInstance;
        twitterFactorInstance = new TwitterFactory().getInstance();
        Log.d(TAG, "TwitterFactory.getInstance returned " + twitterFactorInstance);

        twitterFactorInstance.setOAuthConsumer(
                mContext.getResources().getString(R.string.consumer_key),
                mContext.getResources().getString(R.string.consumer_secret));
        AccessToken a = new AccessToken(
                mContext.getResources().getString(
                R.string.access_token),
                mContext.getResources().getString(
                        R.string.access_token_secret));
        twitterFactorInstance.setOAuthAccessToken(a);

        try {
            return twitterFactorInstance;
        } catch (Exception e) {
            Log.d(TAG, "Exception while getting Twitter instance = " + e);
        }
        return null;
    }

    // Note well, I am not using the twitter4j API correctly here.
    // May be i need to hold on to previous index into the page
    // and ask for records after that. This like maxId, getSinceId()
    // May be some day....

    synchronized private void fetchTweetsAboutTopic(String queryString) {
        try {
            QueryResult mQueryResults;
            if (queryString.equals("next")) {
                if (TwitterConnectionTask.mTwitterQuery != null) {
                    mQueryResults = mTwitter.search(TwitterConnectionTask.mTwitterQuery);
                    statuses = mQueryResults.getTweets();
                    flag_loading = false;
                } else {
                    mActivity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(mContext, R.string.no_more_tweets,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                Log.d(TAG, "Sending query " + queryString + " to twitter");
                TwitterConnectionTask.mTwitterQuery = new Query(queryString);
                TwitterConnectionTask.mTwitterQuery.setSince("2015-01-01");
                TwitterConnectionTask.mTwitterQuery.setCount(50);
                // Query result object as described in
                // http://twitter4j.org/javadoc/twitter4j/QueryResult.html

                mQueryResults = mTwitter.search(TwitterConnectionTask.mTwitterQuery);

                statuses = mQueryResults.getTweets();
            }


        } catch (Exception e) {
            Log.d(TAG, "Twitter query failed - " + e.toString());
            mActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(mContext, "Unexpected error while fetching data, please retry",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    protected String doInBackground(String... args) {
        if (mTwitter == null)
            mTwitter = getTwitterHandle();

        if (mTwitter != null) {
            fetchTweetsAboutTopic(args[0]);
        } else
            Log.d(TAG, "mTwitter is null");
        return null;
    }

    protected synchronized void onPostExecute(String r) {
        // Data contained in statuses is found at
        // http://twitter4j.org/javadoc/twitter4j/Status.html
        TwitterConnectionTask.mTwitterQuery.setMaxId(0);
        lowestTweetId = mActivity.onUpdate(statuses,lowestTweetId);
        if ( TwitterConnectionTask.mTwitterQuery != null )
            TwitterConnectionTask.mTwitterQuery.setMaxId(lowestTweetId);
    }
}
