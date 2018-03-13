package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lifekau.android.lifekau.DialogMaker;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.model.Post;

import java.util.Date;

public class PostWriteActivity extends AppCompatActivity implements View.OnTouchListener, TextWatcher {

    private static final String SHARED_LAST_WRITE_TIME = "shared_last_write_time";
    private EditText mPostEditText;
    private ImageButton mBackButton;
    private TextView mSubmitButton;
    private DatabaseReference mDatabase;
    private boolean mPushed = false;
    private String mSharedPreferenceApp;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, PostWriteActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_write);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
      
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mSharedPreferenceApp = getString(R.string.shared_preference_app);
        mPostEditText = findViewById(R.id.write_post_edit_text);
        mBackButton = findViewById(R.id.write_post_back_image_button);
        mSubmitButton = findViewById(R.id.write_post_submit_button);

        mBackButton.setBackgroundResource(R.color.colorPrimary);
        mSubmitButton.setBackgroundResource(R.color.colorPrimary);

        mBackButton.setOnTouchListener(this);
        mSubmitButton.setOnTouchListener(this);
        mPostEditText.addTextChangedListener(this);
    }

    private void writePost(Post post) {
        setResult(RESULT_OK);
        DatabaseReference postsRef = mDatabase.child(getString(R.string.firebase_database_posts));
        String postKey = postsRef.push().getKey();
        postsRef
                .child(postKey)
                .setValue(post);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(SHARED_LAST_WRITE_TIME, new Date().getTime());
        editor.apply();
    }

    private void askDiscardTextOrNot() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        finish();
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(PostWriteActivity.this);
        builder.setMessage(getString(R.string.text_gone_warning_message)).setPositiveButton(getString(R.string.yes), dialogClickListener)
                .setNegativeButton(getString(R.string.no), dialogClickListener).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int textLen = getValidCharCount(mPostEditText.getText().toString());
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (textLen != 0) {
                askDiscardTextOrNot();
            } else {
                finish();
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        if (getValidCharCount(editable.toString()) == 0) {
            mSubmitButton.setTextColor(Color.GRAY);
            mSubmitButton.setFocusable(false);
        } else {
            mSubmitButton.setTextColor(Color.WHITE);
            mSubmitButton.setFocusable(true);
        }

        int limitTextCnt = getResources().getInteger(R.integer.post_write_text_limit);
        if(editable.length() > limitTextCnt) {
            String toastMsg = String.format(getString(R.string.text_limit_message), limitTextCnt);
            DialogMaker.showOkButtonDialog(this, toastMsg);
            editable.delete(limitTextCnt, editable.length());
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int id = view.getId();
        int posX = (int) event.getRawX();
        int posY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!view.isFocusable()) break;
                mPushed = true;
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                break;
            case MotionEvent.ACTION_UP:
                if (!mPushed) break;
                if (id == R.id.write_post_back_image_button) {
                    if (getValidCharCount(mPostEditText.getText().toString()) != 0) {
                        askDiscardTextOrNot();
                    } else {
                        finish();
                    }
                } else if (id == R.id.write_post_submit_button) {
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    long lastTime = sharedPref.getLong(SHARED_LAST_WRITE_TIME, 0);
                    if(new Date().getTime() - lastTime < 60 * 1000) {
                        Toast.makeText(this, getString(R.string.can_write_post_after_1_minute), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    mSubmitButton.setFocusable(false);
                    writePost(new Post(LoginManager.get(this).getStudentId(), compressText(mPostEditText.getText().toString())));
                    Toast.makeText(this, getString(R.string.post_write_success_message), Toast.LENGTH_SHORT).show();
                    finish();
                }
                mPushed = false;
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isViewInBounds(view, posX, posY)) {
                    mPushed = false;
                    view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
        }
        return true;
    }

    private String compressText(String s) {
        String ret = "";
        boolean charAppear = false;
        for(int i = 0; i < s.length(); i++){
            char ch = s.charAt(i);
            if(ch != ' ' && ch != '\n') {
                charAppear = true;
            } else if(!charAppear) {
                continue;
            }
            ret += ch;
        }
        ret = ret.replaceAll("\n{3,}", "\n\n");
        return ret;
    }

    private int getValidCharCount(String str) {
        int ret = 0;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            if (ch != ' ' && ch != '\n') ret++;
        }
        return ret;
    }

    private boolean isViewInBounds(View view, int x, int y) {
        Rect outRect = new Rect();
        int[] location = new int[2];

        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }
}
