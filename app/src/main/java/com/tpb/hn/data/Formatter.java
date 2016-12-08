package com.tpb.hn.data;


import android.content.Context;
import android.util.Pair;

import com.tpb.hn.R;
import com.tpb.hn.Util;

/**
 * Created by theo on 19/10/16.
 */

public class Formatter {
    private static final String TAG = Formatter.class.getSimpleName();

    public static String timeAgo(long time) {
        final long now = System.currentTimeMillis() / 1000;
        final long delta = (now - time);
        if(delta / (365 * 24 * 3600) > 0) {
            final long div = delta / (365 * 24 * 3600);
            return div + (div == 1 ? " year" : " years");
        } else if(delta / (28 * 24 * 3600) > 0) {
            final long div = delta / (28 * 24 * 3600);
            return div + (div == 1 ? " month" : " months");
        } else if(delta / (7 * 24 * 3600) > 0) {
            final long div = delta / (7 * 24 * 3600);
            return div + (div == 1 ? " week" : " weeks");
        } else if(delta / (24 * 3600) > 0) {
            final long div = delta / (24 * 3600);
            return div + (div == 1 ? " day" : " days");
        } else if(delta / (3600) > 0) {
            final long div = delta / (3600);
            return div + (div == 1 ? " hour" : " hours");
        } else {
            final long div = delta / 60;
            if(div > 5) {
                return div + (div == 1 ? " minute" : " minutes");
            } else {
                return "just now";
            }
        }
    }

    public static String shortTimeAgo(long time) {
        final long now = System.currentTimeMillis() / 1000;
        final long delta = (now - time);

        if(delta / (365 * 24 * 3600) > 0) {
            final long div = delta / (365 * 24 * 3600);
            return div + (div == 1 ? " yr" : " yrs");
        } else if(delta / (28 * 24 * 3600) > 0) {
            final long div = delta / (28 * 24 * 3600);
            return div + (div == 1 ? " mo" : " mos");
        } else if(delta / (7 * 24 * 3600) > 0) {
            final long div = delta / (7 * 24 * 3600);
            return div + (div == 1 ? " wk" : " wks");
        } else if(delta / (24 * 3600) > 0) {
            final long div = delta / (24 * 3600);
            return div + (div == 1 ? " dy" : " dys");
        } else if(delta / (3600) > 0) {
            final long div = delta / (3600);
            return div + (div == 1 ? " hr" : " hrs");
        } else {
            final long div = delta / 60;
            if(div > 5) {
                return div + (div == 1 ? " min" : " mins");
            } else {
                return "just now";
            }
        }
    }

    public static String appendAgo(String time) {
        return time.contains("just") ? time : time + " ago";
    }

    public static String hmToString(int hour, int minute, String separator) {
        String val = Integer.toString(hour);

        val += separator;
        if(minute == 0) {
            val += "0";
        }
        return val + Integer.toString(minute);
    }

    public static int hmToInt(int h, int m) {
        return h * 60 + m;
    }

    public static Pair<Integer, Integer> intTohm(int time) {
        final int min = time % 60;
        return new Pair<>((time - min) / 60, min);
    }

    public static String capitaliseFirst(String original) {
        if(original == null || original.length() == 0) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1).toLowerCase();
    }

    public static String formatHTTPError(Context context, int resId, int code) {
        return formatHTTPError(context, context.getString(resId), code);
    }

    private static String formatHTTPError(Context context, String message, int code) {
        if(context == null) return "Error";
        switch(code) {
            case -100: //Custom error parsing
                return String.format(context.getString(R.string.error_http_format_string), message, context.getString(R.string.error_parsing_readable_text));
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

    public static String setTextColor(Context context, String markup, int bgColor, int textColor) {
        return context.getString(R.string.html,
                14f,
                Util.toHtmlColor(bgColor),
                10f, 10f, 10f, 10f,
                1f,
                Util.toHtmlColor(textColor),
                markup,
                Util.toHtmlColor(textColor));
    }

}
