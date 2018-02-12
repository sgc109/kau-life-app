package com.lifekau.android.lifekau;

import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by sgc109 on 2018-02-12.
 */

public class PxDpConverter {
    public static int convertDpToPx(int dp) {
        return Math.round(dp * (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    public static int convertPxToDp(int px) {
        return Math.round(px / (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
