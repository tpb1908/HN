package com.tpb.hn;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by theo on 28/10/16.
 */

public class Analytics extends Application {
    private Tracker mTracker;

    public static final String CATEGORY_NAVIGATION = "Navigation";
    public static final String KEY_PAGE = "PAGE ";

    public static final String CATEGORY_ITEM = "ITEM";
    public static final String KEY_OPEN_ITEM = "OPENED";

    @Override
    public void onCreate() {
        super.onCreate();
        if(LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        //LeakCanary.install(this);
    }

    synchronized public Tracker getDefaultTracker() {
        if(mTracker == null) {
            final GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.enableAutoActivityReports(this);
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }


}
