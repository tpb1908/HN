package com.tpb.hn.network;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import okhttp3.HttpUrl;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by theo on 25/10/16.
 * http://www.hidroh.com/2016/05/19/hacking-up-ad-blocker-android/
 */

public class AdBlocker {
    private static final String AD_HOSTS_FILE = "adhosts.txt";
    private static final Set<String> AD_HOSTS = new HashSet<>();

    public static void init(final Context context) {
        if(AD_HOSTS.size() == 0) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        loadFromAssets(context);
                    } catch(IOException ioe) {

                    }
                    return null;
                }
            }.execute();
        }
    }

    private static void loadFromAssets(Context context) throws IOException {
        InputStream stream = context.getAssets().open(AD_HOSTS_FILE);
        BufferedSource buffer = Okio.buffer(Okio.source(stream));
        String line;
        while((line = buffer.readUtf8Line()) != null) {
            AD_HOSTS.add(line);
        }
        buffer.close();
        stream.close();
    }

    public static boolean isAd(String url) {
        HttpUrl httpUrl = HttpUrl.parse(url);
        return isAdHost(httpUrl != null ? httpUrl.host() : "");
    }

    private static boolean isAdHost(String host) {
        if(TextUtils.isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) ||
                index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

}
