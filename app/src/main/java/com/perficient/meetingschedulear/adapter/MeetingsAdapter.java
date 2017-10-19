package com.perficient.meetingschedulear.adapter;


import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.model.MeetingInfo;

import java.util.List;

public class MeetingsAdapter extends BaseQuickAdapter<MeetingInfo, BaseViewHolder>{

    public MeetingsAdapter(@LayoutRes int layoutResId, @Nullable List<MeetingInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MeetingInfo item) {
        String attenders = TextUtils.join(", ", item.getAttenders());
        helper.setText(R.id.item_meetings_title_textView, item.getName())
                .setText(R.id.item_meetings_time_textView, item.getTime())
                .setText(R.id.item_meetings_attender_textView, attenders);
    }
}
