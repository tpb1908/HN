package com.tpb.hn.network;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by theo on 21/10/16.
 */

public class NetworkChecker {

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnected();
    }

}
