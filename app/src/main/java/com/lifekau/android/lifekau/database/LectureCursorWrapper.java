package com.lifekau.android.lifekau.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.lifekau.android.lifekau.model.Lecture;

import java.util.UUID;

import static com.lifekau.android.lifekau.database.LectureDbSchema.*;

/**
 * Created by sgc109 on 2018-02-08.
 */

public class LectureCursorWrapper extends CursorWrapper {
    public LectureCursorWrapper(Cursor cursor) {
        super(cursor);
    }
    public Lecture getLecture(){
        String uuidString = getString(getColumnIndex(LectureTable.Cols.UUID));
        String name = getString(getColumnIndex(LectureTable.Cols.NAME));
        Lecture lecture = new Lecture(UUID.fromString(uuidString));
        lecture.setName(name);

        return lecture;
    }
}
