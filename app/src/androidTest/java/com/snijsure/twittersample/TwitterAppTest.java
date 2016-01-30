package com.snijsure.twittersample;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.matcher.ViewMatchers.withId;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class TwitterAppTest {

    @Rule
    public ActivityTestRule<TwitterActivity> mActivityRule = new ActivityTestRule<>(
            TwitterActivity.class);

    @Test
    public void basicTweetLoad_andSort() {
        onView(withId(R.id.sortDateButton)).perform(click());
        onView(withId(R.id.listview)).perform(swipeDown());
        onView(withId(R.id.listview)).perform(swipeDown());
        onView(withId(R.id.sortDateButton)).perform(click());
        onView(withId(R.id.sortTextButton)).perform(click());
        onView(withId(R.id.mostfav)).perform(click());
        onView(withId(R.id.listview)).perform(swipeDown());
        onView(withId(R.id.listview)).perform(swipeDown());
    }
}
