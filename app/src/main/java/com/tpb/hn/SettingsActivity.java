package com.tpb.hn;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.tpb.hn.data.Formatter;
import com.tpb.hn.storage.SharedPrefsController;

import java.util.Calendar;
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
    @BindViews({R.id.settings_theme, R.id.settings_content, R.id.settings_comments, R.id.settings_browser, R.id.settings_data, R.id.settings_info}) List<ExpandableRelativeLayout> mSettings;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.settings_text_auto_start) TextView mThemeStart;
    @BindView(R.id.settings_text_auto_end) TextView mThemeEnd;

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
        ((Switch) ButterKnife.findById(this, R.id.switch_dark_theme)).setChecked(prefs.getUseDarkTheme());
        ((Switch) ButterKnife.findById(this, R.id.switch_content_cards)).setChecked(prefs.getUseCards());
        ((Switch) ButterKnife.findById(this, R.id.switch_comment_cards)).setChecked(prefs.getUseCardsComments());
        ((Switch) ButterKnife.findById(this, R.id.switch_comment_expansion)).setChecked(prefs.getExpandComments());
        ((Switch) ButterKnife.findById(this, R.id.switch_comment_animation)).setChecked(prefs.getAnimateComments());
        ((Switch) ButterKnife.findById(this, R.id.switch_mark_read)).setChecked(prefs.getMarkReadWhenPassed());
        ((Switch) ButterKnife.findById(this, R.id.switch_browser_ads)).setChecked(prefs.getBlockAds());
        ((Switch) ButterKnife.findById(this, R.id.switch_scroll_to_top)).setChecked(prefs.getShouldScrollToTop());
        ((Switch) ButterKnife.findById(this, R.id.switch_browser_scrolling)).setChecked(prefs.getDisableHorizontalScrolling());
        ((Switch) ButterKnife.findById(this, R.id.switch_browser_lazy_load)).setChecked(prefs.getLazyLoad());
        ((Switch) ButterKnife.findById(this, R.id.switch_auto_dark_theme)).setChecked(prefs.getAutoDark());
        ((Switch) ButterKnife.findById(this, R.id.switch_volume_navigation)).setChecked(prefs.getVolumeNavigation());
        ((Switch) ButterKnife.findById(this, R.id.switch_skimmer_show_seek_hint)).setChecked(prefs.showSeekBarHint());
        final Pair<Integer, Integer> timeRange = prefs.getDarkTimeRange();
        if(timeRange.first != -1) {
            final Pair<Integer, Integer> start = Formatter.intTohm(timeRange.first);
            mThemeStart.setText(Formatter.hmToString(start.first, start.second, ":"));
        }
        if(timeRange.second != -1) {
            final Pair<Integer, Integer> end = Formatter.intTohm(timeRange.second);
            mThemeEnd.setText(Formatter.hmToString(end.first, end.second, ":"));
        }
        mThemeStart.setEnabled(prefs.getAutoDark());
        mThemeEnd.setEnabled(prefs.getAutoDark());
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
            case R.id.switch_volume_navigation:
                prefs.setVolumeNavigation(sView.isChecked());
                break;
            case R.id.switch_auto_dark_theme:
                prefs.setAutoDark(sView.isChecked());
                mThemeStart.setEnabled(sView.isChecked());
                mThemeEnd.setEnabled(sView.isChecked());
                break;
            case R.id.switch_skimmer_show_seek_hint:
                prefs.setShowSeekBarHint(sView.isChecked());
                break;
        }
    }

    public void onTimeClick(View view) {
        final boolean start = view.getId() == R.id.settings_text_auto_start;
        final int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        final TimePickerDialog picker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int h, int m) {
                if(start) {
                    mThemeStart.setText(Formatter.hmToString(h, m, ":"));
                    prefs.setDarkTimes(Formatter.hmToInt(h, m), prefs.getDarkTimeRange().second);
                } else {
                    mThemeEnd.setText(Formatter.hmToString(h, m, ":"));
                    prefs.setDarkTimes(prefs.getDarkTimeRange().first, Formatter.hmToInt(h, m));
                }
            }
        }, hour, 0, true);

        picker.setTitle(start ? "Start time" : "End time");
        picker.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        prefs.reset();
        restartRequired = true;
        setViews();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        final Intent i = new Intent();
        i.putExtra("restart", restartRequired);
        setResult(RESULT_OK, i);
        super.onBackPressed();
    }
}
