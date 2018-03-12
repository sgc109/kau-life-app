package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dgreenhalgh.android.simpleitemdecoration.grid.GridDividerItemDecoration;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.model.ExaminationTimeTable;

import java.lang.ref.WeakReference;

public class ExaminationTimeTableActivity extends AppCompatActivity {

    private static final int TABLE_COL_SIZE = 6;
    private static final int FIRST_LINE = 6;
    private static final int FIRST_ROW = 0;
    private LMSPortalManager mLMSPortalManager;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private ViewGroup mMainLayout;
    private ViewGroup mProgressBarLayout;
    private PullExaminationTimeTableAsyncTask mPullExaminationTimeTableAsyncTask;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ExaminationTimeTableActivity.class);
        return intent;
    }

    @Override
    protected void onStart() {
        super.onStart();
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if(nm != null) nm.cancel(3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examination_time_table);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mLMSPortalManager = LMSPortalManager.getInstance();
        mLMSPortalManager.clearExaminationTimeTable();
        mRecyclerView = findViewById(R.id.examination_time_table_recycler_view);
        mRecyclerAdapter = new RecyclerView.Adapter<ExaminationTimeTableActivity.ExaminationTimeTableItemViewHolder>() {
            @Override
            public ExaminationTimeTableActivity.ExaminationTimeTableItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view;
                ExaminationTimeTableActivity.ExaminationTimeTableItemViewHolder viewHolder;
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_time_table, parent, false);
                viewHolder = new ExaminationTimeTableActivity.ExaminationTimeTableItemViewHolder(view);
                return viewHolder;
            }

            @Override
            public void onBindViewHolder(ExaminationTimeTableActivity.ExaminationTimeTableItemViewHolder holder, int position) {
                holder.bind(position);
            }

            @Override
            public int getItemCount() {
                return mLMSPortalManager.getExaminationTimeTableSize();
            }
        };
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 6, GridLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        Drawable horizontalDivider = ContextCompat.getDrawable(this, R.drawable.time_table_list_border);
        Drawable verticalDivider = ContextCompat.getDrawable(this, R.drawable.time_table_list_border);
        mRecyclerView.addItemDecoration(new GridDividerItemDecoration(horizontalDivider, verticalDivider, 6));
        mMainLayout = findViewById(R.id.examination_time_table_main_layout);
        mMainLayout.setVisibility(View.GONE);
        mProgressBarLayout = findViewById(R.id.examination_time_table_progress_bar_layout);
        mProgressBarLayout.setVisibility(View.VISIBLE);
        executeAsyncTask();
    }

    public void executeAsyncTask() {
        mPullExaminationTimeTableAsyncTask = new PullExaminationTimeTableAsyncTask(getApplication(), this);
        mPullExaminationTimeTableAsyncTask.execute();
    }

    public class ExaminationTimeTableItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mTimeTableSubjectTitleTextView;
        private TextView mTimeTableProfessorNameTextView;
        private TextView mTimeTablePlaceTextView;

        private ExaminationTimeTableItemViewHolder(View itemView) {
            super(itemView);
            mTimeTableSubjectTitleTextView = itemView.findViewById(R.id.list_item_time_table_subject_title_text_view);
            mTimeTablePlaceTextView = itemView.findViewById(R.id.list_item_time_table_place_text_view);
        }

        public void bind(int position) {
            ExaminationTimeTable examinationTimeTable = mLMSPortalManager.getExaminationTimeTable(position);
            mTimeTableSubjectTitleTextView.setText(examinationTimeTable.subjectTitle);
            if(position % TABLE_COL_SIZE == FIRST_ROW || position < FIRST_LINE) mTimeTablePlaceTextView.setText(examinationTimeTable.professorName);
            else mTimeTablePlaceTextView.setText(examinationTimeTable.place);
        }
    }

    private static class PullExaminationTimeTableAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<ExaminationTimeTableActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        PullExaminationTimeTableAsyncTask(Application application, ExaminationTimeTableActivity ExaminationTimeTableActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(ExaminationTimeTableActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ExaminationTimeTableActivity examinationTimeTableActivity = activityReference.get();
            if (examinationTimeTableActivity == null || examinationTimeTableActivity.isFinishing())
                return;
            examinationTimeTableActivity.mLMSPortalManager.clearAccumulatedGradeSummary();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            Resources resources = applicationWeakReference.get().getResources();
            ExaminationTimeTableActivity examinationTimeTableActivity = activityReference.get();
            if (examinationTimeTableActivity == null || examinationTimeTableActivity.isFinishing()) {
                return resources.getInteger(R.integer.unexpected_error);
            }
            int count = 0;
            int result;
            LMSPortalManager lm = examinationTimeTableActivity.mLMSPortalManager;
            while (!examinationTimeTableActivity.isFinishing() && !isCancelled() &&
                    (result = lm.pullExaminationTimeTable(applicationWeakReference.get())) != resources.getInteger(R.integer.no_error)) {
                if (result == resources.getInteger(R.integer.network_error)) {
                    sleep(1000);
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
            ExaminationTimeTableActivity examinationTimeTableActivity = activityReference.get();
            Resources resources = applicationWeakReference.get().getResources();
            if (examinationTimeTableActivity == null || examinationTimeTableActivity.isFinishing())
                return;
            if (result == resources.getInteger(R.integer.no_error)) {
                examinationTimeTableActivity.mRecyclerAdapter.notifyDataSetChanged();
                examinationTimeTableActivity.mProgressBarLayout.setVisibility(View.GONE);
                examinationTimeTableActivity.mMainLayout.setVisibility(View.VISIBLE);
            } else if(result == resources.getInteger(R.integer.missing_data_error)){
                examinationTimeTableActivity.mProgressBarLayout.setVisibility(View.GONE);
                examinationTimeTableActivity.mMainLayout.setVisibility(View.VISIBLE);
                examinationTimeTableActivity.showToast(resources.getString(R.string.portal_no_time_table_error_message));
            }
            else if (result == resources.getInteger(R.integer.network_error)) {
                //네트워크 관련 예외 처리
                examinationTimeTableActivity.showToast(resources.getString(R.string.portal_network_error_message));
            } else if (result == resources.getInteger(R.integer.session_error) || result == resources.getInteger(R.integer.ssl_hand_shake_error)) {
                //세션 관련 예외 처리
                examinationTimeTableActivity.showToast(resources.getString(R.string.portal_session_disconnect_error_message));
                Intent intent = LoginActivity.newIntent(examinationTimeTableActivity);
                examinationTimeTableActivity.startActivity(intent);
                examinationTimeTableActivity.finish();
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

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if(fm.getBackStackEntryCount() > 0) {
            super.onBackPressed();
        }
        else{
            Intent intent = HomeActivity.newIntent(getApplicationContext(), 4);
            startActivity(intent);
            finish();
        }
    }
}


