package com.lifekau.android.lifekau.viewholder;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.lifekau.android.lifekau.DateDisplayer;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.HomeActivity;
import com.lifekau.android.lifekau.activity.PostDetailActivity;
import com.lifekau.android.lifekau.adapter.PostRecyclerAdapter;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.Post;

import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by sgc109 on 2018-02-11.
 */

public class PostViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView mTextView;
    public LinearLayout mCommentButtonContainer;
    public TextView mCommentCountTextView;
    private ImageView mLikeButtonImageView;
    private TextView mLikeButtonTextView;
    private ImageView mCommentButtonImageView;
    private TextView mCommentButtonTextView;
    private TextView mLikeCountTextView;
    private ImageView mCircleHeartImageView;
    private TextView mDateTextView;
    private LinearLayout mLikeButtonContainer;
    private ImageView mMoreButtonImageView;
    public LinearLayout mDeleteContainer;
    private LinearLayout mEditContainer;
    private Context mContext;
    private Post mPost;
    private String mPostKey;
    public BottomSheetDialog mBottomSheetDialog;
    private PostRecyclerAdapter mAdapter;
    private int mItemPosition;
    private boolean mIsInDetail;

    public PostViewHolder(View itemView, Context context, PostRecyclerAdapter adapter, boolean isInDetail) {
        super(itemView);
        mIsInDetail = isInDetail;
        mContext = context;
        View sheetView = LayoutInflater.from(mContext).inflate(R.layout.bottom_sheet_dialog_edit_and_delete, null);
        mDeleteContainer = sheetView.findViewById(R.id.fragment_community_bottom_sheet_delete);
        mEditContainer = sheetView.findViewById(R.id.fragment_community_bottom_sheet_edit);
        mTextView = itemView.findViewById(R.id.list_item_post_content_text_view);
        mLikeButtonImageView = itemView.findViewById(R.id.list_item_post_like_button_image_view);
        mLikeButtonTextView = itemView.findViewById(R.id.list_item_post_like_button_text_view);
        mCommentButtonImageView = itemView.findViewById(R.id.list_item_post_comment_button_image_view);
        mCommentButtonTextView = itemView.findViewById(R.id.list_item_post_comment_button_text_view);
        mCommentCountTextView = itemView.findViewById(R.id.list_item_post_comment_count_text_view);
        mLikeCountTextView = itemView.findViewById(R.id.list_item_post_like_count);
        mCircleHeartImageView = itemView.findViewById(R.id.list_item_post_circle_heart_image_view);
        mLikeButtonContainer = itemView.findViewById(R.id.list_item_post_like_button_container);
        mCommentButtonContainer = itemView.findViewById(R.id.list_item_post_comment_button_container);
        mDateTextView = itemView.findViewById(R.id.post_list_date_text_view);
        mMoreButtonImageView = itemView.findViewById(R.id.list_item_post_more_button);
        mAdapter = adapter;

        mDeleteContainer.setOnClickListener(this);
        mEditContainer.setOnClickListener(this);
        mCommentButtonContainer.setOnClickListener(this);
        if(!mIsInDetail) {
            mTextView.setOnClickListener(this);
        }
        mCommentCountTextView.setOnClickListener(this);
        mLikeButtonContainer.setOnClickListener(this);
        mMoreButtonImageView.setOnClickListener(this);

        mBottomSheetDialog = new BottomSheetDialog(mContext);
        mBottomSheetDialog.setContentView(sheetView);
    }

    public Post getPost() {
        return mPost;
    }

    public void bind(Post post, String postKey, int itemPosition) {
        mPost = post;
        mPostKey = postKey;
        mItemPosition = itemPosition;
        updateUI();
    }

    public void updateUI() {
        if(mIsInDetail || !shouldItBeFolded(mPost.text)){
            mTextView.setText(mPost.text);
        } else {
            String cut = cutString(mPost.text);
            SpannableString spannableString = new SpannableString(cut);
            String left = Html.toHtml(spannableString);
            left = left.replace("<p dir=\"ltr\">[\\s\\S]*</p>", "$1<br/>");
            String right = "...<font color='#A0A0A0'>계속 읽기</font>";
            mTextView.setText(Html.fromHtml(left + right));
        }
        mCommentCountTextView.setText(String.format(
                mContext.getString(R.string.post_comment_count),
                NumberFormat.getNumberInstance(Locale.US).format(mPost.commentCount)));
        mLikeCountTextView.setText(NumberFormat.getNumberInstance(Locale.US).format(mPost.likeCount));
        mDateTextView.setText(DateDisplayer.dateToStringFormat(new Date(mPost.date)));
        if (mPost.likeCount == 0) {
            mLikeCountTextView.setVisibility(View.GONE);
            mCircleHeartImageView.setVisibility(View.GONE);
        } else {
            mLikeCountTextView.setVisibility(View.VISIBLE);
            mCircleHeartImageView.setVisibility(View.VISIBLE);
        }
        if (mIsInDetail || mPost.commentCount == 0) {
            mCommentCountTextView.setVisibility(View.GONE);
        } else {
            mCommentCountTextView.setVisibility(View.VISIBLE);
        }

        if (mPost.likes.get(LoginManager.get(mContext).getStudentId()) != null) {
            mLikeButtonImageView.setImageResource(R.drawable.ic_heart);
            mLikeButtonImageView.setColorFilter(mContext.getResources().getColor(R.color.heart_color));
            mLikeButtonTextView.setTextColor(mContext.getResources().getColor(R.color.heart_color));
        } else {
            mLikeButtonImageView.setImageResource(R.drawable.ic_heart_empty);
            mLikeButtonImageView.setColorFilter(mContext.getResources().getColor(android.R.color.tab_indicator_text));
            mLikeButtonTextView.setTextColor(mContext.getResources().getColor(android.R.color.tab_indicator_text));
        }

        if (!LoginManager.get(mContext).getStudentId().equals(mPost.author)) {
            mMoreButtonImageView.setVisibility(View.GONE);
        }
    }

    private void onLikeClicked(DatabaseReference postRef) {
        final String studentId = LoginManager.get(mContext).getStudentId();
        postRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Post p = mutableData.getValue(Post.class);
                if (p == null) {
                    return Transaction.success(mutableData);
                }

                if (p.likes.containsKey(studentId)) {
                    p.likeCount--;
                    p.likes.remove(studentId);
                } else {
                    p.likeCount++;
                    p.likes.put(studentId, true);
                }

                mutableData.setValue(p);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d("sgc109_debug", "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void startDetailActivity(boolean hasClickedComment) {
        Intent intent = PostDetailActivity.newIntent(mContext, mPostKey, hasClickedComment, mItemPosition);
        ((Activity)mContext).startActivityForResult(intent, HomeActivity.REQUEST_POST_DETAIL);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.list_item_post_comment_button_container:
                startDetailActivity(true);
                break;
            case R.id.list_item_post_content_text_view:
                startDetailActivity(false);
                break;
            case R.id.list_item_post_comment_count_text_view:
                startDetailActivity(false);
                break;
            case R.id.list_item_post_like_button_container:
                String studentId = LoginManager.get(mContext).getStudentId();
                DatabaseReference postRef = FirebaseDatabase.getInstance().getReference()
                        .child(mContext.getString(R.string.firebase_database_posts))
                        .child(mPostKey);
                if (mPost.likes.containsKey(studentId)) {
                    mPost.likes.remove(studentId);
                    mPost.likeCount--;
                } else {
                    mPost.likes.put(studentId, true);
                    mPost.likeCount++;
                }
                onLikeClicked(postRef);
                updateUI();
                break;
            case R.id.list_item_post_more_button:
                mBottomSheetDialog.show();
                break;
            case R.id.fragment_community_bottom_sheet_delete:
                mBottomSheetDialog.dismiss();
                showYesOrNoDialog();
                break;
            case R.id.fragment_community_bottom_sheet_edit:
                mBottomSheetDialog.dismiss();
                break;
        }
    }

    private void showYesOrNoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AppCompatAlertDialogStyle);

        builder.setTitle(mContext.getString(R.string.post_delete_alert_dialog_title));
        builder.setMessage(mContext.getString(R.string.post_delete_alert_dialog_message));
        builder.setPositiveButton(mContext.getString(R.string.dialog_delete), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deletePost();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.dialog_cancel), null);
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
            }
        });
        dialog.show();
    }

    public void deletePost() {
        int position = getAdapterPosition();
        if (mAdapter != null) { // 글 목록에서 삭제할 때만 업뎃, 그외 엔 CommunityFragment 의 onActivityResult 업뎃해줘야함
            mAdapter.mPosts.remove(position);
            mAdapter.mPostKeys.remove(position);
            mAdapter.notifyItemRemoved(position);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        DatabaseReference postRef = ref
                .child(mContext.getString(R.string.firebase_database_posts))
                .child(mPostKey);
        postRef.removeValue();
        DatabaseReference postCommentRef = ref
                .child(mContext.getString(R.string.firebase_database_post_comments))
                .child(mPostKey);
        postCommentRef.removeValue();
    }

    private boolean shouldItBeFolded(String str){
        int cntNewLine = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == '\n') {
                cntNewLine++;
            }
        }
        int cnt = str.length() + cntNewLine * 5;
        if(cnt > 100 || cntNewLine > 5) return true;
        return false;
    }

    private String cutString(String str){
        int cntNewLine = 0;
        int cnt = 0;
        String ret = "";
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == '\n'){
                cntNewLine++;
                cnt += 6;
            } else {
                cnt++;
            }
            if(cnt > 100 || cntNewLine > 5) return ret;
            ret += str.charAt(i);
        }
        return ret;
    }
}