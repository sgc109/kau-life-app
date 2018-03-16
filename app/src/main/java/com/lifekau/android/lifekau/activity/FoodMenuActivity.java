package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
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

    private NoticeManager mNoticeManager = NoticeManager.getInstance();
    private WebView mFoodMenuWebView;
    private Button mShowStudentRestFoodMenuButton;
    private Button mShowDormitoryRestFoodMenuButton;

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

        mFoodMenuWebView = findViewById(R.id.activity_food_menu_web_view);
        mFoodMenuWebView.setInitialScale(1);
        mFoodMenuWebView.getSettings().setLoadWithOverviewMode(true);
        mFoodMenuWebView.getSettings().setUseWideViewPort(true);
        mFoodMenuWebView.getSettings().setSupportZoom(true);
        mFoodMenuWebView.getSettings().setBuiltInZoomControls(true);
        mFoodMenuWebView.getSettings().setDisplayZoomControls(false);
        mShowStudentRestFoodMenuButton = findViewById(R.id.activity_food_menu_show_student_restaurant_button);
        mShowStudentRestFoodMenuButton.setOnClickListener(this);
        mShowDormitoryRestFoodMenuButton = findViewById(R.id.activity_food_menu_dormitory_restaurant_button);
        mShowDormitoryRestFoodMenuButton.setOnClickListener(this);
        executeGetStudentRestFoodMenu();
        executeGetDormitoryRestFoodMenu();
        showToast("최신 식단표를 처음 불러오는 경우 시간이 오래 소요됩니다.");
    }

    void executeGetStudentRestFoodMenu(){
        GetStudentRestMenuASyncTask aSyncTask = new GetStudentRestMenuASyncTask(getApplication(), this);
        aSyncTask.execute();
    }

    void executeGetDormitoryRestFoodMenu(){
        GetDormitoryRestMenuASyncTask aSyncTask = new GetDormitoryRestMenuASyncTask(getApplication(), this);
        aSyncTask.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_food_menu_show_student_restaurant_button:
                setFoodMenuImage(mNoticeManager.getStudentRestFoodMenuFileName());
                break;
            case R.id.activity_food_menu_dormitory_restaurant_button:
                setFoodMenuImage(mNoticeManager.getDormitoryRestFileName());
                break;
        }
    }

    public void setFoodMenuImage(Set<String> fileNames){
        String[] fileNameArray = fileNames.toArray(new String[fileNames.size()]);
        Arrays.sort(fileNameArray);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html><table>");
        stringBuilder.append("<head><style>img{max-width: 100%; width:auto; height: auto;}</style></head>");
        for (String fileName : fileNameArray) {
            stringBuilder.append("<tr><td><img src= 'file://");
            stringBuilder.append(getFilesDir().getAbsolutePath());
            stringBuilder.append("/");
            stringBuilder.append(fileName);
            stringBuilder.append("'/></td></tr>");
        }
        stringBuilder.append("</table></html>");
        mFoodMenuWebView.loadDataWithBaseURL("",
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
            foodMenuActivity.setFoodMenuImage(nm.getStudentRestFoodMenuFileName());
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
