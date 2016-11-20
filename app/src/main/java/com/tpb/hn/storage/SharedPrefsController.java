package com.tpb.hn.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tpb.hn.item.FragmentPagerAdapter;

import java.util.Arrays;

/**
 * Created by theo on 18/10/16.
 * TODO- Font sizes
 * http://stackoverflow.com/questions/4877153/android-application-wide-font-size-preference
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
    private static final String KEY_USE_CARDS = "USE_CARDS";
    private static final String KEY_MARK_READ_WHEN_PASSED = "MARK_READ_WHEN_PASSED";
    private static final String KEY_DARK_THEME = "DARK_THEME";
    private static final String KEY_USE_CARDS_COMMENTS = "CARD_COMMENTS";
    private static final String KEY_EXPAND_COMMENTS = "EXPAND_COMMENTS";
    private static final String KEY_ANIMATE_COMMENTS = "ANIMATE_COMMENTS";
    private static final String KEY_BLOCK_ADS = "BLOCK_ADS";
    private static final String KEY_SCROLL_TO_TOP = "SCROLL_TO_TOP";
    private static final String KEY_DISABLE_HORIZONTAL_SCROLLING = "HORIZONTAL_SCROLLING";
    private static final String KEY_LAZY_LOAD = "LAZY_LOAD";
    private static final String KEY_VOLUME_NAVIGATION = "VOLUME_NAVIGATION";

    private static FragmentPagerAdapter.PageType[] pageTypes;
    private static int skimmerWPM;
    private static String defaultPage;
    private static boolean useCards;
    private static boolean useCardsComments;
    private static boolean markReadWhenPassed;
    private static boolean useDarkTheme;
    private static boolean expandComments;
    private static boolean animateComments;
    private static boolean shouldBlockAds;
    private static boolean shouldScrollToTop;
    private static boolean disableHorizontalScrolling;
    private static boolean lazyLoad;
    private static boolean volumeNavigation;

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
        getValues();
        editor.apply();

    }

    private void getValues() {
        useCards = prefs.getBoolean(KEY_USE_CARDS, false);
        markReadWhenPassed = prefs.getBoolean(KEY_MARK_READ_WHEN_PASSED, false);
        useDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false);
        useCardsComments = prefs.getBoolean(KEY_USE_CARDS_COMMENTS, false);
        expandComments = prefs.getBoolean(KEY_EXPAND_COMMENTS, true);
        animateComments = prefs.getBoolean(KEY_ANIMATE_COMMENTS, true);
        shouldBlockAds = prefs.getBoolean(KEY_BLOCK_ADS, true);
        shouldScrollToTop = prefs.getBoolean(KEY_SCROLL_TO_TOP, true);
        disableHorizontalScrolling = prefs.getBoolean(KEY_DISABLE_HORIZONTAL_SCROLLING, false);
        lazyLoad = prefs.getBoolean(KEY_LAZY_LOAD, false);
        volumeNavigation = prefs.getBoolean(KEY_VOLUME_NAVIGATION, false);
    }

    private void initInitialValues() {
        final String defaultPages = "BCRS";
        editor.putString(KEY_STORY_TABS, defaultPages);
        editor.putInt(KEY_SKIMMER_WPM, 500);
        editor.putString(KEY_DEFAULT_PAGE, "TOP");

        editor.apply();
    }

    public void reset() {
        editor.clear();
        editor.commit();
        getValues();
        initInitialValues();

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

    public boolean getUseCardsComments() {
        return useCardsComments;
    }

    public void setUseCardsComments(boolean shouldUseCardsComments) {
        useCardsComments = shouldUseCardsComments;
        editor.putBoolean(KEY_USE_CARDS_COMMENTS, useCardsComments);
        editor.commit();
    }

    public boolean getExpandComments() {
        return expandComments;
    }

    public void setExpandComments(boolean shouldExpandComments) {
        expandComments = shouldExpandComments;
        editor.putBoolean(KEY_EXPAND_COMMENTS, expandComments);
        editor.commit();
    }

    public boolean getAnimateComments() {
        return animateComments;
    }

    public void setAnimateComments(boolean shouldAnimateComments) {
        animateComments = shouldAnimateComments;
        editor.putBoolean(KEY_ANIMATE_COMMENTS, animateComments);
        editor.commit();
    }

    public boolean getBlockAds() {
        return shouldBlockAds;
    }

    public void setBlockAds(boolean blockAds) {
        shouldBlockAds = blockAds;
        editor.putBoolean(KEY_ANIMATE_COMMENTS, shouldBlockAds);
        editor.commit();
    }

    public boolean getShouldScrollToTop() {
        return shouldScrollToTop;
    }

    public void setShouldScrollToTop(boolean shouldScroll) {
        shouldScrollToTop = shouldScroll;
        editor.putBoolean(KEY_SCROLL_TO_TOP, shouldScrollToTop);
        editor.commit();
    }

    public boolean getDisableHorizontalScrolling() {
        return disableHorizontalScrolling;
    }

    public void setDisableHorizontalScrolling(boolean shouldDisable) {
        disableHorizontalScrolling = shouldDisable;
        editor.putBoolean(KEY_DISABLE_HORIZONTAL_SCROLLING, disableHorizontalScrolling);
        editor.commit();
    }

    public boolean getLazyLoad() {
        return lazyLoad;
    }

    public void setLazyLoad(boolean shouldLazyLoad) {
        lazyLoad = shouldLazyLoad;
        editor.putBoolean(KEY_LAZY_LOAD, lazyLoad);
        editor.commit();
    }

    public boolean getVolumeNavigation() {
        return volumeNavigation;
    }

    public void setVolumeNavigation(boolean useVolume) {
        volumeNavigation = useVolume;
        editor.putBoolean(KEY_VOLUME_NAVIGATION, volumeNavigation);
        editor.commit();
    }

    public FragmentPagerAdapter.PageType[] getPageTypes() {
        if(pageTypes == null) {
            final String tabs = prefs.getString(KEY_STORY_TABS, "BCRS").toUpperCase();
            Log.i(TAG, "getPageTypes: Loading page types " + tabs);
            pageTypes = new FragmentPagerAdapter.PageType[tabs.length()];
            for(int i = 0; i < tabs.length(); i++) {
                switch(tabs.charAt(i)) {
                    case 'C':
                        pageTypes[i] = FragmentPagerAdapter.PageType.COMMENTS;
                        break;
                    case 'B':
                        Log.i(TAG, "getPageTypes: Adding browser");
                        pageTypes[i] = FragmentPagerAdapter.PageType.BROWSER;
                        break;
                    case 'R':
                        pageTypes[i] = FragmentPagerAdapter.PageType.TEXT_READER;
                        break;
                    case 'S':
                        pageTypes[i] = FragmentPagerAdapter.PageType.SKIMMER;
                        break;
                }
            }
        }
        Log.i(TAG, "getPageTypes: Array is " + Arrays.toString(pageTypes));
        return pageTypes;
    }

}
