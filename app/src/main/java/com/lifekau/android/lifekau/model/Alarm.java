package com.lifekau.android.lifekau.model;

import java.util.Date;
import java.util.UUID;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class Alarm {
    private UUID mId;
    private String mContent;
    private Date mDate;

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

    public Alarm(String content){
        mId = UUID.randomUUID();
        mContent = content;
        mDate = new Date();
    }

    public Alarm(UUID id){
        mId = id;
    }
}
