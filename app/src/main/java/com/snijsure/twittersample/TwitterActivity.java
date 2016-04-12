package com.snijsure.twittersample;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TwitterActivity extends Activity implements OnTweetUpdate {

    @Bind(R.id.listview)
    RecyclerView mRecyclerView;

    private final String TAG = "TwitterActivity";
    private final String CALLBACKURL = "T4J_OAuth://callback_main";
    private List<RowItem> rowItems;
    private TwitterFeedManager mStreamLoader;

    private ProgressDialog dialog;
    private CustomViewAdapter adapter;
    private SwipeRefreshWrapper mRefreshLayout;
    @Bind(R.id.tweetCount)
    TextView mTotalTweetCount;
    @Bind(R.id.sortDateButton)
    View sortByDateButton;
    private LinearLayout mainWindowLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        Context mContext = getApplicationContext();
        mainWindowLayout = (LinearLayout) findViewById(R.id.main_window);


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
                mRefreshLayout.setEnabled(false);
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

        mRecyclerView.setAdapter(adapter);


        mStreamLoader = ((TwitterApp) getApplicationContext()).getTask();

        // Load twitter stream that talks about travel
        if (savedInstanceState == null) {
            dialog.show();
            TwitterFeedManager.fetchTweets("#travel", mStreamLoader)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new TweeterFeedSubscriber(this));
        } else {
            /* This will cause us to fetch more data on rotation
            TwitterFeedManager.fetchTweets("next", mStreamLoader)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new TweeterFeedSubscriber(this));
             */
        }

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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        ArrayList<RowItem> data = new ArrayList<>(adapter.getRowItems());
        savedInstanceState.putParcelableArrayList("AdapterData", data);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<RowItem> data = savedInstanceState.getParcelableArrayList("AdapterData");
        if (data != null && data.size() > 0) {
            rowItems.clear();
            rowItems.addAll(data);
        }
    }


    synchronized private void loadMoreItems(boolean showDialog) {
        if (showDialog)
            dialog.show();
        Log.d(TAG, "loadMoreItems fetching tweets");
        TwitterFeedManager.fetchTweets("next", mStreamLoader)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new TweeterFeedSubscriber(this));
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
    public void onUpdate(List<twitter4j.Status> newTweets) {
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
        mRefreshLayout.setEnabled(true);
    }

    @Override
    public void onError(Throwable e) {
        Snackbar.make(mainWindowLayout, "Error while updating tweet", Snackbar.LENGTH_LONG)
                .setAction("Close", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                .show();
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
                loadMoreItems(true);
            }
            return ret;
        }

    }
}
