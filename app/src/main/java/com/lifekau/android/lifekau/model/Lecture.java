package com.lifekau.android.lifekau.model;

import java.util.UUID;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class Lecture {
    private String mName;

    public Lecture(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

}
