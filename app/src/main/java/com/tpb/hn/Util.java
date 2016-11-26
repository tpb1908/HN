package com.tpb.hn;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.util.TypedValue;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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

    public static int pxFromDp(final int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxFromSp(final float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,  Resources.getSystem().getDisplayMetrics());
    }

    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getActiveNetworkInfo() != null && manager.getActiveNetworkInfo().isConnected();
    }

    public static int[] convertIntegers(List<Integer> integers) {
        final int[] ret = new int[integers.size()];
        final Iterator<Integer> iterator = integers.iterator();
        for(int i = 0; i < ret.length; i++) {
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

    public static int indexOf(String[] values, String key) {
        for(int i = 0; i < values.length; i++) {
            if(key.equals(values[i])) return i;
        }
        return -1;
    }

    public static FloatingActionButton getFAB(Context context) {
        final Context themeWrapper = new ContextThemeWrapper(context, R.style.AppTheme);
        return new FloatingActionButton(themeWrapper);
    }

    public static void largeDebugDump(String tag, String dump) {
        final int len = dump.length();
        for(int i = 0; i < len; i += 1024) {
            if(i + 1024 < len) {
                Log.d(tag, dump.substring(i, i + 1024));
            } else {
                Log.d(tag, dump.substring(i, len));
            }
        }
    }

    public static String toHtmlColor(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    public static void putIntArrayInPrefs(SharedPreferences.Editor editor, String key, int[] data) {
        final StringBuilder builder = new StringBuilder();
        for(int i : data) builder.append(i).append(",");
        editor.putString(key, builder.toString());
        editor.commit();
    }

    public static int[] getIntArrayFromPrefs(SharedPreferences prefs, String key) {
        final String tokens = prefs.getString(key, "");
        final StringTokenizer tokenizer = new StringTokenizer(tokens,  ",");
        final int[] ints = new int[tokenizer.countTokens()];
        for(int i = 0; i < ints.length; i++) ints[i] = Integer.parseInt(tokenizer.nextToken());
        return ints;
    }
}
