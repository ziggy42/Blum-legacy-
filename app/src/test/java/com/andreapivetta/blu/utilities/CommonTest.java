package com.andreapivetta.blu.utilities;


import android.graphics.Bitmap;

import com.andreapivetta.blu.BuildConfig;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CommonTest extends TestCase {

    private static final long TWEET_ID = 635858225502531584L;
    private static final String PIC_URL = "http://i57.tinypic.com/5duohs.jpg";

    @Test
    public void testGetFavoriters() throws Exception {
        ArrayList<Long> favoriters = Common.getFavoriters(TWEET_ID);
        assertNotNull(favoriters);
        assertEquals(favoriters.size(), 0);
    }

    @Test
    public void testGetRetweeters() throws Exception {
        ArrayList<Long> retweeters = Common.getRetweeters(TWEET_ID);
        assertNotNull(retweeters);
        assertEquals(retweeters.size(), 2);
    }

    @Test
    public void testGetBitmapFromURL() throws Exception {
        Bitmap bitmap = Common.getBitmapFromURL(PIC_URL);
        assertNotNull(bitmap);
    }

    @Test
    public void testDpToPx() throws Exception {
        int px = Common.dpToPx(RuntimeEnvironment.application, 10);
        assertNotNull(px);
        assertTrue(px > 0);
    }
}