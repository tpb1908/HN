package com.tpb.hn.item;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.tpb.hn.data.Item;
import com.tpb.hn.item.fragments.Browser;
import com.tpb.hn.item.fragments.Comments;
import com.tpb.hn.item.fragments.Content;
import com.tpb.hn.item.fragments.Readability;
import com.tpb.hn.item.fragments.Skimmer;

import java.util.ArrayList;

/**
 * Created by theo on 18/10/16.
 */

public class ItemAdapter extends FragmentPagerAdapter {
    private static final String TAG = ItemAdapter.class.getSimpleName();

    private ArrayList<PageType> pages = new ArrayList<>();
    private Fragment[] fragments;

    private Fullscreen fullscreen;
    private Item item;

    public ItemAdapter(final Fullscreen fullscreen, FragmentManager fragmentManager, ViewPager pager, PageType[] possiblePages, Item item) {
        super(fragmentManager);
        this.item = item;
        this.fullscreen = fullscreen;
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
                case SKIMMER:
                    if(item.getUrl() != null || item.getText() != null) {
                        pages.add(PageType.SKIMMER);
                    }
                    break;
            }
        }
        pages.add(PageType.AMP_READER);
        fragments = new Fragment[pages.size()];
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int oldPos = 0;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(pages.get(position) == PageType.TEXT_READER) {
                    fullscreen.openFullScreen();
                } else if(pages.get(oldPos) == PageType.TEXT_READER){
                    fullscreen.closeFullScreen();
                }
                ((FragmentCycle) fragments[oldPos]).onPauseFragment();
                ((FragmentCycle) fragments[position]).onResumeFragment();
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
                page = Comments.newInstance(fullscreen);
                fragments[position] = page;
                break;
            case BROWSER:
                page = Browser.newInstance(fullscreen);
                fragments[position] = page;
                break;
            case TEXT_READER:
                page = Readability.newInstance();
                fragments[position] =  page;
                break;
            case SKIMMER:
                page = Skimmer.newInstance(fullscreen);
                fragments[position] = page;
                break;
            case AMP_READER:
                page = Content.newInstance(PageType.BROWSER);
                fragments[position] = page;
        }
        if(fragments[position] != null && item != null) {
            ((ItemLoader) fragments[position]).loadItem(item);
        }
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
            case SKIMMER:
                return "SKIM";
            case AMP_READER:
                return "READ";
        }
        return "Error";
    }


    public interface FragmentCycle {

        void onPauseFragment();

        void onResumeFragment();

    }

    public interface Fullscreen {

        void openFullScreen();

        void closeFullScreen();

    }

    public enum PageType {
        COMMENTS, BROWSER, TEXT_READER, AMP_READER, SKIMMER
    }

}
