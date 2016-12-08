package com.tpb.hn.feed;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.tpb.hn.Analytics;
import com.tpb.hn.R;
import com.tpb.hn.helpers.Util;
import com.tpb.hn.helpers.DividerDecoration;
import com.tpb.hn.helpers.Formatter;
import com.tpb.hn.data.Item;
import com.tpb.hn.viewer.FragmentPagerAdapter;
import com.tpb.hn.viewer.views.ViewHolderSwipeCallback;
import com.tpb.hn.network.Loader;
import com.tpb.hn.settings.SharedPrefsController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.tpb.hn.feed.FeedAdapter.AdapterState.*;

/**
 * Created by theo on 18/10/16.
 */

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        Loader.ItemLoader,
        Loader.idLoader,
        FastScrollRecyclerView.SectionedAdapter {

    private static final String TAG = FeedAdapter.class.getSimpleName();
    //Views
    private final Context mContext;
    private final Loader mLoader;
    private final ContentManager mManager;
    private final RecyclerView mRecycler;
    private final SwipeRefreshLayout mSwiper;
    //Resources
    @BindColor(R.color.colorPrimaryText) int lightText;
    @BindColor(R.color.colorPrimaryTextInverse) int darkText;
    private FeedActivity.Section mCurrentPage;
    //Data and scrolling information
    private int[] mIds = new int[0];
    private int[] mOldIds = new int[0];
    private Item[] mData = new Item[0];
    private long mLastUpdateTime;
    private int mLastPosition;
    private int mCountGuess;
    private LinearLayoutManager mLayoutManager;
    //Settings flags
    private AdapterState mState;
    private boolean mIsDarkTheme;
    private boolean mShouldScrollOnChange;
    private boolean mLoadInBackground;
    private boolean mIsOffline;
    private boolean mIsUsingCards = false;
    private boolean mShouldMarkRead = false;

    public FeedAdapter(Context context,
                       ContentManager manager,
                       RecyclerView recycler,
                       final LinearLayoutManager layoutManager,
                       final SwipeRefreshLayout swiper) {
        ButterKnife.bind(this, recycler);
        mState = manager instanceof FeedActivity ? Items : User;
        mContext = context;
        mManager = manager;
        mSwiper = swiper;
        mLayoutManager = layoutManager;
        mRecycler = recycler;

        mLoader = Loader.getInstance(context);
        getPrefs(context);
        mLastUpdateTime = new Date().getTime() / 1000;

        mLoader.addNetworkListener(netAvailable -> {
            mIsOffline = !netAvailable;
            notifyDataSetChanged();
        });

        swiper.setOnRefreshListener(() -> {
            if(mState == Items) {
                loadItems(mCurrentPage);
            } else {
                mIds = new int[0];
                notifyDataSetChanged();
                mManager.openUser(null); //Reload the user
            }
            if(mShouldScrollOnChange) mRecycler.scrollToPosition(0);
        });

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if(newState == RecyclerView.SCROLL_STATE_SETTLING || newState == RecyclerView.SCROLL_STATE_IDLE) {
                    loadItemsOnScroll(false);
                }
            }
        });

        if(mRecycler instanceof FastScrollRecyclerView) {
            ((FastScrollRecyclerView) mRecycler).setStateChangeListener(new OnFastScrollStateChangeListener() {
                @Override
                public void onFastScrollStart() {

                }

                @Override
                public void onFastScrollStop() {
                    loadItemsOnScroll(true);
                }
            });
        }

        final ViewHolderSwipeCallback callback = new ViewHolderSwipeCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, "Left", "Right") {
            @Override
            public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final int pos = viewHolder.getAdapterPosition();
                //Check whether we should allow swiping or not
                return (pos >= 0 && pos < mData.length && mData[pos] != null &&
                        !mData[pos].isComment() && !mData[pos].isDeleted()) ?
                        super.getSwipeDirs(recyclerView, viewHolder) : 0;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                notifyItemChanged(viewHolder.getAdapterPosition());
                final int pos = viewHolder.getAdapterPosition();
                if(pos < mData.length && mData[pos] != null) {
                    if(mData[pos].isSaved()) {
                        mLoader.unsaveItem(mData[pos]);
                    } else {
                        mLoader.saveItem(mData[pos], mContext, new Loader.ItemSaveListener() {
                            @Override
                            public void itemSaved(Item item) {
                                item.setSaved(true);
                                Toast.makeText(mContext, "Item saved", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void saveError(Item item, int code) {
                                Toast.makeText(mContext, "Item not saved", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public String getSwipeText(boolean right, int adapterPosition) {
                if(adapterPosition >= mData.length || adapterPosition < 0 || mData[adapterPosition] == null) {
                    return "";
                }
                return mData[adapterPosition].isSaved() ? "Unsave" : "Save";
            }
        };
        callback.setColor(darkText);
        new ItemTouchHelper(callback).attachToRecyclerView(recycler);
    }

    void getPrefs(Context context) {
        final SharedPrefsController prefs = SharedPrefsController.getInstance(context);
        mIsOffline = !Util.isNetworkAvailable(context);
        mShouldMarkRead = prefs.getMarkReadWhenPassed();
        mIsDarkTheme = prefs.getUseDarkTheme();
        mShouldScrollOnChange = prefs.getShouldScrollToTop();
        mLoadInBackground = prefs.getLoadInBackground();
        if(mRecycler instanceof FastScrollRecyclerView) {
            ((FastScrollRecyclerView) mRecycler).setScrollBarEnabled(prefs.getShowScrollbar());
            ((FastScrollRecyclerView) mRecycler).setFastScrollEnabled(prefs.getFastScroll());
        }
        final boolean oldCardStyle = mIsUsingCards;
        mIsUsingCards = prefs.getUseCards();
        /*We don't want to do this when the adapter is created
        * so we check that the data has been updated at least once
        * We then save the current position and reset the adapter on
        * the recycler, forcing it to redraw
        * notifyDataSetChanged won't show the new style until the
        * user scrolls, because only then are the ViewHolders recycled
         */
        if(mIsUsingCards != oldCardStyle && mLastUpdateTime != 0) {
            final int pos = mLayoutManager.findFirstVisibleItemPosition();
            mRecycler.setAdapter(this);
            mRecycler.scrollToPosition(pos);
            mRecycler.invalidateItemDecorations();
            if(!mIsUsingCards) {
                mRecycler.addItemDecoration(new DividerDecoration(mContext.getDrawable(android.R.drawable.divider_horizontal_dim_dark)));
            }
        }
    }

    private void loadItemsOnScroll(boolean fastScroll) {
        int pos = Math.max(mLayoutManager.findFirstVisibleItemPosition(), 0);
        //Marking items as read
        if(pos > mLastPosition && pos < mData.length && mShouldMarkRead) {
            for(int i = mLastPosition; i < pos; i++) {
                if(mData[i] != null) mData[i].setViewed(true);
            }
        }
        if(mIds != null) {
            int pos2;
            /*
            If we are scrolling down the list, we load the items below
            the first visible.
            Also if we have just fastscrolled to a position, as there
            is no inertia and we have stopped at this point
             */
            if(pos > mLastPosition || fastScroll) {
                pos2 = Math.min(pos + 25, mIds.length);
            } else {
                pos2 = Math.max(pos - 25, 0);
            }
            if(pos2 < pos) {
                final int t = pos;
                pos = pos2;
                pos2 = t;
            }
            final ArrayList<Integer> toLoad = new ArrayList<>();
            for(int i = pos; i < pos2 && pos2 < mData.length; i++) {
                if(mData[i] == null) toLoad.add(mIds[i]);
            }
            if(toLoad.size() > 0) mLoader.loadItems(Util.convertIntegers(toLoad), false, this);
        }
        mLastPosition = pos;
    }

    public void scrollDown() {
        final int currentPos = mLayoutManager.findLastVisibleItemPosition();
        int height = currentPos - mLayoutManager.findFirstVisibleItemPosition();
        mRecycler.smoothScrollToPosition(Math.min(currentPos + height, mData.length));
    }

    public void scrollUp() {
        final int currentPos = mLayoutManager.findFirstVisibleItemPosition();
        int height = mLayoutManager.findLastVisibleItemPosition() - currentPos;
        mRecycler.smoothScrollToPosition(Math.max(currentPos - height, 0));
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return Integer.toString(position + 1);
    }

    void loadItems(FeedActivity.Section page) {
        this.mData = new Item[0];
        if(mCurrentPage != null && mCurrentPage.equals(page)) {
            this.mOldIds = mIds.clone();
            Arrays.sort(mOldIds);
        } else {
            mOldIds = new int[0];
        }
        mCurrentPage = page;
        AndroidNetworking.forceCancelAll();
        notifyDataSetChanged();
        mLoader.getIds(page, this);
        mCountGuess = Util.getApproximateNumberOfItems(mCurrentPage);
        if(Analytics.VERBOSE) Log.i(TAG, "loadItems: page selected");
        if(mIds.length == 0) mIds = new int[mCountGuess];
        if(mShouldScrollOnChange) mRecycler.scrollToPosition(0);
        mLastUpdateTime = new Date().getTime() / 1000;
        mManager.displayLastUpdate(mLastUpdateTime);
    }

    @Override
    public void itemLoaded(Item item) {
        for(int i = 0; i < mIds.length; i++) {
            if(mIds[i] == item.getId()) {
                mData[i] = item;
                if(mOldIds.length > 0) {
                    final int possiblePos = Arrays.binarySearch(mOldIds, item.getId());
                    item.setNew(possiblePos < 0 || possiblePos > mOldIds.length);
                }
                notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void itemError(int id, int code) {
        if(Analytics.VERBOSE) Log.i(TAG, "itemError: " + id + " | " + code);
    }

    @Override
    public void idsLoaded(int[] ids) {
        if(Analytics.VERBOSE) Log.i(TAG, "IdLoadDone: " + ids.length);
        this.mIds = ids;
        final int currentPos = Math.max(mLayoutManager.findFirstVisibleItemPosition(), 0);
        if(currentPos > ids.length) {
            mLayoutManager.scrollToPosition(ids.length);
        }
        mData = new Item[ids.length + 1];
        AndroidNetworking.forceCancel("BG");
        mLoader.loadItems(Arrays.copyOfRange(ids, currentPos, Math.min(currentPos + 10, ids.length)), false, this);
        notifyDataSetChanged();
        mSwiper.setRefreshing(false);
        beginBackgroundLoading();
        //Id loading will only happen once each time the mData is to be set
    }

    @Override
    public void idError(int code) {
        Toast.makeText(mContext, R.string.error_id_loading, Toast.LENGTH_SHORT).show();
        loadItems(mCurrentPage);
    }

    void beginBackgroundLoading() {
        if(mLoadInBackground) {
            AsyncTask.execute(() -> {
                final int[] notLoaded = new int[mIds.length];
                int count = 0;
                for(int i = 0; i < mIds.length; i++) {
                    if(mData[i] == null) notLoaded[count++] = mIds[i];
                }
                mLoader.loadItems(Arrays.copyOfRange(notLoaded, 0, count), true, FeedAdapter.this);
            });
        }
    }

    private void attemptLoadAgain(int pos) {
        if(pos < mIds.length) {
            mLoader.loadItem(mIds[pos], this);
            Toast.makeText(mContext, R.string.text_attempting_item_load, Toast.LENGTH_SHORT).show();
        }
    }

    void getLastUpdate() {
        mManager.displayLastUpdate(mLastUpdateTime);
    }

    private void openItem(int pos, FragmentPagerAdapter.PageType type) {
        if(mData != null && pos < mData.length && mData[pos] != null) {
            mData[pos].setViewed(true);
            notifyItemChanged(pos);
            mManager.openItem(mData[pos], type);
        } else {
            attemptLoadAgain(pos);
        }
    }

    private void openUser(int pos) {
        if(mData != null && pos < mData.length && mData[pos] != null) {
            mManager.openUser(mData[pos]);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == 0) {
            return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_item, parent, false));
        } else if(viewType == 1) {
            return new CommentHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_user_comment, parent, false));
        } else {
            return new EmptyHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_empty, parent, false));
        }

    }

    //<editor-fold-desc="Binding" >

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final int pos = viewHolder.getAdapterPosition();
        if(viewHolder instanceof ItemHolder) {
            final ItemHolder holder = (ItemHolder) viewHolder;
            if(mData.length > pos && mData[pos] != null) {
                final Item item = mData[pos];
                holder.mInfo.setText(item.getInfo());
                if(item.isDeleted()) {
                    //This only happens in the UserActivity
                    holder.mTitle.setText(R.string.text_item_deleted);
                    holder.mNumber.setVisibility(View.GONE);
                    holder.mAuthor.setVisibility(View.GONE);
                    holder.mURL.setVisibility(View.GONE);
                } else {
                    holder.mTitle.setText(item.getTitle());
                    if(mState == Items) {
                        holder.mAuthor.setVisibility(View.VISIBLE);
                        holder.mAuthor.setText(item.getFormattedBy());
                        holder.mNumber.setText(String.format(Locale.getDefault(), "%d", pos + 1));
                        if(item.isSaved() && mIsOffline) {
                            holder.mNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, R.drawable.ic_offline);
                        } else {
                            holder.mNumber.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                        }
                    } else {
                        //In the UserActivity we don't want to show author or number
                        holder.mAuthor.setVisibility(View.GONE);
                        holder.mNumber.setVisibility(View.GONE);
                    }

                    if(item.getUrl() == null) {
                        holder.mURL.setVisibility(View.GONE);
                    } else {
                        holder.mURL.setVisibility(View.VISIBLE);
                        holder.mURL.setText(item.getFormattedURL());
                    }
                    if(item.isViewed()) {
                        holder.mTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Material_Medium_Inverse);
                    }
                    if(item.isNew()) {
                        holder.mNumber.setTextAppearance(mContext, android.R.style.TextAppearance_Material_Large);
                    } else {
                        holder.mNumber.setTextAppearance(mContext, android.R.style.TextAppearance_Material_Medium);
                        holder.mNumber.setTextColor(mIsDarkTheme ? darkText : lightText);
                    }

                }
            }
            if(mIsUsingCards) {
                setCardParams(holder.mCard);
            }
        } else if(viewHolder instanceof CommentHolder) {
            final CommentHolder commentHolder = (CommentHolder) viewHolder;
            if(pos < mData.length && mData[pos] != null) {
                commentHolder.mTime.setText(Formatter.appendAgo(Formatter.timeAgo(mData[pos].getTime())));
                if(mData[pos].isDeleted()) {
                    commentHolder.mBody.setText(R.string.text_deleted_comment);
                } else if(mData[pos].getText() != null) {
                    commentHolder.mBody.setText(Util.parseHTMLText(mData[pos].getText()));
                }
            }
            if(mIsUsingCards) {
                setCardParams(commentHolder.mCard);
            }
        } else if(mIsUsingCards) {
            setCardParams(((EmptyHolder) viewHolder).mCard);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position < mData.length && mData[position] != null) {
            return mData[position].isComment() ? mState == Items ? 0 : 1 : 0;
        }
        return mState == Items ? 0 : -1;
    }

    @Override
    public int getItemCount() {
        return mData.length > 0 ? mData.length - 1 : mCountGuess;
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);
        if(viewHolder instanceof ItemHolder) {
            final ItemHolder holder = (ItemHolder) viewHolder;
            holder.mTitle.setTextAppearance(mContext, android.R.style.TextAppearance_Material_Title);
            holder.mTitle.setTextColor(mIsDarkTheme ? darkText : lightText);
            //Reset the rest of the view
            holder.mInfo.setText(R.string.text_info_empty);
            holder.mAuthor.setText("");
            holder.mURL.setText("");
            holder.mNumber.setText("");
            //Reset the view height if necessary
            if(holder.mTitle.getLineCount() > 1) {
                holder.mTitle.setText("");
                holder.itemView.requestLayout();
            }
            holder.mTitle.setText(R.string.text_title_empty);
        } else if(viewHolder instanceof CommentHolder) {
            //Reset the height of the comment holder
            ((CommentHolder) viewHolder).mBody.requestLayout();
        }

    }

    private void setCardParams(CardView card) {
        card.setUseCompatPadding(true);
        card.setCardElevation(Util.pxFromDp(4));
        card.setRadius(Util.pxFromDp(3));
        card.setPadding(0, Util.pxFromDp(8), 0, Util.pxFromDp(8));
    }

    enum AdapterState {
        Items, User, Search
    }

    //</editor-fold>

    //<editor-fold-desc="Viewholder classes">

    public interface ContentManager {

        void openItem(Item item);

        void openItem(Item item, FragmentPagerAdapter.PageType type);

        void openUser(Item item);

        void displayLastUpdate(long lastUpdate);

    }

    class ItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_card) CardView mCard;
        @BindView(R.id.item_title) TextView mTitle;
        @BindView(R.id.item_stats) TextView mInfo;
        @BindView(R.id.item_author) TextView mAuthor;
        @BindView(R.id.item_url) TextView mURL;
        @BindView(R.id.item_number) TextView mNumber;

        ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FeedAdapter.this.openItem(getAdapterPosition(), null);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    FeedAdapter.this.attemptLoadAgain(getAdapterPosition());
                    return true;
                }
            });
        }

        @OnClick(R.id.item_url)
        void urlClick() {
            FeedAdapter.this.openItem(getAdapterPosition(), FragmentPagerAdapter.PageType.BROWSER);
        }

        @OnClick(R.id.item_author)
        void authorClick() {
            FeedAdapter.this.openUser(getAdapterPosition());
        }

    }

    class CommentHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.comment_card) CardView mCard;
        @BindView(R.id.comment_time) TextView mTime;
        @BindView(R.id.comment_body) TextView mBody;

        CommentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mBody.setOnClickListener(view -> FeedAdapter.this.openItem(getAdapterPosition(), null));
        }

    }

    //</editor-fold>

    class EmptyHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.empty_card) CardView mCard;

        EmptyHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
