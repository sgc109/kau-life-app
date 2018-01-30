package com.lifekau.android.lifekau;

import java.util.Date;
import java.util.UUID;

/**
 * Created by sgc109 on 2018-01-28.
 */

public class FoodReview {
    public int mCornerType;
    public String mComment;
    public float mRating;
    public float mRatingRev;
    public long mDate;
    public long mDateRev;
    public FoodReview(){
    }
    public FoodReview(float rating, int cornerType, String comment){
        mCornerType = cornerType;
        mComment = comment;
        mRating = rating;
        mRatingRev = -rating;
        mDate = new Date().getTime();
        mDateRev = -mDate;
    }
}
