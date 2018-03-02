package com.lifekau.android.lifekau.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.lifekau.android.lifekau.DateDisplayer;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.PostDetailActivity;
import com.lifekau.android.lifekau.adapter.PostRecyclerAdapter;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.Post;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sgc109 on 2018-02-11.
 */

public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView mTextView;
    public LinearLayout mCommentButtonContainer;
    public TextView mCommentCountTextView;
    private ImageView mLikeButtonImageView;
    private TextView mLikeButtonTextView;
    private ImageView mCommentButtonImageView;
    private TextView mCommentButtonTextView;
    private TextView mLikeCountTextView;
    private ImageView mCircleHeartImageView;
    private TextView mDateTextView;
    private LinearLayout mLikeButtonContainer;
    private ImageView mMoreButtonImageView;
    private Context mContext;
    private Post mPost;
    private String mPostKey;
    private BottomSheetDialog mBottomSheetDialog;
    private PostRecyclerAdapter mAdapter;

    public PostViewHolder(View itemView, Context context, PostRecyclerAdapter adapter) {
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
        mMoreButtonImageView = itemView.findViewById(R.id.list_item_post_more_button);
        mAdapter = adapter;
    }

    public void bind(Post post, String postKey) {
        mPost = post;
        mPostKey = postKey;

        mCommentButtonContainer.setOnClickListener(this);
        mTextView.setOnClickListener(this);
        mCommentCountTextView.setOnClickListener(this);
        mLikeButtonContainer.setOnClickListener(this);
        mMoreButtonImageView.setOnClickListener(this);
        updateUI();
    }

    public void updateUI() {
        mTextView.setText(mPost.text);
        mCommentCountTextView.setText(String.format(
                mContext.getString(R.string.post_comment_count),
                NumberFormat.getNumberInstance(Locale.US).format(mPost.commentCount)));
        mLikeCountTextView.setText(NumberFormat.getNumberInstance(Locale.US).format(mPost.likeCount));
        mDateTextView.setText(DateDisplayer.dateToStringFormat(new Date(mPost.date)));
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
            mLikeButtonImageView.setColorFilter(mContext.getResources().getColor(R.color.heart_color));
            mLikeButtonTextView.setTextColor(mContext.getResources().getColor(R.color.heart_color));
        } else {
            mLikeButtonImageView.setImageResource(R.drawable.ic_heart_empty);
            mLikeButtonImageView.setColorFilter(mContext.getResources().getColor(android.R.color.tab_indicator_text));
            mLikeButtonTextView.setTextColor(mContext.getResources().getColor(android.R.color.tab_indicator_text));
        }

        if (!LoginManager.get(mContext).getStudentId().equals(mPost.author)) {
            mMoreButtonImageView.setVisibility(View.GONE);
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
                Log.d("sgc109_debug", "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void startDetailActivity(boolean hasClickedComment) {
        Intent intent = PostDetailActivity.newIntent(mContext, mPostKey, hasClickedComment);
        mContext.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.list_item_post_comment_button_container:
                startDetailActivity(true);
                break;
            case R.id.list_item_post_content_text_view:
                startDetailActivity(false);
                break;
            case R.id.list_item_post_comment_count_text_view:
                startDetailActivity(false);
                break;
            case R.id.list_item_post_like_button_container:
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
                break;
            case R.id.list_item_post_more_button:
                mBottomSheetDialog = new BottomSheetDialog(mContext);
                View sheetView = LayoutInflater.from(mContext).inflate(R.layout.bottom_sheet_dialog_edit_and_delete, null);
                LinearLayout deleteContainer = sheetView.findViewById(R.id.fragment_community_bottom_sheet_delete);
                LinearLayout editContainer = sheetView.findViewById(R.id.fragment_community_bottom_sheet_edit);
                deleteContainer.setOnClickListener(this);
                editContainer.setOnClickListener(this);
                mBottomSheetDialog.setContentView(sheetView);
                mBottomSheetDialog.show();
                break;
            case R.id.fragment_community_bottom_sheet_delete:
                mBottomSheetDialog.dismiss();
                deletePost();
                break;
            case R.id.fragment_community_bottom_sheet_edit:
                mBottomSheetDialog.dismiss();
                break;
        }
    }

    private void deletePost() {
        int position = getAdapterPosition();
        if (mAdapter != null) {
            mAdapter.mPosts.remove(position);
            mAdapter.mPostKeys.remove(position);
            mAdapter.notifyItemRemoved(position);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference postRef = ref
                .child(mContext.getString(R.string.firebase_database_posts))
                .child(mPostKey);
        postRef.removeValue();
        DatabaseReference postCommentRef = ref
                .child(mContext.getString(R.string.firebase_database_post_comments))
                .child(mPostKey);
        postCommentRef.removeValue();
    }
}