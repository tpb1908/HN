package com.tpb.hn.settings;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;

import com.tpb.hn.R;
import com.tpb.hn.viewer.FragmentPagerAdapter;

/**
 * Created by theo on 12/12/16.
 */
public class Preferences {

    /*Viewer
    * Pages
    * Sheet?
    *
    * Data
    * Block ads
    * Load in background
    *
    * Skimmer
    * Speed
    * Show hint in seekbar
    * Show parsed text
    *
    * Mercury
    * Font
    * Font size
    * Line spacing
    *
    * Browser
    * Default  mode
    * Hide navigation in fullscreen
    * Lazy loading
    * Redirect through parser
    * Lock horizontal scrolling
    *
    * Comments
    * Cards
    * Initial state
    * Font
    * Font size
    * Line spacing
    * Navigate with volume keys
    * Enable floating fab
    * Show child count button
    * Line count for large comments
    *
    * Content
    * Cards
    * Font
    * Font size
    * Line spacing
    * Mark new items
    * Mark read when passed
    * Scroll to top on refresh
    * Navigate with volume keys
    * Show scroll bar
    * Enable fast scrolling
    * Enable floating fab
    *
    * Theming
    * Colour scheme
    * Toolbar position
    * (App wide font settings?)
    * */


    private Preferences() {
    }
    


    public static FragmentPagerAdapter.PageType getViewerPages(Context context) {
        return null;
    }

    public static boolean shouldShowViewerInSheet(Context context) {
        return getBool(context, R.string.pref_viewer_sheet, false);
    }

    public static boolean shouldBlockAds(Context context) {
        return getBool(context, R.string.pref_data_ad_block, true);
    }

    public static boolean shouldLoadInBackground(Context context) {
        return getBool(context, R.string.text_load_in_background, true);
    }

    public static int getSkimmerSpeed(Context context) {
        return 0;
    }

    public static boolean shouldShowSkimmerSeekbarHint(Context context) {
        return getBool(context, R.string.pref_skimmer_seekbar_hint, false);
    }

    public static boolean shouldShowSkimmerParsedText(Context context) {
        return getBool(context, R.string.pref_skimmer_parsed_text, true);
    }

    //TODO fonts, sizes, and line spacing

    public static FragmentPagerAdapter.PageType getDefaultBrowserMode(Context context) {
        return null;
    }

    public static boolean shouldHideNavigationInBrowserFullscreen(Context context) {
        return getBool(context, R.string.text_hide_navigation_fullscreen, false);
    }

    public static boolean isLazyLoadEnabled(Context context) {
        return getBool(context, R.string.pref_browser_lazy, false);
    }

    public static boolean shouldRedirectThroughService(Context context) {
        return getBool(context, R.string.pref_browser_redirect, true);
    }

    public static boolean isHorizontalBrowserScrollingLocked(Context context) {
        return getBool(context, R.string.pref_browser_horizontal_scrolling, false);
    }

    public static boolean shouldDisplayCommentsAsCards(Context context) {
        return getBool(context, R.string.pref_comments_cards, true);
    }

    public static boolean shouldExpandCommentTrees(Context context) {
        return getBool(context, R.string.text_expand_comments, true);
    }

    //TODO fonts, sizes, and line spacing

    public static boolean isCommentVolumeNavigationEnabled(Context context) {
        return getBool(context, R.string.pref_comments_volume_navigation, false);
    }

    public static boolean isCommentFloatingFABEnabled(Context context) {
        return getBool(context, R.string.pref_comments_floating_fab, false);
    }

    public static boolean shouldShowCommentChildCount(Context context) {
        return getBool(context, R.string.pref_comments_child_count, true);
    }

    public static boolean shouldShortenLongComments(Context context) {
        return getBool(context, R.string.pref_comments_shorten_long_comments, false);
    }

    public static boolean shouldDisplayFeedAsCards(Context context) {
        return getBool(context, R.string.pref_feed_cards, true);
    }

    //TODO fonts, sizes, and line spacing

    public static boolean shouldMarkNewItems(Context context) {
        return getBool(context, R.string.pref_feed_mark_new, true);
    }

    public static boolean shouldMarkReadWhenPassed(Context context) {
        return getBool(context, R.string.pref_feed_mark_read, false);
    }

    public static boolean shouldScrollToTopOnRefresh(Context context) {
        return getBool(context, R.string.pref_feed_scroll_to_top, true);
    }

    public static boolean isFeedCommentVolumeNavigationEnabled(Context context) {
        return getBool(context, R.string.pref_feed_volume_navigation, false);
    }

    public static boolean shouldShowFeedScrollbar(Context context) {
        return getBool(context, R.string.pref_feed_scroll_bar, true);
    }

    public static boolean isFeedFastScrollingEnabled(Context context) {
        return getBool(context, R.string.pref_feed_fast_scrolling_bar, true);
    }

    public static boolean isFeedFloatingFABEnabled(Context context) {
        return getBool(context, R.string.pref_feed_floating_fab, false);
    }
    
    //TODO Colour scheme
    
