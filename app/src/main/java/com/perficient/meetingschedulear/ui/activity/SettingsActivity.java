package com.perficient.meetingschedulear.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.model.MeetingInfo;
import com.perficient.meetingschedulear.util.ActivityUtil;

import java.util.ArrayList;
import java.util.List;


public class SettingsActivity extends PreferenceActivity {

    private static final String EXTRA_KEY_DATA = "extra_data";

    private Preference mPreference;

    private List<MeetingInfo> mData;

    public static void actionStart(Context context, ArrayList<MeetingInfo> data) {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(EXTRA_KEY_DATA, data);
        intent.putExtra(EXTRA_KEY_DATA, bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUtil.setUpToolBar(this, R.string.title_setting_activity);

        addPreferencesFromResource(R.xml.preferences);

        mPreference = findPreference(getString(R.string.pref_title_recent_scanned));
        Intent intent = new Intent(this, RecentScannedActivity.class);
        mPreference.setIntent(intent);
    }
}
