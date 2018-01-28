package com.lifekau.android.lifekau;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = FoodReviewListActivity.newIntent(this, FoodReviewListActivity.RESTAURENT_TYPE_STUDENT);
        startActivity(intent);
    }
}
