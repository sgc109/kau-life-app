package com.lifekau.android.lifekau.activity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;

import org.jsoup.Jsoup;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
