package com.lifekau.android.lifekau.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.adapter.LectureSearchAdapter;
import com.lifekau.android.lifekau.manager.LectureManager;
import com.lifekau.android.lifekau.model.Lecture;

import java.util.ArrayList;
import java.util.List;

public class LectureReviewSearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private AutoCompleteTextView mAutoCompleteSearchBar;
//    private ActionBar mActionBar;
    private List<Lecture> mLectureList;
    private ProgressDialog mProgressDialog;
    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LectureReviewSearchActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_search);

        if(getSupportActionBar() != null ) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().hide();
        }
        mAutoCompleteSearchBar = findViewById(R.id.lecture_review_search_bar);

        if (mLectureList == null) mLectureList = new ArrayList<>();


        mProgressDialog = ProgressDialog.show(this, "설정 중", "초기 설정을 하는 중입니다.", true, false);
        List<Lecture> lectures = LectureManager.get(this).getAllLectures();
        if(lectures.size() != 0) {
            mLectureList = lectures;
            updateAutoComplete();
            mProgressDialog.dismiss();
        } else {
            updateLectureList();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String lecture = (String) parent.getItemAtPosition(position);
        Intent intent = LectureReviewListActivity.newIntent(this, lecture);
        startActivity(intent);
    }

    void updateLectureList() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(getString(R.string.firebase_database_lectures));
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Lecture> newLectureList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String lectureName = snapshot.getValue(String.class);
                    newLectureList.add(new Lecture(lectureName));
                }
                Log.d("sgc109_debug", "cnt : " + newLectureList.size());
                mLectureList = newLectureList;
                LectureManager.get(LectureReviewSearchActivity.this).addLectures(mLectureList);
                updateAutoComplete();
                mProgressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateAutoComplete() {
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        List<String> lectureNameList = new ArrayList<>();

        for(Lecture lecture : mLectureList){
            lectureNameList.add(lecture.getName());
        }

        LectureSearchAdapter adapter = new LectureSearchAdapter(this, layoutItemId, lectureNameList);
        mAutoCompleteSearchBar.setAdapter(adapter);
        mAutoCompleteSearchBar.setThreshold(0);
        mAutoCompleteSearchBar.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAutoCompleteSearchBar.setText("");
    }
}
