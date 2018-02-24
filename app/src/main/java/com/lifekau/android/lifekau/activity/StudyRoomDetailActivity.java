package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LibraryManager;

import java.lang.ref.WeakReference;

import dmax.dialog.SpotsDialog;

public class StudyRoomDetailActivity extends AppCompatActivity {

    private static LibraryManager mLibraryManager = LibraryManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_room_detail);
        Button closeButton = (Button)findViewById(R.id.study_room_detail_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        StudyRoomDetailAsyncTask studyRoomDetailAsyncTask = new StudyRoomDetailAsyncTask(getApplication(), this);
        studyRoomDetailAsyncTask.execute(getIntent().getIntExtra("roomNum", 0));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private static class StudyRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<Application> applicationWeakReference;
        private WeakReference<StudyRoomDetailActivity> studyRoomDetailActivityWeakReference;

        public StudyRoomDetailAsyncTask(Application application, StudyRoomDetailActivity studyRoomDetailActivity){
            applicationWeakReference = new WeakReference<>(application);
            studyRoomDetailActivityWeakReference = new WeakReference<>(studyRoomDetailActivity);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            publishProgress();
            if (mLibraryManager.getStudyRoomDetailStatus(applicationWeakReference.get(), params[0]) != -1) return params[0];
            return -1;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            super.onProgressUpdate(params);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            StudyRoomDetailActivity studyRoomDetailActivity = studyRoomDetailActivityWeakReference.get();
            if(studyRoomDetailActivity == null || studyRoomDetailActivity.isFinishing()) return;
            if (result != -1) {
                for (int i = 6; i <= 23; i++) {
                    String textViewId = "study_room_detail_time_text_view_" + String.format("%02d", i - 5);
                    TextView textView = studyRoomDetailActivity.findViewById(applicationWeakReference.get().getResources().getIdentifier(textViewId, "id", "com.lifekau.android.lifekau"));
                    String showText = String.format("%02d:%02d ", i, 0) + (mLibraryManager.getStudyRoomDetailStatus(result, i) ? "이용 가능" : "이용 불가");
                    textView.setText(showText);
                }
            } else {
                //예외 처리
                studyRoomDetailActivity.showErrorMessage();
            }
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
