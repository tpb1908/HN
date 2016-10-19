package com.tpb.hn.data;

/**
 * Created by theo on 19/10/16.
 */

public class Formatter {

    public static String timeAgo(long time) {
        return (String) android.text.format.DateUtils.getRelativeTimeSpanString(time);
    }



}
