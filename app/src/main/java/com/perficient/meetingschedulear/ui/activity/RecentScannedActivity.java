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
import com.perficient.meetingschedulear.model.MeetingInfo;
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

    private List<MeetingInfo> mMeetingInfos;

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

        mMeetingInfos = new ArrayList<>();

        for (String nameKey : meetingData.keySet()) {
            MeetingInfo meetingInfo = new MeetingInfo();
            meetingInfo.setRoomName(nameKey);

            Iterator<String> iterator = meetingData.get(nameKey).iterator();
            List<String> meetings = new ArrayList<>();
            while (iterator.hasNext()) {
                String val = iterator.next();
                if (val.matches(REGEX_TIME_SECOND)) {
                    meetingInfo.setTime(val);
                    Log.d(TAG, "initData: time set " + val + " for " + meetingInfo.getRoomName());
                } else {
                    meetings.add(val);
                    Log.d(TAG, "initData: added " + val + " for " + meetingInfo.getRoomName());
                }
            }
            meetingInfo.setMeetings(meetings);

            mMeetingInfos.add(meetingInfo);

            Log.d(TAG, "initData: added meeting info for " + meetingInfo.getRoomName());
        }

        Collections.sort(mMeetingInfos, new Comparator<MeetingInfo>() {
            @Override
            public int compare(MeetingInfo o1, MeetingInfo o2) {
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
        mAdapter = new RecentScannedAdapter(R.layout.item_recent_scanned, mMeetingInfos);
        mAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                MeetingInfo info = mMeetingInfos.get(position);
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
        });
        mAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final BaseQuickAdapter adapter, View view, final int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RecentScannedActivity.this);
                builder.setTitle(R.string.dialog_title_delete_this_item)
                        .setPositiveButton(getString(R.string.yes_capital), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String roomName = mMeetingInfos.get(position).getRoomName();
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

    public void deleteFromSp(String key) {
        mPreferences.edit()
                .remove(key)
                .apply();
    }
}
