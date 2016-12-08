package com.tpb.hn.settings;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tpb.hn.R;
import com.woxthebox.draglistview.DragItemAdapter;
import com.woxthebox.draglistview.DragListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 02/12/16.
 */

public class DraggableListDialog extends DialogFragment {
    private static final String TAG = DraggableListDialog.class.getSimpleName();

    @BindView(R.id.listview_draggable) DragListView mListView;
    private boolean mUsingCheckboxes = false;
    private String[] mValues;


    public static DraggableListDialog newInstance(String[] values) {
        final DraggableListDialog dialog = new DraggableListDialog();
        dialog.mValues = values;

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_draggable, container, false);
        ButterKnife.bind(this, view);
        init(mValues);
        return view;
    }


    private void init(String[] values) {
        mListView.getRecyclerView().setVerticalScrollBarEnabled(true);
        mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final ArrayList<Pair<Long, String>> items = new ArrayList<>();
        long index = 0;
        for(String s : values) {
            items.add(new Pair<>(index++, s));
        }
        mListView.setCanDragHorizontally(false);
        mListView.setAdapter(new Adapter(items), true);
        mListView.setCustomDragItem(new DragItem(getActivity(), R.layout.listitem_draggable));
    }

    private static class DragItem extends com.woxthebox.draglistview.DragItem {

        DragItem(Context context, int layoutId) {
            super(context, layoutId);
        }

        @Override
        public void onBindDragView(View clickedView, View dragView) {
            dragView.setBackgroundColor(Color.RED);
            super.onBindDragView(clickedView, dragView);
        }
    }

    public class Adapter extends DragItemAdapter<Pair<Long, String>, Adapter.ViewHolder> {
        private final String TAG = Adapter.class.getSimpleName();
        private ArrayList<Pair<Long, String>> mItems;

        public Adapter(ArrayList<Pair<Long, String>> items) {
            setHasStableIds(true);

            setItemList(items);
            mItems = items;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.mTextView.setText(mItems.get(holder.getAdapterPosition()).second);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_draggable, parent, false), R.id.text_draggable, true);
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).first;
        }

        public class ViewHolder extends DragItemAdapter.ViewHolder {

            @BindView(R.id.text_draggable) TextView mTextView;
            //@BindView(R.id.image_draggable) ImageView mHandle;

            public ViewHolder(@NonNull View itemView, int handleRes, boolean dragOnLongPress) {
                super(itemView, handleRes, dragOnLongPress);
                ButterKnife.bind(this, itemView);
            }

            @Override
            public void onItemClicked(View view) {
                super.onItemClicked(view);
            }

            @Override
            public boolean onItemLongClicked(View view) {
                return super.onItemLongClicked(view);
            }
        }
    }

}
