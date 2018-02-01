package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class LectureReviewListActivity extends AppCompatActivity {
    private static final String EXTRA_LECTURE_NAME = "extra_lecture_name";
    private String mLectureName;

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
        Toast.makeText(this, mLectureName, Toast.LENGTH_LONG).show();
    }
}
