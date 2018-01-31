package com.lifekau.android.lifekau;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import butterknife.BindView;

/**
 * Created by sgc109 on 2018-01-27.
 */

public class FoodReviewListActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private final static String EXTRA_RESTAURANT_TYPE = "extra.restaurant_type";
    private final String SAVED_ORDERED_BY_RATING_ASC = "saved_order_by_rating_asc";
    private final String SAVED_ORDERED_BY_TIME_ASC = "saved_order_by_time_asc";
    private final String SAVED_FILTERED_CORNER_TYPE = "saved_filtered_corner_type";
    public final static int RESTAURENT_TYPE_STUDENT = 0;
    public final static int RESTAURENT_TYPE_DORM = 1;

    private Spinner mFilterByFoodCornerSpinner;
    private Button mOrderByTimeButton;
    private Button mOrderByRatingButton;
    private RecyclerView mRecyclerView;
    private TextView mEmptyListMessage;
    private LinearLayout mProgressBar;
    private ActionBar mActionBar;
    private int mFilteredCornerType;
    private int mOrderedByRatingAsc; // -1 or 0 or 1
    private int mOrderedByTimeAsc; // -1 or 0 or 1
    private FirebaseRecyclerAdapter mRecyclerAdapter;

    public static Intent newIntent(Context packageContext, int RestaurantType) {
        Intent intent = new Intent(packageContext, FoodReviewListActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_TYPE, RestaurantType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_review_list);

        mActionBar = ((AppCompatActivity)this).getSupportActionBar();
//        mActionBar.setTitle(Html.fromHtml("<font color='#ffffff'>" + getString(R.string.food_review_title) + "</font>"));
        mActionBar.setTitle(R.string.food_review_title);
//        mActionBar.sz


        if (savedInstanceState != null) {
            mFilteredCornerType = savedInstanceState.getInt(SAVED_FILTERED_CORNER_TYPE);
            mOrderedByTimeAsc = savedInstanceState.getInt(SAVED_ORDERED_BY_TIME_ASC);
            mOrderedByRatingAsc = savedInstanceState.getInt(SAVED_ORDERED_BY_RATING_ASC);
        }

        if(mOrderedByRatingAsc == 0 && mOrderedByTimeAsc == 0) {
            mOrderedByTimeAsc = -1;
        }

        mProgressBar = (LinearLayout) findViewById(R.id.indeterminateBar);
        mEmptyListMessage = (TextView) findViewById(R.id.food_review_list_empty_list_text_view);
        mOrderByRatingButton = (Button)findViewById(R.id.order_by_rating_button);
        mOrderByRatingButton.setOnClickListener(this);
        mOrderByTimeButton = (Button)findViewById(R.id.order_by_time_button);
        mOrderByTimeButton.setOnClickListener(this);
        mFilterByFoodCornerSpinner = (Spinner) findViewById(R.id.food_review_list_spinner);
        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.food_corner_list_for_filtering, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFilterByFoodCornerSpinner.setAdapter(arrayAdapter);
        mFilterByFoodCornerSpinner.setOnItemSelectedListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.food_review_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    public void updateUI() {
        mProgressBar.setVisibility(View.VISIBLE);

        DatabaseReference ref = getRefFilteredByCornerType();
        Query query = getQueryOrderedByTime(ref, -1);
        if(mOrderedByRatingAsc != 0) query = getQueryOrderedByRating(ref, mOrderedByRatingAsc);
        else if(mOrderedByTimeAsc != 0) query = getQueryOrderedByTime(ref, mOrderedByTimeAsc);

        FirebaseRecyclerOptions<FoodReview> options =
                new FirebaseRecyclerOptions.Builder<FoodReview>()
                        .setQuery(query, FoodReview.class)
                        .build();
        mRecyclerAdapter = new FirebaseRecyclerAdapter<FoodReview, FoodReviewHolder>(options) {
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

            @Override
            public void onDataChanged() {
                mEmptyListMessage.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
                mProgressBar.setVisibility(View.GONE);
            }
        };

        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerAdapter.startListening();
    }

    private Query getQueryOrderedByTime(DatabaseReference ref, int rev) {
        Query query;
        if (rev == -1) {
            query = ref.orderByChild("mDateRev");
        } else {
            query = ref.orderByChild("mDate");
        }
        return query;
    }

    private Query getQueryOrderedByRating(DatabaseReference ref, int rev) {
        Query query;
        if (rev == -1) {
            query = ref.orderByChild("mRatingRev");
        } else {
            query = ref.orderByChild("mRating");
        }
        return query;
    }

    private DatabaseReference getRefFilteredByCornerType() {
        return FirebaseDatabase.getInstance()
                .getReference()
                .child("Food_reviews")
                .child(mFilteredCornerType == 0 ? "All" : "Corner " + mFilteredCornerType);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecyclerAdapter.stopListening();
    }

    public void onClickFab(View view) {
        writeReview();
    }

    public void writeReview() {
        Intent intent = FoodReviewWriteActivity.newIntent(this);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.right_to_left_slide_in, R.anim.right_to_left_slide_out);
        startActivity(intent, options.toBundle());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // 특정 코너로 필터링
        mFilteredCornerType = position;
        updateUI();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_FILTERED_CORNER_TYPE, mFilteredCornerType);
        outState.putInt(SAVED_ORDERED_BY_RATING_ASC, mOrderedByRatingAsc);
        outState.putInt(SAVED_ORDERED_BY_TIME_ASC, mOrderedByTimeAsc);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.order_by_rating_button:
                if(mOrderedByRatingAsc != 0) {
                    mOrderedByRatingAsc = -mOrderedByRatingAsc;
                } else{
                    mOrderedByRatingAsc = -1;
                }
                mOrderedByTimeAsc = 0;
                updateUI();
                break;
            case R.id.order_by_time_button:
                if(mOrderedByTimeAsc != 0){
                    mOrderedByTimeAsc = -mOrderedByTimeAsc;
                } else{
                    mOrderedByTimeAsc = -1;
                }
                mOrderedByRatingAsc = 0;
                updateUI();
                break;
            default:
        }
    }
}
