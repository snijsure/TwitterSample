package com.snijsure.twittersample;

import android.app.Activity;
import android.content.Context;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;

/**
 * Created by subodhnijsure on 1/30/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TwitterAppMockTests {
    @Mock  Context mMockContext;
    @Mock  Activity mActivity;
    @InjectMocks TwitterConnectionTask task;

    @Test
    public void testFooBar() {
        assertNotNull(task);
    }
}
