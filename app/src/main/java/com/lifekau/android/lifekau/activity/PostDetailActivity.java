package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.lifekau.android.lifekau.DialogMaker;
import com.lifekau.android.lifekau.PxDpConverter;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.adapter.CommentRecyclerAdapter;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.Comment;
import com.lifekau.android.lifekau.model.Post;
import com.lifekau.android.lifekau.viewholder.CommentViewHolder;
import com.lifekau.android.lifekau.viewholder.PostViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostDetailActivity extends AppCompatActivity implements OnClickListener, SwipeRefreshLayout.OnRefreshListener, TextWatcher {
    private static final String EXTRA_HAS_CLICKED_COMMENT = "extra_has_clicked_comment";
    private static String EXTRA_POST_KEY = "extra_post_key";
    private final int NUM_OF_COMMENT_PER_PAGE = 5;
    private int mTotalItemCount = 0;
    private int mLastVisibleItemPosition;
    private boolean mIsLoading;
    private LinearLayout mPostContainer;
    private View mBottomMarginView;
    private TextView mTextView;
    private PostViewHolder mPostViewHolder;
    private String mPostKey;
    private Post mPost;
    private DatabaseReference mPostRef;
    private DatabaseReference mCommentsRef;
    private RecyclerView mRecyclerView;
    private CommentRecyclerAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ImageView mCommentSubmitImageView;
    private EditText mCommentEditText;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NestedScrollView mNestedScrollView;
    private ImageView mMoreCommentsImageView;
    private ProgressBar mProgressBar;
    private TextView mMoreCommentsTextView;
    private ProgressBar mMoreCommentsProgressBar;
    private LinearLayout mMoreCommentsLinearLayout;
    private RelativeLayout mWriteCommentBar;
    private boolean mHasClickedComment;
    private boolean mJustWroteComment;
    private ContextMenu.ContextMenuInfo mMenuInfo;


    public static Intent newIntent(Context context, String postKey, boolean hasClickedComment) {
        Intent intent = new Intent(context, PostDetailActivity.class);
        intent.putExtra(EXTRA_POST_KEY, postKey);
        intent.putExtra(EXTRA_HAS_CLICKED_COMMENT, hasClickedComment);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        mHasClickedComment = getIntent().getBooleanExtra(EXTRA_HAS_CLICKED_COMMENT, false);
        initializeViews();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mPostRef = database
                .child(getString(R.string.firebase_database_posts))
                .child(mPostKey);
        mCommentsRef = database
                .child(getString(R.string.firebase_database_post_comments))
                .child(mPostKey);

        mPostViewHolder.mDeleteContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostViewHolder.mBottomSheetDialog.dismiss();
                showPostDeleteYesOrNoDialog();
            }
        });
        mCommentEditText.requestFocus();
        mProgressBar.setVisibility(View.VISIBLE);
        mNestedScrollView.setVisibility(View.GONE);
        mWriteCommentBar.setVisibility(View.GONE);
        mBottomMarginView.setVisibility(View.GONE);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setItemAnimator(null);
        mNestedScrollView.setSmoothScrollingEnabled(true);
        mMoreCommentsImageView.setOnClickListener(this);
        mMoreCommentsTextView.setOnClickListener(this);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(true, PxDpConverter.convertDpToPx(72), PxDpConverter.convertDpToPx(100));
        mCommentEditText.addTextChangedListener(this);
        registerForContextMenu(mRecyclerView);
        initPost();
    }

    private void getComments() {
        Query query = mCommentsRef.orderByKey();
        if (mAdapter.getItemCount() != 0) query = query.endAt(mAdapter.getLastKey());
        query = query.limitToLast(NUM_OF_COMMENT_PER_PAGE + 1);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Comment> newComments = new ArrayList<>();
                List<String> newCommentKeys = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    newComments.add(snapshot.getValue(Comment.class));
                    newCommentKeys.add(snapshot.getKey());
                }
                if (newCommentKeys.size() <= NUM_OF_COMMENT_PER_PAGE) {
                    mMoreCommentsLinearLayout.setVisibility(View.GONE);
                } else {
                    mMoreCommentsLinearLayout.setVisibility(View.VISIBLE);

                    newComments = newComments.subList(newComments.size() - NUM_OF_COMMENT_PER_PAGE, newComments.size());
                    newCommentKeys = newCommentKeys.subList(newCommentKeys.size() - NUM_OF_COMMENT_PER_PAGE, newCommentKeys.size());
                }

                if (mAdapter.getItemCount() != 0) {
                    newComments.remove(newComments.size() - 1);
                    newCommentKeys.remove(newCommentKeys.size() - 1);
                }
                Collections.reverse(newComments);
                Collections.reverse(newCommentKeys);

                mAdapter.addAll(newComments, newCommentKeys);
                mIsLoading = false;

                mMoreCommentsProgressBar.setVisibility(View.GONE);
                mMoreCommentsImageView.setVisibility(View.VISIBLE);
                mMoreCommentsTextView.setText(R.string.see_previous_comments);

                if (mJustWroteComment) {
                    mJustWroteComment = false;
                    mNestedScrollView.fullScroll(View.FOCUS_DOWN);
                }

                mProgressBar.setVisibility(View.GONE);
                mWriteCommentBar.setVisibility(View.VISIBLE);
                mNestedScrollView.setVisibility(View.VISIBLE);

                if (mHasClickedComment) {
                    showSoftKeyboard();
                    focusCommentEditText();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("sgc109_debug", "PostDetailActivity:onCancelled");
                mJustWroteComment = false;
            }
        });
    }

    private void initPost() {
        mPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("sgc109_debug", "PostDetailActivity:onDataChange");
                Post post = dataSnapshot.getValue(Post.class);
                if(mPost != null && post != null && !mJustWroteComment) { // 댓글 삭제한거
                    return;
                }
                if (post == null) {
                    if (mPostViewHolder.getPost().author.equals(LoginManager.get(PostDetailActivity.this).getStudentId())) {
                        Toast.makeText(PostDetailActivity.this, getString(R.string.post_deleted_message), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(PostDetailActivity.this, getString(R.string.post_deleted_by_author), Toast.LENGTH_SHORT).show();
                    }
                    PostDetailActivity.this.finish();
                    return;
                }
                mPost = post;
                mPostViewHolder.bind(post, mPostKey);
                mPostViewHolder.mCommentButtonContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusCommentEditText();
                        showSoftKeyboard();
                    }
                });
                mPostViewHolder.mCommentCountTextView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        focusCommentEditText();
                    }
                });
                initComments();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("sgc109_debug", "PostDetailActivity:onCancelled");
            }
        });
    }

    void focusCommentEditText() {
        mCommentEditText.requestFocus();
        mNestedScrollView.fullScroll(View.FOCUS_DOWN);
    }

    void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mCommentEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void initComments() {
        mAdapter = new CommentRecyclerAdapter(mPostKey, this, mPost.author);
        mRecyclerView.setAdapter(mAdapter);
        mIsLoading = true;
        getComments();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initializeViews() {
        mWriteCommentBar = findViewById(R.id.write_comment_bar);
        mProgressBar = findViewById(R.id.post_detail_progress_bar);
        mMoreCommentsLinearLayout = findViewById(R.id.more_comments_linear_layout);
        mMoreCommentsProgressBar = findViewById(R.id.more_comments_progress_bar);
        mMoreCommentsTextView = findViewById(R.id.more_comments_text_view);
        mMoreCommentsImageView = findViewById(R.id.more_comments_refresh_image_view);
        mNestedScrollView = findViewById(R.id.post_detail_nested_scroll_view);
        mCommentSubmitImageView = findViewById(R.id.post_detail_comment_send_image_view);
        mCommentEditText = findViewById(R.id.post_detail_comment_edit_text);
        mRecyclerView = findViewById(R.id.post_detail_recycler_view);
        mPostContainer = findViewById(R.id.post_detail_post_container);
        mPostViewHolder = new PostViewHolder(mPostContainer, this, null, true);
        mBottomMarginView = mPostContainer.findViewById(R.id.list_item_post_bottom_margin_view);
        mSwipeRefreshLayout = findViewById(R.id.post_detail_swipe_refresh_layout);
    }

    private void showPostDeleteYesOrNoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

        builder.setTitle(getString(R.string.post_delete_alert_dialog_title));
        builder.setMessage(getString(R.string.post_delete_alert_dialog_message));
        builder.setPositiveButton(getString(R.string.dialog_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mPostViewHolder.deletePost();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.post_detail_comment_send_image_view:
                writeComment(new Comment(LoginManager.get(this).getStudentId(), mCommentEditText.getText().toString()));
                break;
            case R.id.more_comments_refresh_image_view:
                pressedMoreComments();
                break;
            case R.id.more_comments_text_view:
                pressedMoreComments();
                break;
        }
    }

    private void pressedMoreComments() {
        mMoreCommentsImageView.setVisibility(View.GONE);
        mMoreCommentsProgressBar.setVisibility(View.VISIBLE);
        mMoreCommentsTextView.setText(R.string.reading_more_comments);
        getComments();
    }

    private void writeComment(Comment comment) {
        mCommentsRef.push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                changeCommentCount(1);
                // 여기 장치 회전된다거나 갑자기 어플죽으면 파베디비에 댓글은 써졌는데 댓글개수가 업뎃이 안되는 치명적인 일이 생길 수 있기 때문에
                // 스레드를 만들어서 돌려야할 수도 있을것같음.. 아니면
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                    }
//                }, 0);
                mJustWroteComment = true;
                mCommentEditText.setText("");
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mCommentEditText.getWindowToken(), 0);

                Toast.makeText(PostDetailActivity.this,
                        getString(R.string.successfully_comment_registered),
                        Toast.LENGTH_SHORT).
                        show();

            }
        });
    }

    private void changeCommentCount(final int size) {
        mPostRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }
                p.commentCount += size;

                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                Log.d("sgc109_debug", "commentTransaction:onComplete:" + databaseError);
