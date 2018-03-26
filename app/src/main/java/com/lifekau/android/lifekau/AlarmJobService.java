package com.lifekau.android.lifekau;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.lifekau.android.lifekau.activity.CurrentGradeActivity;
import com.lifekau.android.lifekau.activity.ExaminationTimeTableActivity;
import com.lifekau.android.lifekau.activity.HomeActivity;
import com.lifekau.android.lifekau.activity.ScholarshipActivity;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.manager.LoginManager;
import com.lifekau.android.lifekau.manager.NoticeManager;

public class AlarmJobService extends JobService {

    private static final int ALARM_ID = 312;
    private static final int TOTAL_NOTICE_NUM = 5;
    private static final String SAVE_SCHOLARSHIP_ITEM_NUM = "shared_preferences_save_scholarship_item_num";
    private static final String SAVE_CURRENT_GRADE_ITEM_NUM = "shared_preferences_save_ient_grade_item_num";
    private static final String SAVE_EXAMINATION_TIMETABLE_ITEM_NUM = "shared_preferences_save_examination_timetable_item_num";
    private static final String SAVE_GUID = "shared_preferences_globally_unique_identifier";
    private static final String SAVE_ID = "shared_preferences_save_id";
    private static final String SAVE_PASSWORD = "shared_preferences_save_password";
    private static final String SAVE_SWITCH_NOTICE_GENERAL_STATE = "save_switch_notice_general_state";
    private static final String SAVE_SWITCH_NOTICE_ACADEMIC_STATE = "save_switch_notice_academic_state";
    private static final String SAVE_SWITCH_NOTICE_SCHOLARSHIP_STATE = "save_switch_notice_scholarship_state";
    private static final String SAVE_SWITCH_NOTICE_CAREER_STATE = "save_switch_notice_career_state";
    private static final String SAVE_SWITCH_NOTICE_EVENT_STATE = "save_switch_notice_event_state";
    private static final String SAVE_SWITCH_SCHOLARSHIP_STATE = "save_switch_scholarship_state";
    private static final String SAVE_SWITCH_CURRENT_GRADE_STATE = "save_switch_current_grade_state";
    private static final String SAVE_SWITCH_EXAMINATION_TIMETABLE_STATE = "save_switch_examination_timetable_state";
    private static final String SAVE_SWITCH_LMS_STATE = "save_switch_lms_state";
    private static final String[] SAVE_SWITCH_NOTICE_LIST = {
            SAVE_SWITCH_NOTICE_GENERAL_STATE,
            SAVE_SWITCH_NOTICE_ACADEMIC_STATE,
            SAVE_SWITCH_NOTICE_SCHOLARSHIP_STATE,
            SAVE_SWITCH_NOTICE_CAREER_STATE,
            SAVE_SWITCH_NOTICE_EVENT_STATE
    };
    private static final String[] NOTICE_NAME = {"일반공지, ", "학사공지, ", "장학/대출공지, ", "취업공지, ", "행사공지, "};
    private static final String[] SAVE_NOTICE_LIST_NUM = {
            "shared_preferences_save_notice_general_list_latest_num",
            "shared_preferences_save_notice_academic_list_latest_num",
            "shared_preferences_save_notice_scholarship_list_latest_num",
            "shared_preferences_save_notice_career_list_latest_num",
            "shared_preferences_save_notice_event_list_latest_num"
    };

