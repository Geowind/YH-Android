package com.intfocus.yh_android.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by 40284 on 2016/8/16.
 */
public class OtherUtil {

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

//    public String getDashboardDataCount(String tabCode, String tabType, String dataCount){
//
//    }

    public static void writeShared(Context context, String key, boolean value){
        SharedPreferences.Editor editor = context.getSharedPreferences(key, Context.MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }
}
