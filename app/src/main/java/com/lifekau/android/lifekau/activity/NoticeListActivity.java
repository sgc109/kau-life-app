package com.lifekau.android.lifekau.activity;

import android.app.Application;
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
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.NoticeManager;
import com.lifekau.android.lifekau.model.Notice;

import java.lang.ref.WeakReference;
import java.util.Arrays;

public class NoticeListActivity extends AppCompatActivity {

    private static final int TOTAL_NOTICE_LIST_NUM = 5;
    private static final int VIEW_ITEM = 0;
    private static final int VIEW_PROGRESS = 1;

    private static NoticeManager mNoticeManager = NoticeManager.getInstance();

    private boolean mLoading;
    private int mNoticeType;
    private int mLoadedPageNum[];
    private NoticeManagerAsyncTask mNoticeManagerAsyncTask;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_list);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mLoading = false;
        Intent intent = getIntent();
        mNoticeType = intent.getIntExtra("noticeType", 0);
        mLoadedPageNum = new int[TOTAL_NOTICE_LIST_NUM];
        Arrays.fill(mLoadedPageNum, 1);
        mRecyclerAdapter = new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view;
                RecyclerView.ViewHolder viewHolder;
                if (viewType == VIEW_ITEM) {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notice, parent, false);
                    viewHolder = new NoticeListItemViewHolder(view);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_progress, parent, false);
                    viewHolder = new NoticeListProgressViewHolder(view);
                }
                return viewHolder;
            }

            @Override
            public int getItemViewType(int position) {
                return mNoticeManager.getNotice(mNoticeType, position) != null ? VIEW_ITEM : VIEW_PROGRESS;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof NoticeListItemViewHolder)
                    ((NoticeListItemViewHolder) holder).bind(position);
                else ((NoticeListProgressViewHolder) holder).bind(position);
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
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount();
                if (!mLoading && lastVisibleItemPosition >= itemTotalCount - 2) {
                    mLoading = true;
                    mNoticeManagerAsyncTask = new NoticeManagerAsyncTask(getApplication(), (NoticeListActivity) recyclerView.getContext());
                    mNoticeManagerAsyncTask.execute(mLoadedPageNum[mNoticeType]);
                }
            }
        });
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mNoticeManagerAsyncTask = new NoticeManagerAsyncTask(getApplication(), this);
        mNoticeManagerAsyncTask.execute(mLoadedPageNum[mNoticeType]);
    }

    public class NoticeListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mWriterTextView;
        private TextView RegistrationTextView;

        public NoticeListItemViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.list_item_notice_title);
            mWriterTextView = itemView.findViewById(R.id.list_item_notice_writer);
            RegistrationTextView = itemView.findViewById(R.id.list_item_notice_registration_date);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            Notice notice = mNoticeManager.getNotice(mNoticeType, position);
            mTitleTextView.setText(notice.postTitle);
            mWriterTextView.setText(notice.writer);
            RegistrationTextView.setText(notice.RegistrationDate);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(view.getContext(), NoticePageActivity.class);
                intent.putExtra("URL", mNoticeManager.getURL(mNoticeType));
                intent.putExtra("POST", mNoticeManager.getPOST(mNoticeType, position));
                startActivity(intent);
            }
        }
    }

    public class NoticeListProgressViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar mProgressBar;

        public NoticeListProgressViewHolder(View progressView) {
            super(progressView);
            mProgressBar = progressView.findViewById(R.id.list_item_progress_bar);
        }

        public void bind(int position) {

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!mNoticeManager.getAllPageFetched(mNoticeType)) mLoading = false;
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemRangeChanged(0, getResources().getStringArray(R.array.food_corner_list).length);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNoticeManagerAsyncTask.cancel(true);
    }

    private static class NoticeManagerAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<NoticeListActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;
        private int prevPageSize;

        NoticeManagerAsyncTask(Application application, NoticeListActivity NoticeListActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(NoticeListActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            NoticeListActivity NoticeListActivity = activityReference.get();
            if (NoticeListActivity == null || NoticeListActivity.isFinishing()) return;
            prevPageSize = mNoticeManager.getListCount(NoticeListActivity.mNoticeType);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            NoticeListActivity NoticeListActivity = activityReference.get();
            while ((NoticeListActivity != null && !NoticeListActivity.isFinishing()) &&
                    mNoticeManager.getNoticeList(activityReference.get().mNoticeType, params[0]) == -1 && !isCancelled()) {
                Log.e("ERROR", "페이지 불러오기 실패!");
                sleep(3000);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            NoticeListActivity NoticeListActivity = activityReference.get();
            if (NoticeListActivity == null || NoticeListActivity.isFinishing()) return;
            int noticeType = NoticeListActivity.mNoticeType;
            if (result != -1) {
                if (mNoticeManager.getAllPageFetched(noticeType)) {
                    NoticeListActivity.mRecyclerAdapter.notifyItemRemoved(mNoticeManager.getListCount(noticeType) + 1);
                } else {
                    NoticeListActivity.mLoading = false;
                    NoticeListActivity.mLoadedPageNum[noticeType]++;
                    NoticeListActivity.mRecyclerAdapter.notifyItemRangeChanged(prevPageSize - 1, mNoticeManager.getListCount(NoticeListActivity.mNoticeType));
                }
            } else {
                //예외 처리
                if(!mNoticeManager.getAllPageFetched(noticeType)) NoticeListActivity.mLoading = false;
                NoticeListActivity.showErrorMessage();
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