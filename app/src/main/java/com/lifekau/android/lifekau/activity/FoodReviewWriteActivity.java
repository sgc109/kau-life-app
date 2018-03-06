package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lifekau.android.lifekau.DialogMaker;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.FoodReview;

public class FoodReviewWriteActivity extends AppCompatActivity implements TextWatcher, View.OnClickListener {
    private static final String EXTRA_FOOD_CORNER_TYPE = "extra_corner_type";
    private static final String EXTRA_ALREADY_WRITTEN = "extra_already-written";

    private Button mSubmitButton;
    private Button mCancelButton;
    private EditText mCommentEditText;
    private RatingBar mRatingBar;
    private int mFoodCornerType;
    private FirebaseDatabase mDatabase;
    private boolean mAlreadyWritten;

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
        mAlreadyWritten = getIntent().getBooleanExtra(EXTRA_ALREADY_WRITTEN, false);

        mRatingBar = findViewById(R.id.food_review_write_rating_bar);
        mCommentEditText = findViewById(R.id.food_review_write_comment_edit_text);
        mCommentEditText.addTextChangedListener(this);
        mCancelButton = findViewById(R.id.food_review_write_cancel_button);
        mCancelButton.setOnClickListener(this);
        mSubmitButton = findViewById(R.id.food_review_write_submit_button);
        mSubmitButton.setOnClickListener(this);
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

        int limitTextCnt = getResources().getInteger(R.integer.food_review_write_text_limit);
        if (editable.length() > limitTextCnt) {
            String toastMsg = String.format(getString(R.string.text_limit_message), limitTextCnt);
            DialogMaker.showOkButtonDialog(this, toastMsg);
            editable.delete(limitTextCnt, editable.length());
            return;
        }
    }

    @Override
    public void onClick(View view) {
        int commentLength = mCommentEditText.getText().toString().length();
        switch (view.getId()) {
            case R.id.food_review_write_cancel_button:
                if (!mAlreadyWritten) {
                    if (commentLength != 0) {
                        askDiscardTextOrNot();
                    } else {
                        setResult(RESULT_CANCELED, new Intent());
                        finish();
                    }
                } else {
                    askCancelEditingOrNot();
                }
                break;
            case R.id.food_review_write_submit_button:
                if (commentLength != 0) {
                    insertReviewToDB(mFoodCornerType, mRatingBar.getRating(), mCommentEditText.getText().toString()); // 특정 코너
                } else {
                    Snackbar.make(findViewById(R.id.food_review_write_linear_layout),
                            getString(R.string.please_write_review_alert_message),
                            Snackbar.LENGTH_SHORT).
                            show();
                }
                break;
            default:
                return;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (!mAlreadyWritten) {
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
}
