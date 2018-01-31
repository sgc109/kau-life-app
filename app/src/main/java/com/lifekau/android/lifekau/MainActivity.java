package com.lifekau.android.lifekau;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = (Button)findViewById(R.id.main_activity_button);
        mButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_activity_button:
                Intent intent = FoodReviewListActivity.newIntent(this, FoodReviewListActivity.RESTAURENT_TYPE_STUDENT);
                startActivity(intent);
            default:
                return;
        }
    }
}
