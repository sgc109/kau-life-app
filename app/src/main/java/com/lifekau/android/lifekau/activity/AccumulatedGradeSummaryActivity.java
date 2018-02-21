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
import com.lifekau.android.lifekau.model.AccumulatedGradeSummary;

import java.lang.ref.WeakReference;


public class AccumulatedGradeSummaryActivity extends AppCompatActivity {

    private static final int VIEW_ITEM = 0;
    private static final int VIEW_PROGRESS = 1;

    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private PullAccumulatedGradeSummaryAsyncTask mPullAccumulatedGradeSummaryAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accumulated_grade);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mSwipeRefreshLayout = findViewById(R.id.portal_accumulated_swipe_refresh_layout);
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
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_accumulated_grade_summary, parent, false);
                    viewHolder = new AccumulatedGradeSummaryItemViewHolder(view);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_progress, parent, false);
                    viewHolder = new ItemProgressViewHolder(view);
                }
                return viewHolder;
            }

            @Override
            public int getItemViewType(int position) {
                return mLMSPortalManager.getAccumulatedGradeSummary(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof AccumulatedGradeSummaryItemViewHolder)
                    ((AccumulatedGradeSummaryItemViewHolder) holder).bind(position);
                else ((ItemProgressViewHolder) holder).bind(position);
            }

            @Override
            public int getItemCount() {
                int size = mLMSPortalManager.getAccumulatedGradeSummarySize();
                return (size > 0) ? size : 1;
            }
        };
        mLMSPortalManager.clearAccumulatedGradeSummary();
        mRecyclerView = findViewById(R.id.portal_accumulated_recycler_view);
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
        mPullAccumulatedGradeSummaryAsyncTask.cancel(true);
    }

    public void executeAsyncTask() {
        mPullAccumulatedGradeSummaryAsyncTask = new PullAccumulatedGradeSummaryAsyncTask(getApplication(), this);
        mPullAccumulatedGradeSummaryAsyncTask.execute();
    }

    public class AccumulatedGradeSummaryItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mSemesterTextView;
        private TextView mGPAAndTotalGradesTextView;
        private TextView mCreditsTextView;

        public AccumulatedGradeSummaryItemViewHolder(View itemView) {
            super(itemView);
            mSemesterTextView = itemView.findViewById(R.id.list_item_accumulated_grade_summary_semester);
            mGPAAndTotalGradesTextView = itemView.findViewById(R.id.list_item_accumulated_grade_summary_GPA_and_total_grades);
            mCreditsTextView = itemView.findViewById(R.id.list_item_accumulated_grade_summary_credits);
        }

        public void bind(int position) {
            AccumulatedGradeSummary accumulatedGradeSummary = mLMSPortalManager.getAccumulatedGradeSummary(position);
            mSemesterTextView.setText(String.valueOf(accumulatedGradeSummary.semester));
            mGPAAndTotalGradesTextView.setText(accumulatedGradeSummary.GPA + " / " + accumulatedGradeSummary.totalGrades);
            mCreditsTextView.setText(accumulatedGradeSummary.acquiredCredits + " / " + accumulatedGradeSummary.registeredCredits);
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

    private static class PullAccumulatedGradeSummaryAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<AccumulatedGradeSummaryActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        PullAccumulatedGradeSummaryAsyncTask(Application application, AccumulatedGradeSummaryActivity AccumulatedGradeSummaryActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(AccumulatedGradeSummaryActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AccumulatedGradeSummaryActivity accumulatedGradeSummaryActivity = activityReference.get();
            if (accumulatedGradeSummaryActivity == null || accumulatedGradeSummaryActivity.isFinishing())
                return;
            accumulatedGradeSummaryActivity.mLMSPortalManager.clearAccumulatedGradeSummary();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            AccumulatedGradeSummaryActivity accumulatedGradeSummaryActivity = activityReference.get();
            int count = 0;
            while ((accumulatedGradeSummaryActivity != null && !accumulatedGradeSummaryActivity.isFinishing()) &&
                    accumulatedGradeSummaryActivity.mLMSPortalManager.pullAccumulatedGradeSummary(activityReference.get()) == -1 && !isCancelled()) {
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
            final AccumulatedGradeSummaryActivity accumulatedGradeSummaryActivity = activityReference.get();
            if (accumulatedGradeSummaryActivity == null || accumulatedGradeSummaryActivity.isFinishing())
                return;
            if (result != -1) {
                accumulatedGradeSummaryActivity.mRecyclerAdapter.notifyDataSetChanged();
            } else {
                //예외 처리
                accumulatedGradeSummaryActivity.showErrorMessage();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        accumulatedGradeSummaryActivity.mSwipeRefreshLayout.setRefreshing(false);
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