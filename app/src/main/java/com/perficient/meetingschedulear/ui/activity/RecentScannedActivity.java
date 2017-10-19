package com.perficient.meetingschedulear.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.adapter.RecentScannedAdapter;
import com.perficient.meetingschedulear.model.MeetingRoomInfo;
import com.perficient.meetingschedulear.util.TimeUtil;

import java.util.*;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.perficient.meetingschedulear.common.Constants.PREF_MEETING_INFO;

public class RecentScannedActivity extends BaseActivity {

    private static final String TAG = RecentScannedActivity.class.getSimpleName();

    private static final String REGEX_TIME_SECOND = "\\d{4}-\\d{2}-\\d{2}\\s(\\d{2}:){2}\\d{2}";

    @BindView(R.id.activity_scanned_recyclerView)
    RecyclerView mRecyclerView;

    private RecentScannedAdapter mAdapter;

    private SharedPreferences mPreferences;

    private List<MeetingRoomInfo> mMeetingRoomInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_scanned);
        ButterKnife.bind(this);
        addToolBar(R.string.title_recent_scanned);

        mPreferences = getSharedPreferences(PREF_MEETING_INFO, Context.MODE_PRIVATE);

        initData();

        initView();
    }

    private void initData() {
        //noinspection unchecked
        Map<String, Set<String>> meetingData = (Map<String, Set<String>>) mPreferences.getAll();

        mMeetingRoomInfos = new ArrayList<>();

        for (String nameKey : meetingData.keySet()) {
            MeetingRoomInfo meetingRoomInfo = new MeetingRoomInfo();
            meetingRoomInfo.setRoomName(nameKey);

            Iterator<String> iterator = meetingData.get(nameKey).iterator();
            List<String> meetings = new ArrayList<>();
            while (iterator.hasNext()) {
                String val = iterator.next();
                if (val.matches(REGEX_TIME_SECOND)) {
                    meetingRoomInfo.setTime(val);
                    Log.d(TAG, "initData: time set " + val + " for " + meetingRoomInfo.getRoomName());
                } else {
                    meetings.add(val);
                    Log.d(TAG, "initData: added " + val + " for " + meetingRoomInfo.getRoomName());
                }
            }
            meetingRoomInfo.setMeetings(meetings);

            mMeetingRoomInfos.add(meetingRoomInfo);

            Log.d(TAG, "initData: added meeting info for " + meetingRoomInfo.getRoomName());
        }

        Collections.sort(mMeetingRoomInfos, new Comparator<MeetingRoomInfo>() {
            @Override
            public int compare(MeetingRoomInfo o1, MeetingRoomInfo o2) {
                Date date1 = TimeUtil.stringToDate(o1.getTime(), TimeUtil.FORMAT_DATE_TIME_SECOND);
                Date date2 = TimeUtil.stringToDate(o2.getTime(), TimeUtil.FORMAT_DATE_TIME_SECOND);

                // the nearest time is ahead of the further one
                if (date1 != null && date2 != null) {
                    if (date1.getTime() > date2.getTime()) {
                        return -1;
                    } else if (date1.getTime() < date2.getTime()) {
                        return 1;
                    } else {
                        return 0;
                    }
                } else {
                    new RuntimeException("Date is null");
                    return 0;
                }
            }
        });
    }

    private void initView() {
        mAdapter = new RecentScannedAdapter(R.layout.item_recent_scanned, mMeetingRoomInfos);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                //showMeetingDialog(position);

                String roomName = mMeetingRoomInfos.get(position).getRoomName();
                MeetingsActivity.actionStart(RecentScannedActivity.this, roomName);
            }
        });
        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final BaseQuickAdapter adapter, View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RecentScannedActivity.this);
                builder.setTitle(R.string.dialog_title_delete_this_item)
                        .setPositiveButton(getString(R.string.yes_capital), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String roomName = mMeetingRoomInfos.get(position).getRoomName();
                                deleteFromSp(roomName);
                                adapter.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel_capital), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false);

                builder.create().show();
                return true;
            }
        });

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void showMeetingDialog(int position) {
        MeetingRoomInfo info = mMeetingRoomInfos.get(position);
        String content = TextUtils.join("\n", info.getMeetings());
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(RecentScannedActivity.this)
                .setTitle(info.getRoomName())
                .setMessage(content)
                .setCancelable(true)
                .setPositiveButton(R.string.ok_capital, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.create().show();
    }

    public void deleteFromSp(String key) {
        mPreferences.edit()
                .remove(key)
                .apply();
    }
}
