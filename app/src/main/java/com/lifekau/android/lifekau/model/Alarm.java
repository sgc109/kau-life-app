package com.lifekau.android.lifekau.model;

import java.util.Date;
import java.util.UUID;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class Alarm {
    public static final int TYPE_COMMENT = 0;
    public static final int TYPE_NOTICE = 1;
    public static final int TYPE_LECTURE_MATERIAL = 2;
    public static final int TYPE_GRADE = 3;
    public static final int TYPE_TEST_SCHEDULE = 4;

    private UUID uid;
    private String text;
    private int type;
    private Long date;
    private String postKey;

    public Alarm(String text, int type){
        this.uid = UUID.randomUUID();
        this.text = text;
        this.type = type;
        this.date = new Date().getTime();
    }

    public Alarm(String text, int type, String postKey){
        this.uid = UUID.randomUUID();
        this.text = text;
        this.type = type;
        this.date = new Date().getTime();
        this.postKey = postKey;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public Alarm(UUID id){
        this.uid = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public UUID getUid() {
        return uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getDate() {
        return date;
    }
}
