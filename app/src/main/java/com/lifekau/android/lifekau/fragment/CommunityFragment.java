package com.lifekau.android.lifekau.fragment;


import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.PostDetailActivity;
import com.lifekau.android.lifekau.model.Post;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CommunityFragment extends PagerFragment implements SwipeRefreshLayout.OnRefreshListener {

    private final int NUM_OF_POST_PER_PAGE = 10;
    private int mTotalItemCount = 0;
    private int mLastVisibleItemPosition;
    private boolean mIsLoading;
    private DatabaseReference mPostsRef;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private PostAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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

        mPostsRef = FirebaseDatabase.getInstance().getReference().child(getString(R.string.firebase_database_posts));
        mRecyclerView = view.findViewById(R.id.community_recycler_view);
        mProgressBar = view.findViewById(R.id.post_list_progress_bar);
        mSwipeRefreshLayout = view.findViewById(R.id.post_list_swipe_refresh_layout);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void initPostList(){
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);

        mLayoutManager = new LinearLayoutManager(getContext());

        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(true, convertDpToPx(72), convertDpToPx(100));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mIsLoading = false;
        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();

                if (!mIsLoading && mTotalItemCount <= (mLastVisibleItemPosition + NUM_OF_POST_PER_PAGE)) {
                    mIsLoading = true;
                    getPosts();
                }
            }
        };

        mRecyclerView.addOnScrollListener(scrollListener);
        setHasOptionsMenu(true);
        getPosts();
    }

    private void getPosts() {

        Query query = mPostsRef
                .orderByKey();

        if (mAdapter.getItemCount() != 0) {
            query = query.endAt(mAdapter.getLastKey());
        }
        query = query.limitToLast(NUM_OF_POST_PER_PAGE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Post> newPosts = new ArrayList<>();
                List<String> newPostKeys = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    newPosts.add(snapshot.getValue(Post.class));
                    newPostKeys.add(snapshot.getKey());
                }
                if(mAdapter.mPosts.size() != 0) {
                    newPosts.remove(newPosts.size() - 1);
                    newPostKeys.remove(newPostKeys.size() - 1);
                }
                Collections.reverse(newPosts);
                Collections.reverse(newPostKeys);

                mAdapter.addAll(newPosts, newPostKeys);
                mIsLoading = false;

                mRecyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mIsLoading = false;
            }
        });

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


    @Override
    public void onRefresh() {
        initPostList();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }

    class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {
        private final int UPPER_MOST_POST = 1;
        private final int NOT_UPPER_MOST_POST = 2;

        public List<Post> mPosts = new ArrayList<>();
        public List<String> mPostKeys = new ArrayList<>();

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
        public void onBindViewHolder(PostViewHolder holder, int position) {
            final String postKey = mPostKeys.get(position);
            holder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = PostDetailActivity.newIntent(CommunityFragment.this.getContext(), postKey);
                    startActivity(intent);
                }
            });
            holder.bind(mPosts.get(position));
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

    ;

    class PostViewHolder extends RecyclerView.ViewHolder {
        public CardView mCardView;
        public TextView mTextView;
        public ImageButton mLikeButton;
        public ImageView mCommentButton;
        public TextView mCommentCountTextView;
        public TextView mLikeCountTextView;
        public ImageView mCircleHeartImageView;

        public PostViewHolder(View itemView) {
            super(itemView);
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
                    getString(R.string.post_comment_count),
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
        initPostList();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void findFragmentContainer(View view) {
        mFragmentContainer = view.findViewById(R.id.fragment_community_container);
    }

    @Override
    public void refresh() {

    }
}
