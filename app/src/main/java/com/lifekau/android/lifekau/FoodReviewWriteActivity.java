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

public class FoodReviewWriteActivity extends AppCompatActivity implements TextWatcher{
    private static final String EXTRA_FOOD_CORNER_TYPE = "extra_corner_type";

    private Button mSubmitButton;
    private Button mCancelButton;
    private EditText mCommentEditText;
    private RatingBar mRatingBar;
    private int mFoodCornerType;

    public static Intent newIntent(Context packageContext, int foodCornerType){
        Intent intent = new Intent(packageContext, FoodReviewWriteActivity.class);
        intent.putExtra(EXTRA_FOOD_CORNER_TYPE, foodCornerType);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_review_write);

        mFoodCornerType = getIntent().getIntExtra(EXTRA_FOOD_CORNER_TYPE, 0);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.food_corner_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mRatingBar = (RatingBar)findViewById(R.id.food_review_write_rating_bar);
        mCommentEditText = (EditText)findViewById(R.id.food_review_write_comment_edit_text);
        mCommentEditText.addTextChangedListener(this);
        mCancelButton = (Button)findViewById(R.id.food_review_write_cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCommentEditText.getText().toString().equals("")){
                    setResult(RESULT_CANCELED, new Intent());
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
                insertReviewToDB(mRatingBar.getRating(), mCommentEditText.getText().toString()); // 특정 코너
                Intent intent = new Intent();
                // intent.putExtra
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
    public void insertReviewToDB(float rating, String comment){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(getString(R.string.firebase_database_food_reviews));
        FoodReview review = new FoodReview(rating, comment);
        ref.child(String.format(getString(R.string.firebase_database_food_review_corner_id), mFoodCornerType))
                .push()
                .setValue(review);
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
