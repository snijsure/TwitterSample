package com.snijsure.twittersample;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TimingLogger;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.R.menu;
import android.view.Menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import org.json.JSONException;
import org.json.JSONObject;
import com.digits.sdk.android.AuthCallback;

public class TwitterActivity extends AppCompatActivity implements OnTweetUpdate {

    @Bind(R.id.listview)
    RecyclerView mRecyclerView;

    private final String TAG = "TwitterActivity";
    private final String CALLBACKURL = "T4J_OAuth://callback_main";
    private List<RowItem> rowItems;
    private TwitterFeedManager mStreamLoader;

    private ProgressDialog progressDialog;
    private CustomViewAdapter adapter;
    private SwipeRefreshWrapper mRefreshLayout;
    @Bind(R.id.tweetCount)
    TextView mTotalTweetCount;
    @Bind(R.id.sortDateButton)
    View sortByDateButton;
    private LinearLayout mainWindowLayout;
    private boolean updatePending = false;
    MixpanelAPI mixpanel;
    DrawerLayout drawerLayout;
    SessionManager loginSession;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);
        Context mContext = getApplicationContext();
        String project_token = mContext.getString(R.string.mixPanelToken);
        mixpanel = MixpanelAPI.getInstance(mContext,project_token);

        loginSession = new SessionManager(getApplicationContext());
        loginSession.checkLogin();

        if ( loginSession.isLoggedIn() ) {
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
                    loadMoreItems(false);
                }
            });

            mTotalTweetCount = (TextView) findViewById(R.id.tweetCount);
            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            if (navigationView != null) {
                setupDrawerContent(navigationView);
            }

            rowItems = new ArrayList<>();

            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Updating Tweets...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);

            adapter = new CustomViewAdapter(rowItems);

            mRecyclerView.setAdapter(adapter);


            mStreamLoader = ((TwitterApp) getApplicationContext()).getTask();

            // Load twitter stream that talks about travel
            if (savedInstanceState == null) {


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
            progressDialog.show();
            int location[] = new int[2];
            mRecyclerView.getLocationOnScreen(location);
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Swipe down to referesh", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL,
                    sortByDateButton.getRight() - 25, location[1] + 20);
            toast.show();
        }

        Debug.stopMethodTracing();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_logout:
                Log.d(TAG, "onOptionsItemSelected Logging out user");
                loginSession.logoutUser();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(final NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        switch (menuItem.getItemId()) {
                            case R.id.nav_home:
                                drawerLayout.closeDrawers();
                                return true;
                            case R.id.action_logout:
                                Log.d(TAG, "setupDrawerContent Logging out user");
                                loginSession.logoutUser();
                                drawerLayout.closeDrawers();
                                return true;
                            default:
                                return false;
                        }
                    }
                });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if ( adapter != null ) {
            ArrayList<RowItem> data = new ArrayList<>(adapter.getRowItems());
            if ( data != null && data.size() > 0 ) {
                savedInstanceState.putParcelableArrayList("AdapterData", data);
            }
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<RowItem> data = savedInstanceState.getParcelableArrayList("AdapterData");
        if (data != null && data.size() > 0) {
            rowItems.clear();
            rowItems.addAll(data);
            String totalCount = "# " + rowItems.size();
            mTotalTweetCount.setText(totalCount);
        }
    }


    synchronized private void loadMoreItems(boolean showDialog) {
        if ( !updatePending ) {
            updatePending = true;
            if (showDialog)
                progressDialog.show();
            mRefreshLayout.setRefreshing(true);
            mRefreshLayout.setEnabled(false);
            TwitterFeedManager.fetchTweets("next", mStreamLoader)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new TweeterFeedSubscriber(this));
        }
    }

    public void mostFav(@SuppressWarnings("UnusedParameters") View v) {
        progressDialog.show();
        Collections.sort(rowItems, new RowItem.OrderByFavCount());
        adapter.notifyDataSetChanged();

        progressDialog.dismiss();
        mRecyclerView.scrollToPosition(0);

        TimingLogger timings = new TimingLogger(TAG, "MixPanelStart");
        try {
            JSONObject props = new JSONObject();
            props.put("Most Fav Sort", true);
            mixpanel.track("MostFav",props);
        } catch(JSONException e) {
            Log.e(TAG,"Unale to add prop Most Fav Sort");
        }
        timings.dumpToLog();
        timings.reset(TAG,"MixPanelStart");
        Log.d(TAG, "MixPanelStart End");
    }

    // Method to sort list by date
    public synchronized void sortByDate(@SuppressWarnings("UnusedParameters") View v) {
        progressDialog.show();
        Collections.sort(rowItems, new RowItem.OrderByDate());
        adapter.notifyDataSetChanged();

        progressDialog.dismiss();
        mRecyclerView.scrollToPosition(0);
        TimingLogger timings = new TimingLogger(TAG, "MixPanelStart");
        try {
            JSONObject props = new JSONObject();
            props.put("Date Sort", true);
            mixpanel.track("DateSort",props);
        } catch(JSONException e) {
            Log.e(TAG,"Unale to add prop Date Sort");
        }
        timings.dumpToLog(); // Dump the timing.
        timings.reset(TAG,"MixPanelStart");
        Log.d(TAG, "MixPanelStart End");
    }

    // Method to sort list by tweat text
    public void sortByText(@SuppressWarnings("UnusedParameters") View v) {
        progressDialog.show();
        Collections.sort(rowItems, new RowItem.OrderByText());
        adapter.notifyDataSetChanged();
        progressDialog.dismiss();
        mRecyclerView.scrollToPosition(0);

        TimingLogger timings = new TimingLogger(TAG, "MixPanelStart");
        try {
            JSONObject props = new JSONObject();
            props.put("Text Sort", true);
            mixpanel.track("TextSort",props);
        } catch(JSONException e) {
            Log.e(TAG,"Unale to add prop Text Sort");
        }
        timings.dumpToLog();
        timings.reset(TAG,"MixPanelStart");
        Log.d(TAG, "MixPanelStart End");
    }

    @Override
    synchronized public void onUpdate(List<twitter4j.Status> newTweets) {
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
        adapter.getItemCount();
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        if (mTotalTweetCount != null) {
            String totalCount = "# " + adapter.getItemCount();
            mTotalTweetCount.setText(totalCount);
        }
        mRefreshLayout.setRefreshing(false);
        mRefreshLayout.setEnabled(true);
        updatePending = false;
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
                if (!updatePending && lastVisibleItemPosition != RecyclerView.NO_POSITION
                        && lastVisibleItemPosition == recyclerView.getAdapter().getItemCount() - 1) {
                    return true;
                }
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
        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return !updatePending && super.onInterceptTouchEvent(ev);
        }

    }
}
