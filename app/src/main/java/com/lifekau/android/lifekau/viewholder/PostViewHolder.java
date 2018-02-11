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

import java.text.NumberFormat;
import java.util.Locale;
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
    }

    public void bind(Post post) {
        mTextView.setText(post.text);
        mCommentCountTextView.setText(String.format(
                mContext.getString(R.string.post_comment_count),
                NumberFormat.getNumberInstance(Locale.US).format(post.commentCount)));
        mLikeCountTextView.setText(NumberFormat.getNumberInstance(Locale.US).format(post.likeCount));
        if(post.likeCount == 0){
            mLikeCountTextView.setVisibility(View.GONE);
            mCircleHeartImageView.setVisibility(View.GONE);
        }
        if(post.commentCount == 0){
            mCommentCountTextView.setVisibility(View.GONE);
        }
    }
}