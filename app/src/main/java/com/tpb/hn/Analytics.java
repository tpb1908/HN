package com.tpb.hn;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by theo on 28/10/16.
 */

public class Analytics extends Application {
    private Tracker mTracker;

    public static final String CATEGORY_NAVIGATION = "Navigation";
    public static final String KEY_PAGE = "PAGE ";

    public static final String CATEGORY_ITEM = "ITEM";
    public static final String KEY_OPEN_ITEM = "OPENED";


    synchronized public Tracker getDefaultTracker() {
        if(mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }


}
