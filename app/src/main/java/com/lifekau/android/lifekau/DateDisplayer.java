package com.lifekau.android.lifekau;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sgc109 on 2018-02-13.
 */

public class DateDisplayer {
    public static String dateToStringFormat(Date past) {
        Date now = new Date();
        SimpleDateFormat toYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat toMonth = new SimpleDateFormat("M");
        SimpleDateFormat toDay = new SimpleDateFormat("d");

        int nowYear = Integer.parseInt(toYear.format(now));
        int pastYear = Integer.parseInt(toYear.format(past));
        int nowMonth = Integer.parseInt(toMonth.format(now));
        int pastMonth = Integer.parseInt(toMonth.format(past));
        int nowDay = Integer.parseInt(toDay.format(now));
        int pastDay = Integer.parseInt(toDay.format(past));

        if (nowYear != pastYear) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy년 M월 d일 a h:mm");
            return formatter.format(past);
        } else if (nowMonth != pastMonth
                || nowDay >= pastDay + 2) {
            SimpleDateFormat formatter = new SimpleDateFormat("M월 d일 a h:mm");
            return formatter.format(past);
        } else if (nowDay >= pastDay + 1) {
            SimpleDateFormat formatter = new SimpleDateFormat("어제 a h:mm");
            return formatter.format(past);
        }
        long diff = now.getTime() - past.getTime();
        diff /= 1000;
        long hour = diff / 3600;
        long minute = diff / 60;

        if (minute == 0) {
            return "방금 전";
        } else if (hour == 0) {
            return "" + minute + "분 전";
        } else {
            SimpleDateFormat formatter = new SimpleDateFormat("a h:mm");
            return formatter.format(past);
        }
    }
}
