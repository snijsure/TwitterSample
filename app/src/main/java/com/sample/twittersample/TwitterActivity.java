package com.sample.twittersample;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class TwitterActivity extends Activity {
    Context mContext;
    Twitter mTwitter;

    RecyclerView mRecyclerView;

    private final String TAG = "TwitterActivity";
    private final String CALLBACKURL = "T4J_OAuth://callback_main";
    ArrayList<String> statusTexts = new ArrayList<String>();
    List<RowItem> rowItems;
    boolean flag_loading = false;
    Query twitterQuery;
    QueryResult twitterQueryResults;
    public TwitterConnectionTask streamLoader;
    ProgressDialog dialog;
    CustomViewAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mRecyclerView = (RecyclerView) findViewById(R.id.listview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mContext = getApplicationContext();
        rowItems = new ArrayList<RowItem>();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Tweets...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        adapter = new CustomViewAdapter(rowItems);
        mRecyclerView.setAdapter(adapter);

		/* For fancy auto-loading on hitting bootom of list following code can be used
         * Needs more testing
         * */

        dialog.show();
        streamLoader = new TwitterConnectionTask(mContext);

        // Load twitter stream that talks about travel

        streamLoader.execute("#travel");
    }

    public void loadMoreItems() {
        dialog.show();
        streamLoader = new TwitterConnectionTask(mContext);
        streamLoader.execute("next");
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // Method to sort list by date
    public void sortByDate(View v) {
        dialog.show();
        Log.d(TAG, "Sort By Date");
        Collections.sort(rowItems, new RowItem.OrderByDate());
        adapter.notifyDataSetChanged();

        dialog.dismiss();
    }

    // Method to sort list by tweat text
    public void sortByText(View v) {
        dialog.show();
        Log.d(TAG, "Sort By Text");
        Collections.sort(rowItems, new RowItem.OrderByText());
        adapter.notifyDataSetChanged();
        dialog.dismiss();

    }

    // This task  "opens" as connection using Twitter4j API and fetches tweets

    private class TwitterConnectionTask extends
            AsyncTask<String, String, String> {
        private java.util.List<twitter4j.Status> statuses;

        private Twitter getTwitterHandle() {
            Log.d(TAG,"In method getTwitterHandle");
            Twitter twitterFactorInstance;
            twitterFactorInstance = new TwitterFactory().getInstance();
            Log.d(TAG, "TwitterFactory.getInstance returned " + twitterFactorInstance);

            twitterFactorInstance.setOAuthConsumer(
                    getResources().getString(R.string.consumer_key),
                    getResources().getString(R.string.consumer_secret));
            AccessToken a = new AccessToken(getResources().getString(
                    R.string.access_token),
                    getResources().getString(
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
                if (queryString.equals("next")) {
                    twitterQuery = twitterQueryResults.nextQuery();
                    if (twitterQuery != null) {
                        twitterQueryResults = mTwitter.search(twitterQuery);
                        statuses = twitterQueryResults.getTweets();
                        flag_loading = false;
                    } else {
                        runOnUiThread(new Runnable() {
                            public void run()
                            {
                                Toast.makeText(mContext,R.string.no_more_tweets,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Log.d(TAG, "Sending query " + queryString + " to twitter");
                    twitterQuery = new Query(queryString);
                    twitterQuery.setSince("2015-01-01");
                    twitterQuery.setCount(50);
                    // Query result object as described in
                    // http://twitter4j.org/javadoc/twitter4j/QueryResult.html

                    twitterQueryResults = mTwitter.search(twitterQuery);
                    Log.d(TAG, "Twitt - Count =  " + twitterQueryResults.getCount());

                    statuses = twitterQueryResults.getTweets();
                }


            } catch (Exception e) {
                statusTexts.add("Twitter query failed: " + e.toString());
                Log.d(TAG, "Twitter query failed - " + e.toString());
            }

        }

        public TwitterConnectionTask(Context context) {
            mContext = context;
            statuses = new ArrayList<twitter4j.Status>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

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

        protected void onPostExecute(String file_url) {
            // Data contained in statuses is found at
            // http://twitter4j.org/javadoc/twitter4j/Status.html
            for (twitter4j.Status s : statuses) {
                RowItem item = new RowItem(
                        s.getUser().getBiggerProfileImageURL(),
                        s.getText(),
                        s.getCreatedAt(),
                        s.getUser().getName(),
                        s.getFavoriteCount()
                );
                rowItems.add(item);
            }
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        }
    }
}
