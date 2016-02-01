package com.snijsure.twittersample;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.schedulers.Schedulers;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import static android.support.v7.widget.helper.ItemTouchHelper.Callback;

public class TwitterActivity extends Activity implements rx.Observer<List<Status>> {

    @Bind(R.id.listview)
    RecyclerView mRecyclerView;
    private Subscription subscription;

    private final String TAG = "TwitterActivity";
    private final String CALLBACKURL = "T4J_OAuth://callback_main";
    private List<RowItem> rowItems;

    private TwitterConnectionTask mStreamLoader;


    private ProgressDialog dialog;
    private CustomViewAdapter adapter;
    private SwipeRefreshWrapper mRefreshLayout;
    @Bind(R.id.tweetCount)
    TextView mTotalTweetCount;
    @Bind(R.id.sortDateButton)
    View sortByDateButton;
    private Twitter mTwitter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTwitter = getTwitterHandle();
        mStreamLoader =  TwitterConnectionTask.create(mTwitter, "#travel");

        this.subscription = mStreamLoader.
                subscribeOn(Schedulers.newThread())
                .subscribe(this);

        setContentView(R.layout.main);
        ButterKnife.bind(this);

        Context mContext = getApplicationContext();
        LinearLayout mainWindowLayout = (LinearLayout) findViewById(R.id.main_window);


            /* Note: Swipe down to refresh from SwipeRefresh view and infinite scroll of
             * RecyclerView don't work well together.
             * Hence we create RefreshWrapper by hand, which overrides the canChildScrollUp
             * method.
             * Below is the code that removes the RecyclerView from R.id.main_window
             * and inserts as child of SwipeRefreshWrapper
             */

        mRefreshLayout = new SwipeRefreshWrapper(mContext);

        mainWindowLayout.removeView(mRecyclerView);

        mainWindowLayout.addView(mRefreshLayout, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);

        mRefreshLayout.addView(mRecyclerView, android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Refresh items
                mRefreshLayout.setRefreshing(true);
                loadMoreItems();
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

        // Load twitter stream that talks about travel
        if (getActionBar() != null) {
            getActionBar().setDisplayShowHomeEnabled(true);
            getActionBar().setLogo(R.mipmap.ic_launcher);
            getActionBar().setDisplayUseLogoEnabled(true);
        }


        int location[] = new int[2];
        mRecyclerView.getLocationOnScreen(location);
        Toast toast = Toast.makeText(getApplicationContext(),
                "Swipe down to referesh", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, sortByDateButton.getRight() - 25, location[1] + 20);
        toast.show();

    }

    synchronized private void loadMoreItems( ) {

        //mStreamLoader = new TwitterConnectionTask(this);
        //mStreamLoader.execute("next");
    }


    protected void onDestroy() {
        this.subscription.unsubscribe();
        super.onDestroy();
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
    public synchronized void sortByDate(@SuppressWarnings("UnusedParameters") View v) {
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

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private  Twitter getTwitterHandle() {
        Log.d(TAG, "In method getTwitterHandle");
        Twitter twitterFactoryInstance;
        twitterFactoryInstance = new TwitterFactory().getInstance();
        Log.d(TAG, "TwitterFactory.getInstance returned " + twitterFactoryInstance);

        twitterFactoryInstance.setOAuthConsumer(
                this.getResources().getString(R.string.consumer_key),
                this.getResources().getString(R.string.consumer_secret));
        AccessToken a = new AccessToken(
                this.getResources().getString(
                        R.string.access_token),
                this.getResources().getString(
                        R.string.access_token_secret));
        twitterFactoryInstance.setOAuthAccessToken(a);

        try {
            return twitterFactoryInstance;
        } catch (Exception e) {
            Log.d(TAG, "Exception while getting Twitter instance = " + e);
        }
        return null;
    }

    public synchronized long onUpdate(List<twitter4j.Status> newTweets, long lowestTweetId) {
        long ret = lowestTweetId;
        for (twitter4j.Status s : newTweets) {
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
                ret = s.getId();
            }
        }
        Collections.sort(rowItems, new RowItem.OrderByDate());
        adapter.notifyDataSetChanged();
        if (dialog.isShowing())
            dialog.dismiss();
        if (mTotalTweetCount != null) {
            String totalCount = "# " + adapter.getItemCount();
            mTotalTweetCount.setText(totalCount);
        }
        mRefreshLayout.setRefreshing(false);
        return ret;
    }


    @Override
    public void onCompleted() {
        Log.d(TAG, "onCompleted");
    }

    @Override
    public void onError(Throwable e) {
        Log.e("MainActivity", "Uh oh! Error loading tweets: " + e.getCause().getMessage());
        dialog.dismiss();
    }

    @Override
    public void onNext(List<twitter4j.Status> tweets) {
        onUpdate(tweets,0);
        dialog.dismiss();
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
                        ((LinearLayoutManager) recyclerView.getLayoutManager()).
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
            if (ret) {
                loadMoreItems();
            }
            return ret;
        }

    }
}
