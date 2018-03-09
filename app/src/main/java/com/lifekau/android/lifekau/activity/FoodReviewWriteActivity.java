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
import com.lifekau.android.lifekau.model.FoodReview;

import java.util.Date;

public class FoodReviewWriteActivity extends AppCompatActivity implements TextWatcher, View.OnTouchListener {
    private static final String EXTRA_FOOD_CORNER_TYPE = "extra_corner_type";
    private static final String EXTRA_ALREADY_WRITTEN = "extra_already-written";

    private TextView mSubmitButton;
    private EditText mCommentEditText;
    private RatingBar mRatingBar;
    private int mFoodCornerType;
    private FirebaseDatabase mDatabase;
    private boolean mIsEditting;
    private ImageButton mBackButton;
    private boolean mPushed;
    private long mLastWritePostTime;

    public static Intent newIntent(Context packageContext, int foodCornerType, boolean alreadyWritten) {
        Intent intent = new Intent(packageContext, FoodReviewWriteActivity.class);
        intent.putExtra(EXTRA_FOOD_CORNER_TYPE, foodCornerType);
        intent.putExtra(EXTRA_ALREADY_WRITTEN, alreadyWritten);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_review_write);


        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().hide();
        }

        mDatabase = FirebaseDatabase.getInstance();
        mFoodCornerType = getIntent().getIntExtra(EXTRA_FOOD_CORNER_TYPE, 0);
        mIsEditting = getIntent().getBooleanExtra(EXTRA_ALREADY_WRITTEN, false);

        mBackButton = findViewById(R.id.write_food_review_back_image_button);
        mRatingBar = findViewById(R.id.food_review_write_rating_bar);
        mCommentEditText = findViewById(R.id.food_review_write_comment_edit_text);
        mSubmitButton = findViewById(R.id.write_food_review_submit_button);

        mCommentEditText.addTextChangedListener(this);
        mBackButton.setOnTouchListener(this);
        mSubmitButton.setOnTouchListener(this);

    }

    public void insertReviewToDB(int cornerType, float rating, String comment) {
        DatabaseReference ref = mDatabase.getReference()
                .child(getString(R.string.firebase_database_food_reviews))
                .child(String.format(getString(R.string.firebase_database_food_review_corner_id), mFoodCornerType))
                .child(LoginManager.get(this).getStudentId());
        FoodReview review = new FoodReview(cornerType, rating, comment);
        ref
                .setValue(review)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodReviewWriteActivity.this
                                , getString(R.string.review_write_success_message)
                                , Toast.LENGTH_SHORT)
                                .show();
                        FoodReviewWriteActivity.this.setResult(RESULT_OK);
                        FoodReviewWriteActivity.this.finish();
                    }
                });

    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int id = view.getId();
        int posX = (int) event.getRawX();
        int posY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!view.isFocusable()) break;
                mPushed = true;
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                break;
            case MotionEvent.ACTION_UP:
                if (!mPushed) break;
                if (id == R.id.write_food_review_back_image_button) {
                    if (!mIsEditting) {
                        if (getValidCharCount(mCommentEditText.getText().toString()) != 0) {
                            askDiscardTextOrNot();
                        } else {
                            setResult(RESULT_CANCELED, new Intent());
                            finish();
                        }
                    } else {
                        askCancelEditingOrNot();
                    }
                } else if (id == R.id.write_food_review_submit_button) {
                    if (false) {
                        new AlertDialog.Builder(this).setMessage("1분이 지나야 글을 쓸 수가 있습니다.").show();
                        return true;
                    }
                    mLastWritePostTime = new Date().getTime();
                    if (getValidCharCount(mCommentEditText.getText().toString()) != 0) {
                        insertReviewToDB(mFoodCornerType, mRatingBar.getRating(), mCommentEditText.getText().toString()); // 특정 코너
                    } else {
                        Snackbar.make(findViewById(R.id.food_review_write_linear_layout),
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

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        for (int i = editable.length(); i > 0; i--) {
            if (editable.subSequence(i - 1, i).toString().equals("\n")) {
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

        int limitTextCnt = getResources().getInteger(R.integer.food_review_write_text_limit);
        if (editable.length() > limitTextCnt) {
            String toastMsg = String.format(getString(R.string.text_limit_message), limitTextCnt);
            DialogMaker.showOkButtonDialog(this, toastMsg);
            editable.delete(limitTextCnt, editable.length());
            return;
        }
    }

    private int getValidCharCount(String str) {
        int ret = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch != ' ' && ch != '\n') ret++;
        }
        return ret;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (!mIsEditting) {
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
        builder.setMessage(getString(R.string.review_gone_warning_message)).setPositiveButton(getString(R.string.yes), dialogClickListener)
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

    private boolean isViewInBounds(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];

        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }
}
