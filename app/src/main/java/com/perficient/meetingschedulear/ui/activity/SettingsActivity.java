package com.perficient.meetingschedulear.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.util.ActivityUtil;


@SuppressWarnings("deprecation")
public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUtil.setUpToolBar(this, R.string.title_setting_activity);

        addPreferencesFromResource(R.xml.preferences);

        initView();
    }

    private void initView() {
        Preference recentScanned = findPreference(getString(R.string.key_recent_scanned));
        Intent intent = new Intent(this, RecentScannedActivity.class);
        recentScanned.setIntent(intent);
    }
}
