package com.lifekau.android.lifekau.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sgc109 on 2018-02-09.
 */

public class Comment {
    public String author;
    public int likeCount;
    public String text;
    public long date;
    public Map<String, Boolean> likes = new HashMap<>();

    public Comment() {
    }

    public Comment(String author, String text){
        this.author = author;
        this.likeCount = 0;
        this.text = text;
        this.date = new Date().getTime();
    }
}
