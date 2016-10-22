package com.tpb.hn.content;

import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;

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

    @BindView(R.id.item_large_title_wrapper)
    View expandedView;

    @BindView(R.id.item_detail_layout)
    View collapsedView;


    public PanelController(SlidingUpPanelLayout panelLayout) {
        this.panelLayout = panelLayout;
        this.slidingPanel = ButterKnife.findById(panelLayout, R.id.story_panel);
        ButterKnife.bind(this, panelLayout);
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
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

    public void setTitle(Item item) {
        ((TextView) expandedView.findViewById(R.id.item_large_title)).setText(item.getTitle());
        ((TextView) collapsedView.findViewById(R.id.item_title)).setText(item.getTitle());
        ((TextView) collapsedView.findViewById(R.id.item_url)).setText(item.getFormattedURL());
        ((TextView) collapsedView.findViewById(R.id.item_stats)).setText(item.getFormattedInfo());
        ((TextView) collapsedView.findViewById(R.id.item_author)).setText(
                String.format(collapsedView.getContext().getString(R.string.text_item_by), item.getBy()));
        panelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void onResume() {
        Log.i(TAG, "onResume: ");
        if(isExpanded()) {
            expandedView.setAlpha(1);
            collapsedView.setAlpha(0);
        }
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
