package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.lifekau.android.lifekau.R;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private Switch mSwitchEntire;
    private Switch mSwitchNotice;
    private Switch mSwitchNoticeGeneral;
    private Switch mSwitchNoticeBachelor;
    private Switch mSwitchNoticeScholar;
    private Switch mSwitchNoticeJob;
    private Switch mSwitchNoticeEvent;
    private Switch mSwitchScholar;
    private Switch mSwitchGrade;
    private Switch mSwitchExamTime;
    private Switch mSwitchLMS;
    private LinearLayout mLogoutContainer;

    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if(getSupportActionBar() != null){
            getSupportActionBar().setTitle("설정");
        }

        initUI();

        mSwitchEntire.setOnClickListener(this);
        mSwitchNotice.setOnClickListener(this);
        mSwitchNoticeGeneral.setOnClickListener(this);
        mSwitchNoticeBachelor.setOnClickListener(this);
        mSwitchNoticeScholar.setOnClickListener(this);
        mSwitchNoticeJob.setOnClickListener(this);
        mSwitchNoticeEvent.setOnClickListener(this);
        mSwitchScholar.setOnClickListener(this);
        mSwitchGrade.setOnClickListener(this);
        mSwitchExamTime.setOnClickListener(this);
        mSwitchLMS.setOnClickListener(this);
        mLogoutContainer.setOnClickListener(this);
    }
    private void initUI(){
        mSwitchEntire = findViewById(R.id.alarm_switch_entire);
        mSwitchNotice = findViewById(R.id.alarm_switch_notice);
        mSwitchNoticeGeneral = findViewById(R.id.alarm_switch_notice_general);
        mSwitchNoticeBachelor = findViewById(R.id.alarm_switch_notice_bachelor);
        mSwitchNoticeScholar = findViewById(R.id.alarm_switch_notice_scholarship);
        mSwitchNoticeJob = findViewById(R.id.alarm_switch_notice_job);
        mSwitchNoticeEvent = findViewById(R.id.alarm_switch_notice_event);
        mSwitchScholar = findViewById(R.id.alarm_switch_scholarship);
        mSwitchGrade = findViewById(R.id.alarm_switch_grade);
        mSwitchExamTime = findViewById(R.id.alarm_switch_exam_timetable);
        mSwitchLMS = findViewById(R.id.alarm_switch_lms);
        mLogoutContainer = findViewById(R.id.logout_container);
    }

    @Override
    public void onClick(View view) {
        boolean nowCheck;
        switch (view.getId()){
            case R.id.alarm_switch_entire:
                nowCheck = mSwitchEntire.isChecked();
                mSwitchEntire.setChecked(nowCheck);
                mSwitchNotice.setChecked(nowCheck);
                mSwitchNoticeGeneral.setChecked(nowCheck);
                mSwitchNoticeBachelor.setChecked(nowCheck);
                mSwitchNoticeScholar.setChecked(nowCheck);
                mSwitchNoticeJob.setChecked(nowCheck);
                mSwitchNoticeEvent.setChecked(nowCheck);
                mSwitchScholar.setChecked(nowCheck);
                mSwitchGrade.setChecked(nowCheck);
                mSwitchExamTime.setChecked(nowCheck);
                mSwitchLMS.setChecked(nowCheck);
                break;
            case R.id.alarm_switch_notice:
                nowCheck = mSwitchNotice.isChecked();
                mSwitchNotice.setChecked(nowCheck);
                mSwitchNoticeGeneral.setChecked(nowCheck);
                mSwitchNoticeBachelor.setChecked(nowCheck);
                mSwitchNoticeScholar.setChecked(nowCheck);
                mSwitchNoticeJob.setChecked(nowCheck);
                mSwitchNoticeEvent.setChecked(nowCheck);
                break;
            case R.id.alarm_switch_notice_general:
                break;
            case R.id.alarm_switch_notice_bachelor:
                break;
            case R.id.alarm_switch_notice_scholarship:
                break;
            case R.id.alarm_switch_notice_job:
                break;
            case R.id.alarm_switch_notice_event:
                break;
            case R.id.alarm_switch_scholarship:
                break;
            case R.id.alarm_switch_grade:
                break;
            case R.id.alarm_switch_exam_timetable:
                break;
            case R.id.alarm_switch_lms:
                break;
            case R.id.logout_container:
                SharedPreferences sharedPref = getSharedPreferences(LoginActivity.mSharedPreferenceApp, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(LoginActivity.SAVE_AUTO_LOGIN, false);
                editor.apply();

                Intent intent = LoginActivity.newIntent(this);
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }
}
