package com.lifekau.android.lifekau.fragment;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.PostDetailActivity;
import com.lifekau.android.lifekau.model.Post;

import java.text.NumberFormat;
import java.util.Locale;

public class CommunityFragment extends PagerFragment {

    private DatabaseReference mDatabase;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private FirebaseRecyclerAdapter<Post, PostViewHolder> mAdapter;

    public CommunityFragment() {
    }

    public static CommunityFragment newInstance() {
        CommunityFragment fragment = new CommunityFragment();
        return fragment;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_community, container, false);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mRecyclerView = view.findViewById(R.id.community_recycler_view);
        mProgressBar = view.findViewById(R.id.post_list_progress_bar);
        mRecyclerView.setHasFixedSize(true);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);

        Query postsQuery = mDatabase.child(getString(R.string.firebase_database_posts))
                .limitToFirst(100);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            private final int UPPER_MOST_POST = 1;
            private final int NOT_UPPER_MOST_POST = 2;

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                mProgressBar.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);
                if (viewType == UPPER_MOST_POST) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, convertDpToPx(72), 0, 0);
                    view.setLayoutParams(params);
                }
                return new PostViewHolder(view);
            }

            @Override
            public void onBindViewHolder(PostViewHolder holder, int position, final Post model) {
                final String postKey = getRef(position).getKey();
                holder.mTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = PostDetailActivity.newIntent(CommunityFragment.this.getContext(), postKey);
                        startActivity(intent);
                    }
                });
                holder.bind(model);
            }

            @Override
            public int getItemViewType(int position) {
                if (position == getItemCount() - 1) return UPPER_MOST_POST;
                return NOT_UPPER_MOST_POST;
            }
        };
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.setAdapter(mAdapter);
        setHasOptionsMenu(true);
    }

    private int convertDpToPx(int dp) {
        return Math.round(dp * (getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    private int convertPxToDp(int px) {
        return Math.round(px / (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_items_alarm, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTextView;
        public ImageButton mLikeButton;
        public ImageView mCommentButton;
        public TextView mCommentCountTextView;
        public TextView mLikeCountTextView;

        public PostViewHolder(View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.list_item_post_card_view);
            mTextView = itemView.findViewById(R.id.list_item_post_content_text_view);
            mLikeButton = itemView.findViewById(R.id.list_item_post_like_image_button);
            mCommentButton = itemView.findViewById(R.id.list_item_post_comment_image_button);
            mCommentCountTextView = itemView.findViewById(R.id.list_item_post_comment_count_text_view);
            mLikeCountTextView = itemView.findViewById(R.id.list_item_post_like_count);
        }

        public void bind(Post post) {
            mTextView.setText(post.text);
            mCommentCountTextView.setText(String.format(
                    getString(R.string.post_comment_count),
                    NumberFormat.getNumberInstance(Locale.US).format(post.commentCount)));
            mLikeCountTextView.setText(NumberFormat.getNumberInstance(Locale.US).format(post.likeCount));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void findFragmentContainer(View view) {
        mFragmentContainer = view.findViewById(R.id.fragment_community_container);
    }

    @Override
    public void refresh() {

    }
}
