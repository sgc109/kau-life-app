package com.lifekau.android.lifekau.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.lifekau.android.lifekau.database.AlarmDbSchema.*;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class AlarmBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "alarmBase.db";

    public AlarmBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + AlarmTable.NAME + "(" +
        " _id integer primary key autoincrement, " +
                AlarmTable.Cols.UUID + ", " +
                AlarmTable.Cols.CONTENT + ", " +
                AlarmTable.Cols.TYPE + ", " +
                AlarmTable.Cols.DATE +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
