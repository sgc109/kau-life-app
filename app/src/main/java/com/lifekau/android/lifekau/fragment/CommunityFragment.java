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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.PostDetailActivity;
import com.lifekau.android.lifekau.model.Post;

public class CommunityFragment extends PagerFragment {

    private DatabaseReference mDatabase;
    private RecyclerView mRecyclerView;
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

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Query postsQuery = mDatabase.child(getString(R.string.firebase_database_posts))
                .limitToFirst(100);

        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post.class)
                .build();

        mAdapter = new FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {
            private final int UPPER_MOST_POST = 1;
            private final int NOT_UPPER_MOST_POST = 2;

            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);
                if (viewType == UPPER_MOST_POST) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, convertDpToPx(56), 0, 0);
                    view.setLayoutParams(params);
                }
                return new PostViewHolder(view);
            }

            @Override
            public void onBindViewHolder(PostViewHolder holder, int position, final Post model) {
                final String postKey = getRef(position).getKey();
                holder.bind(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = PostDetailActivity.newIntent(CommunityFragment.this.getContext(), postKey);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public int getItemViewType(int position) {
                if (position == 0) return UPPER_MOST_POST;
                return NOT_UPPER_MOST_POST;
            }
        };
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);
        setHasOptionsMenu(true);
    }

    private int convertDpToPx(int dp){
        return Math.round(dp*(getResources().getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));

    }

    private int convertPxToDp(int px){
        return Math.round(px/(Resources.getSystem().getDisplayMetrics().xdpi/DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_items_alarm, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        CardView mCardView;
        TextView mTextView;

        public PostViewHolder(View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.card_view);
            mTextView = itemView.findViewById(R.id.layout_item_demo_title);
        }

        public void bind(Post post, View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            mTextView.setText(post.text);
            // post 내용으로 뷰 세팅하기
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
