package com.perficient.meetingschedulear.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.perficient.meetingschedulear.R;
import com.perficient.meetingschedulear.model.MeetingInfo;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class RecentScannedAdapter extends RecyclerView.Adapter<RecentScannedAdapter.RecentScannedViewHolder> {

    private List<MeetingInfo> mData;

    public RecentScannedAdapter() {
        mData = new ArrayList<>();
    }

    public RecentScannedAdapter(List<MeetingInfo> data) {
        mData = data;
    }

    @Override
    public RecentScannedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_scanned, parent, false);
        return new RecentScannedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecentScannedViewHolder holder, int position) {
        holder.mTitleTv.setText(mData.get(position).getRoomName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(List<MeetingInfo> data) {
        mData = data;
    }

    class RecentScannedViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.item_recent_scanned_title_textView)
        TextView mTitleTv;

        public RecentScannedViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
