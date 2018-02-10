package com.lifekau.android.lifekau.model;

import java.util.Date;

/**
 * Created by sgc109 on 2018-02-09.
 */

public class Post {
    public String author;
    public int likeCount;
    public String text;
    public long date;
    public Post(){}
    public Post(String author, String text){
        this.author = author;
        this.likeCount = 0;
        this.text = text;
        this.date = new Date().getTime();
    }
}
