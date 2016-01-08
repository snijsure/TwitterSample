package com.snijsure.twittersample;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import static android.support.v7.widget.helper.ItemTouchHelper.Callback;

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
    private TextView mTotalTweetCount;
    private SwipeRefreshWrapper mRefreshLayout;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mContext = getApplicationContext();

        LinearLayout mainWindowLayout = (LinearLayout) findViewById(R.id.main_window);
        /* Note: Swipe down to referesh from SwipeRefresh view and infinite scroll of
         * RecyclerView don't work well together.
         * Hence we create RefreshWrapper by hand, which overrides the canChildScrollUp
         * method.
         * Below is the code that removes the RecyclerView from R.id.main_window
         * and inserts as child of SwipeRefreshWrapper
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.listview);

        mRefreshLayout = new SwipeRefreshWrapper(mContext);

        mainWindowLayout.removeView(mRecyclerView);

        mainWindowLayout.addView(mRefreshLayout, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);

        mRefreshLayout.addView(mRecyclerView, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                flag_loading = true;
                mRefreshLayout.setRefreshing(true);
                loadMoreItems(false);
            }
        });

        mTotalTweetCount = (TextView) findViewById(R.id.tweetCount);
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

        dialog.show();
        mStreamLoader = new TwitterConnectionTask(mContext);

        // Load twitter stream that talks about travel

        mStreamLoader.execute("#travel");

        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setLogo(R.mipmap.ic_launcher);
        getActionBar().setDisplayUseLogoEnabled(true);


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        int location[]=new int[2];
        View v = findViewById(R.id.sortDateButton);
        mRecyclerView.getLocationOnScreen(location);
        Toast toast=Toast.makeText(getApplicationContext(),
                "Swipe down to referesh", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP| Gravity.CENTER_HORIZONTAL,v.getRight()-25, location[1]+20);
        toast.show();

    }

    synchronized private void loadMoreItems(boolean showDialog) {
        if ( showDialog == true )
            dialog.show();
        mStreamLoader = new TwitterConnectionTask(mContext);
        mStreamLoader.execute("next");
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    public void mostFav(@SuppressWarnings("UnusedParameters") View v) {
        dialog.show();
        Log.d(TAG, "Sort By fav count");
        Collections.sort(rowItems, new RowItem.OrderByFavCount());
        adapter.notifyDataSetChanged();

        dialog.dismiss();
        mRecyclerView.scrollToPosition(0);
    }

    // Method to sort list by date
    public void sortByDate(@SuppressWarnings("UnusedParameters") View v) {
        dialog.show();
        Log.d(TAG, "Sort By Date");
        Collections.sort(rowItems, new RowItem.OrderByDate());
        adapter.notifyDataSetChanged();

        dialog.dismiss();
        mRecyclerView.scrollToPosition(0);

    }

    // Method to sort list by tweat text
    public void sortByText(@SuppressWarnings("UnusedParameters") View v) {
        dialog.show();
        Log.d(TAG, "Sort By Text");
        Collections.sort(rowItems, new RowItem.OrderByText());
        adapter.notifyDataSetChanged();
        dialog.dismiss();
        mRecyclerView.scrollToPosition(0);

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Twitter Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.snijsure.twittersample/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Twitter Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.snijsure.twittersample/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    // This task  "opens" as connection using Twitter4j API and fetches tweets

    private class TwitterConnectionTask extends
            AsyncTask<String, String, String> {

        private List<twitter4j.Status> statuses;

        private Twitter getTwitterHandle() {
            Log.d(TAG, "In method getTwitterHandle");
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
                            public void run() {
                                Toast.makeText(mContext, R.string.no_more_tweets,
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
                    public void run() {
                        Toast.makeText(mContext, "Unexpected error while fetching data, please retry",
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
                        s.getFavoriteCount(),
                        s.getGeoLocation()
                );
                rowItems.add(item);
                if (s.getId() < lowestTweetId) {
                    lowestTweetId = s.getId();
                    mTwitterQuery.setMaxId(lowestTweetId);
                }
            }
            adapter.notifyDataSetChanged();
            if (dialog.isShowing())
                dialog.dismiss();
            if (mTotalTweetCount != null) {
                String totalCount = "# " + adapter.getItemCount();
                mTotalTweetCount.setText(totalCount);
            }
            mRefreshLayout.setRefreshing(false);
        }
    }


    public class SwipeRefreshWrapper extends SwipeRefreshLayout {

        public SwipeRefreshWrapper(Context context) {
            super(context);
        }

        public SwipeRefreshWrapper(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        private boolean isLastItemDisplaying(RecyclerView recyclerView) {
            if (recyclerView.getAdapter().getItemCount() != 0) {
                int lastVisibleItemPosition =
                        ((LinearLayoutManager)recyclerView.getLayoutManager()).
                                findLastCompletelyVisibleItemPosition();
                if (lastVisibleItemPosition != RecyclerView.NO_POSITION
                        && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1)
                    return true;
            }
            return false;
        }

        @Override
        synchronized public boolean canChildScrollUp() {
            boolean ret = isLastItemDisplaying(mRecyclerView);
            if (ret == true && !flag_loading) {
                flag_loading = true;
                loadMoreItems(true);
            }
            return ret;
        }

    }
}
