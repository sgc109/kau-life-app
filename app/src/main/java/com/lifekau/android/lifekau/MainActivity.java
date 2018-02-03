package com.lifekau.android.lifekau;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mButton1;
    private Button mButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton1 = (Button) findViewById(R.id.main_activity_button1);
        mButton1.setOnClickListener(this);
        mButton2 = (Button) findViewById(R.id.main_activity_button2);
        mButton2.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_activity_button1:
                Intent intent1 = FoodReviewCornerListActivity.newIntent(this);
                startActivity(intent1);
                break;
            case R.id.main_activity_button2:
                Intent intent2 = LectureReviewSearchActivity.newIntent(this);
                startActivity(intent2);
                break;
            default:
                return;
        }
    }
}
