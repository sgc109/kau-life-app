package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.lifekau.android.lifekau.R;

public class EmptyLectureRoomListActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int LECTURE_ROOM_NUM = 5;

    public static Intent newIntent(Context context) {
        return new Intent(context, EmptyLectureRoomListActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_lecture_room_list);
        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().hide();
        }
        ViewGroup[] showButtons = new ViewGroup[LECTURE_ROOM_NUM];
        showButtons[0] = findViewById(R.id.activity_show_lecture_room_button_01);
        showButtons[0].setOnClickListener(this);
        showButtons[1] = findViewById(R.id.activity_show_lecture_room_button_02);
        showButtons[1].setOnClickListener(this);
        showButtons[2] = findViewById(R.id.activity_show_lecture_room_button_03);
        showButtons[2].setOnClickListener(this);
        showButtons[3] = findViewById(R.id.activity_show_lecture_room_button_04);
        showButtons[3].setOnClickListener(this);
        showButtons[4] = findViewById(R.id.activity_show_lecture_room_button_05);
        showButtons[4].setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = EmptyLectureRoomActivity.newIntent(getApplicationContext(), 0);
        switch (v.getId()){
            case R.id.activity_show_lecture_room_button_01:
                intent = EmptyLectureRoomActivity.newIntent(getApplicationContext(), 0);
                break;
            case R.id.activity_show_lecture_room_button_02:
                intent = EmptyLectureRoomActivity.newIntent(getApplicationContext(), 1);
                break;
            case R.id.activity_show_lecture_room_button_03:
                intent = EmptyLectureRoomActivity.newIntent(getApplicationContext(), 2);
                break;
            case R.id.activity_show_lecture_room_button_04:
                intent = EmptyLectureRoomActivity.newIntent(getApplicationContext(), 3);
                break;
            case R.id.activity_show_lecture_room_button_05:
                intent = EmptyLectureRoomActivity.newIntent(getApplicationContext(), 4);
                break;
        }
        startActivity(intent);
    }
}
