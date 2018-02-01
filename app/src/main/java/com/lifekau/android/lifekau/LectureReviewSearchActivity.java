package com.lifekau.android.lifekau;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class LectureReviewSearchActivity extends AppCompatActivity {

    AutoCompleteTextView mAutoCompleteSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_search);

        String[] strs = new String[40];
        for(int i = 0; i < 20; i++) strs[i] = "a a";
        for(int i = 0; i < 20; i++) strs[i + 20] = "aa";
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, layoutItemId, strs);


    }
}
