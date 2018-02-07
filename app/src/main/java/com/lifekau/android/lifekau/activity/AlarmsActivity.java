package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lifekau.android.lifekau.R;

public class AlarmsActivity extends AppCompatActivity {
    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, AlarmsActivity.class);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);
    }
}
