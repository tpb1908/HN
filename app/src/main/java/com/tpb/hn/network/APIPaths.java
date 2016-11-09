package com.tpb.hn.network;

import android.util.Log;

import com.tpb.hn.BuildConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by theo on 17/10/16.
 */

public class APIPaths {
    private static final String TAG = APIPaths.class.getSimpleName();

    private static final String BASE_PATH = "https://hacker-news.firebaseio.com/v0/";
    private static final String ITEM = "item/";
    private static final String USER = "user/";
    private static final String MAX_ITEM = "maxitem";
    private static final String NEW_STORIES = "newstories";
    private static final String TOP_STORIES = "topstories";
    private static final String BEST_STORIES = "beststories";
    private static final String ASK_STORIES = "askstories";
    private static final String SHOW_STORIES = "showstories";
    private static final String JOB_STORIES = "jobstories";
    private static final String UPDATED = "updates/";

    private static final String BOILERPIPE_PATH = "http://boilerpipe-web.appspot.com/extract?url=";

    private static final String MERCURY_PARSER_PATH = "https://mercury.postlight.com/parser?url=";
    private static final String MERCURY_HEADER_KEY = "x-api-key";
    private static final String MERCURY_KEY = BuildConfig.MERCURY_API_TOKEN;
    public static OkHttpClient MERCURY_CLIENT = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        final Request request = chain.request();
                        final Request newRequest = request.newBuilder()
                                .addHeader(MERCURY_HEADER_KEY, MERCURY_KEY)
                                .build();
                        return chain.proceed(newRequest);
                    }
                })
                .build();

    private static final String MERCURY_AMP_PATH = "https://mercury.postlight.com/amp?url=";

    private static final String READABILITY_PARSER = "https://readability.com/api/content/v1/parser?url=";
    private static final String READABILITY_PARSER_KEY = BuildConfig.MERCURY_API_TOKEN;

    private static final String DOCS_PDF_BASE = "https://docs.google.com/gview?embedded=true&url=";

    //https://hn.algolia.com/api
    private static final String ALGOLIA_BASE = "http://hn.algolia.com/api/v1/search?query=";
    private static final String ALGOLIA_DATE_BASE = "http://hn.algolia.com/api/v1/search_by_date?query=";

    public static final String JSON = ".json";
    public static final String PRETTY = "?print=pretty";

    private String addTagsToAlgolia(String path, String[] tags) {
        for(String tag : tags) path += tag;
        return path;
    }

    private String searchStoryComments(int storyId) {
        return ALGOLIA_BASE + "?tags=comment," + storyId;
    }



    public static String getItemPath(int itemId) {
        return BASE_PATH + ITEM + itemId + JSON;
    }

    public static String getUserPath(String usedId) {
        return BASE_PATH + USER + usedId + JSON;
    }

    public static String getMaxItemPath() {
        return BASE_PATH + MAX_ITEM + JSON;
    }

    public static String getNewPath() {
        return BASE_PATH + NEW_STORIES + JSON;
    }

    public static String getTopPath() {
        return BASE_PATH + TOP_STORIES + JSON;
    }

    public static String getBestPath() {
        return BASE_PATH + BEST_STORIES + JSON;
    }

    public static String getAskPath() {
        return BASE_PATH + ASK_STORIES + JSON;
    }

    public static String getShowPath() {
        return BASE_PATH + SHOW_STORIES + JSON;
    }

    public static String getJobPath() {
        return BASE_PATH + JOB_STORIES + JSON;
    }

    public static String getUpdatedPath() {
        return BASE_PATH + UPDATED + JSON;
    }

    public static String getPrettyPath(String path) {
        return path + PRETTY;
    }

    public static String getReadabilityParserPath(String url) {
        return READABILITY_PARSER + url + "&token=" + READABILITY_PARSER_KEY;
    }

    public static String getMercuryAmpPath(String url) {
        return MERCURY_AMP_PATH + url;
    }

    public static String getMercuryParserPath(String url) {
        return MERCURY_PARSER_PATH + url;
    }

    public static String getBoilerpipePath(String url) {
        return  BOILERPIPE_PATH + url + "&output=htmlFragment";
    }

    public static String getPDFDisplayPath(String url) {
        return DOCS_PDF_BASE + url;
    }

    public static int parseUrl(String url) {
        if(url.contains("item?")) {
            try {
                final String idString = (String) url.subSequence(url.indexOf("=") + 1, url.length());
                Log.i(TAG, "parseUrl: idString " + idString);
                final int id = Integer.parseInt(idString);
                Log.i(TAG, "parseUrl: id is " + id);
                return id;

            } catch(Exception e) {
                return -1;
            }
        }
        return -1;
    }


}
