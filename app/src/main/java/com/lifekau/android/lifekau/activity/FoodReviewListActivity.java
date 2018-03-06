package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.FoodReview;
import com.lifekau.android.lifekau.viewholder.FoodReviewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sgc109 on 2018-01-27.
 */

public class FoodReviewListActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener, DialogInterface.OnClickListener {
    private static final String SAVED_IS_WRITING = "saved_is_writing";
    private final static String EXTRA_FOOD_CORNER_TYPE = "extra_food_corner_type";
    private final String SAVED_ORDERED_BY_RATING_ASC = "saved_order_by_rating_asc";
    private final String SAVED_ORDERED_BY_TIME_ASC = "saved_order_by_time_asc";
    private final String SAVED_FOOD_CORNER_TYPE = "saved_food_corner_type";
//    private final String SAVED_IS_CHECK_FINISHED = "saved_is_check_finished";
    private final int REQUEST_FOOD_REVIEW = 0;
    public final static int RESTAURENT_TYPE_STUDENT = 0;
    public final static int RESTAURENT_TYPE_DORM = 1;

    private Button mOrderByTimeButton;
    private Button mOrderByRatingButton;
    private RecyclerView mRecyclerView;
    private TextView mEmptyListMessage;
    private ProgressBar mProgressBar;
    private ActionBar mActionBar;
    private TextView mToolbarTextView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AlertDialog mOrderByAlertDialog;
    private FoodReview mMyFoodReview;
    private int mFoodCornerType;
    private int mOrderedByRatingAsc; // -1 or 0 or 1
    private int mOrderedByTimeAsc; // -1 or 0 or 1
    private ImageView mBackImageView;
    private TextView mOrderByTextView;
    private FirebaseDatabase mDatabase;
    private RecyclerView.Adapter mRecyclerAdapter;
    private List<FoodReview> mFoodReviews;
    private boolean mAlreadyWritten;

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
            mOrderedByTimeAsc = savedInstanceState.getInt(SAVED_ORDERED_BY_TIME_ASC);
            mOrderedByRatingAsc = savedInstanceState.getInt(SAVED_ORDERED_BY_RATING_ASC);
//            mIsCheckFinished = savedInstanceState.getBoolean(SAVED_IS_CHECK_FINISHED);
        }

        mDatabase = FirebaseDatabase.getInstance();

        mFoodCornerType = getIntent().getIntExtra(EXTRA_FOOD_CORNER_TYPE, 0);

