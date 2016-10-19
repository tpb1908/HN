package com.tpb.hn.content;

import android.view.View;
import android.widget.FrameLayout;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;

import butterknife.ButterKnife;

/**
 * Created by theo on 19/10/16.
 */

public class PanelController {
    private SlidingUpPanelLayout panelLayout;
    private FrameLayout slidingPanel;
    private float lastPanelOffset = 0.0f;


    public PanelController(SlidingUpPanelLayout panelLayout, final View collapsedView, final View expandedView) {
        this.panelLayout = panelLayout;
        this.slidingPanel = ButterKnife.findById(panelLayout, R.id.story_panel);
        panelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(slideOffset > lastPanelOffset) {
                    collapsedView.setAlpha(slideOffset);
                    expandedView.setAlpha(1 - slideOffset);
                } else {
                    collapsedView.setAlpha(slideOffset);
                    expandedView.setAlpha(1 - slideOffset);

                }
                lastPanelOffset = slideOffset;
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });
    }

    public void setPanelVisible() {
        slidingPanel.setVisibility(View.VISIBLE);
    }

    public void setPanelInvisible() {
        slidingPanel.setVisibility(View.INVISIBLE);
    }

    public boolean isPanelVisible() {
        return slidingPanel.getVisibility() == View.VISIBLE;
    }

    public void expand() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void collapse() {
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
    
    
    public boolean isCollapsed() {
        return false;
    }
    
    public boolean isExpanded() {
        return false;
    }

}
