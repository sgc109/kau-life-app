package com.lifekau.android.lifekau;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class LMSInfomationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    private class LMSConnectSession extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params){
//            if(.connectSession(params[0], params[1]) != -1) return 0;
//            return -1;
            return 0;
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
}
