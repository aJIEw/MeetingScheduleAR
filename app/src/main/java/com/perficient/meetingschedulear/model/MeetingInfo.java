package com.perficient.meetingschedulear.model;


import java.util.List;

public class MeetingInfo {

    private String name;

    private String time;

    private List<String> attenders;

    public MeetingInfo() {
    }

    public MeetingInfo(String name, String time, List<String> attenders) {
        this.name = name;
        this.time = time;
        this.attenders = attenders;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<String> getAttenders() {
        return attenders;
    }

    public void setAttenders(List<String> attenders) {
        this.attenders = attenders;
    }
}
