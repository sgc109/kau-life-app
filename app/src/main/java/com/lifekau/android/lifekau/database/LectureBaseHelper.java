package com.lifekau.android.lifekau.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.lifekau.android.lifekau.database.LectureDbSchema.*;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class LectureBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "lectureBase.db";

    private SQLiteDatabase mDatabase;

    public LectureBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + LectureTable.NAME + "(" +
                "_id integer primary key autoincrement, " +
                LectureTable.Cols.NAME +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
