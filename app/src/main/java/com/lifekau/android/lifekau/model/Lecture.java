package com.lifekau.android.lifekau.model;

import java.util.UUID;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class Lecture {
    public UUID getId() {
        return mId;
    }

    public void setId(UUID id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    private UUID mId;
    private String mName;

    public Lecture(UUID id) {
        mId = id;
    }
}
