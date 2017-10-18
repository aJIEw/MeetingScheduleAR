package com.perficient.meetingschedulear.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class MeetingInfo implements Parcelable {

    private String time;

    private String roomName;

    private List<String> meetings;

    public MeetingInfo() {
    }

    public MeetingInfo(String roomName, List<String> meetings) {
        this.roomName = roomName;
        this.meetings = meetings;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public List<String> getMeetings() {
        return meetings;
    }

    public void setMeetings(List<String> meetings) {
        this.meetings = meetings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.time);
        dest.writeString(this.roomName);
        dest.writeStringList(this.meetings);
    }

    protected MeetingInfo(Parcel in) {
        this.time = in.readString();
        this.roomName = in.readString();
        this.meetings = in.createStringArrayList();
    }

    public static final Creator<MeetingInfo> CREATOR = new Creator<MeetingInfo>() {
        @Override
        public MeetingInfo createFromParcel(Parcel source) {
            return new MeetingInfo(source);
        }

        @Override
        public MeetingInfo[] newArray(int size) {
            return new MeetingInfo[size];
        }
    };
}
