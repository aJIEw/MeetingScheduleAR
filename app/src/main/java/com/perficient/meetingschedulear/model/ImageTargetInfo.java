package com.perficient.meetingschedulear.model;


public class ImageTargetInfo {

    private String url;

    private String imageName;

    private String targetName;

    private float[] size;

    private String uid;

    /**
     * url and file name is required
     * */
    public ImageTargetInfo(String url, String imageName) {
        this.url = url;
        this.imageName = imageName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public float[] getSize() {
        return size;
    }

    public void setSize(float[] size) {
        this.size = size;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
