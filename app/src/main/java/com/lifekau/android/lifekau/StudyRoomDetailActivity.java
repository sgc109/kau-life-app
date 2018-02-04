package com.lifekau.android.lifekau;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

public class StudyRoomDetailActivity extends AppCompatActivity {

    LibraryInfomation mLibraryInfomation;
    static SpotsDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_room_detail);
        mLibraryInfomation = new LibraryInfomation();
        mProgressDialog = new SpotsDialog(this, R.style.Custom);
        mProgressDialog.setCancelable(false);
        Intent intent = new Intent();
        int studyRoomNum = intent.getIntExtra("studyRoomNum", 1);
        StudyRoomDetailAsyncTask studyRoomDetailAsyncTask = new StudyRoomDetailAsyncTask();
        studyRoomDetailAsyncTask.execute(studyRoomNum);
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(mProgressDialog != null){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    protected  void onResume(){
        super.onResume();
        if(mProgressDialog == null){
            mProgressDialog = new SpotsDialog(this, R.style.Custom);
            mProgressDialog.setCancelable(false);
        }
    }

    private class StudyRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer>{
        @Override
        protected Integer doInBackground(Integer... params){
            publishProgress();
            if(mLibraryInfomation.getStudyRoomDetailStatus(params[0]) != -1) return params[0];
            return -1;
        }
        @Override
        protected void onProgressUpdate(Void... params){
            super.onProgressUpdate(params);
            mProgressDialog.show();
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result != -1) {
                for(int i = 1; i <= 18; i++) {
                    String textViewId = "study_room_detail_time_text_view_" + String.format("%02d", i);
                    TextView textView = (TextView) findViewById(getResources().getIdentifier(textViewId, "id", "com.lifekau.android.lifekau"));
                    String showText = String.format("%02d:%02d ", i + 5, 0) + (mLibraryInfomation.getStudyRoomDetailStatus(result, i) ? "이용 가능" : "이용 불가");
                    textView.setText(showText);
                }
            }
            else{
                //예외 처리
                showErrorMessage();
            }
            mProgressDialog.hide();
        }
    }

    public void showErrorMessage(){
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
