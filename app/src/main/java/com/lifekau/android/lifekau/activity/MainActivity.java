package com.lifekau.android.lifekau.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.lifekau.android.lifekau.PushAlarmInstanceIDService;
import com.lifekau.android.lifekau.PushAlarmService;
import com.lifekau.android.lifekau.R;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HashMap<String, String> params = new HashMap<>();
        params.put("regid", FirebaseInstanceId.getInstance().getToken());

        Intent intent = HomeActivity.newIntent(this);
        startActivity(intent);
        finish();
    }
}
