package com.lifekau.android.lifekau;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

public class ReadingRoomDetailActivity extends AppCompatActivity {

    static LibraryInfomation libInfo = new LibraryInfomation();
    TextView readingRoomTitleTextView;
    ConstraintLayout layout;
    SpotsDialog progressDialog;
    SwipeRefreshLayout swipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_room_detail);
        readingRoomTitleTextView = (TextView)findViewById(R.id.reading_room_detail_title);
        layout = (ConstraintLayout)findViewById(R.id.reading_room_detail_seat_layout);
        progressDialog = new SpotsDialog(this, R.style.Custom);
        ReadingRoomDetailAsyncTask readingRoomDetailAsyncTask = new ReadingRoomDetailAsyncTask();
        final Intent intent = getIntent();
        readingRoomDetailAsyncTask.execute(intent.getIntExtra("readingRoomNum", 1));
        swipeLayout = (SwipeRefreshLayout)findViewById(R.id.reading_room_detail_swipe_layout);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ReadingRoomDetailAsyncTask readingRoomDetailAsyncTask = new ReadingRoomDetailAsyncTask();
                readingRoomDetailAsyncTask.execute(intent.getIntExtra("readingRoomNum", 1));
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

    private class ReadingRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            publishProgress();
            if (libInfo.getReadingRoomDetailStatus(params[0]) != -1) return params[0];
            return -1;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            super.onProgressUpdate(params);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result != -1) {
                if(result == 1){
                    readingRoomTitleTextView.setText(libInfo.getReadingRoomName(1));
                    registerTextViewOnLayout(1, 1, 138);
                    registerTextViewOnLayout(1, 229, 240);
                }
                if(result == 2){
                    readingRoomTitleTextView.setText(libInfo.getReadingRoomName(2));
                    registerTextViewOnLayout(2, 139, 228);
                    registerTextViewOnLayout(2, 241, 252);
                }
                if(result == 3){
                    readingRoomTitleTextView.setText(libInfo.getReadingRoomName(3));
                    registerTextViewOnLayout(3, 1, 204);
                }
                if(result == 4){
                    readingRoomTitleTextView.setText(libInfo.getReadingRoomName(4));
                    registerTextViewOnLayout(4, 1, 204);
                }
                if(result == 5){
                    readingRoomTitleTextView.setText(libInfo.getReadingRoomName(5));
                    registerTextViewOnLayout(5, 1, 108);
                }
            } else {
                //예외 처리
                showErrorMessage();
            }
            progressDialog.hide();
        }

    }

    public void registerTextViewOnLayout(Integer roomNum, Integer seatStartNum, Integer seatEndNum){
        Bitmap emptySeatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty_seat);
        Bitmap usedSeatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_used_seat);
        for(int i = seatStartNum; i <= seatEndNum; i++){
            Point point = libInfo.getReadingRoomSeatPoint(roomNum, i);
            ImageView imageView = new ImageView(getApplicationContext());
            imageView.setX(point.x - 10);
            imageView.setY(point.y);
            Bitmap bitmap = libInfo.getReadingRoomDetailStatus(roomNum, i) ?
                    emptySeatBitmap.createScaledBitmap(emptySeatBitmap, 60, 60, true) :
                    usedSeatBitmap.createScaledBitmap(usedSeatBitmap, 60, 60, true);
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            paint.setColor(Color.BLACK);
            paint.setTextSize(18);
            paint.setTextAlign(Paint.Align.CENTER);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(String.valueOf(i),bitmap.getWidth() / 2 , bitmap.getHeight() / 2, paint);
            imageView.setImageBitmap(bitmap);
            layout.addView(imageView);
        }
    }

    public void showErrorMessage(){
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
