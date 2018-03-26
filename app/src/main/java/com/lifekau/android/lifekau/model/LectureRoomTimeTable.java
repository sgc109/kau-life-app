package com.lifekau.android.lifekau.model;

import java.io.Serializable;
import java.util.ArrayList;

public class LectureRoomTimeTable implements Serializable{

    private static final long serialVersionUID = 8282;

    public LectureRoomTimeTable(){
        startTimes = new ArrayList<>();
        endTimes = new ArrayList<>();
    }

    public String lectureRoomName;
    public int index;
    public ArrayList<Integer> startTimes;
    public ArrayList<Integer> endTimes;
}
