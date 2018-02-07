package com.lifekau.android.lifekau.manager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lifekau.android.lifekau.database.AlarmBaseHelper;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class AlarmManager {
    private static AlarmManager sAlarmManager;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    private AlarmManager(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new AlarmBaseHelper(mContext).getWritableDatabase();
    }
}
