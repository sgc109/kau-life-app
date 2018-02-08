package com.lifekau.android.lifekau.model;

import java.util.Date;
import java.util.UUID;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class Alarm {
    public static final int TYPE_COMMENT = 0;
    public static final int TYPE_LIKE = 1;
    public static final int TYPE_NOTICE = 2;
    public static final int TYPE_LECTURE_MATERIAL = 3;
    public static final int TYPE_GRADE = 4;;
    public static final int TYPE_TEST_SCHEDULE = 5;

    private UUID mId;
    private String mContent;
    private int mType;
    private Date mDate;

    public Alarm(String content, int type){
        mId = UUID.randomUUID();
        mContent = content;
        mType = type;
        mDate = new Date();
    }

    public Alarm(UUID id){
        mId = id;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public UUID getId() {
        return mId;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        mContent = content;
    }

    public Date getDate() {
        return mDate;
    }
}
