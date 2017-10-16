package com.perficient.meetingschedulear.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.util.ActivityUtil;


public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityUtil.setUpToolBar(this, R.string.title_setting_activity);

        addPreferencesFromResource(R.xml.preferences);

        final SwitchPreference lightSwitch = (SwitchPreference) findPreference(getString(R.string.key_flash_light_on));
        lightSwitch.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ActivityUtil.toggleFlashLight(lightSwitch.isChecked());
                return true;
            }
        });

        Preference recentScanned = findPreference(getString(R.string.key_recent_scanned));
        Intent intent = new Intent(this, RecentScannedActivity.class);
        recentScanned.setIntent(intent);
    }
}
