package com.tpb.hn.story;

import android.os.AsyncTask;
import android.util.Log;

import java.util.HashMap;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;
import de.jetwick.snacktory.SCache;

/**
 * Created by theo on 19/10/16.
 * http://stackoverflow.com/questions/6053602/what-arguments-are-passed-into-asynctaskarg1-arg2-arg3
 */

public class ReadabilityLoader extends AsyncTask<String, Short, JResult> {
    private static final String TAG = ReadabilityLoader.class.getCanonicalName();
    private static HtmlFetcher fetcher = new HtmlFetcher();

    static {
        fetcher.setCache(new ReadabilityCache());
    }

    @Override
    protected JResult doInBackground(String... params) {
        if(params.length == 0) throw new UnsupportedOperationException("Must pass URL to ReadabilityLoader");
        final long s = System.nanoTime();
        try {
            final JResult res = fetcher.fetchAndExtract(params[0],
                    10000, true);
            Log.i(TAG, "doInBackground " + res.getText());
            Log.i(TAG, "doInBackground: Time diff" + (System.nanoTime() - s)/1E9);
            return res;
        } catch(Exception e) {
            Log.e(TAG, "onCreate: Fetcher", e);
        }

        return null;
    }

    private static class ReadabilityCache implements SCache {
        private static HashMap<String, JResult> map = new HashMap<>();

        @Override
        public JResult get(String s) {
            return map.get(s);
        }

        @Override
        public void put(String s, JResult jResult) {
            map.put(s, jResult);
        }

        @Override
        public int getSize() {
            return map.size();
        }
    }

    static void clearChache() {
        fetcher.clearCacheCounter();
    }

    public static SCache getCache() {
        return fetcher.getCache();
    }

}
