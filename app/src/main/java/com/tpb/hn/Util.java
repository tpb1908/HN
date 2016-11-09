package com.tpb.hn;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.view.ContextThemeWrapper;

import java.util.Iterator;
import java.util.List;

/**
 * Created by theo on 24/10/16.
 */

public class Util {

    public static float dpFromPx(final float px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
    }

    public static float pxFromDp(final float dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnected();
    }

    public static int[] convertIntegers(List<Integer> integers) {
        final int[] ret = new int[integers.size()];
        final Iterator<Integer> iterator = integers.iterator();
        for (int i = 0; i < ret.length; i++) {
            ret[i] = iterator.next();
        }
        return ret;
    }

    public static int linearSearch(int[] items, int key) {
        for(int i = 0; i < items.length; i++) {
            if(items[i] == key) return i;
        }
        return -1;
    }

    public static FloatingActionButton getFAB(Context context) {
        final Context themeWrapper = new ContextThemeWrapper(context, R.style.AppTheme);
        return new FloatingActionButton(themeWrapper);
    }

}
