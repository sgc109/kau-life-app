package com.lifekau.android.lifekau;

import java.util.Date;

/**
 * Created by sgc109 on 2018-02-01.
 */

public class LectureReview extends Review{
    LectureReview(){}
    public LectureReview(float rating, String comment){
        mComment = comment;
        mRating = rating;
        mRatingRev = -rating;
        mDate = new Date().getTime();
        mDateRev = -mDate;
    }
}
