package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.PxDpConverter;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.adapter.CommentRecyclerAdapter;
import com.lifekau.android.lifekau.adapter.PostRecyclerAdapter;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.Comment;
import com.lifekau.android.lifekau.model.Post;
import com.lifekau.android.lifekau.viewholder.CommentViewHolder;
import com.lifekau.android.lifekau.viewholder.PostViewHolder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String EXTRA_HAS_CLICKED_COMMENT = "extra_has_clicked_comment";
    private static String EXTRA_POST_KEY = "extra_post_key";
    private final int NUM_OF_COMMENT_PER_PAGE = 5;
    private int mTotalItemCount = 0;
    private int mLastVisibleItemPosition;
    private boolean mIsLoading;
    private LinearLayout mPostContainer;
    private View mBottomMarginView;
    private TextView mTextView;
    private PostViewHolder mPostViewHolder;
    private String mPostKey;
    private DatabaseReference mPostRef;
    private DatabaseReference mCommentsRef;
    private RecyclerView mRecyclerView;
    private CommentRecyclerAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ImageView mCommentSubmitImageView;
    private EditText mCommentEditText;
    private SwipeRefreshLayout mSwipeRefreshLayout;


    public static Intent newIntent(Context context, String postKey, boolean hasClickedComment) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra(EXTRA_POST_KEY, postKey);
        intent.putExtra(EXTRA_HAS_CLICKED_COMMENT, hasClickedComment);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);

        initializeViews();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mPostRef = database
                .child(getString(R.string.firebase_database_posts))
                .child(mPostKey);
        mCommentsRef = database
                .child(getString(R.string.firebase_database_post_comments))
                .child(mPostKey);

        mBottomMarginView.setVisibility(View.GONE);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mCommentSubmitImageView.setOnClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(true, PxDpConverter.convertDpToPx(72), PxDpConverter.convertDpToPx(100));
        updateUI();
    }

    private void getComments() {
        Query query = mCommentsRef.orderByKey();
        if (mAdapter.getItemCount() != 0) query = query.startAt(mAdapter.getLastKey());
        query = query.limitToFirst(NUM_OF_COMMENT_PER_PAGE);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Comment> newComments = new ArrayList<>();
                List<String> newCommentKeys = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    newComments.add(snapshot.getValue(Comment.class));
                    newCommentKeys.add(snapshot.getKey());
                }
                if (mAdapter.getItemCount() != 0) {
                    newComments.remove(0);
                    newCommentKeys.remove(0);
                }
                mAdapter.addAll(newComments, newCommentKeys);
                mIsLoading = false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("fuck", "PostDetailActivity:onCancelled");
            }
        });
    }

    private void updatePost() {
        mPostRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("fuck", "PostDetailActivity:onDataChange");
                Post post = dataSnapshot.getValue(Post.class);
                mPostViewHolder.bind(post, mPostKey);
                mPostViewHolder.mCommentButtonContainer.setOnClickListener(null);
                mPostViewHolder.mTextView.setOnClickListener(null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("fuck", "PostDetailActivity:onCancelled");
            }
        });
    }

    private void updateComments() {
        mAdapter = new CommentRecyclerAdapter(mPostKey, this);
        mRecyclerView.setAdapter(mAdapter);
        mIsLoading = true;
        getComments();
    }

    private void updateUI() {
//        mProgressBar.setVisibility(View.VISIBLE);
//        mRecyclerView.setVisibility(View.GONE);
        updatePost();
        updateComments();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initializeViews() {
        mCommentSubmitImageView = findViewById(R.id.post_detail_comment_send_image_view);
        mCommentEditText = findViewById(R.id.post_detail_comment_edit_text);
        mRecyclerView = findViewById(R.id.post_detail_recycler_view);
        mPostContainer = findViewById(R.id.post_detail_post_container);
        mPostViewHolder = new PostViewHolder(mPostContainer, this);
        mBottomMarginView = mPostContainer.findViewById(R.id.list_item_post_bottom_margin_view);
        mSwipeRefreshLayout = findViewById(R.id.post_detail_swipe_refresh_layout);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.post_detail_comment_send_image_view:
                writeComment(new Comment(LoginManager.get(this).getStudentId(), mCommentEditText.getText().toString()));
                mCommentEditText.setText("");
                break;
        }
    }

    private void writeComment(Comment comment) {
        mCommentsRef.push().setValue(comment);
        Toast.makeText(this, getString(R.string.successfully_comment_registered), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRefresh() {
        if (mIsLoading) return;
        updateUI();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }
//    private void getMoreComments() {
//        mIsLoading = true;
//        getComments();
//    }
}
