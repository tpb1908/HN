package com.tpb.hn.content;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.androidnetworking.AndroidNetworking;
import com.tpb.hn.R;
import com.tpb.hn.data.Item;
import com.tpb.hn.data.User;
import com.tpb.hn.item.ItemAdapter;
import com.tpb.hn.item.ItemViewer;
import com.tpb.hn.network.AdBlocker;
import com.tpb.hn.network.HNLoader;
import com.tpb.hn.network.HNUserLoader;
import com.tpb.hn.network.Login;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by theo on 17/10/16.
 */

public class Content extends AppCompatActivity implements HNLoader.HNItemLoadDone, ContentAdapter.ContentOpener, Login.LoginListener, HNUserLoader.HNUserLoadDone {
    private static final String TAG = Content.class.getSimpleName();

    @BindView(R.id.content_toolbar)
    Toolbar mContentToolbar;

    @BindView(R.id.nav_spinner)
    Spinner mNavSpinner;

    @BindView(R.id.content_recycler)
    RecyclerView mRecycler;

    private ContentAdapter mAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final SharedPrefsController prefs = SharedPrefsController.getInstance(this);
        prefs.setUseDarkTheme(false);
        prefs.setUseCards(true);
        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        }
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);
        AdBlocker.init(this);
        AndroidNetworking.initialize(getApplicationContext());

        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(android.R.drawable.divider_horizontal_dim_dark)));

        mAdapter = new ContentAdapter(this, mRecycler, (LinearLayoutManager) mRecycler.getLayoutManager());
        mRecycler.setAdapter(mAdapter);
        mNavSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.nav_spinner_items)
        ));
        mNavSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mAdapter.loadItems(mNavSpinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mAdapter.loadItems(SharedPrefsController.getInstance(this).getDefaultPage());

        new HNUserLoader(this).loadUser("tpb1908");
    }

    @Override
    public void userLoaded(User user) {

    }

    @Override
    public void response(boolean success) {
        Log.i(TAG, "response: Was login successful ? " + success);
    }

    @Override
    public void itemLoaded(Item item, boolean success) {

    }

    @Override
    public void itemsLoaded(ArrayList<Item> items, boolean success) {
    }

    @Override
    public void openItem(Item item) {
        final Intent i = new Intent(Content.this, ItemViewer.class);
        i.putExtra("item", item);
        startActivity(i);
    }

    @Override
    public void openUser(Item item) {

    }

    @Override
    public void openPage(Item item, ItemAdapter.PageType type) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        //TODO- Allow the fragments to override back press
        super.onBackPressed();
    }

}
