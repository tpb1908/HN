package com.tpb.hn.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import okhttp3.HttpUrl;

/**
 * Created by theo on 25/10/16.
 * http://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
 */

public class AdBlocker {
    private static final String TAG = AdBlocker.class.getSimpleName();
    private static final String AD_HOSTS_FILE = "adhosts.txt";
    private static final Set<String> AD_HOSTS = new HashSet<>();

    public static void init(final Context context) {
        if(AD_HOSTS.size() == 0) {
            AsyncTask.execute(() -> {
                try {
                    loadFromAssets(context);
                } catch(IOException ioe) {

                }
            });
        }
    }

    private static void loadFromAssets(Context context) throws IOException {
        final long start = System.nanoTime();
        final InputStream stream = context.getAssets().open(AD_HOSTS_FILE);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line;
        while((line = reader.readLine()) != null) {
            if(!line.startsWith("#")) AD_HOSTS.add(line);
        }

        reader.close();
        stream.close();
        Log.i(TAG, "loadFromAssets: time " + (System.nanoTime() - start) / 1E9);
    }

    public static boolean isAd(String url) {
        final HttpUrl httpUrl = HttpUrl.parse(url);
        return isAdHost(httpUrl != null ? httpUrl.host() : "");
    }

    private static boolean isAdHost(String host) {
        if(TextUtils.isEmpty(host)) {
            return false;
        }
        final int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) ||
                index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

}
