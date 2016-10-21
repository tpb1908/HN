package com.tpb.hn.content;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 19/10/16.
 */

public class PanelController {
    private static final String TAG = PanelController.class.getSimpleName();

    private SlidingUpPanelLayout panelLayout;
    private RelativeLayout slidingPanel;
    private float lastPanelOffset = 0.0f;

    @BindView(R.id.item_title)
    TextView mTitle;

    @BindView(R.id.item_large_title)
    View expandedView;

    @BindView(R.id.item_detail_layout)
    View collapsedView;


    public PanelController(SlidingUpPanelLayout panelLayout) {
        this.panelLayout = panelLayout;
        this.slidingPanel = ButterKnife.findById(panelLayout, R.id.story_panel);
        ButterKnife.bind(this, panelLayout);

        panelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(slideOffset > lastPanelOffset) {
                    expandedView.setAlpha(Math.min(slideOffset * 1.5f, 1.0f));
                    collapsedView.setAlpha(1 - slideOffset);
                } else {
                    expandedView.setAlpha(slideOffset);
                    collapsedView.setAlpha(1 - Math.min(slideOffset * 1.5f, 1.0f));

                }
                lastPanelOffset = slideOffset;
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });
    }

    public void onResume() {
        Log.i(TAG, "onResume: ");
        if(isExpanded()) {
            Log.i(TAG, "onResume: Panel visible");
            expandedView.setAlpha(1);
            collapsedView.setAlpha(0);
        }
        Log.i(TAG, "onResume: expanded " + expandedView.getAlpha() + " collapsed " + collapsedView.getAlpha());
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
        return panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED;
    }
    
    public boolean isExpanded() {
        return panelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
    }

}
