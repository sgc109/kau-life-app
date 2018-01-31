package com.lifekau.android.lifekau;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class LibraryInfomationActivity extends AppCompatActivity {

    static LibraryInfomation libInfo = new LibraryInfomation();
//    static LMSInfomation lmsInfomation = new LMSInfomation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_infomation);
        ReadingRoomStatusAsyncTask readingRoomStatusAsyncTask = new ReadingRoomStatusAsyncTask();
        readingRoomStatusAsyncTask.execute();
//        LMSConnectSession lmsConnectSession = new LMSConnectSession();
//        lmsConnectSession.execute("id", "password");
    }

    private class ReadingRoomStatusAsyncTask extends AsyncTask<Void, Void, Integer>{
        @Override
        protected Integer doInBackground(Void... params){
            if(libInfo.getReadingRoomStatus() != -1) return 0;
            return -1;
        }
        @Override
        protected void onProgressUpdate(Void... value){
            super.onProgressUpdate(value);
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result != -1) {
                for(int i = 1; i <= 5; i++) {
                    Button btn = (Button)findViewById(getResources().getIdentifier("readingRoom" + String.valueOf(i) + "_btn", "id", "com.lifekau.android.lifekau"));
                    btn.setText(libInfo.getReadingRoomName(i) + "(" + String.valueOf(libInfo.getReadingRoomAvailableSeat(i)) + " / " + String.valueOf(libInfo.getReadingRoomTotalSeat(i)) + ")");
                }
            }
            else{
                //예외 처리
            }
        }
    }

    private class ReadingRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer>{
        @Override
        protected Integer doInBackground(Integer... params){
            if(libInfo.getReadingRoomDetailStatus(params[0]) != -1) return 0;
            return -1;
        }
        @Override
        protected void onProgressUpdate(Void... params){
            super.onProgressUpdate(params);
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result != -1) {
            }
            else{
                //예외 처리
            }
        }
    }

    private class StudyRoomAsyncTask extends AsyncTask<Void, Void, Integer>{
        @Override
        protected Integer doInBackground(Void... params){
            if(libInfo.getStudyRoomStatus() != -1) return 0;
            return -1;
        }
        @Override
        protected void onProgressUpdate(Void... params){
            super.onProgressUpdate(params);
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result != -1) {
            }
            else{
                //예외 처리
            }
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
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if(result != -1) {
            }
            else{
                //예외 처리
            }
        }
    }

//    private class LMSConnectSession extends AsyncTask<String, Void, Integer>{
//        @Override
//        protected Integer doInBackground(String... params){
//            if(lmsInfomation.connectSession(params[0], params[1]) != -1) return 0;
//            return -1;
//        }
//        @Override
//        protected void onProgressUpdate(Void... params){
//            super.onProgressUpdate(params);
//        }
//        @Override
//        protected void onPostExecute(Integer result){
//            super.onPostExecute(result);
//            if(result != -1) {
//            }
//            else{
//                //예외 처리
//            }
//        }
//    }
}