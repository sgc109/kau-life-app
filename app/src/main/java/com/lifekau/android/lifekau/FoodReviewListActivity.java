package com.lifekau.android.lifekau;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgc109 on 2018-01-27.
 */

public class FoodReviewListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private final static String EXTRA_RESTAURANT_TYPE = "extra.restaurant_type";
//    private final String SAVED_CORNER_TYPE_FILTER = "saved_corner_type_filter";
//    private final String SAVED_ORDERED_BY_RATING = "saved_order_by_rating";
//    private final String SAVED_ORDERED_BY_TIME = "saved_order_by_time";
    public final static int RESTAURENT_TYPE_STUDENT = 0;
    public  final static int RESTAURENT_TYPE_DORM = 1;

    private Spinner mFilterByFoodCornerSpinner;
    private Button mOrderByTimeButton;
    private Button mOrderByRatingButton;
    private RecyclerView mRecyclerView;
//    private boolean orderedByRating;
//    private boolean orderedByTime;

    public static Intent newIntent(Context packageContext, int RestaurantType){
        Intent intent = new Intent(packageContext, FoodReviewListActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_TYPE, RestaurantType);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_review_list);

        if(savedInstanceState != null){

        }
        mFilterByFoodCornerSpinner = (Spinner)findViewById(R.id.food_review_list_spinner);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.food_corner_list, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFilterByFoodCornerSpinner.setAdapter(arrayAdapter);
        mFilterByFoodCornerSpinner.setOnItemSelectedListener(this);

        mRecyclerView = (RecyclerView)findViewById(R.id.food_review_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("food_reviews");

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("food_reviews");
        FirebaseRecyclerOptions<FoodReview> options =
                new FirebaseRecyclerOptions.Builder<FoodReview>()
                .setQuery(query, FoodReview.class)
                .build();
        FirebaseRecyclerAdapter recyclerAdapter = new FirebaseRecyclerAdapter<FoodReview, FoodReviewHolder>(options) {
            @Override
            public FoodReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_food_review, parent, false);
                return new FoodReviewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FoodReviewHolder holder, int position, @NonNull FoodReview foodReview) {
                holder.bindFoodReview(foodReview);
            }
        };

        mRecyclerView.setAdapter(recyclerAdapter);
    }

    public void onClickFab(View view){
        writeReview();
    }

    public void writeReview(){
        Intent intent = FoodReviewWriteActivity.newIntent(this);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.right_to_left_slide_in, R.anim.right_to_left_slide_out);
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
