package com.lifekau.android.lifekau;

import android.support.v4.app.ActivityOptionsCompat;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

/**
 * Created by sgc109 on 2018-01-27.
 */

public class FoodReviewListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private final static String EXTRA_RESTAURANT_TYPE = "extra.restaurant_type";
    public final static int RESTAURENT_TYPE_STUDENT = 0;
    public  final static int RESTAURENT_TYPE_DORM = 1;

    private Spinner mFilterByFoodCornerSpinner;
    private Button mOrderByTimeButton;
    private Button mOrderByRatingButton;
//    private FloatingActionButton mWriteReviewFloatingActionButton;
    private RecyclerView mRecyclerView;

    public static Intent newIntent(Context packageContext, int RestaurantType){
        Intent intent = new Intent(packageContext, FoodReviewListActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_TYPE, RestaurantType);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_review_list);

        mRecyclerView = (RecyclerView)findViewById(R.id.food_review_recycler_view);
    }

    public void onClickFab(View view){
        writeReview();
    }

    public void writeReview(){
        Intent intent = FoodReviewWriteActivity.newIntent(this);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.right_to_left_slide_in, R.anim.right_to_left_slide_out);
        startActivity(intent, options.toBundle());
//        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // 학식 코너 선택시 필터링
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
