package com.tpb.hn.item;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.tpb.hn.data.Item;
import com.tpb.hn.item.fragments.Comments;
import com.tpb.hn.item.fragments.Content;
import com.tpb.hn.item.fragments.Skimmer;

import java.util.ArrayList;

/**
 * Created by theo on 18/10/16.
 */

public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private static final String TAG = FragmentPagerAdapter.class.getSimpleName();

    private ArrayList<PageType> pages = new ArrayList<>();
    private Fragment[] fragments;

    private Item item;

    public FragmentPagerAdapter(FragmentManager fragmentManager, ViewPager pager, PageType[] possiblePages, Item item) {
        super(fragmentManager);
        this.item = item;
        possiblePages = new PageType[] {PageType.BROWSER, PageType.TEXT_READER, PageType.AMP_READER,  PageType.SKIMMER};
        for(PageType pt : possiblePages) {
            switch(pt) {
                case COMMENTS:
                    pages.add(PageType.COMMENTS);
                    break;
                case BROWSER:
                    if(item.getUrl() != null) {
                        pages.add(PageType.BROWSER);
                    }
                    break;
                case TEXT_READER:
                    if(item.getUrl() != null || item.getText() != null) {
                        pages.add(PageType.TEXT_READER);
                    }
                    break;
                case AMP_READER:
                    if(item.getUrl() != null) {
                        pages.add(PageType.AMP_READER);
                    }
                    break;
                case SKIMMER:
                    if(item.getUrl() != null || item.getText() != null) {
                        pages.add(PageType.SKIMMER);
                    }
                    break;
            }
        }
       // pages.add(PageType.AMP_READER);
        fragments = new Fragment[pages.size()];
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int oldPos = 0;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i(TAG, "onPageSelected: From " + oldPos + " to " + position);
                ((FragmentCycleListener) fragments[oldPos]).onPauseFragment();
                ((FragmentCycleListener) fragments[position]).onResumeFragment();
                oldPos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public Fragment getItem(int position) {
        Fragment page = new Fragment();
        switch(pages.get(position)) {
            case COMMENTS:
                page = Comments.newInstance();
                fragments[position] = page;
                break;
            case BROWSER:
                page = Content.newInstance(PageType.BROWSER);
                fragments[position] = page;
                break;
            case TEXT_READER:
                page = Content.newInstance(PageType.TEXT_READER);
                fragments[position] =  page;
                break;
            case AMP_READER:
                page = Content.newInstance(PageType.AMP_READER);
                fragments[position] = page;
                break;
            case SKIMMER:
                page = Skimmer.newInstance();
                fragments[position] = page;
                break;
        }
        if(fragments[position] != null && item != null) {
            ((ItemLoader) fragments[position]).loadItem(item);
        }
        Log.i(TAG, "getItem: Getting item " + position);
        return page;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(pages.get(position)) {
            case COMMENTS:
                return "COMMENT";
            case BROWSER:
                return "ARTICLE";
            case TEXT_READER:
                return "READ";
            case AMP_READER:
                return "AMP";
            case SKIMMER:
                return "SKIM";
        }
        return "Error";
    }

    public interface FragmentCycleListener {

        void onPauseFragment();

        void onResumeFragment();

    }

    public interface Fullscreen {

        void openFullScreen();

        void closeFullScreen();

    }

    public enum PageType {
        COMMENTS, //The comment adapter fragment
        BROWSER, //Loads the URL TODO- Merge this with the Content fragment
        TEXT_READER, //Content fragment
        AMP_READER, //Content fragment
        SKIMMER //Skimmer fragment
    }

}
