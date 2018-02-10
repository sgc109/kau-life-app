package com.lifekau.android.lifekau.activity;

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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LibraryManager;

import dmax.dialog.SpotsDialog;

public class ReadingRoomDetailActivity extends AppCompatActivity {

    private TextView mReadingRoomTitleTextView;
    private ConstraintLayout mReadingRoomDetailSeatLayout;
    private LibraryManager mLibraryManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_room_detail);
        mReadingRoomTitleTextView = findViewById(R.id.reading_room_detail_title);
        mReadingRoomDetailSeatLayout = findViewById(R.id.reading_room_detail_seat_layout);
        mLibraryManager = mLibraryManager.getInstance();
        Button closeButton = (Button)findViewById(R.id.reading_room_detail_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ReadingRoomDetailAsyncTask readingRoomDetailAsyncTask = new ReadingRoomDetailAsyncTask();
        readingRoomDetailAsyncTask.execute(getIntent().getIntExtra("roomNum", 0));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private class ReadingRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            publishProgress();
            if (mLibraryManager.getReadingRoomDetailStatus(getApplicationContext(), params[0]) != -1) return params[0];
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
                if (result == 0) {
                    mReadingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(0));
                    registerTextViewOnLayout(0, 1, 138);
                    registerTextViewOnLayout(0, 229, 240);
                }
                if (result == 1) {
                    mReadingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(1));
                    registerTextViewOnLayout(1, 139, 228);
                    registerTextViewOnLayout(1, 241, 252);
                }
                if (result == 2) {
                    mReadingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(2));
                    registerTextViewOnLayout(2, 1, 204);
                }
                if (result == 3) {
                    mReadingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(3));
                    registerTextViewOnLayout(3, 1, 204);
                }
                if (result == 4) {
                    mReadingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(4));
                    registerTextViewOnLayout(4, 1, 108);
                }
            } else {
                //예외 처리
                showErrorMessage();
            }
        }
    }

    public void registerTextViewOnLayout(int roomNum, int startSeatNum, int endSeatNum) {
        Bitmap emptySeatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_empty_seat);
        Bitmap usedSeatBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_used_seat);
        View view = getWindow().getDecorView();
        Point size = new Point();
        size.x = view.getWidth();
        size.y = view.getHeight();
        int bitmapSize = size.x / 20, textSize = size.x / 60;
        for (int i = startSeatNum; i <= endSeatNum; i++) {
            Point point = mLibraryManager.getReadingRoomSeatPoint(roomNum, i);
            ImageView imageView = new ImageView(this);
            imageView.setX((int) (size.x * point.x / 1200.0 + 0.5));
            imageView.setY((int) (size.y * point.y / 700.0 + 0.5));
            Bitmap bitmap = mLibraryManager.getReadingRoomDetailStatus(roomNum, i) ?
                    emptySeatBitmap.createScaledBitmap(emptySeatBitmap, bitmapSize, bitmapSize, true) :
                    usedSeatBitmap.createScaledBitmap(usedSeatBitmap, bitmapSize, bitmapSize, true);
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/SpoqaHanSansRegular.ttf");
            Paint paint = new Paint();
            paint.setTypeface(typeface);
            paint.setColor(Color.BLACK);
            paint.setTextSize(textSize);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawText(String.valueOf(i), bitmap.getWidth() / 2, bitmap.getHeight() / 2 + 5, paint);
            imageView.setImageBitmap(bitmap);
            mReadingRoomDetailSeatLayout.addView(imageView);
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
