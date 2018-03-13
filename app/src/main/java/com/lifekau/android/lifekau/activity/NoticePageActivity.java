package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.lifekau.android.lifekau.R;

public class NoticePageActivity extends AppCompatActivity {
    private static String EXTRA_URL = "extra_url";
    private static String EXTRA_POST = "extra_post";

    public static Intent newIntent(Context context, String url, String post){
        Intent intent = new Intent(context, NoticePageActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_POST, post);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_page);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        WebView webView = findViewById(R.id.notice_page);
        webView.setWebChromeClient(new WebChromeClient(){});
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        Intent intent = getIntent();
        webView.postUrl(intent.getStringExtra(EXTRA_URL), intent.getStringExtra(EXTRA_POST).getBytes());
    }
}
