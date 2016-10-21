package com.tpb.hn.data;


import android.util.Log;

import java.util.Date;

/**
 * Created by theo on 19/10/16.
 */

public class Formatter {
    private static final String TAG = Formatter.class.getSimpleName();

    public static String timeAgo(long time) {
        final long now = new Date().getTime()/1000;
        final long delta = (now - time);
        Log.i(TAG, "timeAgo: " + delta);
        if(delta / (365 * 24 * 3600) > 0) {
            final long div = delta / (365 * 24 * 3600);
            return  div + (div == 1 ? " year" : " years") + " ago";
        } else if(delta / (28 * 24 * 3600) > 0) {
            final long div = delta / (28 * 24 * 3600);
            return  div + (div == 1 ? " month" : " months") + " ago";
        } else if(delta / (7 * 24 * 3600) > 0) {
            final long div = delta / (7 * 24 * 3600);
            return  div + (div == 1 ? " week" : " weeks") + " ago";
        } else if(delta / (24 * 3600) > 0) {
            final long div = delta / (24 * 3600);
            return div + (div == 1 ? " day" : " days") + " ago";
        } else if(delta / (3600) > 0) {
            final long div = delta / (3600);
            return div + (div == 1 ? " hour" : " hours") + " ago";
        } else {
            final long div = delta / 60;
            if(div > 15) {
                return div + (div == 1 ? " minute" : " minutes") + " ago";
            } else {
                return " just now";
            }
        }
    }



}
