package com.tpb.hn.data;


import android.content.Context;

import com.tpb.hn.R;

import java.util.Date;

/**
 * Created by theo on 19/10/16.
 */

public class Formatter {
    private static final String TAG = Formatter.class.getSimpleName();

    public static String timeAgo(long time) {
        final long now = new Date().getTime()/1000;
        final long delta = (now - time);
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

    public static String wrapInDiv(String text) {
        return "<div>" + text + "</div>";
    }

}
