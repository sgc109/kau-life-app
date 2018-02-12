package com.lifekau.android.lifekau.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sgc109 on 2018-02-09.
 */

public class Post {
    public String author;
    public int commentCount;
    public int likeCount;
    public String text;
    public long date;
    public Map<String, Boolean> likes = new HashMap<>();
    public Post(){}
    public Post(String author, String text){
        this.author = author;
        this.commentCount = 0; // TODO 0으로 바꿔라
        this.likeCount = 0; // TODO 0으로 바꿔라
        this.text = text;
        this.date = new Date().getTime();
    }
}
