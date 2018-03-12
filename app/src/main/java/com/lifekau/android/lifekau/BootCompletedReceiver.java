package com.lifekau.android.lifekau;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("호출됐니???", "체크");
            JobScheduler jobScheduler = context.getSystemService(JobScheduler.class);
                    jobScheduler.schedule(new JobInfo.Builder(0, new ComponentName(context, AlarmJobService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(30 * 60 * 1000)
                    .build());
        }
    }
}
