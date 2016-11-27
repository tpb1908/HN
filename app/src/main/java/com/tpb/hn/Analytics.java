package com.tpb.hn;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.leakcanary.LeakCanary;
import com.tpb.hn.network.loaders.Loader;

/**
 * Created by theo on 28/10/16.
 */

public class Analytics extends Application {
    public static final String CATEGORY_NAVIGATION = "Navigation";
    public static final String KEY_PAGE = "PAGE ";
    public static final String CATEGORY_ITEM = "ITEM";
    public static final String KEY_OPEN_ITEM = "OPENED";
    private Tracker mTracker;

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

    @Override
    public void onTerminate() {
        Loader.getInstance(this).removeReceiver(this);
        super.onTerminate();
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
