package com.tpb.hn.story;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tpb.hn.data.Item;

/**
 * Created by theo on 18/10/16.
 */

public class StoryAdapter extends FragmentPagerAdapter implements StoryLoader{
    private PageType[] pages;
    private StoryLoader[] loaders;

    public StoryAdapter(FragmentManager fragmentManager, PageType[] pages) {
        super(fragmentManager);
        this.pages = pages;
        loaders = new StoryLoader[pages.length + 1];
    }

    @Override
    public void loadStory(Item item) {
        for(StoryLoader sl : loaders) {
            sl.loadStory(item);
        }
    }

    @Override
    public Fragment getItem(int position) {
        switch(pages[position]) {
            case COMMENTS:
                final Comments comments = new Comments();
                loaders[position] = comments;
                return comments;
            case BROWSER:
                final Browser browser = new Browser();
                loaders[position] = browser;
                return browser;
            case READABILITY:
                final Readability readability = new Readability();
                loaders[position] = readability;
                return readability;
            case SKIMMER:
                final Skimmer skimmer = new Skimmer();
                loaders[position] = skimmer;
                return skimmer;
        }
        return new Fragment();
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



    public enum PageType {
        COMMENTS, BROWSER, READABILITY, SKIMMER
    }

}
