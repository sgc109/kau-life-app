package com.lifekau.android.lifekau.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.PostDetailActivity;
import com.lifekau.android.lifekau.manager.LoginManager;
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
    public TextView mTextView;
    public ImageView mLikeButtonImageView;
    public TextView mLikeButtonTextView;
    public ImageView mCommentButtonImageView;
    public TextView mCommentButtonTextView;
    public TextView mCommentCountTextView;
    public TextView mLikeCountTextView;
    public ImageView mCircleHeartImageView;
    public TextView mDateTextView;
    public LinearLayout mLikeButtonContainer;
    public LinearLayout mCommentButtonContainer;
    public Context mContext;
    public Post mPost;
    public String mPostKey;

    public PostViewHolder(View itemView, Context context) {
        super(itemView);
        mContext = context;
        mTextView = itemView.findViewById(R.id.list_item_post_content_text_view);
        mLikeButtonImageView = itemView.findViewById(R.id.list_item_post_like_button_image_view);
        mLikeButtonTextView = itemView.findViewById(R.id.list_item_post_like_button_text_view);
        mCommentButtonImageView = itemView.findViewById(R.id.list_item_post_comment_button_image_view);
        mCommentButtonTextView = itemView.findViewById(R.id.list_item_post_comment_button_text_view);
        mCommentCountTextView = itemView.findViewById(R.id.list_item_post_comment_count_text_view);
        mLikeCountTextView = itemView.findViewById(R.id.list_item_post_like_count);
        mCircleHeartImageView = itemView.findViewById(R.id.list_item_post_circle_heart_image_view);
        mLikeButtonContainer = itemView.findViewById(R.id.list_item_post_like_button_container);
        mCommentButtonContainer = itemView.findViewById(R.id.list_item_post_comment_button_container);
        mDateTextView = itemView.findViewById(R.id.post_list_date_text_view);
    }

    public void bind(Post post, String postKey) {
        mPost = post;
        mPostKey = postKey;

        mCommentButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDetailActivity();
            }
        });
        mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDetailActivity();
            }
        });

        updateUI();

        mLikeButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String studentId = LoginManager.get(mContext).getStudentId();
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.firebase_database_posts))
                        .child(mPostKey);
                if (mPost.likes.containsKey(studentId)) {
                    mPost.likes.remove(studentId);
                    mPost.likeCount--;
                } else {
                    mPost.likes.put(studentId, true);
                    mPost.likeCount++;
                }
                onLikeClicked(postRef);
                updateUI();
            }
        });
    }

    public void updateUI() {
        mTextView.setText(mPost.text);
        mCommentCountTextView.setText(String.format(
                mContext.getString(R.string.post_comment_count),
                NumberFormat.getNumberInstance(Locale.US).format(mPost.commentCount)));
        mLikeCountTextView.setText(NumberFormat.getNumberInstance(Locale.US).format(mPost.likeCount));
        mDateTextView.setText(dateToString(new Date(mPost.date)));
        if (mPost.likeCount == 0) {
            mLikeCountTextView.setVisibility(View.GONE);
            mCircleHeartImageView.setVisibility(View.GONE);
        } else {
            mLikeCountTextView.setVisibility(View.VISIBLE);
            mCircleHeartImageView.setVisibility(View.VISIBLE);
        }
        if (mPost.commentCount == 0) {
            mCommentCountTextView.setVisibility(View.GONE);
        } else {
            mCommentCountTextView.setVisibility(View.VISIBLE);
        }

        if (mPost.likes.get(LoginManager.get(mContext).getStudentId()) != null) {
            mLikeButtonImageView.setImageResource(R.drawable.ic_heart);
            mLikeButtonImageView.setColorFilter(mContext.getResources().getColor(R.color.heart_hot_pink));
            mLikeButtonTextView.setTextColor(mContext.getResources().getColor(R.color.heart_hot_pink));
        } else {
            mLikeButtonImageView.setImageResource(R.drawable.ic_heart_empty);
            mLikeButtonImageView.setColorFilter(mContext.getResources().getColor(android.R.color.tab_indicator_text));
            mLikeButtonTextView.setTextColor(mContext.getResources().getColor(android.R.color.tab_indicator_text));
        }
    }

    private void onLikeClicked(DatabaseReference postRef) {
        final String studentId = LoginManager.get(mContext).getStudentId();
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.likes.containsKey(studentId)) {
                    p.likeCount--;
                    p.likes.remove(studentId);
                } else {
                    p.likeCount++;
                    p.likes.put(studentId, true);
                }

                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d("fuck", "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void startDetailActivity() {
        Intent intent = PostDetailActivity.newIntent(mContext, mPostKey);
        mContext.startActivity(intent);
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