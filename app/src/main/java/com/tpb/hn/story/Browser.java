package com.tpb.hn.story;

import android.support.v4.app.Fragment;

import com.tpb.hn.data.Item;

/**
 * Created by theo on 18/10/16.
 * Loads the full page of an article
 * If Chrome custom tabs are supported, they will be used
 * Otherwise it will load a webview
 */

public class Browser extends Fragment implements StoryLoader {

    @Override
    public void loadStory(Item item) {

    }
}
