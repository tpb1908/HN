package com.tpb.hn.network;

/**
 * Created by theo on 17/10/16.
 */

public class APIPaths {
    public static final String BASE_PATH = "https://hacker-news.firebaseio.com/v0/";
    public static final String ITEM = "item/";
    public static final String USER = "user/";
    public static final String MAX_ITEM = "maxitem";
    public static final String NEW_STORIES = "newstories/";
    public static final String TOP_STORIES = "topstories/";
    public static final String BEST_STORIES = "beststories/";
    public static final String ASK_STORIES = "askstories/";
    public static final String SHOW_STORIES = "showstories";
    public static final String JOB_STORIES = "jobstories/";
    public static final String UPDATED = "updates/";

    public static final String READABILITY_PARSER = "https://readability.com/api/content/v1/parser?url=";
    public static final String READABILITY_PARSER_KEY = "387ce7ecc4df735d0a7c1e748a9e093fb8213ade";

    public static final String JSON = ".json";
    public static final String PRETTY = "?print=pretty";

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


}
