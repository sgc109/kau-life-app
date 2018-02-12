package com.lifekau.android.lifekau.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.lifekau.android.lifekau.PxDpConverter;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.PostDetailActivity;
import com.lifekau.android.lifekau.fragment.CommunityFragment;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.Post;
import com.lifekau.android.lifekau.viewholder.PostViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgc109 on 2018-02-12.
 */

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostViewHolder> {
    private final int UPPER_MOST_POST = 1;
    private final int NOT_UPPER_MOST_POST = 2;
    private Context mContext;
    public List<Post> mPosts = new ArrayList<>();
    public List<String> mPostKeys = new ArrayList<>();

    public PostRecyclerAdapter(Context context){
        mContext = context;
    }
    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);
        if (viewType == UPPER_MOST_POST) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, PxDpConverter.convertDpToPx(72), 0, 0);
            view.setLayoutParams(params);
        }
        return new PostViewHolder(view, mContext);
    }

    @Override
    public void onBindViewHolder(final PostViewHolder holder, final int position) {
        final String postKey = mPostKeys.get(position);
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = PostDetailActivity.newIntent(mContext, postKey);
                mContext.startActivity(intent);
            }
        });
        final Post post = mPosts.get(position);
        holder.bind(post);
        holder.mLikeButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String studentId = LoginManager.get(mContext).getStudentId();
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.firebase_database_posts))
                        .child(mPostKeys.get(position));
                if(post.likes.containsKey(studentId)) {
                    post.likes.remove(studentId);
                    post.likeCount--;
                } else {
                    post.likes.put(studentId, true);
                    post.likeCount++;
                }
                onLikeClicked(postRef);
                holder.updateUI();
//                notifyItemChanged(position);
            }
        });
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
                    // Unstar the post and remove self from stars
                    p.likeCount--;
                    p.likes.remove(studentId);
                } else {
                    // Star the post and add self to stars
                    p.likeCount++;
                    p.likes.put(studentId, true);
                }

                // Set value and report transaction success
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

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return UPPER_MOST_POST;
        return NOT_UPPER_MOST_POST;
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public void addAll(List<Post> posts, List<String> postKeys) {
        int initialSize = mPosts.size();
        mPosts.addAll(posts);
        mPostKeys.addAll(postKeys);
        notifyItemRangeInserted(initialSize, posts.size());
    }

    public String getLastKey() {
        return mPostKeys.get(mPostKeys.size() - 1);
    }
}