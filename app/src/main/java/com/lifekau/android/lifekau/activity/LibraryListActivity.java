package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LibraryManager;

import java.lang.ref.WeakReference;

public class LibraryListActivity extends AppCompatActivity {

    private static final int TOTAL_READING_ROOM_NUM = 5;
    private static final int TOTAL_STUDY_ROOM_NUM = 6;

    private static LibraryManager mLibraryManager = LibraryManager.getInstance();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private ProgressBar mProgressBar;
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
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_reading_room, parent, false);
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
        LibraryManagerAsyncTask libraryManagerAsyncTask = new LibraryManagerAsyncTask(getApplication(), this);
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

    private static class LibraryManagerAsyncTask extends AsyncTask<Void, Void, Integer> {

        private WeakReference<LibraryListActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        // only retain a weak reference to the activity
        LibraryManagerAsyncTask(Application application, LibraryListActivity libraryListActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(libraryListActivity);
        }

        @Override
        protected Integer doInBackground(Void... params) {
            for(int i = 0; i < TOTAL_STUDY_ROOM_NUM; i++) {
                if (mLibraryManager.getStudyRoomDetailStatus(applicationWeakReference.get(), i) == -1) return -1;
            }
            if (mLibraryManager.getStudyRoomStatus(applicationWeakReference.get()) == -1) return -1;
            if (mLibraryManager.getReadingRoomStatus(applicationWeakReference.get()) == -1) return -1;
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            LibraryListActivity libraryListActivity = activityReference.get();
            if(libraryListActivity == null || libraryListActivity.isFinishing()) return;
            if (result != -1) {
            } else {
                //예외 처리
                libraryListActivity.showErrorMessage();
            }
            ProgressBar progressBar = libraryListActivity.findViewById(R.id.library_list_progress_bar);
            RecyclerView recyclerView = libraryListActivity.findViewById(R.id.library_list_recycler_view);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}
