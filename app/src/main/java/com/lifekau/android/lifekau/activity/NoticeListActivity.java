package com.lifekau.android.lifekau.activity;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.lifekau.android.lifekau.model.Notice;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoticeListActivity extends AppCompatActivity {

    private static final String NOTICE_TYPE = "notice_type";
    private static final int VIEW_ITEM = 0;
    private static final int VIEW_PROGRESS = 1;

    private NoticeManager mNoticeManager = NoticeManager.getInstance();

    private boolean mLoading;
    private int mNoticeType;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private NoticeManagerAsyncTask mNoticeManagerAsyncTask;

    public static Intent newIntent(Context context, int noticeType) {
        Intent intent = new Intent(context, ExaminationTimeTableActivity.class);
        intent.putExtra(NOTICE_TYPE, noticeType);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_list);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mLoading = false;
        Intent intent = getIntent();
//        mNoticeManager.clear(mNoticeType);
        mNoticeType = intent.getIntExtra(NOTICE_TYPE, 0);
        mSwipeRefreshLayout = findViewById(R.id.notice_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mNoticeManager.clear(mNoticeType);
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
        mRecyclerView = findViewById(R.id.notice_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                super.onScrollStateChanged(recyclerView, scrollState);
                int lastVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int itemTotalCount = recyclerView.getAdapter().getItemCount();
                if (!mLoading && lastVisibleItemPosition >= itemTotalCount - 2) {
                    mLoading = true;
                    executeAsyncTask();
                }
            }
        });
        mRecyclerView.setAdapter(mRecyclerAdapter);
        executeAsyncTask();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mNoticeManager.getAllPageFetched(mNoticeType)) mLoading = false;
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemRangeChanged(0, getResources().getStringArray(R.array.food_corner_list).length);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mNoticeManagerAsyncTask.cancel(true);
    }

    public void executeAsyncTask() {
        mNoticeManagerAsyncTask = new NoticeManagerAsyncTask(getApplication(), this);
        mNoticeManagerAsyncTask.execute();
    }

    public class NoticeListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mWriterTextView;
        private TextView RegistrationTextView;
        private TextView mIsNewTextView;
        private TextView mIsTopNoticeTextView;

        public NoticeListItemViewHolder(View itemView) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.list_item_notice_title);
            mWriterTextView = itemView.findViewById(R.id.list_item_notice_writer);
            RegistrationTextView = itemView.findViewById(R.id.list_item_notice_registration_date);
            mIsNewTextView = itemView.findViewById(R.id.list_item_notice_is_new_text_view);
            mIsTopNoticeTextView = itemView.findViewById(R.id.list_item_notice_top_notice);
            itemView.setOnClickListener(this);
        }

        public void bind(int position) {
            mIsNewTextView.setVisibility(View.GONE);
            mIsTopNoticeTextView.setVisibility(View.GONE);
            Notice notice = mNoticeManager.getNotice(mNoticeType, position);
            mTitleTextView.setText(notice.postTitle);
            mWriterTextView.setText(notice.writer);
            RegistrationTextView.setText(notice.RegistrationDate);

            Date today = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if(notice.RegistrationDate.equals(dateFormat.format(today).toString())){
                mIsNewTextView.setVisibility(View.VISIBLE);
            }
            if(notice.postNum == 0){
                mIsTopNoticeTextView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Intent intent = NoticePageActivity.newIntent(NoticeListActivity.this,
                        mNoticeManager.getURL(mNoticeType),
                        mNoticeManager.getPOST(mNoticeType, position));
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

    private static class NoticeManagerAsyncTask extends AsyncTask<Integer, Void, Integer> {

        private WeakReference<NoticeListActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;
        private int prevPageSize;

        NoticeManagerAsyncTask(Application application, NoticeListActivity noticeListActivity) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(noticeListActivity);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            NoticeListActivity noticeListActivity = activityReference.get();
            if (noticeListActivity == null || noticeListActivity.isFinishing()) return;
            prevPageSize = noticeListActivity.mNoticeManager.getListCount(noticeListActivity.mNoticeType);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            NoticeListActivity noticeListActivity = activityReference.get();
            Resources resources = applicationWeakReference.get().getResources();
            if (noticeListActivity == null || noticeListActivity.isFinishing()) return resources.getInteger(R.integer.unexpected_error);
            int result;
            int noticeType = noticeListActivity.mNoticeType;
            int count = 0;
            NoticeManager nm = noticeListActivity.mNoticeManager;
            while ((result = nm.pullNoticeList(applicationWeakReference.get(), noticeType)) != resources.getInteger(R.integer.no_error) && !isCancelled()) {
                if (result == resources.getInteger(R.integer.network_error)) {
                    sleep(3000);
                    count++;
                } else return result;
                if (count == resources.getInteger(R.integer.maximum_retry_num))
                    return resources.getInteger(R.integer.network_error);
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            final NoticeListActivity noticeListActivity = activityReference.get();
            if (noticeListActivity == null || noticeListActivity.isFinishing()) return;
            NoticeManager nm = noticeListActivity.mNoticeManager;
            int noticeType = noticeListActivity.mNoticeType;
            if (result != -1) {
                if (nm.getAllPageFetched(noticeType)) {
                    noticeListActivity.mRecyclerAdapter.notifyItemRemoved(nm.getListCount(noticeType) + 1);
                } else {
                    noticeListActivity.mLoading = false;
                    noticeListActivity.mRecyclerAdapter.notifyItemRangeChanged(prevPageSize - 1, nm.getListCount(noticeListActivity.mNoticeType));
                }
            } else {
                //예외 처리
                if (!nm.getAllPageFetched(noticeType))
                    noticeListActivity.mLoading = false;
                noticeListActivity.showErrorMessage();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        noticeListActivity.mSwipeRefreshLayout.setRefreshing(false);
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