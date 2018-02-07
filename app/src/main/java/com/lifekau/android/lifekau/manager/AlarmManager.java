package com.lifekau.android.lifekau.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lifekau.android.lifekau.database.AlarmBaseHelper;
import com.lifekau.android.lifekau.database.AlarmCursorWrapper;
import com.lifekau.android.lifekau.database.AlarmDbSchema;
import com.lifekau.android.lifekau.model.Alarm;

import java.util.ArrayList;
import java.util.List;

import static com.lifekau.android.lifekau.database.AlarmDbSchema.*;

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

    public static AlarmManager get(Context context){
        if(sAlarmManager == null){
            sAlarmManager = new AlarmManager(context);
        }
        return sAlarmManager;
    }

    public void addAlarm(Alarm alarm){
        ContentValues values = getContentValues(alarm);
        mDatabase.insert(AlarmTable.NAME, null, values);
    }

    public void deleteAlarm(Alarm alarm){
        mDatabase.delete(
                AlarmTable.NAME,
                AlarmTable.Cols.UUID + " = ?",
                new String[]{alarm.getId().toString()}
                );
    }

    private static ContentValues getContentValues(Alarm alarm){
        ContentValues values = new ContentValues();
        values.put(AlarmTable.Cols.UUID, alarm.getId().toString());
        values.put(AlarmTable.Cols.CONTENT, alarm.getContent());
        values.put(AlarmTable.Cols.DATE, alarm.getDate().getTime());

        return values;
    }

    public List<Alarm> getAllAlarms(){
        List<Alarm> alarms = new ArrayList<>();

        AlarmCursorWrapper cursor = queryAlarms(
                null,
                null
        );

        try{
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                alarms.add(cursor.getAlarm());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return alarms;
    }

    private AlarmCursorWrapper queryAlarms(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                AlarmTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                AlarmTable.Cols.DATE
        );
        return new AlarmCursorWrapper(cursor);
    }
}
