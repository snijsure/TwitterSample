package com.snijsure.twittersample;

import java.util.List;

import twitter4j.Status;

@SuppressWarnings("unused")
interface OnTweetUpdate {
    void onUpdate(List<Status> s);
    void onError(Throwable e);
}
