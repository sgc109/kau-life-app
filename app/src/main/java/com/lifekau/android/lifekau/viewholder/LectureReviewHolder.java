package com.lifekau.android.lifekau.viewholder;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.lifekau.android.lifekau.R;

/**
 * Created by sgc109 on 2018-02-01.
 */

public class LectureReviewHolder extends ReviewHolder {
    public LectureReviewHolder(View itemView) {
        super(itemView);
        mRatingBar = (RatingBar)itemView.findViewById(R.id.list_item_lecture_review_rating_bar);
        mCommentTextView = (TextView) itemView.findViewById(R.id.list_item_lecture_comment_text_view);
        mTimeTextView = (TextView)itemView.findViewById(R.id.list_item_lecture_time_text_view);
    }
}
