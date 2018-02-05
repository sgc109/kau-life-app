package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LectureReviewSearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private AutoCompleteTextView mAutoCompleteSearchBar;
//    private ActionBar mActionBar;
    private List<String> mLectureList;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LectureReviewSearchActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_search);

        if(getSupportActionBar() != null ) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
//        Toolbar toolbar = findViewById(R.id.lecture_review_search_toolbar);
//        setSupportActionBar(toolbar);
        mAutoCompleteSearchBar = (AutoCompleteTextView) findViewById(R.id.lecture_review_search_bar);

        if (mLectureList == null) mLectureList = new ArrayList<>();

        updateLectureList();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String lecture = (String) parent.getItemAtPosition(position);
        Intent intent = LectureReviewListActivity.newIntent(this, lecture);
        startActivity(intent);
    }

    void updateLectureList() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Lectures");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> newLectureList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String lecture = snapshot.getValue(String.class);
                    newLectureList.add(lecture);
                }
                mLectureList = newLectureList;
                updateAutoComplete();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void updateAutoComplete() {
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        LectureSearchAdapter adapter = new LectureSearchAdapter(this, layoutItemId, mLectureList);
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
