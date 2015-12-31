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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static android.support.v7.widget.helper.ItemTouchHelper.*;

public class TwitterActivity extends Activity {
    private Context mContext;
    private Twitter mTwitter;

    private RecyclerView mRecyclerView;

    private final String TAG = "TwitterActivity";
    private final String CALLBACKURL = "T4J_OAuth://callback_main";
    private List<RowItem> rowItems;
    private boolean flag_loading = false;

    private TwitterConnectionTask mStreamLoader;
    private Query mTwitterQuery;
    private QueryResult mQueryResults;

    private ProgressDialog dialog;
    private CustomViewAdapter adapter;
    private LinearLayoutManager mLayoutManager;
    private int pastVisiblesItems;
    private int visibleItemCount;
    private int totalItemCount;
    private long lowestTweetId = Long.MAX_VALUE;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mRecyclerView = (RecyclerView) findViewById(R.id.listview);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    visibleItemCount = mLayoutManager.getChildCount();
                    totalItemCount = mLayoutManager.getItemCount();
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!flag_loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            flag_loading = true;
                            Log.v(TAG, "Last Item Wow now load more items");
                            loadMoreItems();
                        }
                    }
                }
            }
        });

        mContext = getApplicationContext();
        rowItems = new ArrayList<>();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Updating Tweets...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        adapter = new CustomViewAdapter(rowItems);

        Callback callback = new SimpleItemTouchHelperCallback(adapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

        adapter.setTouchHelper(touchHelper);

        mRecyclerView.setAdapter(adapter);


		/* For fancy auto-loading on hitting bootom of list following code can be used
         * Needs more testing
         * */

        dialog.show();
        mStreamLoader = new TwitterConnectionTask(mContext);

        // Load twitter stream that talks about travel

        mStreamLoader.execute("#travel");
    }

    private void loadMoreItems() {
        dialog.show();
        mStreamLoader = new TwitterConnectionTask(mContext);
        mStreamLoader.execute("next");
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void mostFav(View v) {
        dialog.show();
        Log.d(TAG, "Sort By fav count");
        Collections.sort(rowItems, new RowItem.OrderByFavCount());
        adapter.notifyDataSetChanged();

        dialog.dismiss();
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
                    if (mTwitterQuery != null) {
                        mQueryResults = mTwitter.search(mTwitterQuery);
                        statuses = mQueryResults.getTweets();
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
                    mTwitterQuery = new Query(queryString);
                    mTwitterQuery.setSince("2015-01-01");
                    mTwitterQuery.setCount(50);
                    // Query result object as described in
                    // http://twitter4j.org/javadoc/twitter4j/QueryResult.html

                    mQueryResults = mTwitter.search(mTwitterQuery);

                    statuses = mQueryResults.getTweets();
                }


            } catch (Exception e) {
                Log.d(TAG, "Twitter query failed - " + e.toString());
                runOnUiThread(new Runnable() {
                    public void run()
                    {
                        Toast.makeText(mContext,"Unexpected error while fetching data, please retry",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

        public TwitterConnectionTask(Context context) {
            mContext = context;
            statuses = new ArrayList<>();
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
                if (s.getId() < lowestTweetId) {
                    lowestTweetId = s.getId();
                    mTwitterQuery.setMaxId(lowestTweetId);
                }
            }
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        }
    }
}