    public static boolean shouldDisplayToolbarOnBottom(Context context) {
        return getBool(context, R.string.pref_theme_toolbar_bottom, false);
    }

    private static boolean getBool(Context context, @StringRes int keyId, boolean defaultVal) {
        return PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean(context.getString(keyId), defaultVal);
    }

    private static String getString(Context context, @StringRes int keyId, String defaultVal) {
        return  PreferenceManager.getDefaultSharedPreferences(context).
                getString(context.getString(keyId), defaultVal);
    }
    
    private static int getInt(Context context, @StringRes int keyId, int defaultVal) {
        return PreferenceManager.getDefaultSharedPreferences(context).
                getInt(context.getString(keyId), defaultVal);
    }


    public static void setViewerPages(Context context) {
        
    }

    public static void setShouldShowViewerInSheet(Context context, boolean show) {
        setBool(context, R.string.pref_viewer_sheet, show);
    }

    public static void setShouldBlockAds(Context context, boolean block) {
        setBool(context, R.string.pref_data_ad_block, block);
    }

    public static void setShouldLoadInBackground(Context context, boolean background) {
        setBool(context, R.string.pref_data_bg_loading, background);
    }

    public static void setSkimmerSpeed(Context context, int value) {
        setInt(context, R.string.pref_skimmer_speed, value);
    }

    public static void setShouldShowSkimmerSeekbarHvoid(Context context, boolean show) {
        setBool(context, R.string.pref_skimmer_seekbar_hint, show);
    }

    public static void setShouldShowSkimmerParsedText(Context context, boolean show) {
        setBool(context, R.string.pref_skimmer_parsed_text, show);
    }

    //TODO fonts, sizes, and line spacing

    public static void setDefaultBrowserMode(Context context) {
        
    }

    public static void setShouldHideNavigationInBrowserFullscreen(Context context, boolean hide) {
        setBool(context, R.string.text_hide_navigation_fullscreen, hide);
    }

    public static void setLazyLoadEnabled(Context context, boolean enabled) {
        setBool(context, R.string.pref_browser_lazy, enabled);
    }

    public static void setShouldRedirectThroughService(Context context, boolean redirect) {
        setBool(context, R.string.pref_browser_redirect, redirect);
    }

    public static void setHorizontalBrowserScrollingLocked(Context context, boolean lock) {
        setBool(context, R.string.pref_browser_redirect, lock);
    }

    public static void setShouldDisplayCommentsAsCards(Context context, boolean cards) {
        setBool(context, R.string.pref_comments_cards, cards);
    }

    public static void setShouldExpandCommentTrees(Context context, boolean expand) {
        setBool(context, R.string.text_expand_comments, expand);
    }

    //TODO fonts, sizes, and line spacing

    public static void setCommentVolumeNavigationEnabled(Context context, boolean enabled) {
        setBool(context, R.string.pref_comments_volume_navigation, enabled);
    }

    public static void setCommentFloatingFABEnabled(Context context, boolean enabled) {
        setBool(context, R.string.pref_comments_floating_fab, enabled);
    }

    public static void setShouldShowCommentChildCount(Context context, boolean show) {
        setBool(context, R.string.pref_comments_child_count, show);
    }

    public static void setShouldShortenLongComments(Context context, boolean shorten) {
        setBool(context, R.string.pref_comments_shorten_long_comments, shorten);
    }

    public static void setShouldDsetplayFeedAsCards(Context context, boolean cards) {
        setBool(context, R.string.pref_feed_cards, cards);
    }

    //TODO fonts, sizes, and line spacing

    public static void setShouldMarkNewItems(Context context, boolean mark) {
        setBool(context, R.string.pref_feed_mark_new, mark);
    }

    public static void setShouldMarkReadWhenPassed(Context context, boolean mark) {
        setBool(context, R.string.pref_feed_mark_read, mark);
    }

    public static void setShouldScrollToTopOnRefresh(Context context, boolean scroll) {
        setBool(context, R.string.pref_feed_scroll_to_top, scroll);
    }

    public static void setFeedCommentVolumeNavigationEnabled(Context context, boolean enabled) {
        setBool(context, R.string.pref_feed_volume_navigation, enabled);
    }

    public static void setShouldShowFeedScrollbar(Context context, boolean show) {
        setBool(context, R.string.pref_feed_scroll_bar, show);
    }

    public static void setFeedFastScrollingEnabled(Context context, boolean enabled) {
        setBool(context, R.string.pref_feed_fast_scrolling_bar, enabled);
    }

    public static void setFeedFloatingFABEnabled(Context context, boolean enabled) {
        setBool(context, R.string.pref_feed_floating_fab, enabled);
    }

    //TODO Colour scheme

    public static void setShouldDisplayToolbarOnBottom(Context context, boolean bottom) {
        setBool(context, R.string.pref_theme_toolbar_bottom, bottom);
    }

    private static void setBool(Context context, @StringRes int keyId, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(keyId), value)
                .apply();
    }

    private static void setString(Context context, @StringRes int keyId, String value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(keyId), value)
                .apply();
    }

    private static void setInt(Context context, @StringRes int keyId, int value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(context.getString(keyId), value)
                .apply();
    }

}
