package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by sgc109 on 2018-01-27.
 */

public class FoodReviewActivity extends AppCompatActivity {
    private final static String EXTRA_RESTAURANT_TYPE = "extra.restaurant_type";
    public final static int RESTAURENT_TYPE_STUDENT = 0;
    public  final static int RESTAURENT_TYPE_DORM = 1;

    public static Intent newIntent(Context packageContext, int RestaurantType){
        Intent intent = new Intent(packageContext, FoodReviewActivity.class);
        intent.putExtra(EXTRA_RESTAURANT_TYPE, RestaurantType);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        writeReview();
        insertReviewToDB(3, FoodReview.CORNER_TYPE_A, "Good!");
    }

    public void writeReview(){

    }

    public void insertReviewToDB(int score, int corner, String comment){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("food_reviews");
        FoodReview review = new FoodReview(score, corner, comment);
        ref.push().setValue(review);
    }
}
