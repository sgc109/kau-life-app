package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.lifekau.android.lifekau.R;

public class MainActivity extends AppCompatActivity {
    private static String EXTRA_NO_MAIN_IMAGE = "extra_no_main_image";
    public static Intent newIntent(Context context, boolean noMainImage){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NO_MAIN_IMAGE, true);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final boolean noMainImage = getIntent().getBooleanExtra(EXTRA_NO_MAIN_IMAGE, false);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, noMainImage ? 0 : 2000);
    }
}
