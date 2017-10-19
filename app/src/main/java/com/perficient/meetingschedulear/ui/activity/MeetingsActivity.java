package com.perficient.meetingschedulear.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.adapter.MeetingsAdapter;
import com.perficient.meetingschedulear.model.MeetingInfo;
import com.perficient.meetingschedulear.util.ToastUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MeetingsActivity extends BaseActivity {

    private static final String TAG = MeetingsActivity.class.getSimpleName();

    private static final int FAKE_FETCH_DATA_TIME = 500;

    private static final String EXTRA_MEETING_NAMES = "";

    @BindView(R.id.activity_meetings_loading_progressBar)
    ContentLoadingProgressBar mLoadingBar;
    @BindView(R.id.activity_meetings_recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.activity_meetings_book_fab)
    FloatingActionButton mBookRoomFab;

    private MeetingsAdapter mAdapter;

    private List<MeetingInfo> mData;

    private String mTitle;

    private List<String> mMeetingNames;

    public static void actionStart(Context context, String title /*ArrayList<String> meetingNames*/) {
        Intent intent = new Intent(context, MeetingsActivity.class);
        //intent.putExtra(EXTRA_MEETING_NAMES, meetingNames);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);
        ButterKnife.bind(this);

        initData();

        initView();

        callDummyData();
    }

    private void initData() {
        Intent received = getIntent();
        if (received != null) {
            mTitle = received.getStringExtra(Intent.EXTRA_TITLE);
            mMeetingNames = received.getStringArrayListExtra(EXTRA_MEETING_NAMES);
        } else {
            mMeetingNames = new ArrayList<>();
        }
        mData = new ArrayList<>();

        mAdapter = new MeetingsAdapter(R.layout.item_meetings, mData);
    }

    private void initView() {
        addToolBar(mTitle);

        showLoading();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);

        mBookRoomFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.showToast("To book page");
            }
        });
    }

    private void showLoading() {
        mLoadingBar.setVisibility(View.VISIBLE);
        mBookRoomFab.setVisibility(View.INVISIBLE);
    }

    private void hideLoading() {
        mLoadingBar.setVisibility(View.GONE);
        mBookRoomFab.setVisibility(View.VISIBLE);
    }

    private void callDummyData() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MeetingInfo syncUp = new MeetingInfo();
                syncUp.setName("Meeting Schedule AR");
                syncUp.setTime("2017-10-19 10:00 - 11:00");
                syncUp.setAttenders(fetchAttenders());
                mData.add(syncUp);

                MeetingInfo ks = new MeetingInfo();
                ks.setName("Knowledge Sharing");
                ks.setTime("2017-10-19 13:00 - 14:00");
                ks.setAttenders(fetchAttenders());
                mData.add(ks);

                MeetingInfo bt = new MeetingInfo();
                bt.setName("Break Tea");
                bt.setTime("2017-10-19 15:00 - 15:30");
                bt.setAttenders(fetchAttenders());
                mData.add(bt);

                mAdapter.setNewData(mData);

                hideLoading();
            }
        }, FAKE_FETCH_DATA_TIME);
    }

    private List<String> fetchAttenders() {
        List<String> attenders = new ArrayList<>();
        attenders.add("Aaron Chen");
        attenders.add("Bruk Cai");
        attenders.add("Frankie Chen");
        attenders.add("Waters Chen");

        Collections.shuffle(attenders);

        return attenders;
    }

}
