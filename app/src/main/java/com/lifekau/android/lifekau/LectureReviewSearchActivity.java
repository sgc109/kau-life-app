package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class LectureReviewSearchActivity extends AppCompatActivity {

    private AutoCompleteTextView mAutoCompleteSearchBar;

    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, LectureReviewSearchActivity.class);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_search);

        mAutoCompleteSearchBar = (AutoCompleteTextView)findViewById(R.id.lecture_review_search_bar);

        String[] strs = {"나다라", "나다", "나", "다"};
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, layoutItemId, strs);
        mAutoCompleteSearchBar.setAdapter(adapter);
    }
}
