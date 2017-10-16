package com.perficient.meetingschedulear.util;

import android.widget.Toast;

import com.perficient.meetingschedulear.BaseApplication;

public class ToastUtil {

    private static String oldMsg;
    private static Toast toast;
    private static long oneTime;
    private static long twoTime;

    public static void showToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(BaseApplication.getContextObject(), msg, Toast.LENGTH_SHORT);
            //((TextView) ((LinearLayout) toast.getView()).getChildAt(0)).setGravity(Gravity.CENTER);
            toast.show();
            oneTime = System.currentTimeMillis();
        } else {
            twoTime = System.currentTimeMillis();
            if (msg.equals(oldMsg)) {
                if (twoTime - oneTime > Toast.LENGTH_SHORT) {
                    toast.show();
                }
            } else {
                oldMsg = msg;
                toast.setText(msg);
                toast.show();
            }
        }
        oneTime = twoTime;
    }


    public static void showToast(int resId) {
        showToast(BaseApplication.getContextObject().getString(resId));
    }
}
