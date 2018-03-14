package com.lifekau.android.lifekau;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String SAVE_ALARM_STATE = "save_alarm_state";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Resources resources = context.getResources();
            SharedPreferences sharedPref = context.getSharedPreferences(resources.getString(R.string.shared_preference_app), Context.MODE_PRIVATE);
            boolean alarmState = sharedPref.getBoolean(SAVE_ALARM_STATE, false);
            JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
            if(jobScheduler != null) {
                if (alarmState) {
                    jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(context, AlarmJobService.class))
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setPeriodic(30 * 60 * 1000)
                            .build());
                } else jobScheduler.cancel(0);
            }
            else{//초기화 실패

            }
        }
    }
}
