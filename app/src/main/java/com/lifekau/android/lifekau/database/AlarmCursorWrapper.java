package com.lifekau.android.lifekau.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.lifekau.android.lifekau.model.Alarm;

import java.util.Date;
import java.util.UUID;

import static com.lifekau.android.lifekau.database.AlarmDbSchema.*;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class AlarmCursorWrapper extends CursorWrapper {
    public AlarmCursorWrapper(Cursor cursor) {
        super(cursor);
    }
    public Alarm getAlarm(){
        String uuidString = getString(getColumnIndex(AlarmTable.Cols.UUID));
        String content = getString(getColumnIndex(AlarmTable.Cols.CONTENT));
        int type = getInt(getColumnIndex(AlarmTable.Cols.TYPE));
        long date = getLong(getColumnIndex(AlarmTable.Cols.DATE));
        Alarm alarm = new Alarm(UUID.fromString(uuidString));
        alarm.setContent(content);
        alarm.setType(type);
        alarm.setDate(new Date(date));

        return alarm;
    }
}
