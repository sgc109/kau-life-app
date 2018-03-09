package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lifekau.android.lifekau.DialogMaker;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.LectureReview;

import java.util.Date;

public class LectureReviewWriteActivity extends AppCompatActivity implements TextWatcher, View.OnTouchListener{
    private static final String EXTRA_LECTURE_NAME = "extra_lecture_name";
    private static final String EXTRA_LECTURE_ALREADY_WRITTEN = "extra_already_written";
    private static final String SAVED_LECTURE_NAME = "saved_lecture_name";

    private TextView mSubmitButton;
    private EditText mCommentEditText;
    private RatingBar mRatingBar;
    private String mLectureName;
    FirebaseDatabase mDatabase;
    private boolean mIsEditing;
    private boolean mPushed;
    private long mLastWritePostTime;
    private ImageButton mBackButton;

    public static Intent newIntent(Context packageContext, String lectureName, boolean alreadyWritten){
        Intent intent = new Intent(packageContext, LectureReviewWriteActivity.class);
        intent.putExtra(EXTRA_LECTURE_NAME, lectureName);
        intent.putExtra(EXTRA_LECTURE_ALREADY_WRITTEN, alreadyWritten);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_write);

        if(savedInstanceState != null){
            mLectureName = savedInstanceState.getString(SAVED_LECTURE_NAME);
        }

        if(getSupportActionBar() != null ) {
            getSupportActionBar().hide();
        }

        mDatabase = FirebaseDatabase.getInstance();

        mLectureName = getIntent().getStringExtra(EXTRA_LECTURE_NAME);
        mIsEditing = getIntent().getBooleanExtra(EXTRA_LECTURE_ALREADY_WRITTEN, false);

        mRatingBar = findViewById(R.id.lecture_review_write_rating_bar);
        mCommentEditText = findViewById(R.id.lecture_review_write_comment_edit_text);
        mCommentEditText.addTextChangedListener(this);
        mBackButton = findViewById(R.id.write_lecture_review_back_image_button);
        mSubmitButton = findViewById(R.id.write_lecture_review_submit_button);

        mBackButton.setOnTouchListener(this);
        mSubmitButton.setOnTouchListener(this);
    }
    public void insertReviewToDB(float rating, String comment){
        DatabaseReference ref = mDatabase.getReference()
                .child(getString(R.string.firebase_database_lecture_reviews))
                .child(mLectureName)
                .child(LoginManager.get(this).getStudentId());
        LectureReview review = new LectureReview(rating, comment);
        ref
                .setValue(review)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(LectureReviewWriteActivity.this
                                , getString(R.string.review_write_success_message)
                                , Toast.LENGTH_SHORT)
                                .show();
                        LectureReviewWriteActivity.this.setResult(RESULT_OK);
                        LectureReviewWriteActivity.this.finish();
                    }
                });
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        for(int i = editable.length(); i > 0; i--) {
            if(editable.subSequence(i - 1, i).toString().equals("\n")) {
                editable.replace(i - 1, i, "");
            }
        }

        if (getValidCharCount(editable.toString()) == 0) {
            mSubmitButton.setTextColor(Color.GRAY);
            mSubmitButton.setFocusable(false);
        } else {
            mSubmitButton.setTextColor(Color.WHITE);
            mSubmitButton.setFocusable(true);
        }

        int limitTextCnt = getResources().getInteger(R.integer.lecture_review_write_text_limit);
        if(editable.length() > limitTextCnt) {
            String toastMsg = String.format(getString(R.string.text_limit_message), limitTextCnt);
            DialogMaker.showOkButtonDialog(this, toastMsg);
            editable.delete(limitTextCnt, editable.length());
            return;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (!mIsEditing) {
                int textLen = mCommentEditText.getText().length();
                if (textLen != 0) {
                    askDiscardTextOrNot();
                } else {
                    setResult(RESULT_CANCELED, new Intent());
                    finish();
                }
            } else {
                askCancelEditingOrNot();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void askDiscardTextOrNot() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.text_gone_warning_message)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }
    private void askCancelEditingOrNot() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.review_edit_cancel_alert_message)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int id = view.getId();
        int posX = (int) event.getRawX();
        int posY = (int) event.getRawY();
        int commentLength = getValidCharCount(mCommentEditText.getText().toString());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!view.isFocusable()) break;
                mPushed = true;
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                break;
            case MotionEvent.ACTION_UP:
                if (!mPushed) break;
                if (id == R.id.write_lecture_review_back_image_button) {
                    if (!mIsEditing) {
                        if (commentLength != 0) {
                            askDiscardTextOrNot();
                        } else {
                            setResult(RESULT_CANCELED, new Intent());
                            finish();
                        }
                    } else {
                        askCancelEditingOrNot();
                    }
                } else if (id == R.id.write_lecture_review_submit_button) {
                    if (false) {
                        new AlertDialog.Builder(this).setMessage("1분이 지나야 글을 쓸 수가 있습니다.").show();
                        return true;
                    }
                    mLastWritePostTime = new Date().getTime();
                    if(commentLength != 0) {
                        insertReviewToDB(mRatingBar.getRating(), mCommentEditText.getText().toString()); // 특정 코너
                    } else {
                        Snackbar.make(findViewById(R.id.lecture_review_write_linear_layout),
                                getString(R.string.please_write_review_alert_message),
                                Snackbar.LENGTH_SHORT).
                                show();
                    }
                }
                mPushed = false;
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isViewInBounds(view, posX, posY)) {
                    mPushed = false;
                    view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
        }
        return true;
    }

    private boolean isViewInBounds(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];

        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }

    private int getValidCharCount(String str) {
        int ret = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch != ' ' && ch != '\n') ret++;
        }
        return ret;
    }
}
