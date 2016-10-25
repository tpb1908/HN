package com.tpb.hn.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tpb.hn.story.StoryAdapter;

import java.util.Arrays;

/**
 * Created by theo on 18/10/16.
 */

public class SharedPrefsController {
    private static final String TAG = SharedPrefsController.class.getSimpleName();

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
    private static final String KEY_DARK_THEME = "DARK_THEME";

    private static StoryAdapter.PageType[] pageTypes;
    private static int skimmerWPM;
    private static String defaultPage;
    private static boolean useCards;
    private static boolean markReadWhenPassed;
    private static boolean useDarkTheme;

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
        useDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false);
        editor.apply();

    }

    private void initInitialValues() {
        final String defaultPages = "BCRS";
        editor.putString(KEY_STORY_TABS, defaultPages);
        editor.putInt(KEY_SKIMMER_WPM, 500);

        editor.putString(KEY_DEFAULT_PAGE, "TOP");

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

    public boolean getUseDarkTheme() {
        return useDarkTheme;
    }

    public void setUseDarkTheme(boolean shouldUseDarkTheme) {
        useDarkTheme = shouldUseDarkTheme;
        editor.putBoolean(KEY_DARK_THEME, useDarkTheme);
        editor.commit();
    }

    public StoryAdapter.PageType[] getPageTypes() {
        if(pageTypes == null) {
            final String tabs = prefs.getString(KEY_STORY_TABS, "BCRS").toUpperCase();
            Log.i(TAG, "getPageTypes: Loading page types " + tabs);
            pageTypes = new StoryAdapter.PageType[tabs.length()];
            for(int i = 0; i < tabs.length(); i++) {
                switch(tabs.charAt(i)) {
                    case 'C':
                        pageTypes[i] = StoryAdapter.PageType.COMMENTS;
                        break;
                    case 'B':
                        Log.i(TAG, "getPageTypes: Adding browser");
                        pageTypes[i] = StoryAdapter.PageType.BROWSER;
                        break;
                    case 'R':
                        pageTypes[i] = StoryAdapter.PageType.READABILITY;
                        break;
                    case 'S':
                        pageTypes[i] = StoryAdapter.PageType.SKIMMER;
                        break;
                }
            }
        }
        Log.i(TAG, "getPageTypes: Array is " + Arrays.toString(pageTypes));
        return pageTypes;
    }

}
