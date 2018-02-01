package com.lifekau.android.lifekau;

import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    static LibraryInfomation libInfo = new LibraryInfomation();
    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_room_detail);
        layout = (ConstraintLayout)findViewById(R.id.reading_room_detail_layout);
        ReadingRoomDetailAsyncTask readingRoomDetailAsyncTask = new ReadingRoomDetailAsyncTask();
        readingRoomDetailAsyncTask.execute(2);
//        Intent intent = new Intent(MainActivity.this, LibraryInfomationActivity.class);
//        startActivity(intent);
//        finish();
    }

    private class ReadingRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            if (libInfo.getReadingRoomDetailStatus(params[0]) != -1) return params[0];
            return -1;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            super.onProgressUpdate(params);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result != -1) {
                if(result == 1){
                    registerTextViewOnLayout(1, 1, 138);
                    registerTextViewOnLayout(1, 229, 240);
                }
                if(result == 2){
                    registerTextViewOnLayout(2, 139, 228);
                    registerTextViewOnLayout(2, 241, 252);
                }
                if(result == 3) registerTextViewOnLayout(3, 1, 204);
                if(result == 4) registerTextViewOnLayout(4, 1, 204);
                if(result == 5) registerTextViewOnLayout(5, 1, 108);
            } else {
                //예외 처리
            }
        }
    }

    public void registerTextViewOnLayout(Integer roomNum, Integer seatStartNum, Integer seatEndNum){
        for(int i = seatStartNum; i <= seatEndNum; i++){
            Point point = libInfo.getReadingRoomSeatPoint(roomNum, i);
            TextView textView = new TextView(getApplicationContext());
            textView.setText(String.valueOf(i));
            textView.setWidth(60);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);
            textView.setX(point.x);
            textView.setY(point.y);
            layout.addView(textView);
        }
    }
}
