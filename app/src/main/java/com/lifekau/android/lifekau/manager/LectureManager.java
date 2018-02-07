package com.lifekau.android.lifekau.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.lifekau.android.lifekau.database.LectureBaseHelper;
import com.lifekau.android.lifekau.database.LectureCursorWrapper;
import com.lifekau.android.lifekau.database.LectureDbSchema;
import com.lifekau.android.lifekau.model.Lecture;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class LectureManager {
    private static LectureManager sLectureManager;

    SQLiteDatabase mDatabase;
    Context mContext;

    private LectureManager(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new LectureBaseHelper(mContext).getWritableDatabase();
    }

    public LectureManager get(Context context) {
        if (sLectureManager == null) {
            sLectureManager = new LectureManager(context);
        }
        return sLectureManager;
    }

    public List<Lecture> getAllLectures() {
        List<Lecture> lectures = new ArrayList<>();

        LectureCursorWrapper cursor = queryLectures(
                null,
                null
        );
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                lectures.add(cursor.getLecture());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return lectures;
    }

    private LectureCursorWrapper queryLectures(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                LectureDbSchema.LectureTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );

        return new LectureCursorWrapper(cursor);
    }
}
