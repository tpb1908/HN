package com.tpb.hn.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;

import com.tpb.hn.viewer.FragmentPagerAdapter;

import java.util.Arrays;

/**
 * Created by theo on 18/10/16.
 * http://stackoverflow.com/questions/4877153/android-application-wide-font-size-preference
 */

public class SharedPrefsController {
    private static final String TAG = SharedPrefsController.class.getSimpleName();
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
    private static final String KEY_BLOCK_ADS = "BLOCK_ADS";
    private static final String KEY_SCROLL_TO_TOP = "SCROLL_TO_TOP";
    private static final String KEY_DISABLE_HORIZONTAL_SCROLLING = "HORIZONTAL_SCROLLING";
    private static final String KEY_LAZY_LOAD = "LAZY_LOAD";
    private static final String KEY_VOLUME_NAVIGATION = "VOLUME_NAVIGATION";
    private static final String KEY_DARK_START = "DARK_START";
    private static final String KEY_DARK_END = "DARK_END";
    private static final String KEY_AUTO_DARK = "AUTO_DARK";
    private static final String KEY_SHOW_SEEKBAR_HINT = "SEEKBAR_HINT";
    private static final String KEY_LOAD_IN_BACKGROUND = "BACKGROUND_LOAD";
    private static final String KEY_SCROLLBAR = "SCROLLBAR";
    private static final String KEY_FAST_SCROLL = "FAST_SCROLL";
    private static final String KEY_BOTTOM_TOOLBAR = "BOTTOM_TOOLBAR";
    private static final String KEY_FULLSCREEN_CONTENT = "FULLSCREEN_CONTENT";
    private static final String KEY_SKIMMER_BODY = "SKIMMER_BODY";
    private static final String KEY_COMMENT_VOLUME_NAVIGATION = "COMMENT_VOLUME_NAVIGATION";
    private static final String KEY_COMMENT_CHILDREN = "COMMENT_CHILDREN";
    private static final String KEY_FLOATING_FAB = "FLOATING_FAB";
    private static SharedPrefsController instance;
    private static FragmentPagerAdapter.PageType[] pageTypes;
    private static int skimmerWPM;
    private static String defaultPage;
    private static boolean useCards;
    private static boolean useCardsComments;
    private static boolean markReadWhenPassed;
    private static boolean useDarkTheme;
    private static boolean expandComments;
    private static boolean shouldBlockAds;
    private static boolean shouldScrollToTop;
    private static boolean disableHorizontalScrolling;
    private static boolean lazyLoad;
    private static boolean volumeNavigation;
    private static boolean autoDarkTheme;
    private static int darkThemeStart;
    private static int darkThemeEnd;
    private static boolean showSeekBarHint;
    private static boolean loadInBackground;
    private static boolean showScrollbar;
    private static boolean fastScroll;
    private static boolean bottomToolbar;
    private static boolean fullscreenInContent;
    private static boolean skimmerTextBody;
    private static boolean commentVolumeNavigation;
    private static boolean commentChildCount;
    private boolean showFloatingFAB;

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

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

    public static SharedPrefsController getInstance(Context context) {
        if(instance == null) {
            instance = new SharedPrefsController(context);
        }
        return instance;
    }

