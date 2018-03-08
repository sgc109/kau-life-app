package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.model.Scholarship;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;

public class ScholarshipActivity extends AppCompatActivity {

    private static final int UNEXPECTED_ERROR = -100;
    private static final int MAXIMUM_RETRY_NUM = 5;

    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private ViewGroup mMainLayout;
    private ViewGroup mProgressBarLayout;
    private GetScholarshipAsyncTask mGetScholarshipAsyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scholarship);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mRecyclerAdapter = new RecyclerView.Adapter<ScholarshipItemViewHolder>() {
            @Override
            public ScholarshipItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_scholarship, parent, false);
                return new ScholarshipItemViewHolder(view);
            }

            @Override
            public void onBindViewHolder(ScholarshipItemViewHolder holder, int position) {
                holder.bind(position);
            }

            @Override
            public int getItemCount() {
                return mLMSPortalManager.getScholarshipSize();
            }
        };

        mLMSPortalManager.clearScholarship();
        mRecyclerView = findViewById(R.id.scholarship_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mMainLayout = findViewById(R.id.scholarship_main_layout);
        mMainLayout.setVisibility(View.GONE);
        mProgressBarLayout = findViewById(R.id.scholarship_progress_bar_layout);
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

        private ScholarshipItemViewHolder(View itemView) {
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

    private static class GetScholarshipAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<ScholarshipActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        GetScholarshipAsyncTask(Application application, ScholarshipActivity ScholarshipActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(ScholarshipActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ScholarshipActivity scholarshipActivity = activityReference.get();
            if (scholarshipActivity == null || scholarshipActivity.isFinishing()) return;
            scholarshipActivity.mLMSPortalManager.clearScholarship();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Resources resources = applicationWeakReference.get().getResources();
            ScholarshipActivity scholarshipActivity = activityReference.get();
            if (scholarshipActivity == null || scholarshipActivity.isFinishing())
                return resources.getInteger(R.integer.unexpected_error);
            int count = 0;
            int result;
            LMSPortalManager lm = scholarshipActivity.mLMSPortalManager;
            while (!scholarshipActivity.isFinishing() && !isCancelled()
                    && (result = lm.pullScholarship(applicationWeakReference.get())) != resources.getInteger(R.integer.no_error) ) {
                if (result == resources.getInteger(R.integer.network_error)) {
                    sleep(3000);
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
            Resources resources = applicationWeakReference.get().getResources();
            ScholarshipActivity scholarshipActivity = activityReference.get();
            if (scholarshipActivity == null || scholarshipActivity.isFinishing()) return;
            if (result == resources.getInteger(R.integer.no_error)) {
                scholarshipActivity.mProgressBarLayout.setVisibility(View.GONE);
                scholarshipActivity.mMainLayout.setVisibility(View.VISIBLE);
                scholarshipActivity.mRecyclerAdapter.notifyDataSetChanged();
            } else if (result == resources.getInteger(R.integer.network_error)) {
                //네트워크 관련 문제
                scholarshipActivity.showErrorMessage();
            } else if (result == resources.getInteger(R.integer.session_error)) {
                //세션 관련 문제
                Intent intent = LoginActivity.newIntent(scholarshipActivity);
                scholarshipActivity.startActivity(intent);
                scholarshipActivity.finish();
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