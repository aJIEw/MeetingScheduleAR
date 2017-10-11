package com.perficient.meetingschedulear.ui.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.util.ActivityUtil;


public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUtil.setUpToolBar(this, R.string.title_setting_activity);

        //getFragmentManager().beginTransaction()
        //        .replace(android.R.id.content, new SettingsFragment())
        //        .commit();

        addPreferencesFromResource(R.xml.preferences);
    }
}
