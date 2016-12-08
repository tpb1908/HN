package com.tpb.hn.item;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.tpb.hn.Analytics;
import com.tpb.hn.data.Item;
import com.tpb.hn.item.fragments.Comments;
import com.tpb.hn.item.fragments.Content;
import com.tpb.hn.item.fragments.Skimmer;
import com.tpb.hn.network.loaders.Loader;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by theo on 18/10/16.
 */

public class FragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter {
    private static final String TAG = FragmentPagerAdapter.class.getSimpleName();

    private final ArrayList<PageType> pages = new ArrayList<>();
    private final Fragment[] fragments;

    private final Item mItem;
    private final int mCommentId;

    public FragmentPagerAdapter(FragmentManager fragmentManager, ViewPager pager, PageType[] possiblePages, Item item, int commentId) {
        super(fragmentManager);
        mItem = item;
        mCommentId = commentId;
        if(Analytics.VERBOSE) Log.i(TAG, "FragmentPagerAdapter: " + item);
        possiblePages = new PageType[] {PageType.COMMENTS, PageType.BROWSER, PageType.TEXT_READER, PageType.AMP_READER, PageType.SKIMMER};
        final boolean pdf = item.getTitle().toLowerCase().contains("[pdf]") ||
                (item.getUrl() != null && item.getUrl().endsWith(".pdf"));

        boolean containsBrowser = Arrays.asList(possiblePages).contains(PageType.BROWSER);

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
                        if(pdf) {
                            if(!containsBrowser) {
                                pages.add(PageType.TEXT_READER);
                                containsBrowser = true;
                            }
                        } else {
                            pages.add(PageType.TEXT_READER);
                        }
                    }
                    break;
                case AMP_READER:
                    if(item.getUrl() != null) {
                        if(pdf) {
                            if(!containsBrowser) {
                                pages.add(PageType.BROWSER);
                                containsBrowser = true;
                            }
                        } else {
                            pages.add(PageType.AMP_READER);
                        }
                    }
                    break;
                case SKIMMER:
                    if((item.getUrl() != null && !pdf) || item.getText() != null) {
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
                if(Analytics.VERBOSE)
                    Log.i(TAG, "onPageSelected: From " + oldPos + " to " + position);
                if(fragments[position] != null) {
                    ((FragmentCycleListener) fragments[oldPos]).onPauseFragment();
                    ((FragmentCycleListener) fragments[position]).onResumeFragment();
                }
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
                page = Comments.getInstance(mCommentId);
                fragments[position] = page;
                break;
            case BROWSER:
                page = Content.newInstance(PageType.BROWSER);
                fragments[position] = page;
                break;
            case TEXT_READER:
                page = Content.newInstance(PageType.TEXT_READER);
                fragments[position] = page;
                break;
            case AMP_READER:
                page = Content.newInstance(PageType.AMP_READER);
                fragments[position] = page;
                break;
            case SKIMMER:
                page = new Skimmer();
                fragments[position] = page;
                break;
        }
        if(fragments[position] != null && mItem != null) {
            ((Loader.ItemLoader) fragments[position]).itemLoaded(mItem);
        }
        if(Analytics.VERBOSE) Log.i(TAG, "getItem: Getting mItem " + position);
        return page;
    }

    int indexOf(PageType type) {
        if(type == PageType.COMMENTS) {
            return pages.indexOf(PageType.COMMENTS);
        } else {
            final int index = pages.indexOf(type);
            if(index == -1) {
                for(int i = 0; i < pages.size(); i++) {
                    if(pages.get(i) == PageType.AMP_READER ||
                            pages.get(i) == PageType.TEXT_READER)
                        return i;
                }
            }
            return index;
        }
    }

    boolean onBackPressed() {
        for(Fragment f : fragments) {
            if(f != null && !((FragmentCycleListener) f).onBackPressed()) return false;
        }
        return true;

    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return PageType.toReadableString(pages.get(position));
    }

    public enum PageType {
        COMMENTS, //The comment adapter fragment
        BROWSER, //Loads the URL
        TEXT_READER, //Content fragment
        AMP_READER, //Content fragment
        SKIMMER; //Skimmer fragment

        public static String toReadableString(PageType type) {
            switch(type) {
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
            return "ERROR";
        }

    }

    public interface FragmentCycleListener {

        void onPauseFragment();

        void onResumeFragment();

        boolean onBackPressed();

    }

    interface Fullscreen {

        void openFullScreen();

        void closeFullScreen();

    }

}
