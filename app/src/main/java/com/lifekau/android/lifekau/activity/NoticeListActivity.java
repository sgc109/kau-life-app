package com.lifekau.android.lifekau.activity;

import android.app.Application;
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
import com.lifekau.android.lifekau.manager.NoticeManager;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class NoticeListActivity extends AppCompatActivity {

    private static final int TOTAL_NOTICE_LIST_NUM = 5;

    private static NoticeManager mNoticeManager = NoticeManager.getInstance();

    private NoticeManagerAsyncTask mNoticeManagerAsyncTask;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private ProgressBar mProgressBar;
    private int mNoticeType;
    private int mLoadedPageNum[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_list);
        if (getSupportActionBar() != null) {
//            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().hide();
        }
        Intent intent = getIntent();
        mNoticeType = intent.getIntExtra("noticeType", 0);
        mLoadedPageNum = new int[5];
        Arrays.fill(mLoadedPageNum, 1);
        mRecyclerAdapter = new RecyclerView.Adapter<NoticeListViewHolder>() {
            @Override
            public NoticeListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notice, parent, false);
                return new NoticeListViewHolder(view);
            }

            @Override
            public void onBindViewHolder(NoticeListViewHolder holder, int position) {
                holder.bind(position);
            }

            @Override
            public int getItemCount() {
                return mNoticeManager.getListCount(mNoticeType);
            }
        };
        mRecyclerView = (RecyclerView) findViewById(R.id.notice_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                super.onScrollStateChanged(recyclerView, scrollState);
                int lastVisibleItemPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount() - 1;
                if(lastVisibleItemPosition == itemTotalCount) {
                    mNoticeManagerAsyncTask = new NoticeManagerAsyncTask(getApplication(), (NoticeListActivity) recyclerView.getContext());
                    mNoticeManagerAsyncTask.execute(mLoadedPageNum[mNoticeType]++);
                }
            }
        });
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mProgressBar = findViewById(R.id.notice_list_progress_bar);
        mNoticeManagerAsyncTask = new NoticeManagerAsyncTask(getApplication(), this);
        mNoticeManagerAsyncTask.execute(mLoadedPageNum[mNoticeType]++);
    }

    public class NoticeListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mWriterTextView;
        private TextView RegistrationTextView;

        public NoticeListViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.list_item_notice_item_title);
            mWriterTextView = itemView.findViewById(R.id.list_item_notice_item_writer);
            RegistrationTextView = itemView.findViewById(R.id.list_item_notice_item_registration_date);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            mTitleTextView.setText(mNoticeManager.getListPageTitle(mNoticeType, position));
            mWriterTextView.setText(mNoticeManager.getListPageWriter(mNoticeType, position));
            RegistrationTextView.setText(mNoticeManager.getListPageRegistrationDate(mNoticeType, position));
        }

        @Override
        public void onClick(View view) {
//            WebView webview = new WebView(view.getContext());
//            setContentView(webview);
//
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemRangeChanged(0, getResources().getStringArray(R.array.food_corner_list).length);
        }
    }

    private static class NoticeManagerAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<NoticeListActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;

        // only retain a weak reference to the activity
        NoticeManagerAsyncTask(Application application, NoticeListActivity NoticeListActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(NoticeListActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            NoticeListActivity NoticeListActivity = activityReference.get();
            if (NoticeListActivity == null || NoticeListActivity.isFinishing()) return;
            ProgressBar progressBar = NoticeListActivity.findViewById(R.id.notice_list_progress_bar);
            RecyclerView recyclerView = NoticeListActivity.findViewById(R.id.notice_list_recycler_view);
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            mNoticeManager.getNotice(activityReference.get().mNoticeType, params[0]);
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            NoticeListActivity NoticeListActivity = activityReference.get();
            if (NoticeListActivity == null || NoticeListActivity.isFinishing()) return;
            if (result != -1) {
            } else {
                //예외 처리
                NoticeListActivity.showErrorMessage();
            }
            ProgressBar progressBar = NoticeListActivity.findViewById(R.id.notice_list_progress_bar);
            RecyclerView recyclerView = NoticeListActivity.findViewById(R.id.notice_list_recycler_view);
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }
}