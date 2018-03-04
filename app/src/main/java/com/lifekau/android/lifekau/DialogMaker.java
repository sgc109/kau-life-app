package com.lifekau.android.lifekau;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by sgc109 on 2018-03-04.
 */

public class DialogMaker {
    public static void showOkButtonDialog(Context context, String message){
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton("확인", null)
                .create();
        dialog.show();

//        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
//        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
//        positiveButtonLL.gravity = Gravity.CENTER;
//        positiveButton.setLayoutParams(positiveButtonLL);
    }
}
