package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

public class LectureReviewWriteActivity extends AppCompatActivity {
    public static final String EXTRA_LECTURE_NAME = "extra_lecture_name";
    private String mLectureName;
    private RatingBar mRatingBar;
    private EditText mCommentEditText;
    private Button mCancelButton;
    private Button mSubmitButton;

    public static Intent newIntent(Context context, String lectureName){
        Intent intent = new Intent(context, LectureReviewWriteActivity.class);
        intent.putExtra(EXTRA_LECTURE_NAME, lectureName);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_write);

        mLectureName = getIntent().getStringExtra(EXTRA_LECTURE_NAME);
    }
}