//                updatePost();
            }
        });
    }

    @Override
    public void onRefresh() {
        if (mIsLoading) return;
        initPost();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 500);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (editable.toString().length() > 0) {
            mCommentSubmitImageView.setColorFilter(getResources().getColor(R.color.colorPrimary));
            mCommentSubmitImageView.setOnClickListener(this);
        } else {
            mCommentSubmitImageView.setColorFilter(getResources().getColor(R.color.bright_gray));
            mCommentSubmitImageView.setOnClickListener(null);
        }

        int limitTextCnt = getResources().getInteger(R.integer.comment_write_text_limit);
        if(editable.length() > limitTextCnt) {
            String toastMsg = String.format(getString(R.string.text_limit_message), limitTextCnt);
            DialogMaker.showOkButtonDialog(this, toastMsg);
            editable.delete(limitTextCnt, editable.length());
            return;
        }
    }

    private void deleteComment(int position){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(getString(R.string.firebase_database_post_comments))
                .child(mPostKey)
                .child(mAdapter.mCommentKeys.get(position));
        ref
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        changeCommentCount(-1);
                    }
                });
        mAdapter.mComments.remove(position);
        mAdapter.mCommentKeys.remove(position);
        mAdapter.notifyItemRemoved(position);
        if(mAdapter.mComments.size() == 0) {
            getComments();
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getOrder();
        switch (item.getItemId()) {
            case CommentViewHolder.ITEM_ID_DELETE_COMMENT:
                showCommentDeleteYesOrNoDialog(id);
                break;
            case CommentViewHolder.ITEM_ID_EDIT_COMMENT:
                break;

        }
        return super.onContextItemSelected(item);
    }

    private void showCommentDeleteYesOrNoDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);

        builder.setTitle(getString(R.string.comment_delete_alert_dialog_title));
        builder.setMessage(getString(R.string.comment_delete_alert_dialog_message));
        builder.setPositiveButton(getString(R.string.dialog_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteComment(position);
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }
        });
        dialog.show();
    }
}
