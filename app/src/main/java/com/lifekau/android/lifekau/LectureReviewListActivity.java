package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LectureReviewListActivity extends AppCompatActivity {
    private static final String EXTRA_LECTURE_NAME = "extra_lecture_name";
    private String mLectureName;
    private ActionBar mActionBar;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private TextView mLectureNameTextView;
    List<LectureReview> mLectureReviews;


    public static Intent newIntent(Context context, String lectureName){
        Intent intent = new Intent(context, LectureReviewListActivity.class);
        intent.putExtra(EXTRA_LECTURE_NAME, lectureName);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecture_review_list);


        Intent intent = getIntent();
        mLectureName = intent.getStringExtra(EXTRA_LECTURE_NAME);

        mActionBar = ((AppCompatActivity)this).getSupportActionBar();
        mActionBar.setTitle(R.string.lecture_review_title);

        mLectureNameTextView = (TextView)findViewById(R.id.lecture_review_list_lecture_text_view);
        mLectureNameTextView.setText(mLectureName);

        mRecyclerAdapter = new RecyclerView.Adapter<LectureReviewHolder>() {
            @Override
            public LectureReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_lecture_review, parent, false);
                return new LectureReviewHolder(view);
            }

            @Override
            public void onBindViewHolder(LectureReviewHolder holder, int position) {
                holder.bindReview(mLectureReviews.get(position));
            }

            @Override
            public int getItemCount() {
                return mLectureReviews.size();
            }
        };
        mRecyclerView = (RecyclerView)findViewById(R.id.lecture_review_recycler_view);
        mRecyclerView.setAdapter(mRecyclerAdapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("/Lecture_reviews").child(mLectureName);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LectureReview> newLectureReviews = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    newLectureReviews.add(snapshot.getValue(LectureReview.class));
                }
                mLectureReviews = newLectureReviews;
                mRecyclerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                
            }
        });
    }
    public void onClickWriteLectureReviewFab(View view){
        writeLectureReview();
    }
    private void writeLectureReview(){
        Intent intent = LectureReviewWriteActivity.newIntent(this, mLectureName);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
