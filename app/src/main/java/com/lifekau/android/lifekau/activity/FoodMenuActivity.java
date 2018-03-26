package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.NoticeManager;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static android.os.SystemClock.sleep;

public class FoodMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int FOOD_MENU_NUM = 2;
    private static final int FOOD_MENU_TYPE_STUDENT_REST = 0;
    private static final int FOOD_MENU_TYPE_DORMITORY_REST = 1;
    private NoticeManager mNoticeManager = NoticeManager.getInstance();
    private WebView[] mFoodMenuWebView;
    private ProgressBar mProgressBar;
    private Button mShowStudentRestFoodMenuButton;
    private Button mShowDormitoryRestFoodMenuButton;
    private boolean mIsLoading;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, FoodMenuActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_menu);

        if (getSupportActionBar() != null) {
//            getSupportActionBar().hide();
            getSupportActionBar().setTitle("식단표");
        }
        mFoodMenuWebView = new WebView[FOOD_MENU_NUM];
        mFoodMenuWebView[FOOD_MENU_TYPE_STUDENT_REST] = findViewById(R.id.activity_food_student_menu_web_view);
        mFoodMenuWebView[FOOD_MENU_TYPE_DORMITORY_REST] = findViewById(R.id.activity_food_dormitory_menu_web_view);
        initWebView(FOOD_MENU_TYPE_STUDENT_REST);
        initWebView(FOOD_MENU_TYPE_DORMITORY_REST);
        mProgressBar = findViewById(R.id.activity_food_menu_progress_bar);
        mShowStudentRestFoodMenuButton = findViewById(R.id.activity_food_menu_show_student_restaurant_button);
        mShowStudentRestFoodMenuButton.setOnClickListener(this);
        mShowDormitoryRestFoodMenuButton = findViewById(R.id.activity_food_menu_dormitory_restaurant_button);
        mShowDormitoryRestFoodMenuButton.setOnClickListener(this);
        mIsLoading = false;
        executeGetStudentRestFoodMenu();
        executeGetDormitoryRestFoodMenu();
    }

    void initWebView(final int index){
        mFoodMenuWebView[index].setVisibility(View.GONE);
        mFoodMenuWebView[index].getSettings().setLoadWithOverviewMode(true);
        mFoodMenuWebView[index].getSettings().setUseWideViewPort(true);
        mFoodMenuWebView[index].getSettings().setSupportZoom(true);
        mFoodMenuWebView[index].getSettings().setBuiltInZoomControls(true);
        mFoodMenuWebView[index].getSettings().setDisplayZoomControls(false);
        mFoodMenuWebView[index].setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!mIsLoading) {
                    mIsLoading = true;
                    mProgressBar.setVisibility(View.GONE);
                }
                mFoodMenuWebView[1 - index].setVisibility(View.GONE);
                mFoodMenuWebView[index].setVisibility(View.VISIBLE);
                mFoodMenuWebView[index].zoomBy(0.1f);
            }
        });
    }

    void executeGetStudentRestFoodMenu() {
        GetStudentRestMenuASyncTask aSyncTask = new GetStudentRestMenuASyncTask(getApplication(), this);
        aSyncTask.execute();
    }

    void executeGetDormitoryRestFoodMenu() {
        GetDormitoryRestMenuASyncTask aSyncTask = new GetDormitoryRestMenuASyncTask(getApplication(), this);
        aSyncTask.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_food_menu_show_student_restaurant_button:
                setFoodMenuImage(FOOD_MENU_TYPE_STUDENT_REST, mNoticeManager.getStudentRestFoodMenuFileName());
                break;
            case R.id.activity_food_menu_dormitory_restaurant_button:
                setFoodMenuImage(FOOD_MENU_TYPE_DORMITORY_REST, mNoticeManager.getDormitoryRestFileName());
                break;
        }
    }

    public void setFoodMenuImage(int index, Set<String> fileNames) {
        String[] fileNameArray = fileNames.toArray(new String[fileNames.size()]);
        Arrays.sort(fileNameArray);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>");
        stringBuilder.append("<head><style>img{max-width: 100%; height: auto;}</style></head>");
        stringBuilder.append("<meta name=viewport' content='width=device-width,initial-scale=1'></head>");
        stringBuilder.append("<table>");
        for (String fileName : fileNameArray) {
            stringBuilder.append("<tr><td><img src= 'file://");
            stringBuilder.append(getFilesDir().getAbsolutePath());
            stringBuilder.append("/");
            stringBuilder.append(fileName);
            stringBuilder.append("'/></td></tr>");
        }
        stringBuilder.append("</table></html>");
        mFoodMenuWebView[index].loadDataWithBaseURL("",
                stringBuilder.toString(),
                "text/html",
                "utf-8",
                null);
    }

    private static class GetStudentRestMenuASyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<FoodMenuActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        GetStudentRestMenuASyncTask(Application application, FoodMenuActivity foodMenuActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(foodMenuActivity);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            FoodMenuActivity foodMenuActivity = activityReference.get();
            Resources resources = applicationWeakReference.get().getResources();
            if (foodMenuActivity == null || foodMenuActivity.isFinishing())
                return resources.getInteger(R.integer.unexpected_error);
            int result;
            int count = 0;
            NoticeManager nm = foodMenuActivity.mNoticeManager;
            while ((result = nm.pullStudentRestFoodMenu(applicationWeakReference.get())) != resources.getInteger(R.integer.no_error) && !isCancelled()) {
                if (result == resources.getInteger(R.integer.network_error)) {
                    sleep(1000);
                    count++;
                } else return result;
                if (count == resources.getInteger(R.integer.maximum_retry_num))
                    return resources.getInteger(R.integer.network_error);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            FoodMenuActivity foodMenuActivity = activityReference.get();
            Resources resources = applicationWeakReference.get().getResources();
            if (foodMenuActivity == null || foodMenuActivity.isFinishing()) return;
            NoticeManager nm = foodMenuActivity.mNoticeManager;
            if (result == resources.getInteger(R.integer.file_write_error)) {
                foodMenuActivity.showToast("학생식당 식단표 파일을 저장하는데 실패하였습니다.");
                return;
            } else if (result == resources.getInteger(R.integer.network_error)) {
                foodMenuActivity.showToast("네트워크 오류로 인해 최신 학생식당 식단표 파일을 가져오는데 실패하였습니다.");
            }
            foodMenuActivity.setFoodMenuImage(FOOD_MENU_TYPE_STUDENT_REST, nm.getStudentRestFoodMenuFileName());
        }
    }

    private static class GetDormitoryRestMenuASyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<FoodMenuActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        GetDormitoryRestMenuASyncTask(Application application, FoodMenuActivity foodMenuActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(foodMenuActivity);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            FoodMenuActivity foodMenuActivity = activityReference.get();
            Resources resources = applicationWeakReference.get().getResources();
            if (foodMenuActivity == null || foodMenuActivity.isFinishing())
                return resources.getInteger(R.integer.unexpected_error);
            int result;
            int count = 0;
            NoticeManager nm = foodMenuActivity.mNoticeManager;
            while ((result = nm.pullDormitoryRestFoodMenu(applicationWeakReference.get())) != resources.getInteger(R.integer.no_error) && !isCancelled()) {
                if (result == resources.getInteger(R.integer.network_error)) {
                    sleep(1000);
                    count++;
                } else return result;
                if (count == resources.getInteger(R.integer.maximum_retry_num))
                    return resources.getInteger(R.integer.network_error);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            FoodMenuActivity foodMenuActivity = activityReference.get();
            Resources resources = applicationWeakReference.get().getResources();
            if (foodMenuActivity == null || foodMenuActivity.isFinishing()) return;
            NoticeManager nm = foodMenuActivity.mNoticeManager;
            if (result == resources.getInteger(R.integer.file_write_error)) {
                foodMenuActivity.showToast("기숙사 식단표 파일을 저장하는데 실패하였습니다.");
                return;
            } else if (result == resources.getInteger(R.integer.network_error)) {
                foodMenuActivity.showToast("네트워크 오류로 인해 최신 기숙사 식단표 파일을 가져오는데 실패하였습니다.");
            }
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
