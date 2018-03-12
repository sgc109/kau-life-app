package com.lifekau.android.lifekau;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.lifekau.android.lifekau.activity.HomeActivity;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.manager.NoticeManager;

public class AlarmJobService extends JobService {

    private static final int ALARM_ID = 312;
    private static final int TOTAL_NOTICE_NUM = 5;
    private static final String[] SAVE_NOTICE_LIST_NUM = {
            "shared_preferences_save_notice_general_list_latest_num",
            "shared_preferences_save_notice_academic_list_latest_num",
            "shared_preferences_save_notice_scholarship_list_latest_num",
            "shared_preferences_save_notice_career_list_latest_num",
            "shared_preferences_save_notice_event_list_latest_num"
    };
    private static final String[] NOTICE_NAME = {"일반공지, ", "학사공지, ", "장학/대출공지, ", "취업공지, ", "행사공지, "};
    private static final String SAVE_SCHOLARSHIP_ITEM_NUM = "shared_preferences_save_scholarship_item_num";
    private static final String SAVE_CURRENT_GRADE_ITEM_NUM = "shared_preferences_save_ient_grade_item_num";
    private static final String SAVE_EXAMINATION_TIME_TABLE_ITEM_NUM = "shared_preferences_save_examination_time_table_item_num";
    private static final String SAVE_GUID = "shared_preferences_globally_unique_identifier";
    private static final String SAVE_ID = "shared_preferences_save_id";
    private static final String SAVE_PASSWORD = "shared_preferences_save_password";

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
        if(mAlarmAsyncTask != null) mAlarmAsyncTask.cancel(true);
        return false;
    }

    private class AlarmAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            SharedPreferences sharedPreferences = getSharedPreferences("LifeKAU", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NoticeManager nm = NoticeManager.getInstance();
            String text = "";
            int mResult = 0;
            for (int i = 0; i < TOTAL_NOTICE_NUM; i++) {
                if (false) continue;
                for (int count = 0; count < 3; count++) {
                    if ((mResult = nm.pullNoticeList(getApplicationContext(), i, 1)) == 0) break;
                }
                if (mResult != 0) continue;
                int currLatestNum = nm.getLatestDetailPageNum(i);
                int prevLatestNum = sharedPreferences.getInt(SAVE_NOTICE_LIST_NUM[i], -1);
                if (prevLatestNum != -1 && currLatestNum > prevLatestNum) {
                    text = text + NOTICE_NAME[i] + " ";
                }
                editor.putInt(SAVE_NOTICE_LIST_NUM[i], currLatestNum);
                editor.apply();
            }
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 2);
                notificationManager.notify(0, buildNotification(getApplicationContext(), "공지 알람", "새로운" + text + "가 등록되었습니다!"));
            }
            LMSPortalManager pm = LMSPortalManager.getInstance();
            if (true) {
                for (int count = 0; count < 3; count++) {
                    if ((mResult = pm.pullStudentId(getApplicationContext())) == 0) break;
                }
            } else ;
            if (mResult != 0) {
                String uniqueID = sharedPreferences.getString(SAVE_GUID, null);
                String id = AdvancedEncryptionStandard.decrypt(sharedPreferences.getString(SAVE_ID, ""), uniqueID);
                String password = AdvancedEncryptionStandard.decrypt(sharedPreferences.getString(SAVE_PASSWORD, ""), uniqueID);
                int mNextResult = 0;
                for (int count = 0; count < 3; count++) {
                    if ((mNextResult = pm.pullSession(getApplicationContext(), id, password)) == 0)
                        break;
                }
                if (mNextResult != 0) return null;
            }
            if(true) {
                for (int count = 0; count < 3; count++) {
                    if ((mResult = pm.pullScholarship(getApplicationContext())) == 0) break;
                }
            }
            else ;
            if (mResult == 0) {
                int iItemNum = pm.getScholarshipSize();
                int prevItemNum = sharedPreferences.getInt(SAVE_SCHOLARSHIP_ITEM_NUM, -1);
                if (prevItemNum != -1 && iItemNum > prevItemNum) {
                    notificationManager.notify(0, buildNotification(getApplicationContext(), "장학금 알람", "새로운 장학금이 등록되었습니다!"));
                }
                editor.putInt(SAVE_SCHOLARSHIP_ITEM_NUM, iItemNum);
                editor.apply();
            }
            if(true) {
                for (int count = 0; count < 3; count++) {
                    if ((mResult = pm.pullCurrentGrade(getApplicationContext())) == 0) break;
                }
            }
            else ;
            if (mResult != 0) {
                int iItemNum = pm.getRegisteredCurrentGradeItemNum();
                int prevItemNum = sharedPreferences.getInt(SAVE_CURRENT_GRADE_ITEM_NUM, -1);
                if (prevItemNum != -1 && iItemNum > prevItemNum) {
                    notificationManager.notify(0, buildNotification(getApplicationContext(), "성적 알람", "새로운 성적이 등록되었습니다!"));
                }
                editor.putInt(SAVE_CURRENT_GRADE_ITEM_NUM, iItemNum);
                editor.apply();
            }
            if(true) {
                for (int count = 0; count < 3; count++) {
                    if ((mResult = pm.pullExaminationTimeTable(getApplicationContext())) == 0)
                        break;
                }
            }
            else ;
            if (mResult == 0) {
                int iItemNum = pm.getRegisteredExaminationTimeTableItemNum();
                int prevItemNum = sharedPreferences.getInt(SAVE_EXAMINATION_TIME_TABLE_ITEM_NUM, -1);
                if (prevItemNum != -1 && iItemNum > prevItemNum) {
                    notificationManager.notify(0, buildNotification(getApplicationContext(), "시험시간표 알람", "시험시간표가 등록되었습니다!"));
                }
                editor.putInt(SAVE_EXAMINATION_TIME_TABLE_ITEM_NUM, iItemNum);
                editor.apply();
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

    public Notification buildNotification(Context context, String title, String text) {
        Intent intent = HomeActivity.newIntent(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        Notification notification = new Notification.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        return notification;
    }
}
