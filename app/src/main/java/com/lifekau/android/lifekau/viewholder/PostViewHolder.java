package com.lifekau.android.lifekau.viewholder;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.model.Post;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import com.lifekau.android.lifekau.R;

/**
 * Created by sgc109 on 2018-02-11.
 */

public class PostViewHolder extends RecyclerView.ViewHolder {
    public CardView mCardView;
    public TextView mTextView;
    public ImageButton mLikeButton;
    public ImageView mCommentButton;
    public TextView mCommentCountTextView;
    public TextView mLikeCountTextView;
    public ImageView mCircleHeartImageView;
    public TextView mDateTextView;
    public Context mContext;

    public PostViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
        mCardView = itemView.findViewById(R.id.list_item_post_card_view);
        mTextView = itemView.findViewById(R.id.list_item_post_content_text_view);
        mLikeButton = itemView.findViewById(R.id.list_item_post_like_image_button);
        mCommentButton = itemView.findViewById(R.id.list_item_post_comment_image_button);
        mCommentCountTextView = itemView.findViewById(R.id.list_item_post_comment_count_text_view);
        mLikeCountTextView = itemView.findViewById(R.id.list_item_post_like_count);
        mCircleHeartImageView = itemView.findViewById(R.id.list_item_post_circle_heart_image_view);
        mDateTextView = itemView.findViewById(R.id.post_list_date_text_view);
    }

    public void bind(Post post) {
        mTextView.setText(post.text);
        mCommentCountTextView.setText(String.format(
                mContext.getString(R.string.post_comment_count),
                NumberFormat.getNumberInstance(Locale.US).format(post.commentCount)));
        mLikeCountTextView.setText(NumberFormat.getNumberInstance(Locale.US).format(post.likeCount));
        mDateTextView.setText(dateToString(new Date(post.date)));
        if (post.likeCount == 0) {
            mLikeCountTextView.setVisibility(View.GONE);
            mCircleHeartImageView.setVisibility(View.GONE);
        }
        if (post.commentCount == 0) {
            mCommentCountTextView.setVisibility(View.GONE);
        }
    }

    private String dateToString(Date past) {
        Date now = new Date();
        SimpleDateFormat toYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat toMonth = new SimpleDateFormat("M");
        SimpleDateFormat toDay = new SimpleDateFormat("d");

        int nowYear = Integer.parseInt(toYear.format(now));
        int pastYear = Integer.parseInt(toYear.format(past));
        int nowMonth = Integer.parseInt(toMonth.format(now));
        int pastMonth = Integer.parseInt(toMonth.format(past));
        int nowDay = Integer.parseInt(toDay.format(now));
        int pastDay = Integer.parseInt(toDay.format(past));

        if (nowYear != pastYear) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy년 M월 d일 a h:mm");
            return formatter.format(past);
        } else if (nowMonth != pastMonth
                || nowDay >= pastDay + 2) {
            SimpleDateFormat formatter = new SimpleDateFormat("M월 d일 a h:mm");
            return formatter.format(past);
        } else if (nowDay >= pastDay + 1) {
            SimpleDateFormat formatter = new SimpleDateFormat("어제 a h:mm");
            return formatter.format(past);
        }
        long diff = now.getTime() - past.getTime();
        diff /= 1000;
        long hour = diff / 3600;
        long minute = diff / 60;

        if (minute == 0) {
            return "방금 전";
        } else if (hour == 0) {
            return "" + minute + "분 전";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("a h:mm");
            return formatter.format(past);
        }
    }
}