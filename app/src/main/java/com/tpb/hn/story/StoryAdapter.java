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

    public StoryAdapter(FragmentManager fragmentManager, PageType[] pages) {
        super(fragmentManager);
        this.pages = pages;
    }

    @Override
    public void loadStory(Item item) {

    }

    @Override
    public Fragment getItem(int position) {
        return null;
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
                return "PAGE";
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
