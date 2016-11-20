package com.tpb.hn;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by theo on 28/10/16.
 */

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @BindViews({R.id.title_settings_theme, R.id.title_settings_content, R.id.title_settings_comments, R.id.title_settings_browser, R.id.title_settings_data, R.id.title_settings_info}) List<TextView> mSettingsTitles;
    @BindViews({R.id.settings_theme, R.id.settings_content, R.id.settings_comments, R.id.settings_browser, R.id.settings_data, R.id.settings_info }) List<ExpandableRelativeLayout> mSettings;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @OnClick(R.id.settings_back_button)
    void onClick() {
        onBackPressed();
    }

    SharedPrefsController prefs;

    private boolean restartRequired = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = SharedPrefsController.getInstance(getApplicationContext());
        setViews();

    }

    private void initViewValues() {
        ((Switch)ButterKnife.findById(this, R.id.switch_dark_theme)).setChecked(prefs.getUseDarkTheme());
        ((Switch)ButterKnife.findById(this, R.id.switch_content_cards)).setChecked(prefs.getUseCards());
        ((Switch)ButterKnife.findById(this, R.id.switch_comment_cards)).setChecked(prefs.getUseCardsComments());
        ((Switch)ButterKnife.findById(this, R.id.switch_comment_expansion)).setChecked(prefs.getExpandComments());
        ((Switch)ButterKnife.findById(this, R.id.switch_comment_animation)).setChecked(prefs.getAnimateComments());
        ((Switch)ButterKnife.findById(this, R.id.switch_mark_read)).setChecked(prefs.getMarkReadWhenPassed());
        ((Switch)ButterKnife.findById(this, R.id.switch_browser_ads)).setChecked(prefs.getBlockAds());
        ((Switch)ButterKnife.findById(this, R.id.switch_scroll_to_top)).setChecked(prefs.getShouldScrollToTop());
        ((Switch)ButterKnife.findById(this, R.id.switch_browser_scrolling)).setChecked(prefs.getDisableHorizontalScrolling());
        ((Switch)ButterKnife.findById(this, R.id.switch_browser_lazy_load)).setChecked(prefs.getLazyLoad());
    }

    private void setViews() {
        if(prefs.getUseDarkTheme()) {
            setTheme(R.style.AppTheme_Dark);
        } else {
            setTheme(R.style.AppTheme);
        }
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        ButterKnife.apply(mSettingsTitles, TOGGLE);
        mToolbar.setTitle(""); //For some reason Android isn't respecting app:title or android:title
        setSupportActionBar(mToolbar);
        if(prefs.getUseDarkTheme()) {
            ButterKnife.findById(this, R.id.settings_root).setBackgroundColor(getResources().getColor(R.color.md_grey_bg));
        } else {
            ButterKnife.findById(this, R.id.settings_root).setBackgroundColor(getResources().getColor(R.color.md_grey_50));
        }
        initViewValues();
    }

    private final ButterKnife.Action<View> TOGGLE = new ButterKnife.Action<View>() {
        @Override
        public void apply(@NonNull View view, final int index) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSettings.get(index).toggle();
                }
            });

        }

    };

    public void onSwitchClick(@NonNull View view) {
        final Switch sView = (Switch) view;
        switch(view.getId()) {
            case R.id.switch_dark_theme:
                prefs.setUseDarkTheme(sView.isChecked());
                restartRequired = true;
                setViews();
                break;
            case R.id.switch_content_cards:
                prefs.setUseCards(sView.isChecked());
                restartRequired = true;
                break;
            case R.id.switch_comment_cards:
                prefs.setUseCardsComments(sView.isChecked());
                break;
            case R.id.switch_comment_expansion:
                prefs.setExpandComments(sView.isChecked());
                break;
            case R.id.switch_comment_animation:
                prefs.setAnimateComments(sView.isChecked());
                break;
            case R.id.switch_mark_read:
                prefs.setMarkReadWhenPassed(sView.isChecked());
                break;
            case R.id.switch_browser_ads:
                prefs.setBlockAds(sView.isChecked());
                break;
            case R.id.switch_scroll_to_top:
                prefs.setShouldScrollToTop(sView.isChecked());
                break;
            case R.id.switch_browser_scrolling:
                prefs.setDisableHorizontalScrolling(sView.isChecked());
                break;
            case R.id.switch_browser_lazy_load:
                prefs.setLazyLoad(sView.isChecked());
                break;
        }
    }

    @Override
    public void onBackPressed() {
        final Intent i = new Intent();
        i.putExtra("restart", restartRequired);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }
}
