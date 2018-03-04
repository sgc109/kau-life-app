package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.model.LectureReview;
import com.lifekau.android.lifekau.viewholder.LectureReviewHolder;

import java.util.ArrayList;
import java.util.List;

public class LectureReviewListActivity extends AppCompatActivity {
    private static final String EXTRA_LECTURE_NAME = "extra_lecture_name";
    private final String SAVED_LECTURE_NAME = "saved_lecture_name";
    private final int REQUEST_WRITE_REVIEW = 0;

    private String mLectureName;
    private ActionBar mActionBar;
    private RecyclerView mRecyclerView;
    private RatingBar mRatingBar;
    private TextView mCntReviewsTextView;
    private TextView mAvgRatingTextView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private TextView mLectureNameTextView;
    private FloatingActionButton mFab;
    private NestedScrollView mNestedScrollView;
    private ProgressBar mProgressBar;
    private TextView mEmptyListMessage;
    //    private TextView mEmptyListMessage;
    List<LectureReview> mLectureReviews;


    public static Intent newIntent(Context context, String lectureName) {
        Intent intent = new Intent(context, LectureReviewListActivity.class);
        intent.putExtra(EXTRA_LECTURE_NAME, lectureName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_list);

        if (savedInstanceState != null) {
            mLectureName = savedInstanceState.getString(SAVED_LECTURE_NAME);
        }

        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().hide();
        }

        Intent intent = getIntent();
        mLectureName = intent.getStringExtra(EXTRA_LECTURE_NAME);

        if (mLectureReviews == null) {
            mLectureReviews = new ArrayList<>();
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.lecture_review_recycler_view);
        mProgressBar = findViewById(R.id.lecture_review_list_progress_bar);
        mEmptyListMessage = findViewById(R.id.lecture_review_list_empty_list_text_view);
        mNestedScrollView = findViewById(R.id.lecture_review_list_nested_scroll_view);
        mCntReviewsTextView = findViewById(R.id.lecture_review_number_of_reviews_text_view);
        mAvgRatingTextView = findViewById(R.id.lecture_review_average_rating_text_view);
        mRatingBar = findViewById(R.id.lecture_review_list_rating_bar);
        mLectureNameTextView = findViewById(R.id.lecture_review_list_lecture_text_view);
        mLectureNameTextView.setText(mLectureName);

        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mEmptyListMessage.setVisibility(View.GONE);

        mRecyclerAdapter = new RecyclerView.Adapter<LectureReviewHolder>() {
            @Override
            public LectureReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_lecture_review, parent, false);
                return new LectureReviewHolder(view);
            }

            @Override
            public void onBindViewHolder(LectureReviewHolder holder, int position) {
                holder.bindReview(mLectureReviews.get(position));
            }

            @Override
            public int getItemCount() {
                return mLectureReviews.size();
            }
        };
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/" + getString(R.string.firebase_database_lecture_reviews)).child(mLectureName);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LectureReview> newLectureReviews = new ArrayList<>();
                float sumRating = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LectureReview review = snapshot.getValue(LectureReview.class);
                    sumRating += review.mRating;
                    newLectureReviews.add(review);
                }
                mProgressBar.setVisibility(View.GONE);
                setVisibilities();

                int cntReviews = newLectureReviews.size();
                float averageRating = cntReviews > 0 ? sumRating / cntReviews : (float) 0.0;
                mCntReviewsTextView.setText("" + cntReviews + getString(R.string.review_number_of_review_string));
                mAvgRatingTextView.setText("" + String.format("%.1f", averageRating) + " / 5.0");
                mRatingBar.setRating(averageRating);

                mLectureReviews = newLectureReviews;
                mRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//        setVisibilities();
    }

    private void setVisibilities() {
        if (mLectureReviews.size() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyListMessage.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyListMessage.setVisibility(View.GONE);
        }
    }

    public void onClickWriteLectureReviewFab(View view) {
        writeLectureReview();
    }

    private void writeLectureReview() {
        Intent intent = LectureReviewWriteActivity.newIntent(this, mLectureName);
        startActivityForResult(intent, REQUEST_WRITE_REVIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_WRITE_REVIEW:
                    Toast.makeText(this, getString(R.string.review_write_success_message), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SAVED_LECTURE_NAME, mLectureName);
    }
}
