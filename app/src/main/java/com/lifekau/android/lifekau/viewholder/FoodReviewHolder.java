package com.lifekau.android.lifekau.viewholder;

import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.lifekau.android.lifekau.model.FoodReview;
import com.lifekau.android.lifekau.R;

/**
 * Created by sgc109 on 2018-01-28.
 */

public class FoodReviewHolder extends ReviewHolder {
    private TextView mFoodCornerTypeTextView;
    public FoodReviewHolder(View itemView) {
        super(itemView);
        mRatingBar = (RatingBar) itemView.findViewById(R.id.list_item_food_review_rating_bar);
        mTimeTextView = (TextView) itemView.findViewById(R.id.list_item_food_time_text_view);
        mCommentTextView = (TextView) itemView.findViewById(R.id.list_item_food_comment_text_view);
        mContext = itemView.getContext();
    }

    public void bindReview(FoodReview review) {
        super.bindReview(review);
    }
}
