package com.lifekau.android.lifekau;

import java.util.Date;
import java.util.UUID;

/**
 * Created by sgc109 on 2018-01-28.
 */

public class FoodReview extends Review{
    public FoodReview(){}
    public FoodReview(float rating, String comment){
        mComment = comment;
        mRating = rating;
        mRatingRev = -rating;
        mDate = new Date().getTime();
        mDateRev = -mDate;
    }
}
