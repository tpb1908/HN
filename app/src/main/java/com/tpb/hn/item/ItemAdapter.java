package com.tpb.hn.item;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.tpb.hn.data.Item;
import com.tpb.hn.item.fragments.Browser;
import com.tpb.hn.item.fragments.Comments;
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

    private Item item;

    public ItemAdapter(FragmentManager fragmentManager, ViewPager pager,  PageType[] possiblePages, Item item) {
        super(fragmentManager);
        this.item = item;
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
                case READABILITY:
                    if(item.getUrl() != null || item.getText() != null) {
                        pages.add(PageType.READABILITY);
                    }
                    break;
                case SKIMMER:
                    if(item.getUrl() != null || item.getText() != null) {
                        pages.add(PageType.SKIMMER);
                    }
                    break;
            }
        }
        fragments = new Fragment[pages.size()];
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            int oldPos = 0;
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
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
                page = new Comments();
                fragments[position] = page;
                break;
            case BROWSER:
                page = new Browser();
                fragments[position] = page;
                break;
            case READABILITY:
                page = Readability.newInstance();
                fragments[position] =  page;
                break;
            case SKIMMER:
                page = Skimmer.newInstance();
                fragments[position] = page;
                break;
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
            case  READABILITY:
                return "READ";
            case SKIMMER:
                return "SKIM";
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
