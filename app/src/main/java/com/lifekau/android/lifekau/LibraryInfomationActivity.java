package com.lifekau.android.lifekau;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

//TODO: 열람실 상세정보를 보여주어야 한다. (팝업 사용)
//TODO: 스터디룸 상세정보를 보여주어야 한다. (팝업 사용)

public class LibraryInfomationActivity extends AppCompatActivity {

    static LibraryInfomation libInfo = new LibraryInfomation();
    Button []btn = new Button[6];
    SpotsDialog progressDialog;
    SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_infomation);
        for(int i = 1; i <= 5; i++){
            final int readingRoomNum = i;
            btn[i] = (Button)findViewById(getResources().getIdentifier("reading_room_0" + String.valueOf(i) + "_button", "id", "com.lifekau.android.lifekau"));
            btn[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    Intent intent = new Intent(LibraryInfomationActivity.this, ReadingRoomDetailActivity.class);
                    intent.putExtra("readingRoomNum", readingRoomNum);
                    startActivity(intent);
                }
            });
        }
        progressDialog = new SpotsDialog(this, R.style.Custom);
        ReadingRoomStatusAsyncTask readingRoomStatusAsyncTask = new ReadingRoomStatusAsyncTask();
        readingRoomStatusAsyncTask.execute();
        swipeLayout = (SwipeRefreshLayout)findViewById(R.id.library_infomation_swipe_layout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ReadingRoomStatusAsyncTask readingRoomStatusAsyncTask = new ReadingRoomStatusAsyncTask();
                readingRoomStatusAsyncTask.execute();
                swipeLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();
        if(progressDialog == null){
            progressDialog = new SpotsDialog(this, R.style.Custom);
        }
    }

    private class ReadingRoomStatusAsyncTask extends AsyncTask<Void, Void, Integer>{
        @Override
        protected Integer doInBackground(Void... params){
            publishProgress();
            if(libInfo.getReadingRoomStatus() != -1) return 0;
            return -1;
        }
        @Override
        protected void onProgressUpdate(Void... value){
            super.onProgressUpdate(value);
            progressDialog.show();
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result != -1) {
                for(int i = 1; i <= 5; i++) {
                    btn[i].setText(libInfo.getReadingRoomSummary(i));
                }
            }
            else{
                //예외 처리
                showErrorMessage();
            }
            progressDialog.hide();
        }
    }

    private class StudyRoomAsyncTask extends AsyncTask<Void, Void, Integer>{
        @Override
        protected Integer doInBackground(Void... params){
            publishProgress();
            if(libInfo.getStudyRoomStatus() != -1) return 0;
            return -1;
        }
        @Override
        protected void onProgressUpdate(Void... params){
            super.onProgressUpdate(params);
            progressDialog.show();
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result != -1) {
            }
            else{
                //예외 처리
                showErrorMessage();
            }
            progressDialog.hide();
        }
    }

    private class StudyRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer>{
        @Override
        protected Integer doInBackground(Integer... params){
            if(libInfo.getStudyRoomDetailStatus(params[0]) != -1) return 0;
            return -1;
        }
        @Override
        protected void onProgressUpdate(Void... params){
            super.onProgressUpdate(params);
            progressDialog.show();
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result != -1) {
            }
            else{
                //예외 처리
                showErrorMessage();
            }
            progressDialog.hide();
        }
    }

    public void showErrorMessage(){
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}