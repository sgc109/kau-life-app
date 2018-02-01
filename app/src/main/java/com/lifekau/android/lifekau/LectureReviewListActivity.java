package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class LectureReviewListActivity extends AppCompatActivity {
    private static final String EXTRA_LECTURE_NAME = "extra_lecture_name";
    private String mLectureName;
    private ActionBar mActionBar;
    private RecyclerView mRecyclerView;
    private TextView mLectureNameTextView;
    List<LectureReview> mLectureReviews;


    public static Intent newIntent(Context context, String lectureName){
        Intent intent = new Intent(context, LectureReviewListActivity.class);
        intent.putExtra(EXTRA_LECTURE_NAME, lectureName);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_list);


        Intent intent = getIntent();
        mLectureName = intent.getStringExtra(EXTRA_LECTURE_NAME);

        mActionBar = ((AppCompatActivity)this).getSupportActionBar();
        mActionBar.setTitle(R.string.lecture_review_title);

        mLectureNameTextView = (TextView)findViewById(R.id.lecture_review_list_lecture_text_view);
        mLectureNameTextView.setText(mLectureName);

        mRecyclerView = (RecyclerView)findViewById(R.id.lecture_review_recycler_view);
        mRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });

    }
}
