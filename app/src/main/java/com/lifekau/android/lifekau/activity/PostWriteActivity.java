package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.model.Post;

public class PostWriteActivity extends AppCompatActivity implements View.OnTouchListener, TextWatcher {

    private EditText mPostEditText;
    private ImageButton mBackButton;
    private TextView mSubmitButton;
    private DatabaseReference mDatabase;
    private boolean mPushed = false;

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
        DatabaseReference postsRef = mDatabase.child(getString(R.string.firebase_database_posts));
        String postKey = postsRef.push().getKey();
        postsRef.child(postKey).setValue(post);
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
        int textLen = mPostEditText.getText().length();
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
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int id = view.getId();
        int posX = (int) event.getRawX();
        int posY = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!view.isFocusable()) break;
                Log.d("fuck", "down!");
                mPushed = true;
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                break;
            case MotionEvent.ACTION_UP:
                if (!mPushed) break;
                Log.d("fuck", "up!");
                if (id == R.id.write_post_back_image_button) {
                    if (getValidCharCount(mPostEditText.getText().toString()) != 0) {
                        askDiscardTextOrNot();
                    } else {
                        finish();
                    }
                } else if (id == R.id.write_post_submit_button) {
                    mSubmitButton.setFocusable(false);
                    writePost(new Post("2012122327", mPostEditText.getText().toString()));
                    finish();
                }
                mPushed = false;
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                break;
            case MotionEvent.ACTION_MOVE:
                String who = view.getId() == R.id.write_post_back_image_button ? "back_button" : "submit_button";
                Log.d("fuck", "move! (" + who + ")");
                if (!isViewInBounds(view, posX, posY)) {
                    mPushed = false;
                    view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                }
                break;
        }
        return true;
    }

//    int dist

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
