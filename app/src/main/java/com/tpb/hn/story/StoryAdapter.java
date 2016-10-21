package com.tpb.hn.story;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tpb.hn.data.Item;

import java.util.Arrays;

/**
 * Created by theo on 18/10/16.
 */

public class StoryAdapter extends FragmentPagerAdapter implements StoryLoader{
    private PageType[] pages;
    private StoryLoader[] loaders;
    private boolean[] hasLoaded;

    private Item queuedItem;

    public StoryAdapter(FragmentManager fragmentManager, PageType[] pages) {
        super(fragmentManager);
        this.pages = pages;
        loaders = new StoryLoader[pages.length + 1];
        hasLoaded = new boolean[pages.length + 1];
    }

    @Override
    public void loadStory(Item item) {
        queuedItem = item;
        Arrays.fill(hasLoaded, false);
        for(StoryLoader sl : loaders) {
            if(sl != null) {
                sl.loadStory(item);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        Fragment page = new Fragment();
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
                page = new Skimmer();
                loaders[position] = (StoryLoader) page;
                break;
        }
        if(loaders[position] != null && !hasLoaded[position]) {
            loaders[position].loadStory(queuedItem);
            hasLoaded[position] = true;
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
