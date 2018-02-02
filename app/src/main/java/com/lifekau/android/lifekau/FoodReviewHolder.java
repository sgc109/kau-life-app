package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by sgc109 on 2018-01-28.
 */

public class FoodReviewHolder extends ReviewHolder {
    private TextView mFoodCornerTypeTextView;
    public FoodReviewHolder(View itemView) {
        super(itemView);
        mFoodCornerTypeTextView = (TextView) itemView.findViewById(R.id.list_item_food_corner_type_text_view);
        mRatingBar = (RatingBar) itemView.findViewById(R.id.list_item_food_review_rating_bar);
        mTimeTextView = (TextView) itemView.findViewById(R.id.list_item_food_time_text_view);
        mCommentTextView = (TextView) itemView.findViewById(R.id.list_item_food_comment_text_view);
        mContext = itemView.getContext();
    }

    public void bindReview(FoodReview review) {
        super.bindReview(review);
        Resources res = mContext.getResources();
        String[] cornerTypeStrings = res.getStringArray(R.array.food_corner_list);
        mFoodCornerTypeTextView.setText(cornerTypeStrings[review.mCornerType]);
    }
}
