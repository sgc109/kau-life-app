package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.model.Scholarship;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;

public class ScholarshipActivity extends AppCompatActivity {

    private static final int VIEW_ITEM = 0;
    private static final int VIEW_PROGRESS = 1;

    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private GetScholarshipAsyncTask mGetScholarshipAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scholarship);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mSwipeRefreshLayout = findViewById(R.id.portal_scholarship_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRecyclerAdapter.notifyDataSetChanged();
                executeAsyncTask();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 500);
            }
        });
        mRecyclerAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view;
                RecyclerView.ViewHolder viewHolder;
                if (viewType == VIEW_ITEM) {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_scholarship, parent, false);
                    viewHolder = new ScholarshipItemViewHolder(view);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_progress, parent, false);
                    viewHolder = new ItemProgressViewHolder(view);
                }
                return viewHolder;
            }

            @Override
            public int getItemViewType(int position) {
                return mLMSPortalManager.getScholarship(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof ScholarshipItemViewHolder)
                    ((ScholarshipItemViewHolder) holder).bind(position);
                else ((ItemProgressViewHolder) holder).bind(position);
            }

            @Override
            public int getItemCount() {
                int size = mLMSPortalManager.getScholarshipSize();
                return (size > 0) ? size : 1;
            }
        };
        mRecyclerView = findViewById(R.id.portal_scholarship_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        executeAsyncTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGetScholarshipAsyncTask.cancel(true);
    }

    public void executeAsyncTask() {
        mGetScholarshipAsyncTask = new GetScholarshipAsyncTask(getApplication(), this);
        mGetScholarshipAsyncTask.execute();
    }

    public class ScholarshipItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mTypeTextView;
        private TextView mSemesterTextView;
        private TextView mCategorizationTextView;
        private TextView mAmountTextView;

        public ScholarshipItemViewHolder(View itemView) {
            super(itemView);
            mTypeTextView = itemView.findViewById(R.id.list_item_scholarship_type);
            mSemesterTextView = itemView.findViewById(R.id.list_item_scholarship_semester);
            mCategorizationTextView = itemView.findViewById(R.id.list_item_scholarship_categorization);
            mAmountTextView = itemView.findViewById(R.id.list_item_scholarship_amount);
        }

        public void bind(int position) {
            Scholarship scholarship = mLMSPortalManager.getScholarship(position);
            mTypeTextView.setText(scholarship.type);
            mSemesterTextView.setText(scholarship.semester);
            mCategorizationTextView.setText(scholarship.categorization);
            mAmountTextView.setText(NumberFormat.getInstance().format(scholarship.amount) + "원");
        }
    }

    public class ItemProgressViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;

        public ItemProgressViewHolder(View progressView) {
            super(progressView);
            mProgressBar = progressView.findViewById(R.id.list_item_progress_bar);
        }

        public void bind(int position) {

        }
    }

    private static class GetScholarshipAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<ScholarshipActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        GetScholarshipAsyncTask(Application application, ScholarshipActivity ScholarshipActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(ScholarshipActivity);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            ScholarshipActivity scholarshipActivity = activityReference.get();
            int count = 0;
            while ((scholarshipActivity != null && !scholarshipActivity.isFinishing()) &&
                    scholarshipActivity.mLMSPortalManager.pullScholarship(activityReference.get()) == -1 && !isCancelled()) {
                Log.e("ERROR", "페이지 불러오기 실패!");
                sleep(3000);
                count++;
                if (count == 5) return -1;
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            final ScholarshipActivity scholarshipActivity = activityReference.get();
            if (scholarshipActivity == null || scholarshipActivity.isFinishing()) return;
            if (result != -1) {
                scholarshipActivity.mRecyclerAdapter.notifyItemRangeChanged(0, scholarshipActivity.mLMSPortalManager.getScholarshipSize());
            } else {
                //예외 처리
                scholarshipActivity.showErrorMessage();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scholarshipActivity.mSwipeRefreshLayout.setRefreshing(false);
                    }
                }, 1000);
            }
        }

        public void sleep(int time) {
            try {
                Thread.sleep(time);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}