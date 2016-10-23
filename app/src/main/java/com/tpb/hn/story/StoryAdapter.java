package com.tpb.hn.story;

import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;

import com.tpb.hn.data.Item;

/**
 * Created by theo on 18/10/16.
 */

public class StoryAdapter extends FragmentPagerAdapter implements StoryLoader {
    private static final String TAG = StoryAdapter.class.getSimpleName();

    private PageType[] pages;
    private StoryLoader[] loaders;

    private Item queuedItem;

    private AppBarLayout mAppBar;

    public StoryAdapter(FragmentManager fragmentManager, PageType[] pages, AppBarLayout toolbar) {
        super(fragmentManager);
        this.pages = pages;
        loaders = new StoryLoader[pages.length + 1];
        mAppBar = toolbar;
    }

    @Override
    public void loadStory(Item item) {
        queuedItem = item;
        for(StoryLoader sl : loaders) {
            if(sl != null) {
                sl.loadStory(item);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        Fragment page = new Fragment();
        Log.i(TAG, "getItem: " + position);
        switch(pages[position]) {
            case COMMENTS:
                page = new Comments();
                loaders[position] = (StoryLoader) page;
                break;
            case BROWSER:
                page = new Browser();
                loaders[position] = (StoryLoader) page;
                break;
            case READABILITY:
                page = Readability.newInstance(mAppBar);
                loaders[position] = (StoryLoader) page;
                break;
            case SKIMMER:
                page = Skimmer.newInstance();
                loaders[position] = (StoryLoader) page;
                break;
        }
        if(loaders[position] != null && queuedItem != null) {
            loaders[position].loadStory(queuedItem);
        }
        return page;
    }

    @Override
    public int getCount() {
        return pages.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(pages[position]) {
            case COMMENTS:
                return "COMMENTS";
            case BROWSER:
                return "ARTICLE";
            case  READABILITY:
                return "READER";
            case SKIMMER:
                return "SKIMMER";
        }
        return "Error";
    }

    public static class HidingListener implements NestedScrollView.OnScrollChangeListener {

        private AppBarLayout mAppBar;
        private int y;
        private int height;
        private float initialY;

        public HidingListener(AppBarLayout appBarLayout) {
            mAppBar = appBarLayout;
            height = mAppBar.getMeasuredHeight();
            initialY = mAppBar.getY();
        }

        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            //Log.i(TAG, "onScrollChange: sY " + scrollY + ", oY " + oldScrollY);
            int delta = oldScrollY - scrollY;
            Log.i(TAG, "onScrollChange: Y " + y + ", delta " + delta);
            if((y > -mAppBar.getMeasuredHeightAndState() || delta > 0 ) && y <= 0) {
                y += delta;
                if(y > 0) y= 0;
            }

            //Log.i(TAG, "onScrollChange: " + mAppBar.getY() + ", from " + initialY);
            //Log.i(TAG, "onScrollChange: " + mAppBar.getMeasuredHeightAndState());
            mAppBar.setTranslationY(y);
        }
    }

    public interface FragmentCycle {

        void onPauseFragment();

        void onResumeFragment();

    }

    public enum PageType {
        COMMENTS, BROWSER, READABILITY, SKIMMER
    }

}
