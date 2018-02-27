package com.lifekau.android.lifekau.activity;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
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

import java.lang.ref.WeakReference;

import dmax.dialog.SpotsDialog;

public class ReadingRoomDetailActivity extends AppCompatActivity {

    private static LibraryManager mLibraryManager = LibraryManager.getInstance();

    private TextView mReadingRoomTitleTextView;
    private ConstraintLayout mReadingRoomDetailSeatLayout;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ReadingRoomDetailActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_room_detail);
        mReadingRoomTitleTextView = findViewById(R.id.reading_room_detail_title);
        mReadingRoomDetailSeatLayout = findViewById(R.id.reading_room_detail_seat_layout);
        Button closeButton = (Button) findViewById(R.id.reading_room_detail_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ReadingRoomDetailAsyncTask readingRoomDetailAsyncTask = new ReadingRoomDetailAsyncTask(getApplication(), this);
        readingRoomDetailAsyncTask.execute(getIntent().getIntExtra("roomNum", 0));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private static class ReadingRoomDetailAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<ReadingRoomDetailActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        ReadingRoomDetailAsyncTask(Application application, ReadingRoomDetailActivity readingRoomDetailActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(readingRoomDetailActivity);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            publishProgress();
            if (mLibraryManager.getReadingRoomDetailStatus(applicationWeakReference.get(), params[0]) != -1)
                return params[0];
            return -1;
        }

        @Override
        protected void onProgressUpdate(Void... params) {
            super.onProgressUpdate(params);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            ReadingRoomDetailActivity readingRoomDetailActivity = activityReference.get();
            if (readingRoomDetailActivity == null || readingRoomDetailActivity.isFinishing())
                return;
            TextView readingRoomTitleTextView = readingRoomDetailActivity.findViewById(R.id.reading_room_detail_title);
            if (result != -1) {
                if (result == 0) {
                    readingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(0));
                    readingRoomDetailActivity.registerTextViewOnLayout(0, 1, 138);
                    readingRoomDetailActivity.registerTextViewOnLayout(0, 229, 240);
                }
                if (result == 1) {
                    readingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(1));
                    readingRoomDetailActivity.registerTextViewOnLayout(1, 139, 228);
                    readingRoomDetailActivity.registerTextViewOnLayout(1, 241, 252);
                }
                if (result == 2) {
                    readingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(2));
                    readingRoomDetailActivity.registerTextViewOnLayout(2, 1, 204);
                }
                if (result == 3) {
                    readingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(3));
                    readingRoomDetailActivity.registerTextViewOnLayout(3, 1, 204);
                }
                if (result == 4) {
                    readingRoomTitleTextView.setText(mLibraryManager.getReadingRoomName(4));
                    readingRoomDetailActivity.registerTextViewOnLayout(4, 1, 108);
                }
            } else {
                //예외 처리
                readingRoomDetailActivity.showErrorMessage();
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
        int bitmapSize = size.x / 15, textSize = size.x / 50;
        for (int i = startSeatNum; i <= endSeatNum; i++) {
            Point point = mLibraryManager.getReadingRoomSeatPoint(roomNum, i);
            ImageView imageView = new ImageView(this);
            imageView.setX((int) (size.x * point.x / 1100.0 + 0.5));
            imageView.setY((int) (size.y * point.y / 650.0 + 0.5));
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
