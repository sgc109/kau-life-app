package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.Post;
import com.lifekau.android.lifekau.viewholder.CommentViewHolder;
import com.lifekau.android.lifekau.viewholder.PostViewHolder;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

public class PostDetailActivity extends AppCompatActivity {
    private static String EXTRA_POST_KEY = "extra_post_key";
    private LinearLayout mPostContainer;
    private View mBottomMarginView;
    private TextView mTextView;
    private PostViewHolder mPostViewHolder;
    private String mPostKey;
    private DatabaseReference mPostRef;
    private DatabaseReference mCommentsRef;
    private RecyclerView mRecyclerView;

    public static Intent newIntent(Context context, String postKey) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra(EXTRA_POST_KEY, postKey);
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

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mPostRef = database.child(getString(R.string.firebase_database_posts)).child(mPostKey);
        mCommentsRef = database.child(getString(R.string.firebase_database_post_comments)).child(mPostKey);

        initializeViews();

        mBottomMarginView.setVisibility(View.GONE);
    }

    private void updateUI() {
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

        mRecyclerView.setAdapter(new RecyclerView.Adapter<CommentViewHolder>() {
            @Override
            public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(CommentViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateUI();
    }

    private void initializeViews() {
        mPostContainer = findViewById(R.id.post_detail_post_container);
        mBottomMarginView = mPostContainer.findViewById(R.id.list_item_post_bottom_margin_view);
        mPostViewHolder = new PostViewHolder(mPostContainer, this);
        mRecyclerView = findViewById(R.id.post_detail_recycler_view);
    }
}
