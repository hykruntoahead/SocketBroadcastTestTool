package com.example.socketbroadcasttesttool.utils;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LogUtils {
    private static boolean sLogShowUi = false;

    public static void enableLogShowUi(boolean enableUiShow){
        sLogShowUi = enableUiShow;
    }

    public static int d(@Nullable String tag, @NonNull String msg) {
        if (sLogShowUi){
            CommonLogHandler.getInstance().sendLogDebug(CommonLogHandler.LEVEL_D,tag,msg);
        }
        return Log.d(tag,msg);
    }

    public static int i(@Nullable String tag, @NonNull String msg) {
        if (sLogShowUi){
            CommonLogHandler.getInstance().sendLogDebug(CommonLogHandler.LEVEL_I,tag,msg);
        }
        return Log.i(tag,msg);
    }

    public static int w(@Nullable String tag, @NonNull String msg) {
        if (sLogShowUi){
            CommonLogHandler.getInstance().sendLogDebug(CommonLogHandler.LEVEL_W,tag,msg);
        }
        return Log.w(tag,msg);
    }

    public static int e(@Nullable String tag, @NonNull String msg) {
        if (sLogShowUi){
            CommonLogHandler.getInstance().sendLogDebug(CommonLogHandler.LEVEL_E,tag,msg);
        }
        return Log.e(tag,msg);
    }
}
