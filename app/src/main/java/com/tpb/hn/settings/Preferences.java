package com.tpb.hn.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;

import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.viewer.FragmentPagerAdapter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by theo on 12/12/16.
 *
 //TODO fonts, sizes, line spacing, and colour scheme
 */
public class Preferences {
    private static final String TAG = Preferences.class.getSimpleName();

    private final Map<String, Integer> mResourceKeys = new HashMap<>();
    private PreferenceListener mListener;
    private final SharedPreferences.OnSharedPreferenceChangeListener mPrefListener = (sharePreferences, key) -> {
      if(mResourceKeys.containsKey(key) && mListener != null) mListener.preferenceChanged(mResourceKeys.get(key));
    };

    public Preferences(Context context, PreferenceListener listener, @NonNull @StringRes int... keys) {
        mListener = listener;
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(mPrefListener);
        for(int i : keys) addKey(context, i);
    }

    public void addKey(Context context, @StringRes int key) {
        mResourceKeys.put(context.getString(key), key);
    }

    public void removeKey(Context context, @StringRes int key) {
        mResourceKeys.remove(context.getString(key));
    }

    public void unregister(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(mPrefListener);
    }

    public interface PreferenceListener {

        void preferenceChanged(@StringRes int key);

    }

    public static void clearAllPreferences(Context context) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().apply();
    }


    private static FragmentPagerAdapter.PageType pageTypeFromString(String s) {
        for(FragmentPagerAdapter.PageType pt : FragmentPagerAdapter.PageType.values()) {
            if(pt.toString().equals(s)) return pt;
        }
       return FragmentPagerAdapter.PageType.BROWSER;
    }

    public static FragmentPagerAdapter.PageType[] getViewerPages(Context context) {
        final String[] values = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_viewer_pages),
                        FragmentPagerAdapter.PageType.COMMENTS.toString() +
                        FragmentPagerAdapter.PageType.BROWSER.toString() +
                        FragmentPagerAdapter.PageType.TEXT_READER.toString() +
                        FragmentPagerAdapter.PageType.SKIMMER.toString())
                .split(",");
        final FragmentPagerAdapter.PageType[] pages = new FragmentPagerAdapter.PageType[values.length + 1];
        for(int i = 0; i < pages.length; i++) pages[i] = pageTypeFromString(values[i]);
        return pages;
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
        return getInt(context, R.string.pref_skimmer_speed, 600);
    }

    public static boolean shouldShowSkimmerSeekbarHint(Context context) {
        return getBool(context, R.string.pref_skimmer_seekbar_hint, false);
    }

    public static boolean shouldShowSkimmerParsedText(Context context) {
        return getBool(context, R.string.pref_skimmer_parsed_text, true);
    }

    public static FragmentPagerAdapter.PageType getDefaultBrowserMode(Context context) {
        return pageTypeFromString(
                getString(
                        context,
                        R.string.pref_browser_default,
                        FragmentPagerAdapter.PageType.BROWSER.toString()
                )
        );
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


    public static void setViewerPages(Context context, FragmentPagerAdapter.PageType[] pages) {
        String pageString = "";
        for(FragmentPagerAdapter.PageType page : pages) pageString += page.toString() + ",";
        setString(context, R.string.pref_viewer_pages, pageString);
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

    public static void setDefaultBrowserMode(Context context, FragmentPagerAdapter.PageType mode) {
        setString(context, R.string.pref_browser_default, mode.toString());
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

    public static void setShouldDisplayToolbarOnBottom(Context context, boolean bottom) {
        setBool(context, R.string.pref_theme_toolbar_bottom, bottom);
    }

    private static void setBool(Context context, @StringRes int keyId, boolean value) {
        if(Analytics.VERBOSE) Log.i(TAG, "setBool: " + context.getString(keyId) + " to " + value);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(context.getString(keyId), value)
                .apply();
    }

    private static void setString(Context context, @StringRes int keyId, String value) {
        if(Analytics.VERBOSE) Log.i(TAG, "setString: " + context.getString(keyId) + " to " + value);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(context.getString(keyId), value)
                .apply();
    }

    private static void setInt(Context context, @StringRes int keyId, int value) {
        if(Analytics.VERBOSE) Log.i(TAG, "setInt: " + context.getString(keyId) + " to " + value);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(context.getString(keyId), value)
                .apply();
    }

}
