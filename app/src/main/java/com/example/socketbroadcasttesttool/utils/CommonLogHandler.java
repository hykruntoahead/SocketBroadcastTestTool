package com.example.socketbroadcasttesttool.utils;

import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;

public class CommonLogHandler {
    public static final int LEVEL_I = 1;
    public static final int LEVEL_D = 2;
    public static final int LEVEL_W = 3;
    public static final int LEVEL_E = 4;


    public static final int WHAT_LOG = 1;
    private final static CommonLogHandler sCommonLogHandler = new CommonLogHandler();
    private Handler handler;
    private CommonLogHandler(){
    }

    public static CommonLogHandler getInstance(){
        return  sCommonLogHandler;
    }

    public void initLogHandler(Handler handler){
        this.handler = handler;
    }

    public void sendLogDebug(int level,String tag,String msg){
        Message message = Message.obtain(handler);
        message.what = WHAT_LOG;
        String logType = "";
        int levelColor = Color.WHITE;
        switch (level){
            case LEVEL_I:
                logType = "I";
                levelColor = Color.GRAY;
                break;
            case LEVEL_D:
                logType = "D";
                levelColor = Color.GREEN;
                break;
            case LEVEL_W:
                logType = "W";
                levelColor = Color.YELLOW;
                break;
            case LEVEL_E:
                logType = "E";
                levelColor = Color.RED;
                break;
        }
        SpannableString spannableString = new SpannableString(logType+tag+":"+msg);
        spannableString.setSpan(
                new BackgroundColorSpan(levelColor),0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLACK),0,1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE),1,1+tag.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        message.obj = spannableString;
        handler.sendMessageDelayed(message,200);
    }

}