//        checkIfAlreadyWritten();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        if (mFoodReviews == null) mFoodReviews = new ArrayList<>();


        if (mOrderedByRatingAsc == 0 && mOrderedByTimeAsc == 0) {
            mOrderedByTimeAsc = -1;
        }

        mSwipeRefreshLayout = findViewById(R.id.food_review_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mProgressBar = findViewById(R.id.food_review_list_progress_bar);
        mEmptyListMessage = findViewById(R.id.food_review_list_empty_list_text_view);
        mOrderByTextView = findViewById(R.id.food_review_list_order_by_text_view);
        mOrderByTextView.setOnClickListener(this);
        mBackImageView = findViewById(R.id.food_review_list_back_image_view);
        mBackImageView.setOnClickListener(this);
        mRecyclerView = findViewById(R.id.food_review_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        mEmptyListMessage.setVisibility(View.GONE);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference()
                .child(getString(R.string.firebase_database_food_reviews))
                .child(String.format(getString(R.string.firebase_database_food_review_corner_id), mFoodCornerType));

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mFoodReviews.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if(snapshot.getKey().equals(LoginManager.get(FoodReviewListActivity.this).getStudentId())) {
                        mAlreadyWritten = true;
                    }
                    FoodReview review = snapshot.getValue(FoodReview.class);
                    mFoodReviews.add(review);
                }
                mProgressBar.setVisibility(View.GONE);
                setVisibilities();
                rearrangeReviews();
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

    public void setVisibilities() {
        if (mFoodReviews.size() != 0) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEmptyListMessage.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mEmptyListMessage.setVisibility(View.VISIBLE);
        }
    }

    private void rearrangeReviews() {
        Comparator comparator = null;
        if (mOrderedByRatingAsc != 0) {
            if (mOrderedByRatingAsc == -1) {
                comparator = new Comparator<FoodReview>() {
                    @Override
                    public int compare(FoodReview foodReview, FoodReview t1) {
                        return Float.valueOf(t1.mRating).compareTo(foodReview.mRating);
                    }
                };
            } else {
                comparator = new Comparator<FoodReview>() {
                    @Override
                    public int compare(FoodReview foodReview, FoodReview t1) {
                        return Float.valueOf(foodReview.mRating).compareTo(t1.mRating);
                    }
                };
            }
        } else if (mOrderedByTimeAsc != 0) {
            if (mOrderedByTimeAsc == -1) {
                comparator = new Comparator<FoodReview>() {
                    @Override
                    public int compare(FoodReview foodReview, FoodReview t1) {
                        return Long.valueOf(t1.mDate).compareTo(Long.valueOf(foodReview.mDate));
                    }
                };
            } else {
                comparator = new Comparator<FoodReview>() {
                    @Override
                    public int compare(FoodReview foodReview, FoodReview t1) {
                        return Long.valueOf(foodReview.mDate).compareTo(Long.valueOf(t1.mDate));
                    }
                };
            }
        }
        if (comparator == null) {
            Log.e("sgc109_debug", "comparator is not initialized!! It's null.. check this right now");
        }
        Collections.sort(mFoodReviews, comparator);
        mRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    public void onClickWriteFoodReviewFab(View view) {
        if(mAlreadyWritten){
            showEditReviewYesOrNoDialog();
        } else {
            Intent intent = FoodReviewWriteActivity.newIntent(this, mFoodCornerType, mAlreadyWritten);
            startActivityForResult(intent, REQUEST_FOOD_REVIEW);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    private void showEditReviewYesOrNoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

        builder.setMessage(String.format(getString(R.string.food_review_only_once_alert_message), getResources().getStringArray(R.array.food_corner_list)[mFoodCornerType]));
        builder.setPositiveButton(getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = FoodReviewWriteActivity.newIntent(FoodReviewListActivity.this, mFoodCornerType, mAlreadyWritten);
                startActivityForResult(intent, REQUEST_FOOD_REVIEW);
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_no), null);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FOOD_REVIEW) {
            if (resultCode == RESULT_OK) {
                mAlreadyWritten = true;
                mOrderedByTimeAsc = -1;
                mOrderedByRatingAsc = 0;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_FOOD_CORNER_TYPE, mFoodCornerType);
        outState.putInt(SAVED_ORDERED_BY_RATING_ASC, mOrderedByRatingAsc);
        outState.putInt(SAVED_ORDERED_BY_TIME_ASC, mOrderedByTimeAsc);
//        outState.putBoolean(SAVED_IS_CHECK_FINISHED, mIsCheckFinished);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.food_review_list_back_image_view:
                finish();
                break;
            case R.id.food_review_list_order_by_text_view:
                CharSequence[] values = getResources().getStringArray(R.array.order_by_list);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle(getString(R.string.order_by_dialog_title));

                int checked = -1;
                if(mOrderedByRatingAsc == 1){
                    checked = 3;
                } else if(mOrderedByRatingAsc == -1){
                    checked = 2;
                } else if(mOrderedByTimeAsc == 1) {
                    checked = 1;
                } else if(mOrderedByTimeAsc == -1) {
                    checked = 0;
                }

                builder.setSingleChoiceItems(values, checked, this);
                mOrderByAlertDialog = builder.create();
                mOrderByAlertDialog.show();
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

    @Override
    public void onClick(DialogInterface dialogInterface, int item) {
        final int ORDER_BY_TIME_DESC = 0;
        final int ORDER_BY_TIME_ASC = 1;
        final int ORDER_BY_RATING_DESC = 2;
        final int ORDER_BY_RATING_ASC = 3;
        switch (item) {
            case ORDER_BY_TIME_DESC:
                mOrderedByTimeAsc = -1;
                mOrderedByRatingAsc = 0;
                break;
            case ORDER_BY_TIME_ASC:
                mOrderedByTimeAsc = 1;
                mOrderedByRatingAsc = 0;
                break;
            case ORDER_BY_RATING_DESC:
                mOrderedByTimeAsc = 0;
                mOrderedByRatingAsc = -1;
                break;
            case ORDER_BY_RATING_ASC:
                mOrderedByTimeAsc = 0;
                mOrderedByRatingAsc = 1;
                break;
        }
        rearrangeReviews();
//                mRecyclerView.smoothScrollToPosition(0);
        mOrderByAlertDialog.dismiss();
    }
}
