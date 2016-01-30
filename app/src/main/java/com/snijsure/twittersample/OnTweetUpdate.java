package com.snijsure.twittersample;

import java.util.List;

import twitter4j.Status;

@SuppressWarnings("unused")
interface OnTweetUpdate {
    long onUpdate(List<Status> s, long lowestTweet);
}
