package com.lifekau.android.lifekau.activity;

import android.Manifest;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LMSPortalManager;

import java.lang.ref.WeakReference;
import java.util.List;

import okhttp3.Cookie;

public class LMSActivity extends AppCompatActivity {

    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();
    private WebView mWebView;
    private ViewGroup mProgressBarLayout;
    private ViewGroup mMainLayout;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, NoticePageActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lms);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mWebView = findViewById(R.id.lms_web_view);
        mWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBarLayout.setVisibility(View.GONE);
                mMainLayout.setVisibility(View.VISIBLE);
            }
        });
        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(
                        Uri.parse(url));
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
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
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mProgressBarLayout = findViewById(R.id.lms_progress_bar_layout);
        mMainLayout = findViewById(R.id.lms_main_layout);
        mProgressBarLayout.setVisibility(View.VISIBLE);
        mMainLayout.setVisibility(View.GONE);
        CheckSessionAsyncTask checkSessionAsyncTask = new CheckSessionAsyncTask(getApplication(), this);
        checkSessionAsyncTask.execute();
    }

    private static class CheckSessionAsyncTask extends AsyncTask<Void, Void, Integer> {

        private WeakReference<LMSActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        CheckSessionAsyncTask(Application application, LMSActivity LMSActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(LMSActivity);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Resources resources = applicationWeakReference.get().getResources();
            LMSActivity lmsActivity = activityReference.get();
            if (lmsActivity == null || lmsActivity.isFinishing()) {
                return resources.getInteger(R.integer.unexpected_error);
            }
            int count = 0;
            int result = lmsActivity.mLMSPortalManager.pullStudentId(applicationWeakReference.get());
            while (!lmsActivity.isFinishing() && result != resources.getInteger(R.integer.no_error) && !isCancelled()) {
                if (result == resources.getInteger(R.integer.network_error)) {
                    sleep(1000);
                    count++;
                } else return result;
                if (count == resources.getInteger(R.integer.maximum_retry_num))
                    return resources.getInteger(R.integer.network_error);
            }
            return resources.getInteger(R.integer.no_error);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            LMSActivity lmsActivity = activityReference.get();
            Resources resources = applicationWeakReference.get().getResources();
            if (lmsActivity == null || lmsActivity.isFinishing())
                return;
            if (result == resources.getInteger(R.integer.no_error)) {
                CookieManager cookieManager = CookieManager.getInstance();
                cookieManager.setAcceptCookie(true);
                List<Cookie> cookies = lmsActivity.mLMSPortalManager.getCookie(applicationWeakReference.get());
                cookieManager.removeAllCookie();
                for (Cookie cookie : cookies) {
                    String cookieString = cookie.name() + "=" + cookie.value() + "; Domain=" + cookie.domain();
                    cookieManager.setCookie(cookie.domain(), cookieString);
                }
                lmsActivity.mWebView.loadUrl(resources.getString(R.string.lms_my_page));
            } else if (result == resources.getInteger(R.integer.network_error)) {
                //네트워크 관련 예외 처리
                lmsActivity.showToast(resources.getString(R.string.portal_network_error_message));
            } else if (result == resources.getInteger(R.integer.session_error) || result == resources.getInteger(R.integer.ssl_hand_shake_error)) {
                //세션 관련 예외 처리
                lmsActivity.showToast(resources.getString(R.string.portal_session_disconnect_error_message));
                Intent intent = LoginActivity.newIntent(lmsActivity);
                lmsActivity.startActivity(intent);
                lmsActivity.finish();
            }
        }

        public void sleep(int time) {
            try {
                Thread.sleep(time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            Intent intent = HomeActivity.newIntent(getApplicationContext(), 4);
            startActivity(intent);
            finish();
        } else super.onBackPressed();
    }
}
