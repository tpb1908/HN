package com.tpb.hn.network;

import android.util.Log;

import com.tpb.hn.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by theo on 17/10/16.
 */

public class APIPaths {
    private static final String TAG = APIPaths.class.getSimpleName();

    private static final String JSON = ".json";
    private static final String BASE_PATH = "https://hacker-news.firebaseio.com/v0/";
    private static final String ITEM = "item/";
    private static final String USER = "user/";
    private static final String NEW_STORIES = "newstories";
    private static final String TOP_STORIES = "topstories";
    private static final String BEST_STORIES = "beststories";
    private static final String ASK_STORIES = "askstories";
    private static final String SHOW_STORIES = "showstories";
    private static final String JOB_STORIES = "jobstories";
    private static final String MERCURY_PARSER_PATH = "https://mercury.postlight.com/parser?url=";
    private static final String MERCURY_HEADER_KEY = "x-api-key";
    private static final String MERCURY_KEY = BuildConfig.MERCURY_API_TOKEN;
    public static final OkHttpClient MERCURY_CLIENT = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                final Request request = chain.request();
                final Request newRequest = request.newBuilder()
                        .addHeader(MERCURY_HEADER_KEY, MERCURY_KEY)
                        .build();
                return chain.proceed(newRequest);
            })
            .build();
    private static final String MERCURY_AMP_PATH = "https://mercury.postlight.com/amp?url=";
    private static final String DOCS_PDF_BASE = "https://docs.google.com/gview?embedded=true&url=";
    //Algolia methods
    //https://hn.algolia.com/api
    private static final String ALGOLIA_BASE = "http://hn.algolia.com/api/v1/";
    private static final String ALGOLIA_SEARCH = "search?query=";
    private static final String ALGOLIA_TAGS = "&tags=";
    private static final String ALGOLIA_ITEM_PATH = "items/";
    private static final String ALGOLIA_NUMERIC_FILTERS = "&numericFilters=";
    private static final String ALGOLIA_ATTRIBUTE_FILTER = "&restrictSearchableAttributes=";
    private static final String ALGOLIA_SEARCH_BY_DATE = "search_by_date?query=";
    private static final String TAG_STORY = "story";
    private static final String TAG_COMMENT = "comment";
    private static final String TAG_POLL = "poll";
    private static final String TAG_POLLOPT = "pollopt";
    private static final String TAG_SHOW = "show_hn";
    private static final String TAG_ASK = "ask_hn";
    private static final String TAG_FRONT_PAGE = "front_page";
    private static final String TAG_AUTHOR = "author_:";
    private static final String TAG_STORY_SEARCH = "story_:";
    private static final String FILTER_CREATED_AT  = "created_at_i";
    private static final String FILTER_POINTS = "points";
    private static final String FILTER_COMMENT_COUNT = "num_comments";
    private static final String PARAM_PAGE = "page=";
    private static final String PARAM_URL = "url";
    private static final String PARAM_TITLE = "titile";
    private static final String PARAM_HITS_PER_PAGE = "hitsPerPage=";

    public static String getItemPath(int itemId) {
        return BASE_PATH + ITEM + itemId + JSON;
    }

    public static String getUserPath(String usedId) {
        return BASE_PATH + USER + usedId + JSON;
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

    public static String getMercuryAmpPath(String url) {
        return MERCURY_AMP_PATH + url;
    }

    public static String getMercuryParserPath(String url) {
        return MERCURY_PARSER_PATH + url;
    }

    public static String getPDFDisplayPath(String url) {
        return DOCS_PDF_BASE + url;
    }

    public static String getAlgoliaItemPath(int id) {
        return ALGOLIA_BASE + ALGOLIA_ITEM_PATH + id;
    }

    public static int parseItemUrl(String url) {
        if(url.contains("item?")) {
            try {
                final String idString = (String) url.subSequence(url.indexOf("=") + 1, url.length());
                Log.i(TAG, "parseItemUrl: idString " + idString);
                final int id = Integer.parseInt(idString);
                Log.i(TAG, "parseItemUrl: id is " + id);
                return id;

            } catch(Exception e) {
                return -1;
            }
        }
        return -1;
    }

    public static String parseUserUrl(String url) {
        if(url.contains("user?")) {
            return url.substring(url.indexOf("=") + 1);
        }
        return "";
    }

    public String getStorySearchPath(String query) {
        return ALGOLIA_BASE + ALGOLIA_SEARCH + query + ALGOLIA_TAGS + TAG_STORY;
    }

    public String getStorySearchPathByDate(String query) {
        return ALGOLIA_BASE + ALGOLIA_SEARCH_BY_DATE + query + ALGOLIA_TAGS + TAG_STORY;
    }

    public String getCommentSearchPath(String query) {
        return ALGOLIA_BASE + ALGOLIA_SEARCH + query + ALGOLIA_TAGS + TAG_COMMENT;
    }

    public String getCommentSearchPathByDate(String query) {
        return ALGOLIA_BASE + ALGOLIA_SEARCH_BY_DATE + query + ALGOLIA_TAGS + TAG_COMMENT;
    }

    public String getStorySearchByUrl(String query) {
        return appendAttributeFilterToSearch(getStorySearchPath(query), PARAM_URL);
    }

    public String getStorySearchByUrlByDate(String query) {
        return appendAttributeFilterToSearch(getStorySearchPathByDate(query), PARAM_URL);
    }

    public String getStorySearchByTitle(String query) {
        return appendAttributeFilterToSearch(getStorySearchPath(query), PARAM_TITLE);
    }

    public String getStorySearchByTitleByDate(String query) {
        return appendAttributeFilterToSearch(getStorySearchPathByDate(query), PARAM_TITLE);
    }

    public String getCommentSearchPath(String query, int parent) {
        return ALGOLIA_BASE + ALGOLIA_SEARCH + query + ALGOLIA_TAGS + TAG_COMMENT + "," + TAG_STORY_SEARCH + parent;
    }

    public String appendDateFilterToSearch(String base, int since) {
        return base + ALGOLIA_NUMERIC_FILTERS + FILTER_CREATED_AT + ">" + since;
    }

    private String appendAttributeFilterToSearch(String base, String attribute) {
        return base + ALGOLIA_ATTRIBUTE_FILTER + attribute;
    }

}