    JobParameters mParams;
    AlarmAsyncTask mAlarmAsyncTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        mParams = params;
        mAlarmAsyncTask = new AlarmAsyncTask();
        mAlarmAsyncTask.execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mAlarmAsyncTask != null) mAlarmAsyncTask.cancel(true);
        return false;
    }

    private class AlarmAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Resources resources = getResources();
            SharedPreferences sharedPref = getSharedPreferences(getString(R.string.shared_preference_app), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NoticeManager nm = NoticeManager.getInstance();
            String text = "";
            int mResult = resources.getInteger(R.integer.no_error);
            for (int i = 0; i < TOTAL_NOTICE_NUM; i++) {
                if (!sharedPref.getBoolean(SAVE_SWITCH_NOTICE_LIST[i], false)) continue;
                for (int count = 0; count < 3; count++) {
                    if ((mResult = nm.pullNoticeList(getApplicationContext(), i, 1)) == resources.getInteger(R.integer.no_error))
                        break;
                }
                if (mResult != resources.getInteger(R.integer.no_error)) continue;
                int currLatestNum = nm.getLatestDetailPageNum(i);
                int prevLatestNum = sharedPref.getInt(SAVE_NOTICE_LIST_NUM[i], -1);
                if (currLatestNum > prevLatestNum) {
                    if(prevLatestNum != -1) text = text + NOTICE_NAME[i] + " ";
                    editor.putInt(SAVE_NOTICE_LIST_NUM[i], currLatestNum);
                    editor.apply();
                }
            }
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 3);
                Intent intent = HomeActivity.newIntent(getApplicationContext(), 2);
                notificationManager.notify(0, buildNotification(getApplicationContext(), "공지 알람", "새로운 " + text + "가 등록되었습니다!", intent));
            }
            LMSPortalManager pm = LMSPortalManager.getInstance();
            boolean neededLogin = sharedPref.getBoolean(SAVE_SWITCH_SCHOLARSHIP_STATE, false) ||
                    sharedPref.getBoolean(SAVE_SWITCH_CURRENT_GRADE_STATE, false) ||
                    sharedPref.getBoolean(SAVE_SWITCH_EXAMINATION_TIMETABLE_STATE, false) ||
                    sharedPref.getBoolean(SAVE_SWITCH_LMS_STATE, false);
            if (neededLogin) {
                for (int count = 0; count < 3; count++) {
                    LoginManager lm = LoginManager.get(getApplicationContext());
                    if(pm.pullStudentId(getApplicationContext()) == resources.getInteger(R.integer.no_error) &&
                            pm.pullPortalSession(getApplicationContext(), lm.getUserId(), lm.getPassword()) == resources.getInteger(R.integer.no_error)){
                        mResult = resources.getInteger(R.integer.no_error);
                        break;
                    }
                }
            } else mResult = resources.getInteger(R.integer.missing_data_error);
            if (mResult != resources.getInteger(R.integer.no_error)) {
                String uniqueID = sharedPref.getString(SAVE_GUID, null);
                if (uniqueID == null) return null;
                String id = AdvancedEncryptionStandard.decrypt(sharedPref.getString(SAVE_ID, ""), uniqueID);
                String password = AdvancedEncryptionStandard.decrypt(sharedPref.getString(SAVE_PASSWORD, ""), uniqueID);
                int mNextResult = resources.getInteger(R.integer.no_error);
                for (int count = 0; count < 3; count++) {
                    if ((mNextResult = pm.pullSession(getApplicationContext(), id, password)) == resources.getInteger(R.integer.no_error))
                        break;
                }
                if (mNextResult != resources.getInteger(R.integer.no_error)) return null;
            }
            if (sharedPref.getBoolean(SAVE_SWITCH_SCHOLARSHIP_STATE, false)) {
                for (int count = 0; count < 3; count++) {
                    if ((mResult = pm.pullScholarship(getApplicationContext())) == resources.getInteger(R.integer.no_error))
                        break;
                }
            } else mResult = resources.getInteger(R.integer.missing_data_error);
            if (mResult == resources.getInteger(R.integer.no_error)) {
                int currItemNum = pm.getScholarshipSize();
                int prevItemNum = sharedPref.getInt(SAVE_SCHOLARSHIP_ITEM_NUM, -1);
                if (currItemNum > prevItemNum) {
                    Intent intent = ScholarshipActivity.newIntent(getApplicationContext());
                    if(prevItemNum != -1) notificationManager.notify(1, buildNotification(getApplicationContext(), "장학금 알람", "새로운 장학금이 등록되었습니다!", intent));
                    editor.putInt(SAVE_SCHOLARSHIP_ITEM_NUM, currItemNum);
                    editor.apply();
                }
            }
            if (sharedPref.getBoolean(SAVE_SWITCH_CURRENT_GRADE_STATE, false)) {
                for (int count = 0; count < 3; count++) {
                    if ((mResult = pm.pullCurrentGrade(getApplicationContext())) == resources.getInteger(R.integer.no_error))
                        break;
                }
            } else mResult = resources.getInteger(R.integer.missing_data_error);
            if (mResult != resources.getInteger(R.integer.no_error)) {
                int currItemNum = pm.getRegisteredCurrentGradeItemNum();
                int prevItemNum = sharedPref.getInt(SAVE_CURRENT_GRADE_ITEM_NUM, -1);
                if (currItemNum > prevItemNum) {
                    Intent intent = CurrentGradeActivity.newIntent(getApplicationContext());
                    if(prevItemNum != -1) notificationManager.notify(2, buildNotification(getApplicationContext(), "성적 알람", "새로운 성적이 등록되었습니다!", intent));
                    editor.putInt(SAVE_CURRENT_GRADE_ITEM_NUM, currItemNum);
                    editor.apply();
                }
            }
            if (sharedPref.getBoolean(SAVE_EXAMINATION_TIMETABLE_ITEM_NUM, false)) {
                for (int count = 0; count < 3; count++) {
                    if ((mResult = pm.pullExaminationTimeTable(getApplicationContext())) == resources.getInteger(R.integer.no_error))
                        break;
                }
            } else mResult = resources.getInteger(R.integer.missing_data_error);
            if (mResult == resources.getInteger(R.integer.no_error)) {
                int currItemNum = pm.getRegisteredExaminationTimeTableItemNum();
                int prevItemNum = sharedPref.getInt(SAVE_EXAMINATION_TIMETABLE_ITEM_NUM, -1);
                if (currItemNum > prevItemNum) {
                    Intent intent = ExaminationTimeTableActivity.newIntent(getApplicationContext());
                    if(prevItemNum != -1) notificationManager.notify(3, buildNotification(getApplicationContext(), "시험시간표 알람", "시험시간표가 등록되었습니다!", intent));
                    editor.putInt(SAVE_EXAMINATION_TIMETABLE_ITEM_NUM, currItemNum);
                    editor.apply();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            jobFinished(mParams, false);
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public Notification buildNotification(Context context, String title, String text, Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }
}
