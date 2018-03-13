package com.lifekau.android.lifekau.activity;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.lifekau.android.lifekau.AlarmJobService;
import com.lifekau.android.lifekau.R;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String SAVE_ALARM_STATE = "save_alarm_state";
    private static final String SAVE_SWITCH_ENTIRE_STATE = "save_switch_entire_state";
    private static final String SAVE_SWITCH_NOTICE_STATE = "save_switch_notice_state";
    private static final String SAVE_SWITCH_NOTICE_GENERAL_STATE = "save_switch_notice_general_state";
    private static final String SAVE_SWITCH_NOTICE_ACADEMIC_STATE = "save_switch_notice_academic_state";
    private static final String SAVE_SWITCH_NOTICE_SCHOLARSHIP_STATE = "save_switch_notice_scholarship_state";
    private static final String SAVE_SWITCH_NOTICE_CAREER_STATE = "save_switch_notice_career_state";
    private static final String SAVE_SWITCH_NOTICE_EVENT_STATE = "save_switch_notice_event_state";
    private static final String SAVE_SWITCH_SCHOLARSHIP_STATE = "save_switch_scholarship_state";
    private static final String SAVE_SWITCH_CURRENT_GRADE_STATE = "save_switch_current_grade_state";
    private static final String SAVE_SWITCH_EXAMINATION_TIMETABLE_STATE = "save_switch_examination_timetable_state";
    private static final String SAVE_SWITCH_LMS_STATE = "save_switch_lms_state";

    private Switch mSwitchEntire;
    private Switch mSwitchNotice;
    private Switch mSwitchNoticeGeneral;
    private Switch mSwitchNoticeAcademic;
    private Switch mSwitchNoticeScholarship;
    private Switch mSwitchNoticeCareer;
    private Switch mSwitchNoticeEvent;
    private Switch mSwitchScholarship;
    private Switch mSwitchCurrGrade;
    private Switch mSwitchExamTimetable;
    private Switch mSwitchLMS;
    private LinearLayout mLogoutContainer;
    private boolean mIsRunning;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("설정");
        }

        initUI();

        mSwitchEntire.setOnClickListener(this);
        mSwitchNotice.setOnClickListener(this);
        mSwitchNoticeGeneral.setOnClickListener(this);
        mSwitchNoticeAcademic.setOnClickListener(this);
        mSwitchNoticeScholarship.setOnClickListener(this);
        mSwitchNoticeCareer.setOnClickListener(this);
        mSwitchNoticeEvent.setOnClickListener(this);
        mSwitchScholarship.setOnClickListener(this);
        mSwitchCurrGrade.setOnClickListener(this);
        mSwitchExamTimetable.setOnClickListener(this);
        mSwitchLMS.setOnClickListener(this);
        mLogoutContainer.setOnClickListener(this);
    }

    private void initUI() {
        mSwitchEntire = findViewById(R.id.alarm_switch_entire);
        mSwitchNotice = findViewById(R.id.alarm_switch_notice);
        mSwitchNoticeGeneral = findViewById(R.id.alarm_switch_notice_general);
        mSwitchNoticeAcademic = findViewById(R.id.alarm_switch_notice_academic);
        mSwitchNoticeScholarship = findViewById(R.id.alarm_switch_notice_scholarship);
        mSwitchNoticeCareer = findViewById(R.id.alarm_switch_notice_career);
        mSwitchNoticeEvent = findViewById(R.id.alarm_switch_notice_event);
        mSwitchScholarship = findViewById(R.id.alarm_switch_scholarship);
        mSwitchCurrGrade = findViewById(R.id.alarm_switch_grade);
        mSwitchExamTimetable = findViewById(R.id.alarm_switch_exam_timetable);
        mSwitchLMS = findViewById(R.id.alarm_switch_lms);
        mLogoutContainer = findViewById(R.id.logout_container);
    }

    @Override
    public void onClick(View view) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_preference_app), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        boolean nowCheck;
        switch (view.getId()) {
            case R.id.alarm_switch_entire:
                nowCheck = mSwitchEntire.isChecked();
                mSwitchEntire.setChecked(nowCheck);
                mSwitchNotice.setChecked(nowCheck);
                mSwitchNoticeGeneral.setChecked(nowCheck);
                mSwitchNoticeAcademic.setChecked(nowCheck);
                mSwitchNoticeScholarship.setChecked(nowCheck);
                mSwitchNoticeCareer.setChecked(nowCheck);
                mSwitchNoticeEvent.setChecked(nowCheck);
                mSwitchScholarship.setChecked(nowCheck);
                mSwitchCurrGrade.setChecked(nowCheck);
                mSwitchExamTimetable.setChecked(nowCheck);
                mSwitchLMS.setChecked(nowCheck);
                editor.putBoolean(SAVE_SWITCH_ENTIRE_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_GENERAL_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_ACADEMIC_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_SCHOLARSHIP_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_CAREER_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_EVENT_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_SCHOLARSHIP_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_CURRENT_GRADE_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_EXAMINATION_TIMETABLE_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_LMS_STATE, nowCheck);
                break;
            case R.id.alarm_switch_notice:
                nowCheck = mSwitchNotice.isChecked();
                mSwitchNotice.setChecked(nowCheck);
                mSwitchNoticeGeneral.setChecked(nowCheck);
                mSwitchNoticeAcademic.setChecked(nowCheck);
                mSwitchNoticeScholarship.setChecked(nowCheck);
                mSwitchNoticeCareer.setChecked(nowCheck);
                mSwitchNoticeEvent.setChecked(nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_GENERAL_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_ACADEMIC_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_SCHOLARSHIP_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_CAREER_STATE, nowCheck);
                editor.putBoolean(SAVE_SWITCH_NOTICE_EVENT_STATE, nowCheck);
                break;
            case R.id.alarm_switch_notice_general:
                nowCheck = mSwitchNoticeGeneral.isChecked();
                editor.putBoolean(SAVE_SWITCH_NOTICE_GENERAL_STATE, nowCheck);
                break;
            case R.id.alarm_switch_notice_academic:
                nowCheck = mSwitchNoticeAcademic.isChecked();
                editor.putBoolean(SAVE_SWITCH_NOTICE_ACADEMIC_STATE, nowCheck);
                break;
            case R.id.alarm_switch_notice_scholarship:
                nowCheck = mSwitchNoticeScholarship.isChecked();
                editor.putBoolean(SAVE_SWITCH_NOTICE_SCHOLARSHIP_STATE, nowCheck);
                break;
            case R.id.alarm_switch_notice_career:
                nowCheck = mSwitchNoticeCareer.isChecked();
                editor.putBoolean(SAVE_SWITCH_NOTICE_CAREER_STATE, nowCheck);
                break;
            case R.id.alarm_switch_notice_event:
                nowCheck = mSwitchNoticeEvent.isChecked();
                editor.putBoolean(SAVE_SWITCH_NOTICE_EVENT_STATE, nowCheck);
                break;
            case R.id.alarm_switch_scholarship:
                nowCheck = mSwitchScholarship.isChecked();
                editor.putBoolean(SAVE_SWITCH_SCHOLARSHIP_STATE, nowCheck);
                break;
            case R.id.alarm_switch_grade:
                nowCheck = mSwitchCurrGrade.isChecked();
                editor.putBoolean(SAVE_SWITCH_CURRENT_GRADE_STATE, nowCheck);
                break;
            case R.id.alarm_switch_exam_timetable:
                nowCheck = mSwitchExamTimetable.isChecked();
                editor.putBoolean(SAVE_SWITCH_EXAMINATION_TIMETABLE_STATE, nowCheck);
                break;
            case R.id.alarm_switch_lms:
                nowCheck = mSwitchLMS.isChecked();
                editor.putBoolean(SAVE_SWITCH_LMS_STATE, nowCheck);
                break;
            case R.id.logout_container:
                editor.clear();
                editor.apply();
                Intent intent = LoginActivity.newIntent(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                JobScheduler jobScheduler = getSystemService(JobScheduler.class);
                if(jobScheduler != null) jobScheduler.cancel(0);
                startActivity(intent);
                finish();
                return;
        }
        editor.apply();
        boolean currAlarmState = sharedPref.getBoolean(SAVE_SWITCH_ENTIRE_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_NOTICE_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_NOTICE_GENERAL_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_NOTICE_ACADEMIC_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_NOTICE_SCHOLARSHIP_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_NOTICE_CAREER_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_NOTICE_EVENT_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_SCHOLARSHIP_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_CURRENT_GRADE_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_EXAMINATION_TIMETABLE_STATE, false) ||
                sharedPref.getBoolean(SAVE_SWITCH_LMS_STATE, false);
        JobScheduler jobScheduler = getSystemService(JobScheduler.class);
        if (jobScheduler != null) {
            if (currAlarmState) {
                mIsRunning = sharedPref.getBoolean(SAVE_ALARM_STATE, false);
                if(!mIsRunning) {
                    showToast("알람 서비스를 시작합니다!");
                    jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(this, AlarmJobService.class))
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setPeriodic(30 * 60 * 1000)
                            .build());
                }
            } else{
                mIsRunning = false;
                jobScheduler.cancel(0);
                showToast("알람 서비스를 종료합니다!");
            }
        } else {
            //초기화 실패
        }
        editor.putBoolean(SAVE_ALARM_STATE, currAlarmState);
        editor.apply();
    }

    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_preference_app), Context.MODE_PRIVATE);
        mSwitchEntire.setChecked(sharedPref.getBoolean(SAVE_SWITCH_ENTIRE_STATE, false));
        mSwitchNotice.setChecked(sharedPref.getBoolean(SAVE_SWITCH_NOTICE_STATE, false));
        mSwitchNoticeGeneral.setChecked(sharedPref.getBoolean(SAVE_SWITCH_NOTICE_GENERAL_STATE, false));
        mSwitchNoticeAcademic.setChecked(sharedPref.getBoolean(SAVE_SWITCH_NOTICE_ACADEMIC_STATE, false));
        mSwitchNoticeScholarship.setChecked(sharedPref.getBoolean(SAVE_SWITCH_NOTICE_SCHOLARSHIP_STATE, false));
        mSwitchNoticeCareer.setChecked(sharedPref.getBoolean(SAVE_SWITCH_NOTICE_CAREER_STATE, false));
        mSwitchNoticeEvent.setChecked(sharedPref.getBoolean(SAVE_SWITCH_NOTICE_EVENT_STATE, false));
        mSwitchScholarship.setChecked(sharedPref.getBoolean(SAVE_SWITCH_SCHOLARSHIP_STATE, false));
        mSwitchCurrGrade.setChecked(sharedPref.getBoolean(SAVE_SWITCH_CURRENT_GRADE_STATE, false));
        mSwitchExamTimetable.setChecked(sharedPref.getBoolean(SAVE_SWITCH_EXAMINATION_TIMETABLE_STATE, false));
        mSwitchLMS.setChecked(sharedPref.getBoolean(SAVE_SWITCH_LMS_STATE, false));
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
