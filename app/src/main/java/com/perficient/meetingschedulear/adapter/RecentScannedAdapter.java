package com.perficient.meetingschedulear.adapter;

import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.model.MeetingRoomInfo;

import java.util.List;


public class RecentScannedAdapter extends BaseQuickAdapter<MeetingRoomInfo, BaseViewHolder> {

    public RecentScannedAdapter(@LayoutRes int layoutResId, @Nullable List<MeetingRoomInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MeetingRoomInfo item) {
        helper.setText(R.id.item_recent_scanned_title_textView, item.getRoomName())
                .setText(R.id.item_recent_scanned_time_textView, item.getTime());
    }
}
