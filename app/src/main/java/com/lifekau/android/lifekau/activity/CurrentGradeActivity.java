package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.content.Context;
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
import com.lifekau.android.lifekau.model.CurrentGrade;
import com.lifekau.android.lifekau.model.TotalCurrentGrade;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class CurrentGradeActivity extends AppCompatActivity {

    private static final int UNEXPECTED_ERROR = -100;
    private static final int MAXIMUM_RETRY_NUM = 5;

    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private ViewGroup mMainLayout;
    private ViewGroup mProgressBarLayout;
    private GetCurrentGradeActivity mGetCurrentGradeAsyncTask;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, CurrentGradeActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_grade);
        Intent intent = getIntent();
        Calendar now = Calendar.getInstance();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mRecyclerAdapter = new RecyclerView.Adapter<AccumulatedGradeItemViewHolder>() {
            @Override
            public AccumulatedGradeItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_current_grade, parent, false);
                return new AccumulatedGradeItemViewHolder(view);
            }

            @Override
            public void onBindViewHolder(AccumulatedGradeItemViewHolder holder, int position) {
                holder.bind(position);
            }

            @Override
            public int getItemCount() {
                return mLMSPortalManager.getAccumulatedGradeSize();
            }
        };

        mLMSPortalManager.clearAccumulatedGrade();
        mRecyclerView = findViewById(R.id.current_grade_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mMainLayout = findViewById(R.id.current_grade_main_layout);
        mMainLayout.setVisibility(View.GONE);
        mProgressBarLayout = findViewById(R.id.current_grade_progress_bar_layout);
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
        mGetCurrentGradeAsyncTask.cancel(true);
    }

    public void executeAsyncTask() {
        mGetCurrentGradeAsyncTask = new GetCurrentGradeActivity(getApplication(), this);
        mGetCurrentGradeAsyncTask.execute();
    }

    public class AccumulatedGradeItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mCourseTitleTextView;
        private TextView mMajorTextView;
        private TextView mCreditsTextView;
        private TextView mGradeTextView;

        private AccumulatedGradeItemViewHolder(View itemView) {
            super(itemView);
            mCourseTitleTextView = itemView.findViewById(R.id.list_item_current_grade_course_title);
            mMajorTextView = itemView.findViewById(R.id.list_item_current_major);
            mCreditsTextView = itemView.findViewById(R.id.list_item_current_grade_credits);
            mGradeTextView = itemView.findViewById(R.id.list_item_current_grade_grade);
        }

        public void bind(int position) {
            CurrentGrade currentGrade = mLMSPortalManager.getCurrentGrade(position);
            mCourseTitleTextView.setText(currentGrade.courseTitle);
            mMajorTextView.setText(currentGrade.major);
            mCreditsTextView.setText(String.valueOf(currentGrade.credits));
            mGradeTextView.setText(currentGrade.grade);
        }
    }

    private static class GetCurrentGradeActivity extends AsyncTask<Void, Void, Integer> {

        private WeakReference<CurrentGradeActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        GetCurrentGradeActivity(Application application, CurrentGradeActivity CurrentGradeActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(CurrentGradeActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            CurrentGradeActivity currentGradeActivity = activityReference.get();
            if (currentGradeActivity == null || currentGradeActivity.isFinishing()) return;
            currentGradeActivity.mLMSPortalManager.clearScholarship();
        }

        @Override
        protected Integer doInBackground(Void... params) {
            CurrentGradeActivity currentGradeActivity = activityReference.get();
            if (currentGradeActivity == null || currentGradeActivity.isFinishing()) return UNEXPECTED_ERROR;
            Resources resources = currentGradeActivity.getResources();
            int count = 0;
            int result = currentGradeActivity.mLMSPortalManager.pullCurrentGrade(activityReference.get());
            while (!currentGradeActivity.isFinishing() && result != resources.getInteger(R.integer.no_error) && !isCancelled()) {
                if(result == resources.getInteger(R.integer.network_error)){
                    sleep(3000);
                    count++;
                }
                else return result;
                if(count == MAXIMUM_RETRY_NUM) return resources.getInteger(R.integer.network_error);
            }
            return resources.getInteger(R.integer.no_error);
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            Log.e("???", "???");
            CurrentGradeActivity currentGradeActivity = activityReference.get();
            if (currentGradeActivity == null || currentGradeActivity.isFinishing()) return;
            Resources resources = currentGradeActivity.getResources();
            if (result == resources.getInteger(R.integer.no_error)) {
                TotalCurrentGrade totalCurrentGrade = currentGradeActivity.mLMSPortalManager.getTotalCurrentGrade();
                TextView registeredCreditsTextview = currentGradeActivity.findViewById(R.id.current_grade_registered_credits);
                registeredCreditsTextview.setText(String.valueOf(totalCurrentGrade.registeredCredits));
                TextView acquiredCreditsTextview = currentGradeActivity.findViewById(R.id.current_grade_acquired_credits);
                acquiredCreditsTextview.setText(String.valueOf(totalCurrentGrade.acquiredCredits));
                TextView totalGradesTextview = currentGradeActivity.findViewById(R.id.current_grade_total_grades);
                totalGradesTextview.setText(String.valueOf(totalCurrentGrade.totalGrades));
                TextView semesterRankingTextView = currentGradeActivity.findViewById(R.id.current_grade_semester_ranking);
                semesterRankingTextView.setText(String.valueOf(totalCurrentGrade.semesterRanking));
                currentGradeActivity.mProgressBarLayout.setVisibility(View.GONE);
                currentGradeActivity.mMainLayout.setVisibility(View.VISIBLE);
                currentGradeActivity.mRecyclerAdapter.notifyDataSetChanged();
            }
            else if(result == resources.getInteger(R.integer.missing_data_error)){
                Toast toast = Toast.makeText(currentGradeActivity.getApplicationContext(), "아직 학기 성적이 등록되지 않았습니다.", Toast.LENGTH_SHORT);
                currentGradeActivity.mProgressBarLayout.setVisibility(View.GONE);
                currentGradeActivity.mMainLayout.setVisibility(View.VISIBLE);
                toast.show();
//                currentGradeActivity.finish();
            }
            else if(result == resources.getInteger(R.integer.network_error)){
                //네트워크 관련 문제
                currentGradeActivity.showErrorMessage();
            }
            else if(result == resources.getInteger(R.integer.session_error)){
                //세션 관련 문제
                Intent intent = LoginActivity.newIntent(currentGradeActivity);
                currentGradeActivity.startActivity(intent);
                currentGradeActivity.finish();
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