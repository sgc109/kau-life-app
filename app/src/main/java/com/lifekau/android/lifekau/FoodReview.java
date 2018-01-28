package com.lifekau.android.lifekau;

import java.util.Date;
import java.util.UUID;

/**
 * Created by sgc109 on 2018-01-28.
 */

public class FoodReview {
    public UUID mId;
    public float mRating;
    public int mCorner;
    public String mComment;
    public Date mTime;
    public FoodReview(){}
    public FoodReview(float rating, int corner, String comment){
        mId = UUID.randomUUID();
        mRating = rating;
        mCorner = corner;
        mComment = comment;
        mTime = new Date();
    }
}
