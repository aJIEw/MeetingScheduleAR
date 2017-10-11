package com.perficient.meetingschedulear.ui.activity;

import android.os.Bundle;

import com.perficient.meetingschedulear.R;

public class ScannedActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanned);

        addToolBar(R.string.title_recent_scanned);
    }
}
