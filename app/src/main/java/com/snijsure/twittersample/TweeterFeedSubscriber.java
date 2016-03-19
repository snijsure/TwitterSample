package com.snijsure.twittersample;

import java.util.List;

import rx.Subscriber;
import twitter4j.Status;

/**
 * Created by subodhnijsure on 3/19/16.
 */
public class TweeterFeedSubscriber extends Subscriber<List<Status>> {
    TwitterActivity mActivity;
    TweeterFeedSubscriber(TwitterActivity activity) {
        mActivity = activity;
    }
    @Override
    public void onNext(List<Status> tweets) {
        mActivity.onUpdate(tweets);
    }

    @Override
    public void onError(Throwable e) {
        mActivity.onError(e);
    }

    @Override
    public void onCompleted() {
        // Nothing
    }

}
