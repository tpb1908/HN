package com.tpb.hn.story;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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

    public StoryAdapter(FragmentManager fragmentManager, PageType[] pages) {
        super(fragmentManager);
        this.pages = pages;
        loaders = new StoryLoader[pages.length + 1];
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
                page = new Readability();
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

    public interface FragmentCycle {

        void onPauseFragment();

        void onResumeFragment();

    }

    public enum PageType {
        COMMENTS, BROWSER, READABILITY, SKIMMER
    }

}
