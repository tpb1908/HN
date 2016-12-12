package com.tpb.hn.settings;

import android.content.Context;

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
        return false;
    }

    public static boolean shouldBlockAds(Context context) {
        return false;
    }

    public static boolean shouldLoadInBackground(Context context) {
        return false;
    }

    public static int getSkimmerSpeed(Context context) {
        return 0;
    }

    public static boolean shouldShowSkimmerSeekbarHint(Context context) {
        return false;
    }

    public static boolean shouldShowSkimmerParsedText(Context context) {
        return false;
    }

    //TODO fonts, sizes, and line spacing

    public static FragmentPagerAdapter.PageType getDefaultBrowserMode(Context context) {
        return null;
    }

    public static boolean shouldHideNavigationInBrowserFullscreen(Context context) {
        return false;
    }

    public static boolean isLazyLoadEnabled(Context context) {
        return false;
    }

    public static boolean shouldRedirectThroughService(Context context) {
        return false;
    }

    public static boolean isHorizontalBrowserScrollingLocked(Context context) {
        return false;
    }

    public static boolean shouldDisplayCommentsAsCards(Context context) {
        return false;
    }

    public static boolean shouldExpandCommentTrees(Context context) {
        return false;
    }

    //TODO fonts, sizes, and line spacing

    public static boolean isCommentVolumeNavigationEnabled(Context context) {
        return false;
    }

    public static boolean isCommentFloatingFABEnabled(Context context) {
        return false;
    }

    public static boolean shouldShowCommentChildCount(Context context) {
        return false;
    }

    public static boolean shouldShortenLongComments(Context context) {
        return false;
    }

    public static boolean shouldDisplayFeedAsCards(Context context) {
        return false;
    }

    //TODO fonts, sizes, and line spacing

    public static boolean shouldMarkNewItems(Context context) {
        return false;
    }

    public static boolean shouldMarkReadWhenPassed(Context context) {
        return false;
    }

    public static boolean shouldScrollToTopOnRefresh(Context context) {
        return false;
    }

    public static boolean isFeedCommentVolumeNavigationEnabled(Context context) {
        return false;
    }

    public static boolean shouldShowFeedScrollbar(Context context) {
        return false;
    }

    public static boolean isFeedFastScrollingEnabled(Context context) {
        return false;
    }

    public static boolean isFeedFloatingFABEnabled(Context context) {
        return false;
    }
    
    //TODO Colour scheme
    
    public static boolean shouldDisplayToolbarOnBottom(Context context) {
        return false;
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
