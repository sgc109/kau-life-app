package com.lifekau.android.lifekau.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.PxDpConverter;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.adapter.PostRecyclerAdapter;
import com.lifekau.android.lifekau.model.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommunityFragment extends PagerFragment implements SwipeRefreshLayout.OnRefreshListener {
    private final int NUM_OF_POST_PER_PAGE = 20;
    private int mTotalItemCount = 0;
    private int mLastVisibleItemPosition;
    private boolean mIsLoading;
    private DatabaseReference mPostsRef;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    private PostRecyclerAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyMessageTextView;
    private int mCurrentY = 0;

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
        initializeViews(view);

        mLayoutManager = new LinearLayoutManager(getContext());
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(true, PxDpConverter.convertDpToPx(72), PxDpConverter.convertDpToPx(100));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mTotalItemCount = mLayoutManager.getItemCount();
                mLastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition();

                if (!mIsLoading && mTotalItemCount <= (mLastVisibleItemPosition + NUM_OF_POST_PER_PAGE)) {
                    if (!mIsLoading) {
                        mIsLoading = true;
                        getPosts();
                    }
                }

                if (mLayoutManager.findFirstVisibleItemPosition() != 0) {

                }
            }
        };

        mRecyclerView.addOnScrollListener(scrollListener);
        setHasOptionsMenu(true);


        initPostList();

        return view;
    }

    private void initializeViews(View view) {
        mRecyclerView = view.findViewById(R.id.community_recycler_view);
        mProgressBar = view.findViewById(R.id.post_list_progress_bar);
        mSwipeRefreshLayout = view.findViewById(R.id.post_list_swipe_refresh_layout);
        mEmptyMessageTextView = view.findViewById(R.id.post_list_empty_message_text_view);
    }

    public void initPostList() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mEmptyMessageTextView.setVisibility(View.GONE);

        if (!mIsLoading) {
            mIsLoading = true;
            mAdapter = new PostRecyclerAdapter(getActivity());
            mRecyclerView.setAdapter(mAdapter);
            getPosts();
        }
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
                if (mAdapter.getItemCount() > 0 && newPosts.size() > 0) {
                    newPosts.remove(newPosts.size() - 1);
                    newPostKeys.remove(newPostKeys.size() - 1);
                }
                Collections.reverse(newPosts);
                Collections.reverse(newPostKeys);

                mAdapter.addAll(newPosts, newPostKeys);

                setVisibilities();
                mProgressBar.setVisibility(View.GONE);
                mIsLoading = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mIsLoading = false;
            }
        });
    }

    private void setVisibilities() {
        if (mAdapter.getItemCount() == 0) {
            mEmptyMessageTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mEmptyMessageTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_items_alarm, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
        initPostList();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void findFragmentContainer(View view) {
        mFragmentContainer = view.findViewById(R.id.fragment_community_container);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void refresh() {

    }

    public PostRecyclerAdapter getAdapter() {
        return mAdapter;
    }

    public void updatePost(final int position){
        mPostsRef
                .child(mAdapter.mPostKeys.get(position))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot == null){
                            mAdapter.mPosts.remove(position);
                            mAdapter.mPostKeys.remove(position);
                            return;
                        }
                        Post post = dataSnapshot.getValue(Post.class);
                        mAdapter.mPosts.set(position, post);
                        mAdapter.notifyItemChanged(position);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("sgc109_debug", "CommunityFragment.updatePost.onCancelled");
                    }
                });
    }
}
