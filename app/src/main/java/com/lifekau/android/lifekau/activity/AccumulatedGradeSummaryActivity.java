package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.model.AccumulatedGradeSummary;

import java.lang.ref.WeakReference;


public class AccumulatedGradeSummaryActivity extends AppCompatActivity {

    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private ViewGroup mMainLayout;
    private ViewGroup mProgressBarLayout;
    private PullAccumulatedGradeSummaryAsyncTask mPullAccumulatedGradeSummaryAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accumulated_grade_summary);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mRecyclerAdapter = new RecyclerView.Adapter<AccumulatedGradeSummaryItemViewHolder>() {
            @Override
            public AccumulatedGradeSummaryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view;
                AccumulatedGradeSummaryItemViewHolder viewHolder;
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_accumulated_grade_summary, parent, false);
                viewHolder = new AccumulatedGradeSummaryItemViewHolder(view);
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(AccumulatedGradeSummaryItemViewHolder holder, int position) {
                holder.bind(position);
            }

            @Override
            public int getItemCount() {
                return mLMSPortalManager.getAccumulatedGradeSummarySize();
            }
        };
        mLMSPortalManager.clearAccumulatedGradeSummary();
        mRecyclerView = findViewById(R.id.accumulated_grade_summary_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mMainLayout = findViewById(R.id.accumulated_grade_summary_main_layout);
        mMainLayout.setVisibility(View.GONE);
        mProgressBarLayout = findViewById(R.id.accumulated_grade_summary_progress_bar_layout);
        mProgressBarLayout.setVisibility(View.VISIBLE);
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

    public class AccumulatedGradeSummaryItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mSemesterTextView;
        private TextView mRegisteredCreditsTextView;
        private TextView mAcquiredCreditsTextView;
        private TextView mTotalGradesTextView;
        private TextView mGPATextView;

        private AccumulatedGradeSummaryItemViewHolder(View itemView) {
            super(itemView);
            mSemesterTextView = itemView.findViewById(R.id.list_item_accumulated_grade_summary_semester);
            mRegisteredCreditsTextView = itemView.findViewById(R.id.list_item_accumulated_grade_summary_registered_credits);
            mAcquiredCreditsTextView = itemView.findViewById(R.id.list_item_accumulated_grade_summary_acquired_credits);
            mTotalGradesTextView = itemView.findViewById(R.id.list_item_accumulated_grade_summary_total_grades);
            mGPATextView = itemView.findViewById(R.id.list_item_accumulated_grade_summary_GPA);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            AccumulatedGradeSummary accumulatedGradeSummary = mLMSPortalManager.getAccumulatedGradeSummary(position);
            mSemesterTextView.setText(String.valueOf(accumulatedGradeSummary.semester));
            mRegisteredCreditsTextView.setText(String.valueOf(accumulatedGradeSummary.registeredCredits));
            mAcquiredCreditsTextView.setText(String.valueOf(accumulatedGradeSummary.acquiredCredits));
            mTotalGradesTextView.setText(String.valueOf(accumulatedGradeSummary.totalGrades));
            mGPATextView.setText(String.valueOf(accumulatedGradeSummary.GPA));
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                AccumulatedGradeSummary accumulatedGradeSummary = mLMSPortalManager.getAccumulatedGradeSummary(position);
                Intent intent = AccumulatedGradeActivity.newIntent(AccumulatedGradeSummaryActivity.this, accumulatedGradeSummary.year, accumulatedGradeSummary.semesterCode);
                startActivity(intent);
            }
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
            Resources resources = applicationWeakReference.get().getResources();
            AccumulatedGradeSummaryActivity accumulatedGradeSummaryActivity = activityReference.get();
            if (accumulatedGradeSummaryActivity == null || accumulatedGradeSummaryActivity.isFinishing()) {
                return resources.getInteger(R.integer.unexpected_error);
            }
            int count = 0;
            int result;
            LMSPortalManager lm = accumulatedGradeSummaryActivity.mLMSPortalManager;
            while (!accumulatedGradeSummaryActivity.isFinishing() && !isCancelled() &&
                    (result = lm.pullAccumulatedGradeSummary(applicationWeakReference.get())) != resources.getInteger(R.integer.no_error)) {
                if (result == resources.getInteger(R.integer.network_error)) {
                    count++;
                } else return result;
                if (count == resources.getInteger(R.integer.maximum_retry_num))
                    return resources.getInteger(R.integer.network_error);
            }
            return resources.getInteger(R.integer.no_error);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            AccumulatedGradeSummaryActivity accumulatedGradeSummaryActivity = activityReference.get();
            Resources resources = applicationWeakReference.get().getResources();
            if (accumulatedGradeSummaryActivity == null || accumulatedGradeSummaryActivity.isFinishing())
                return;
            if (result == resources.getInteger(R.integer.no_error)) {
                accumulatedGradeSummaryActivity.mRecyclerAdapter.notifyDataSetChanged();
                accumulatedGradeSummaryActivity.mProgressBarLayout.setVisibility(View.GONE);
                accumulatedGradeSummaryActivity.mMainLayout.setVisibility(View.VISIBLE);
            } else if (result == resources.getInteger(R.integer.network_error)) {
                //네트워크 관련 예외 처리
                accumulatedGradeSummaryActivity.showToast(resources.getString(R.string.portal_network_error_message));
            } else if (result == resources.getInteger(R.integer.session_error)) {
                //세션 관련 예외 처리
                accumulatedGradeSummaryActivity.showToast(resources.getString(R.string.portal_session_disconnect_error_message));
                Intent intent = LoginActivity.newIntent(accumulatedGradeSummaryActivity);
                accumulatedGradeSummaryActivity.startActivity(intent);
                accumulatedGradeSummaryActivity.finish();
            }
        }

    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}