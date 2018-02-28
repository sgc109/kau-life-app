package com.lifekau.android.lifekau.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.lifekau.android.lifekau.DateDisplayer;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.Comment;

import java.util.Date;

/**
 * Created by sgc109 on 2018-02-12.
 */

public class CommentViewHolder extends RecyclerView.ViewHolder{
    private TextView mContentTextView;
    private TextView mDateTextView;
    private TextView mLikeTextView;
    private TextView mLikeCountTextView;
    private ImageView mLikeImageView;
    private Context mContext;
    private Comment mComment;
    private String mCommentKey;
    private String mPostKey;
    public CommentViewHolder(View itemView, Context context) {
        super(itemView);
        mContentTextView = itemView.findViewById(R.id.list_item_comment_content_text_view);
        mDateTextView = itemView.findViewById(R.id.list_item_comment_date_text_view);
        mLikeTextView = itemView.findViewById(R.id.list_item_comment_like_text_view);
        mLikeCountTextView = itemView.findViewById(R.id.list_item_comment_like_count_text_view);
        mLikeImageView = itemView.findViewById(R.id.list_item_comment_like_image_view);
        mContext = context;
    }

    public void bind(Comment comment, String commentKey, final String postKey){
        mComment = comment;
        mCommentKey = commentKey;
        mPostKey = postKey;

        updateUI();

        mLikeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String studentId = LoginManager.get(mContext).getStudentId();
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.firebase_database_post_comments))
                        .child(postKey)
                        .child(mCommentKey);
                if (mComment.likes.containsKey(studentId)) {
                    mComment.likes.remove(studentId);
                    mComment.likeCount--;
                } else {
                    mComment.likes.put(studentId, true);
                    mComment.likeCount++;
                }
                onLikeClicked(postRef);
                updateUI();
            }
        });
    }

    private void updateUI(){
        mContentTextView.setText(mComment.text);
        mDateTextView.setText(DateDisplayer.dateToStringFormat(new Date(mComment.date)));
        mLikeCountTextView.setText("" + mComment.likeCount);
        if(mComment.likeCount == 0){
            mLikeImageView.setVisibility(View.GONE);
            mLikeCountTextView.setVisibility(View.GONE);
        } else {
            mLikeImageView.setVisibility(View.VISIBLE);
            mLikeCountTextView.setVisibility(View.VISIBLE);
        }

        if (mComment.likes.get(LoginManager.get(mContext).getStudentId()) != null) {
            mLikeTextView.setTextColor(mContext.getResources().getColor(R.color.heart_color));
        } else {
            mLikeTextView.setTextColor(mContext.getResources().getColor(android.R.color.tab_indicator_text));
        }
    }
    private void onLikeClicked(DatabaseReference postRef) {
        final String studentId = LoginManager.get(mContext).getStudentId();
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Comment p = mutableData.getValue(Comment.class);
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
                Log.d("sgc109_debug", "commentTransaction:onComplete:" + databaseError);
            }
        });
    }
}
