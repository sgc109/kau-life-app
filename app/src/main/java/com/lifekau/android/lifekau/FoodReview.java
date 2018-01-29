package com.lifekau.android.lifekau;

import java.util.Date;
import java.util.UUID;

/**
 * Created by sgc109 on 2018-01-28.
 */

public class FoodReview {
    public float mRating;
    public float mRatingRev;
    public int mCornerType;
    public String mComment;
    public Date mDate;
    public long mDateRev;
    public FoodReview(){
    }
    public FoodReview(float rating, int cornerType, String comment){
        mRating = rating;
        mRatingRev = -rating;
        mCornerType = cornerType;
        mComment = comment;
        mDate = new Date();
        mDateRev = -mDate.getTime();
    }
}
