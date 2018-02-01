package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LectureReviewSearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private AutoCompleteTextView mAutoCompleteSearchBar;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LectureReviewSearchActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_search);

        mAutoCompleteSearchBar = (AutoCompleteTextView) findViewById(R.id.lecture_review_search_bar);

        updateAuthComplete();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String lecture = (String) parent.getItemAtPosition(position);
        Intent intent = LectureReviewListActivity.newIntent(this, lecture);
        startActivity(intent);
    }

    public void updateAuthComplete(){
        // 여기를 파베와 연동
        List<String> list = new ArrayList<>(Arrays.asList("가 나", "나", "가나다"));
        int layoutItemId = android.R.layout.simple_dropdown_item_1line;
        LectureSearchAdapter adapter = new LectureSearchAdapter(this, layoutItemId, list);
        mAutoCompleteSearchBar.setAdapter(adapter);
        mAutoCompleteSearchBar.setThreshold(1);
        mAutoCompleteSearchBar.setOnItemClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAutoCompleteSearchBar.setText("");
    }
}
