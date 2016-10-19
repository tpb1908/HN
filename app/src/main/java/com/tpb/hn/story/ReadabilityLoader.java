package com.tpb.hn.story;

import android.os.AsyncTask;
import android.util.Log;

import de.jetwick.snacktory.HtmlFetcher;
import de.jetwick.snacktory.JResult;

/**
 * Created by theo on 19/10/16.
 * http://stackoverflow.com/questions/6053602/what-arguments-are-passed-into-asynctaskarg1-arg2-arg3
 */

public class ReadabilityLoader extends AsyncTask<String, Short, JResult> {
    private static final String TAG = ReadabilityLoader.class.getCanonicalName();


    @Override
    protected JResult doInBackground(String... params) {
        if(params.length == 0) throw new UnsupportedOperationException("Must pass URL to ReadabilityLoader");
        final HtmlFetcher fetcher = new HtmlFetcher();
        try {
            final JResult res = fetcher.fetchAndExtract(params[0],
                    10000, true);
            Log.i(TAG, "doInBackground " + res.getText());
            return res;
        } catch(Exception e) {
            Log.e(TAG, "onCreate: Fetcher", e);
        }

        return null;
    }


}
