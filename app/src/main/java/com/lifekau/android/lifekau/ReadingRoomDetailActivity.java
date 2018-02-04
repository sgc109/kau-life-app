package com.lifekau.android.lifekau;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import dmax.dialog.SpotsDialog;

public class ReadingRoomDetailActivity extends AppCompatActivity {

    static LibraryInfomation mLibraryInfomation = new LibraryInfomation();
    TextView mReadingRoomTitleTextView;
    ConstraintLayout mReadingRoomDetailSeatLayout;
    SwipeRefreshLayout mSwipeLayout;
    static SpotsDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_room_detail);
        mReadingRoomTitleTextView = (TextView)findViewById(R.id.reading_room_detail_title);
        mReadingRoomDetailSeatLayout = (ConstraintLayout)findViewById(R.id.reading_room_detail_seat_layout);
        mProgressDialog = new SpotsDialog(this, R.style.Custom);
        mProgressDialog.setCancelable(false);
        ReadingRoomDetailAsyncTask readingRoomDetailAsyncTask = new ReadingRoomDetailAsyncTask();
        final Intent intent = getIntent();
        readingRoomDetailAsyncTask.execute(intent.getIntExtra("readingRoomNum", 1));
        mSwipeLayout = (SwipeRefreshLayout)findViewById(R.id.reading_room_detail_swipe_layout);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ReadingRoomDetailAsyncTask readingRoomDetailAsyncTask = new ReadingRoomDetailAsyncTask();
                readingRoomDetailAsyncTask.execute(intent.getIntExtra("readingRoomNum", 1));
                mSwipeLayout.setRefreshing(false);
            }
        });
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

    private class ReadingRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            publishProgress();
            if (mLibraryInfomation.getReadingRoomDetailStatus(params[0]) != -1) return params[0];
            return -1;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            super.onProgressUpdate(params);
            if(mProgressDialog != null && !mProgressDialog.isShowing()) mProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result != -1) {
                if(result == 1){
                    mReadingRoomTitleTextView.setText(mLibraryInfomation.getReadingRoomName(1));
                    registerTextViewOnLayout(1, 1, 138);
                    registerTextViewOnLayout(1, 229, 240);
                }
                if(result == 2){
                    mReadingRoomTitleTextView.setText(mLibraryInfomation.getReadingRoomName(2));
                    registerTextViewOnLayout(2, 139, 228);
                    registerTextViewOnLayout(2, 241, 252);
                }
                if(result == 3){
                    mReadingRoomTitleTextView.setText(mLibraryInfomation.getReadingRoomName(3));
                    registerTextViewOnLayout(3, 1, 204);
                }
                if(result == 4){
                    mReadingRoomTitleTextView.setText(mLibraryInfomation.getReadingRoomName(4));
                    registerTextViewOnLayout(4, 1, 204);
                }
                if(result == 5){
                    mReadingRoomTitleTextView.setText(mLibraryInfomation.getReadingRoomName(5));
                    registerTextViewOnLayout(5, 1, 108);
                }
            } else {
                //예외 처리
                showErrorMessage();
            }
            if(mProgressDialog != null && mProgressDialog.isShowing()) mProgressDialog.hide();
        }

    }

    public void registerTextViewOnLayout(Integer roomNum, Integer startSeatNum, Integer endSeatNum){
        Bitmap emptySeatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty_seat);
        Bitmap usedSeatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_used_seat);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        for(int i = startSeatNum; i <= endSeatNum; i++){
            Point point = mLibraryInfomation.getReadingRoomSeatPoint(roomNum, i);
            ImageView imageView = new ImageView(this);
            imageView.setX((int)(size.x * point.x / 1000.0 + 0.5));
            imageView.setY((int)(size.y * point.y / 600.0 + 0.5));
            Bitmap bitmap = mLibraryInfomation.getReadingRoomDetailStatus(roomNum, i) ?
                    emptySeatBitmap.createScaledBitmap(emptySeatBitmap, 60, 60, true) :
                    usedSeatBitmap.createScaledBitmap(usedSeatBitmap, 60, 60, true);
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/SpoqaHanSansRegular.ttf");
            Paint paint = new Paint();
            paint.setTypeface(typeface);
            paint.setColor(Color.BLACK);
            paint.setTextSize(18);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(String.valueOf(i),bitmap.getWidth() / 2 , bitmap.getHeight() / 2 + 5, paint);
            imageView.setImageBitmap(bitmap);
            mReadingRoomDetailSeatLayout.addView(imageView);
        }
    }

    public void showErrorMessage(){
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