    private void getValues() {
        useCards = prefs.getBoolean(KEY_USE_CARDS, false);
        markReadWhenPassed = prefs.getBoolean(KEY_MARK_READ_WHEN_PASSED, false);
        useDarkTheme = prefs.getBoolean(KEY_DARK_THEME, false);
        useCardsComments = prefs.getBoolean(KEY_USE_CARDS_COMMENTS, false);
        expandComments = prefs.getBoolean(KEY_EXPAND_COMMENTS, true);
        shouldBlockAds = prefs.getBoolean(KEY_BLOCK_ADS, true);
        shouldScrollToTop = prefs.getBoolean(KEY_SCROLL_TO_TOP, true);
        disableHorizontalScrolling = prefs.getBoolean(KEY_DISABLE_HORIZONTAL_SCROLLING, false);
        lazyLoad = prefs.getBoolean(KEY_LAZY_LOAD, false);
        volumeNavigation = prefs.getBoolean(KEY_VOLUME_NAVIGATION, false);
        darkThemeStart = prefs.getInt(KEY_DARK_START, -1);
        darkThemeEnd = prefs.getInt(KEY_DARK_END, -1);
        autoDarkTheme = prefs.getBoolean(KEY_AUTO_DARK, false);
        showSeekBarHint = prefs.getBoolean(KEY_SHOW_SEEKBAR_HINT, false);
        loadInBackground = prefs.getBoolean(KEY_LOAD_IN_BACKGROUND, true);
        showScrollbar = prefs.getBoolean(KEY_SCROLLBAR, true);
        fastScroll = prefs.getBoolean(KEY_FAST_SCROLL, true);
        bottomToolbar = prefs.getBoolean(KEY_BOTTOM_TOOLBAR, false);
        fullscreenInContent = prefs.getBoolean(KEY_FULLSCREEN_CONTENT, true);
        skimmerTextBody = prefs.getBoolean(KEY_SKIMMER_BODY, true);
        commentVolumeNavigation = prefs.getBoolean(KEY_COMMENT_VOLUME_NAVIGATION, false);
        commentChildCount = prefs.getBoolean(KEY_COMMENT_CHILDREN, true);
        showFloatingFAB = prefs.getBoolean(KEY_FLOATING_FAB, false);
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

    public String getDefaultPage() {
        if(defaultPage == null) {
            defaultPage = prefs.getString(KEY_DEFAULT_PAGE, "BEST");
        }
        return defaultPage;
    }

    public void setDefaultPage(String newDefault) {
        defaultPage = newDefault;
        editor.putString(KEY_DEFAULT_PAGE, newDefault);
        editor.apply();
    }

    public boolean getUseCards() {
        return useCards;
    }

    public void setUseCards(boolean shouldUseCards) {
        useCards = shouldUseCards;
        editor.putBoolean(KEY_USE_CARDS, useCards);
        editor.apply();
    }

    public boolean getMarkReadWhenPassed() {
        return markReadWhenPassed;
    }

    public void setMarkReadWhenPassed(boolean markRead) {
        markReadWhenPassed = markRead;
        editor.putBoolean(KEY_MARK_READ_WHEN_PASSED, markReadWhenPassed);
        editor.apply();
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

    public boolean getBlockAds() {
        return shouldBlockAds;
    }

    public void setBlockAds(boolean blockAds) {
        shouldBlockAds = blockAds;
        editor.putBoolean(KEY_BLOCK_ADS, shouldBlockAds);
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

    public boolean getAutoDark() {
        return autoDarkTheme;
    }

    public void setAutoDark(boolean autoDark) {
        autoDarkTheme = autoDark;
        editor.putBoolean(KEY_AUTO_DARK, autoDarkTheme);
        editor.commit();
    }

    public void setDarkTimes(int start, int end) {
        darkThemeStart = start;
        darkThemeEnd = end;
        editor.putInt(KEY_DARK_START, darkThemeStart);
        editor.putInt(KEY_DARK_END, darkThemeEnd);
        editor.commit();
    }

    public void setShowSeekBarHint(boolean show) {
        showSeekBarHint = show;
        editor.putBoolean(KEY_SHOW_SEEKBAR_HINT, showSeekBarHint);
        editor.commit();
    }

    public boolean getLoadInBackground() {
        return loadInBackground;
    }

    public void setLoadInBackground(boolean shouldLoad) {
        loadInBackground = shouldLoad;
        editor.putBoolean(KEY_LOAD_IN_BACKGROUND, loadInBackground);
        editor.commit();
    }

    public boolean showSeekBarHint() {
        return showSeekBarHint;
    }

    public boolean getShowScrollbar() {
        return showScrollbar;
    }

    public void setShowScrollbar(boolean shouldShow) {
        showScrollbar = shouldShow;
        editor.putBoolean(KEY_SCROLLBAR, showScrollbar);
        editor.commit();
    }

    public void setFastScrolling(boolean shouldFastScroll) {
        fastScroll = shouldFastScroll;
        editor.putBoolean(KEY_FAST_SCROLL, fastScroll);
        editor.commit();
    }

    public boolean getFastScroll() {
        return fastScroll;
    }

    public boolean getBottomToolbar() {
        return bottomToolbar;
    }

    public void setBottomToolbar(boolean bottom) {
        bottomToolbar = bottom;
        editor.putBoolean(KEY_BOTTOM_TOOLBAR, bottomToolbar);
        editor.commit();
    }

    public boolean getFullscreenContent() {
        return fullscreenInContent;
    }

    public void setFullScreenContent(boolean fullscreen) {
        fullscreenInContent = fullscreen;
        editor.putBoolean(KEY_FULLSCREEN_CONTENT, fullscreenInContent);
        editor.commit();
    }

    public boolean getSkimmerBody() {
        return skimmerTextBody;
    }

    public void setSkimmerBody(boolean body) {
        skimmerTextBody = body;
        editor.putBoolean(KEY_SKIMMER_BODY, skimmerTextBody);
        editor.commit();
    }

    public void setCommentVolumeNavigation(boolean enabled) {
        commentVolumeNavigation = enabled;
        editor.putBoolean(KEY_COMMENT_VOLUME_NAVIGATION, commentVolumeNavigation);
        editor.commit();
    }

    public boolean getCommentVolumeNavigation() {
        return commentVolumeNavigation;
    }

    public void setCommentChildren(boolean showChildren) {
        commentChildCount = showChildren;
        editor.putBoolean(KEY_COMMENT_CHILDREN, commentChildCount);
        editor.commit();
    }

    public boolean getCommentChildren() {
        return commentChildCount;
    }

    public void setShowFloatingFAB(boolean show) {
        showFloatingFAB = show;
        editor.putBoolean(KEY_FLOATING_FAB, showFloatingFAB);
        editor.commit();
    }

    public boolean getShowFloatingFAB() {
        return showFloatingFAB;
    }

    public Pair<Integer, Integer> getDarkTimeRange() {
        return new Pair<>(darkThemeStart, darkThemeEnd);
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
