package com.lifekau.android.lifekau.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LibraryManager;
import com.lifekau.android.lifekau.model.FoodReview;

import java.util.ArrayList;
import java.util.List;

public class LibraryListActivity extends AppCompatActivity {

    private static final int TOTAL_READING_ROOM_NUM = 5;
    private static final int TOTAL_STUDY_ROOM_NUM = 6;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private ProgressBar mProgressBar;
    private LibraryManager mLibraryManager;
    private int mRoomType;
    private int mSelectedArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_library_list);
        if(getSupportActionBar() != null ) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().hide();
        }
        Intent intent = getIntent();
        mRoomType = intent.getIntExtra("roomType", 0);
        mSelectedArray = mRoomType == 0 ? R.array.library_reading_room_list : R.array.library_study_room_list;
        int listLen = getResources().getStringArray(mSelectedArray).length;
        mRecyclerAdapter = new RecyclerView.Adapter<LibraryListViewHolder>() {
            @Override
            public LibraryListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_library, parent, false);
                return new LibraryListViewHolder(view);
            }

            @Override
            public void onBindViewHolder(LibraryListViewHolder holder, int position) {
                holder.bind(position);
            }

            @Override
            public int getItemCount() {
                return getResources().getStringArray(mSelectedArray).length;
            }
        };
        mRecyclerView = (RecyclerView) findViewById(R.id.library_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mProgressBar = findViewById(R.id.library_list_progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mLibraryManager = LibraryManager.getInstance();
        LibraryManagerAsyncTask libraryManagerAsyncTask = new LibraryManagerAsyncTask();
        libraryManagerAsyncTask.execute();
    }

    public class LibraryListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mNameTextView;
        private TextView mDetailTextView;
        private Context mContext;

        public LibraryListViewHolder(View itemView) {
            super(itemView);
            mNameTextView = itemView.findViewById(R.id.list_item_library_item_name);
            mDetailTextView = itemView.findViewById(R.id.list_item_library_item_detail);
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            mNameTextView.setText(mRoomType == 0 ? mLibraryManager.getReadingRoomName(position) : mLibraryManager.getStudyRoomName(position));
            mDetailTextView.setText(mRoomType == 0 ? mLibraryManager.getReadingRoomSummary(position) : mLibraryManager.getStudyRoomSummary(position));
        }

        @Override
        public void onClick(View view) {
            Intent intent = new Intent(view.getContext(), mRoomType == 0 ? ReadingRoomDetailActivity.class : StudyRoomDetailActivity.class);
            intent.putExtra("roomNum", getAdapterPosition());
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemRangeChanged(0, getResources().getStringArray(R.array.food_corner_list).length);
        }
    }

    private class LibraryManagerAsyncTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
            for(int i = 0; i < TOTAL_STUDY_ROOM_NUM; i++) {
                if (mLibraryManager.getStudyRoomDetailStatus(getApplicationContext(), i) == -1) return -1;
            }
            if (mLibraryManager.getStudyRoomStatus(getApplicationContext()) == -1) return -1;
            if (mLibraryManager.getReadingRoomStatus(getApplicationContext()) == -1) return -1;
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result != -1) {
            } else {
                //예외 처리
                showErrorMessage();
            }
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
