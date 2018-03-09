package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.lifekau.android.lifekau.R;

import okhttp3.HttpUrl;

public class ReadingRoomDetailActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String EXTRA_ROOM_TYPE = "extra_room_type";
    private static final int TOTAL_ROOM_NUM = 5;

    private WebView mWebView;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ReadingRoomDetailActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_room_detail);
        mWebView = findViewById(R.id.activity_reading_room_detail_web_view);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.loadUrl(getUrl(getIntent().getIntExtra(EXTRA_ROOM_TYPE, 1)));
        Button[] buttons = {
                findViewById(R.id.activity_reading_room_detail_show_room_button_01),
                findViewById(R.id.activity_reading_room_detail_show_room_button_02),
                findViewById(R.id.activity_reading_room_detail_show_room_button_03),
                findViewById(R.id.activity_reading_room_detail_show_room_button_04),
                findViewById(R.id.activity_reading_room_detail_show_room_button_05),
        };
        for(int i = 0; i < TOTAL_ROOM_NUM; i++) buttons[i].setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String url = "http://ebook.kau.ac.kr:81/domian5.asp";
        switch(v.getId()){
            case R.id.activity_reading_room_detail_show_room_button_01:
                url = getUrl(1);
                break;
            case R.id.activity_reading_room_detail_show_room_button_02:
                url = getUrl(2);
                break;
            case R.id.activity_reading_room_detail_show_room_button_03:
                url = getUrl(3);
                break;
            case R.id.activity_reading_room_detail_show_room_button_04:
                url = getUrl(4);
                break;
            case R.id.activity_reading_room_detail_show_room_button_05:
                url = getUrl(5);
                break;
        }
        mWebView.loadUrl(url);
    }

    private String getUrl(int roomNum){
        return HttpUrl.parse("http://ebook.kau.ac.kr:81/roomview5.asp").newBuilder()
                .addQueryParameter("room_no", String.valueOf(roomNum))
                .build().toString();
    }
}
