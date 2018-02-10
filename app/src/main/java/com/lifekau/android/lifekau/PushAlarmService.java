package com.lifekau.android.lifekau;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;

public class PushAlarmService extends FirebaseMessagingService {
    public PushAlarmService() {
    }

}
