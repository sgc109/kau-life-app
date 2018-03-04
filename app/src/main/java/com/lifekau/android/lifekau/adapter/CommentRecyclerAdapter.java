package com.lifekau.android.lifekau.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.model.Comment;
import com.lifekau.android.lifekau.viewholder.CommentViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgc109 on 2018-02-14.
 */

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private String mPostKey;
    public List<Comment> mComments = new ArrayList<>();
    public List<String> mCommentKeys = new ArrayList<>();
    private Context mContext;
    private String mPostAuthor;

    public CommentRecyclerAdapter(String postKey, Context context, String postAuthor) {
        mPostKey = postKey;
        mContext = context;
        mPostAuthor = postAuthor;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comment, parent, false);
        return new CommentViewHolder(view, mContext, mPostAuthor);
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        holder.itemView.setLongClickable(true);
        holder.bind(mComments.get(position), mCommentKeys.get(position), mPostKey);
    }

    @Override
    public int getItemCount() {
        return mComments.size();
    }

    public void addAll(List<Comment> comments, List<String> commentKeys) {
        int initialSize = mComments.size();
        mComments.addAll(comments);
        mCommentKeys.addAll(commentKeys);
        notifyItemRangeInserted(initialSize, comments.size());
    }

    public String getLastKey() {
        return mCommentKeys.get(mCommentKeys.size() - 1);
    }
}
