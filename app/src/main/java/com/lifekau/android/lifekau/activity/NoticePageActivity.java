package com.lifekau.android.lifekau.activity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

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

        Intent intent = getIntent();
        WebView webView = findViewById(R.id.notice_page);
        webView.setWebViewClient(new WebViewClient());
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                request.addRequestHeader("User-Agent", userAgent);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        DownloadManager dm = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
                        dm.enqueue(request);
                        Toast.makeText(getApplicationContext(), "파일을 다운로드합니다.", Toast.LENGTH_SHORT).show();
                    }
                    else requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        });
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.postUrl(intent.getStringExtra(EXTRA_URL), intent.getStringExtra(EXTRA_POST).getBytes());
    }
}
