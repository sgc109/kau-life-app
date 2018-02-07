package com.lifekau.android.lifekau;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
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

public class FoodReviewListActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String SAVED_IS_WRITING = "saved_is_writing";
    private final static String EXTRA_FOOD_CORNER_TYPE = "extra_food_corner_type";
    private final String SAVED_ORDERED_BY_RATING_ASC = "saved_order_by_rating_asc";
    private final String SAVED_ORDERED_BY_TIME_ASC = "saved_order_by_time_asc";
    private final String SAVED_FOOD_CORNER_TYPE = "saved_food_corner_type";
    private final int REQUEST_FOOD_REVIEW = 0;
    public final static int RESTAURENT_TYPE_STUDENT = 0;
    public final static int RESTAURENT_TYPE_DORM = 1;

    private Button mOrderByTimeButton;
    private Button mOrderByRatingButton;
    private RecyclerView mRecyclerView;
    private TextView mEmptyListMessage;
    private LinearLayout mProgressBar;
    private ActionBar mActionBar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private int mFoodCornerType;
    private int mOrderedByRatingAsc; // -1 or 0 or 1
    private int mOrderedByTimeAsc; // -1 or 0 or 1
//    private Boolean mIsWriting;
    private RecyclerView.Adapter mRecyclerAdapter;
    private List<FoodReview> mFoodReviews;

    public static Intent newIntent(Context packageContext, int foodCornerType) {
        Intent intent = new Intent(packageContext, FoodReviewListActivity.class);
        intent.putExtra(EXTRA_FOOD_CORNER_TYPE, foodCornerType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_review_list);

        if (savedInstanceState != null) {
//            mFoodCornerType = savedInstanceState.getInt(SAVED_FOOD_CORNER_TYPE);
            mOrderedByTimeAsc = savedInstanceState.getInt(SAVED_ORDERED_BY_TIME_ASC);
            mOrderedByRatingAsc = savedInstanceState.getInt(SAVED_ORDERED_BY_RATING_ASC);

//            mIsWriting = savedInstanceState.getBoolean(SAVED_IS_WRITING);
        }

//        mIsWriting = true;
        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().hide();
        }

        if (mFoodReviews == null) mFoodReviews = new ArrayList<>();

        mFoodCornerType = getIntent().getIntExtra(EXTRA_FOOD_CORNER_TYPE, 0);

//        Toolbar toolbar = findViewById(R.id.food_review_list_toolbar);
//        setSupportActionBar(toolbar);

        if (mOrderedByRatingAsc == 0 && mOrderedByTimeAsc == 0) {
            mOrderedByTimeAsc = -1;
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.food_review_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mProgressBar = (LinearLayout) findViewById(R.id.indeterminateBar);
        mEmptyListMessage = (TextView) findViewById(R.id.food_review_list_empty_list_text_view);
        mOrderByRatingButton = (Button) findViewById(R.id.order_by_rating_button);
        mOrderByRatingButton.setOnClickListener(this);
        mOrderByTimeButton = (Button) findViewById(R.id.order_by_time_button);
        mOrderByTimeButton.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.food_review_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    public void updateUI() {
        mProgressBar.setVisibility(View.VISIBLE);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child(getString(R.string.firebase_database_food_reviews))
                .child(String.format(getString(R.string.firebase_database_food_review_corner_id), mFoodCornerType));

        Query query = getQueryOrderedByTime(ref, -1);
        if (mOrderedByRatingAsc != 0) query = getQueryOrderedByRating(ref, mOrderedByRatingAsc);
        else if (mOrderedByTimeAsc != 0) query = getQueryOrderedByTime(ref, mOrderedByTimeAsc);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFoodReviews.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    mFoodReviews.add(snapshot.getValue(FoodReview.class));
                }
                mEmptyListMessage.setVisibility(mFoodReviews.size() == 0 ? View.VISIBLE : View.GONE);
                mProgressBar.setVisibility(View.GONE);
                mRecyclerAdapter.notifyDataSetChanged();
//                if(!mIsWriting){
//                    Toast.makeText(FoodReviewListActivity.this, getString(R.string.new_review_message), Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mRecyclerAdapter = new RecyclerView.Adapter<FoodReviewHolder>() {
            @Override
            public FoodReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_food_review, parent, false);
                return new FoodReviewHolder(view);
            }

            @Override
            public void onBindViewHolder(FoodReviewHolder holder, int position) {
                holder.bindReview(mFoodReviews.get(position));
            }

            @Override
            public int getItemCount() {
                return mFoodReviews.size();
            }
        };

        mRecyclerView.setAdapter(mRecyclerAdapter);
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

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void onClickWriteFoodReviewFab(View view) {
//        mIsWriting = true;
        writeFoodReview();
    }

    @SuppressLint("RestrictedApi")
    public void writeFoodReview() {
        Intent intent = FoodReviewWriteActivity.newIntent(this, mFoodCornerType);
        ActivityOptionsCompat options =
                ActivityOptionsCompat.makeCustomAnimation(this, R.anim.right_to_left_slide_in, R.anim.right_to_left_slide_out);
        startActivityForResult(intent, REQUEST_FOOD_REVIEW, options.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOOD_REVIEW) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.review_write_success_message), Toast.LENGTH_SHORT).show();
//                mIsWriting = false;
                updateUI();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_FOOD_CORNER_TYPE, mFoodCornerType);
        outState.putInt(SAVED_ORDERED_BY_RATING_ASC, mOrderedByRatingAsc);
        outState.putInt(SAVED_ORDERED_BY_TIME_ASC, mOrderedByTimeAsc);
//        outState.putBoolean(SAVED_IS_WRITING, mIsWriting);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.order_by_time_button:
                if (mOrderedByTimeAsc != 0) {
                    mOrderedByTimeAsc = -mOrderedByTimeAsc;
                } else {
                    mOrderedByTimeAsc = -1;
                }
                mOrderedByRatingAsc = 0;
                updateUI();
//                mRecyclerView.smoothScrollToPosition(0);
                break;
            default:
            case R.id.order_by_rating_button:
                if (mOrderedByRatingAsc != 0) {
                    mOrderedByRatingAsc = -mOrderedByRatingAsc;
                } else {
                    mOrderedByRatingAsc = -1;
                }
                mOrderedByTimeAsc = 0;
                updateUI();
//                mRecyclerView.smoothScrollToPosition(0);
                break;
        }
    }

    @Override
    public void onRefresh() {
        mRecyclerAdapter.notifyDataSetChanged();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }
}
