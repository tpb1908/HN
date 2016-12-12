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

    private static boolean getBool(Context context, @StringRes int resId, boolean defaultVal) {
        return PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean(context.getString(resId), defaultVal);
    }



    public static void setViewerPages(Context context) {
        
    }

    public static void setShouldShowViewerInSheet(Context context) {
        
    }

    public static void setShouldBlockAds(Context context) {
        
    }

    public static void setShouldLoadInBackground(Context context) {
        
    }

    public static void setSkimmerSpeed(Context context) {
        
    }

    public static void setShouldShowSkimmerSeekbarHvoid(Context context) {
        
    }

    public static void setShouldShowSkimmerParsedText(Context context) {
        
    }

    //TODO fonts, sizes, and line spacing

    public static void setDefaultBrowserMode(Context context) {
        
    }

    public static void setShouldHideNavigationInBrowserFullscreen(Context context) {
        
    }

    public static void setLazyLoadEnabled(Context context) {
        
    }

    public static void setShouldRedirectThroughService(Context context) {
        
    }

    public static void setHorizontalBrowserScrollingLocked(Context context) {
        
    }

    public static void setShouldDsetplayCommentsAsCards(Context context) {
        
    }

    public static void setShouldExpandCommentTrees(Context context) {
        
    }

    //TODO fonts, sizes, and line spacing

    public static void setCommentVolumeNavigationEnabled(Context context) {
        
    }

    public static void setCommentFloatingFABEnabled(Context context) {
        
    }

    public static void setShouldShowCommentChildCount(Context context) {
        
    }

    public static void setShouldShortenLongComments(Context context) {
        
    }

    public static void setShouldDsetplayFeedAsCards(Context context) {
        
    }

    //TODO fonts, sizes, and line spacing

    public static void setShouldMarkNewItems(Context context) {
        
    }

    public static void setShouldMarkReadWhenPassed(Context context) {
        
    }

    public static void setShouldScrollToTopOnRefresh(Context context) {
        
    }

    public static void setFeedCommentVolumeNavigationEnabled(Context context) {
        
    }

    public static void setShouldShowFeedScrollbar(Context context) {
        
    }

    public static void setFeedFastScrollingEnabled(Context context) {
        
    }

    public static void setFeedFloatingFABEnabled(Context context) {
        
    }

    //TODO Colour scheme

    public static void setShouldDisplayToolbarOnBottom(Context context) {
        
    }
    

}
