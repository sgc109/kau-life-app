package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.media.Rating;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FoodReviewWriteActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, TextWatcher{
    private final String SAVED_RATING = "saved_rating";
    private final String SAVED_FOOD_CORNER_TYPE = "saved_food_corner_type";
    private final String SAVED_COMMENT = "saved_comment";

    private Button mSubmitButton;
    private Button mCancelButton;
    private EditText mCommentEditText;
    private Spinner mFoodCornerSpinner;
    private RatingBar mRatingBar;

    public static Intent newIntent(Context packageContext){
        Intent intent = new Intent(packageContext, FoodReviewWriteActivity.class);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_review_write);

        mFoodCornerSpinner = (Spinner)findViewById(R.id.food_review_write_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.food_corner_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFoodCornerSpinner.setAdapter(adapter);

        mRatingBar = (RatingBar)findViewById(R.id.food_review_write_rating_bar);
        mCommentEditText = (EditText)findViewById(R.id.food_review_write_comment_edit_text);
        mCommentEditText.addTextChangedListener(this);
        mCancelButton = (Button)findViewById(R.id.food_review_write_cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCommentEditText.getText().toString().equals("")){
                    finish();
                } else {
                    // 작성 중인 거 다 날릴 건지 체크
                }
            }
        });
        mSubmitButton = (Button)findViewById(R.id.food_review_write_submit_button);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertReviewToDB(mRatingBar.getRating(), mFoodCornerSpinner.getSelectedItemPosition(), mCommentEditText.getText().toString()); // 특정 코너
                finish();
            }
        });

        if(savedInstanceState != null){
            mRatingBar.setRating(savedInstanceState.getFloat(SAVED_RATING));
            mFoodCornerSpinner.setSelection(savedInstanceState.getInt(SAVED_FOOD_CORNER_TYPE));
            mCommentEditText.setText(savedInstanceState.getString(SAVED_COMMENT));
        }
    }
    public void insertReviewToDB(float rating, int cornerId, String comment){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(getString(R.string.firebase_database_food_reviews));
        FoodReview review = new FoodReview(rating, cornerId, comment);
        ref.child(String.format(getString(R.string.firebase_database_food_review_corner_id), cornerId + 1))
                .push()
                .setValue(review);
        ref.child(getString(R.string.firebase_database_food_review_corner_all))
                .push()
                .setValue(review);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(SAVED_RATING, mRatingBar.getRating());
        outState.putInt(SAVED_FOOD_CORNER_TYPE, mFoodCornerSpinner.getSelectedItemPosition());
        outState.putString(SAVED_COMMENT, mCommentEditText.getText().toString());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String text = charSequence.toString();
        int limitTextCnt = getResources().getInteger(R.integer.food_review_write_text_limit);
        if(text.length() == limitTextCnt) {
            String toastMsg = String.format(getString(R.string.food_review_text_limit_message), limitTextCnt);
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void afterTextChanged(Editable str) {
        for(int i = str.length(); i > 0; i--) {
            if(str.subSequence(i - 1, i).toString().equals("\n")) {
                str.replace(i - 1, i, "");
            }
        }
    }
}
