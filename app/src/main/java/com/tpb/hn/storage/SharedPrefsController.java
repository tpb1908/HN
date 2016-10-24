package com.tpb.hn.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.tpb.hn.story.StoryAdapter;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by theo on 18/10/16.
 */

public class SharedPrefsController {
    private static SharedPrefsController instance;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String PREF_ID = "PREF_SETTINGS";
    private static final String KEY_FIRST_RUN = "FIRST_RUN";
    private static final String KEY_STORY_TABS = "STORY_TABS";
    private static final String KEY_SKIMMER_WPM = "SKIMMER_WPM";
    private static final String KEY_DEFAULT_PAGE = "DEFAULT_PAGE";
    private static final String KEY_USE_CARDS = "USE_FALSE";
    private static final String KEY_MARK_READ_WHEN_PASSED = "MARK_READ_WHEN_PASSED";

    private static StoryAdapter.PageType[] pageTypes;
    private static int skimmerWPM;
    private static String defaultPage;
    private static boolean useCards;
    private static boolean markReadWhenPassed;

    public static SharedPrefsController getInstance(Context context) {
        if(instance == null) {
            instance = new SharedPrefsController(context);
        }
        return instance;
    }

    private SharedPrefsController(Context context) {
        prefs = context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE);
        editor = prefs.edit();
        if(prefs.getBoolean(KEY_FIRST_RUN, true)) {
            editor.putBoolean(KEY_FIRST_RUN, false);
            initInitialValues();
        }
        useCards = prefs.getBoolean(KEY_USE_CARDS, false);
        markReadWhenPassed = prefs.getBoolean(KEY_USE_CARDS, false);
        editor.apply();

    }

    private void initInitialValues() {
        final HashSet<String> tabs = new HashSet<>();
        tabs.add("C");
        tabs.add("B");
        tabs.add("R");
        tabs.add("S");
        editor.putStringSet(KEY_STORY_TABS, tabs);

        editor.putInt(KEY_SKIMMER_WPM, 500);

        editor.putString(KEY_DEFAULT_PAGE, "TOP");

        editor.putBoolean(KEY_USE_CARDS, false);
        editor.apply();
    }

    public int getSkimmerWPM() {
        if(skimmerWPM == 0) {
            skimmerWPM = prefs.getInt(KEY_SKIMMER_WPM, 500);
        }
        return skimmerWPM;
    }

    public void setSkimmerWPM(int newSkimmerWPM) {
        editor.putInt(KEY_SKIMMER_WPM, newSkimmerWPM);
        editor.apply();
        skimmerWPM = newSkimmerWPM;
    }

    public void setDefaultPage(String newDefault) {
        defaultPage = newDefault;
        editor.putString(KEY_DEFAULT_PAGE, newDefault);
        editor.apply();
    }

    public String getDefaultPage() {
        if(defaultPage == null) {
            defaultPage = prefs.getString(KEY_DEFAULT_PAGE, "BEST");
        }
        return defaultPage;
    }

    public void setUseCards(boolean shouldUseCards) {
        useCards = shouldUseCards;
        editor.putBoolean(KEY_USE_CARDS, useCards);
        editor.apply();
    }

    public boolean getUseCards() {
        return useCards;
    }

    public void setMarkReadWhenPassed(boolean markRead) {
        markReadWhenPassed = markRead;
        editor.putBoolean(KEY_MARK_READ_WHEN_PASSED, markReadWhenPassed);
        editor.apply();
    }

    public boolean getMarkReadWhenPassed() {
        return markReadWhenPassed;
    }

    public StoryAdapter.PageType[] getPageTypes() {
        if(pageTypes == null) {
            final Set<String> tabs = prefs.getStringSet(KEY_STORY_TABS, new HashSet<String>());
            pageTypes = new StoryAdapter.PageType[tabs.size()];
            int i = 0;
            for(String s : tabs) {
                switch(s) {
                    case "C":
                        pageTypes[i] = StoryAdapter.PageType.COMMENTS;
                        break;
                    case "B":
                        pageTypes[i] = StoryAdapter.PageType.BROWSER;
                        break;
                    case "R":
                        pageTypes[i] = StoryAdapter.PageType.READABILITY;
                        break;
                    case "S":
                        pageTypes[i] = StoryAdapter.PageType.SKIMMER;
                        break;
                }
                i++;
            }
        }
        return pageTypes;
    }

}
