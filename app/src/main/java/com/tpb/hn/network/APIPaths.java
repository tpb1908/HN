package com.tpb.hn.network;

/**
 * Created by theo on 17/10/16.
 */

public class APIPaths {
    private static final String BASE_PATH = "https://hacker-news.firebaseio.com/v0/";
    private static final String ITEM = "item/";
    private static final String USER = "user/";
    private static final String MAX_ITEM = "maxitem";
    private static final String NEW_STORIES = "newstories/";
    private static final String TOP_STORIES = "topstories/";
    private static final String BEST_STORIES = "beststories/";
    private static final String ASK_STORIES = "askstories/";
    private static final String SHOW_STORIES = "showstories";
    private static final String JOB_STORIES = "jobstories/";
    private static final String UPDATED = "updates/";

    private static final String READABILITY_PARSER = "https://readability.com/api/content/v1/parser?url=";
    private static final String READABILITY_PARSER_KEY = "387ce7ecc4df735d0a7c1e748a9e093fb8213ade";

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

    public static String getNewStoriesPath() {
        return BASE_PATH + NEW_STORIES + JSON;
    }

    public static String getTopStoriesPath() {
        return BASE_PATH + TOP_STORIES + JSON;
    }

    public static String getBestStoriesPath() {
        return BASE_PATH + BEST_STORIES + JSON;
    }

    public static String getAskStoriesPath() {
        return BASE_PATH + ASK_STORIES + JSON;
    }

    public static String getShowStoriesPath() {
        return BASE_PATH + SHOW_STORIES + JSON;
    }

    public static String getJobStoriesPath() {
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

    public static String getPDFDisplayPath(String url) {
        return DOCS_PDF_BASE + url;
    }


}
