package com.tpb.hn;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.ResultReceiver;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.tpb.hn.content.ContentActivity;

import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by theo on 24/10/16.
 */

public class Util {
    private static final String TAG = Util.class.getSimpleName();

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
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().getDisplayMetrics());
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
        final StringTokenizer tokenizer = new StringTokenizer(tokens, ",");
        final int[] ints = new int[tokenizer.countTokens()];
        for(int i = 0; i < ints.length; i++) ints[i] = Integer.parseInt(tokenizer.nextToken());
        return ints;
    }

    public static CharSequence parseHTMLText(String text) {
        return Html.fromHtml(text);
    }

    public static CharSequence parseHTMLCommentText(String text) {
        final String html = Html.fromHtml(text).toString();
        return html.substring(0, Math.max(html.length() - 2, 0));
    }

    public static void reverse(int[] arr) {
        for(int l = 0, r = arr.length - 1; l < r; l++, r--) {
            final int t = arr[l];
            arr[l] = arr[r];
            arr[r] = t;
        }
    }

    public static int getApproximateNumberOfItems(ContentActivity.Section section) {
        switch(section) {
            case TOP:
                return 500;
            case BEST:
                return 500;
            case ASK:
                return 100;
            case NEW:
                return 500;
            case SHOW:
                return 50;
            case JOB:
                return 25;
            default:
                return 100;
        }
    }

    public static ContentActivity.Section getSection(Context context, String section) {
        final String[] sections = context.getResources().getStringArray(R.array.nav_spinner_items);
        if(section.equals(sections[0])) return ContentActivity.Section.TOP;
        if(section.equals(sections[1])) return ContentActivity.Section.NEW;
        if(section.equals(sections[2])) return ContentActivity.Section.BEST;
        if(section.equals(sections[3])) return ContentActivity.Section.ASK;
        if(section.equals(sections[4])) return ContentActivity.Section.SHOW;
        if(section.equals(sections[5])) return ContentActivity.Section.JOB;
        return ContentActivity.Section.SAVED;
    }

    public static String pluralise(String s, int i) {
        return i == 1 ? s : s + "s";
    }

    public static void toggleKeyboard(Context context) {
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void showKeyboard(Context context, View view) {
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    public static void hideKeyboard(Context context, View view, ResultReceiver reciever) {
        final InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(reciever == null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } else {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0, reciever);
        }
    }

}
