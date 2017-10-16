package com.perficient.meetingschedulear.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.adapter.RecentScannedAdapter;
import com.perficient.meetingschedulear.model.MeetingInfo;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecentScannedActivity extends BaseActivity {

    @BindView(R.id.activity_scanned_recyclerView)
    RecyclerView mRecyclerView;

    private RecentScannedAdapter mAdapter;

    private List<MeetingInfo> mMeetingInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_scanned);
        ButterKnife.bind(this);

        addToolBar(R.string.title_recent_scanned);

        initView();
    }

    private void initView() {
        mAdapter = new RecentScannedAdapter();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }
}
