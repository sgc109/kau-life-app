package com.lifekau.android.lifekau;

import java.util.Date;
import java.util.UUID;

/**
 * Created by sgc109 on 2018-01-28.
 */

public class FoodReview {
    public static final int CORNER_TYPE_A = 0;
    public static final int CORNER_TYPE_B = 1;
    public static final int CORNER_TYPE_C = 2;
    public static final int CORNER_TYPE_D = 3;

    public UUID mId;
    public int mScore;
    public int mCorner;
    public String mComment;
    public Date mTime;
    public FoodReview(){}
    public FoodReview(int score, int corner, String comment){
        mId = UUID.randomUUID();
        mScore = score;
        mCorner = corner;
        mComment = comment;
        mTime = new Date();
    }
}
