package com.lifekau.android.lifekau;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

//TODO: 열람실, 스터디룸을 팝업창으로 보여줘야 한다.

public class LibraryInfomationActivity extends AppCompatActivity {

    final static int TOTAL_READING_ROOM_NUM = 5;
    final static int TOTAL_STDUY_ROOM_NUM = 6;
    static LibraryInfomation mLibraryInfomation;
    static SpotsDialog mProgressDialog;
    Button[] mReadingRoomButtonArray;
    Button[] mStudyRoomButtonArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_infomation);
        mLibraryInfomation = new LibraryInfomation();
        mReadingRoomButtonArray = new Button[TOTAL_READING_ROOM_NUM + 1];
        mStudyRoomButtonArray = new Button[TOTAL_STDUY_ROOM_NUM + 1];
        mProgressDialog = new SpotsDialog(this, R.style.Custom);
        mProgressDialog.setCancelable(false);
        for (int i = 1; i <= 5; i++) {
            final int readingRoomNum = i;
            String buttonId = "library_infomation_reading_room_0" + String.valueOf(i) + "_button";
            mReadingRoomButtonArray[i] = (Button) findViewById(getResources().getIdentifier(buttonId, "id", "com.lifekau.android.lifekau"));
            mReadingRoomButtonArray[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LibraryInfomationActivity.this, ReadingRoomDetailActivity.class);
                    intent.putExtra("readingRoomNum", readingRoomNum);
                    startActivity(intent);
                }
            });
        }
        for (int i = 1; i <= 6; i++) {
            final int studyRoomNum = i;
            String buttonId = "library_infomation_study_room_0" + String.valueOf(i) + "_button";
            mStudyRoomButtonArray[i] = (Button) findViewById(getResources().getIdentifier(buttonId, "id", "com.lifekau.android.lifekau"));
            mStudyRoomButtonArray[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(LibraryInfomationActivity.this, StudyRoomDetailActivity.class);
                    intent.putExtra("studyRoomNum", studyRoomNum);
                    startActivity(intent);
                }
            });
        }
        Button showReadingRoomButton = (Button) findViewById(R.id.library_infomation_show_reading_room_button);
        showReadingRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutVisibility(1);
            }
        });
        Button showStudyRoomButton = (Button) findViewById(R.id.library_infomation_show_study_room_button);
        showStudyRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLayoutVisibility(0);
            }
        });
        setLayoutVisibility(1);
        executeLibraryInfomationAsyncTask();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.actionbar_actions_refresh:
                executeLibraryInfomationAsyncTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mProgressDialog == null) {
            mProgressDialog = new SpotsDialog(this, R.style.Custom);
            mProgressDialog.setCancelable(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

    public void executeLibraryInfomationAsyncTask() {
        LibraryInfomationAsyncTask libraryInfomationAsyncTask = new LibraryInfomationAsyncTask();
        libraryInfomationAsyncTask.execute();
    }

    public void setLayoutVisibility(Integer index) {
        View selectReadingRoomLayout = findViewById(R.id.libarary_infomation_select_reading_room_linear_layout);
        View selectStudyRoomLayout = findViewById(R.id.libarary_infomation_select_study_room_linear_layout);
        selectReadingRoomLayout.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
        selectStudyRoomLayout.setVisibility(index == 1 ? View.GONE : View.VISIBLE);
    }

    private class LibraryInfomationAsyncTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            publishProgress();
            for(int i = 1; i <= 6; i++) {
                if (mLibraryInfomation.getStudyRoomDetailStatus(i) == -1) return -1;
            }
            if (mLibraryInfomation.getStudyRoomStatus() == -1) return -1;
            if (mLibraryInfomation.getReadingRoomStatus() == -1) return -1;
            return 0;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            super.onProgressUpdate(params);
            if (mProgressDialog != null) mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result != -1) {
                for (int i = 1; i <= 5; i++) mReadingRoomButtonArray[i].setText(mLibraryInfomation.getReadingRoomSummary(i));
                for (int i = 1; i <= 6; i++) mStudyRoomButtonArray[i].setText(mLibraryInfomation.getStudyRoomSummary(i));
            } else {
                //예외 처리
                showErrorMessage();
            }
            if (mProgressDialog != null) mProgressDialog.hide();
        }
    }
}
