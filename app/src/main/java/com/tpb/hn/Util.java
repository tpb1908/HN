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

    public static String capitaliseFirst(String original) {
        if(original == null || original.length() == 0) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
    }

    public static String formatHTTPError(Context context, int resId, int code) {
        return formatHTTPError(context, context.getString(resId), code);
    }

    public static String formatHTTPError(Context context, String message, int code) {

        switch(code) {
            case 400: //Bad request
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_400));
            case 401: //Unauthorised
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_401));
            case 403: //Forbidden
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_403));
            case 404: //Not found
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_404));
            case 406: //Not Acceptable
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_406));
            case 500: //Internal server error
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_500));
            case 501: //Not implemented
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_501));
            case 502: //Bad gateway
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_502));
            case 503: //Service unavailable
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_503));
            case 504: //GateWay timeout
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_http_504));
            default:
                return String.format(context.getString(R.string.error_http_format_string), message, String.format(context.getString(R.string.error_http_unknown), code));
        }
    }

}
