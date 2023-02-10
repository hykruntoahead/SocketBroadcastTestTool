package com.example.socketbroadcasttesttool;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.socketbroadcasttesttool.utils.CommonLogHandler;
import com.example.socketbroadcasttesttool.utils.LogUtils;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private final WifiMulBroadCast wifiMulBroadCast = new WifiMulBroadCast();
    public static final String ACTION_WIFI_MUL_BROADCAST = "gs.action.wifi.mul";

    private SwitchCompat switchClientOpen, switchServerOpen, switchServerLockEnable;

    private CheckBox checkBox;

    private EditText etClientMsg;

    private TextView tvLog;

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == CommonLogHandler.WHAT_LOG) {
                tvLog.append((CharSequence) message.obj);
                tvLog.append("\r\n");
                return true;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_WIFI_MUL_BROADCAST);
        registerReceiver(wifiMulBroadCast, intentFilter);

        initView();
    }

    private void initView() {
        switchClientOpen = findViewById(R.id.sc_open_client);
        switchServerOpen = findViewById(R.id.sc_open_server);
        switchServerLockEnable = findViewById(R.id.sc_enable_server_lock);

        checkBox = findViewById(R.id.cb_log_show);
        etClientMsg = findViewById(R.id.edit_msg);
        Button btnBroadcast = findViewById(R.id.btn_broadcast);

        tvLog = findViewById(R.id.textView_log);

        switchClientOpen.setOnCheckedChangeListener(this);
        switchServerOpen.setOnCheckedChangeListener(this);
        switchServerLockEnable.setOnCheckedChangeListener(this);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                LogUtils.enableLogShowUi(checked);
            }
        });
        btnBroadcast.setOnClickListener(this);

        CommonLogHandler.getInstance().initLogHandler(handler);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        unregisterReceiver(wifiMulBroadCast);
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        Intent intent = new Intent(ACTION_WIFI_MUL_BROADCAST);
        if (compoundButton == switchClientOpen) {
            intent.putExtra(WifiMulBroadCast.KEY_CLIENT_OPEN, checked ? 1 : 0);
            findViewById(R.id.layout_client_send_msg).setVisibility(checked ? View.VISIBLE : View.GONE);
        } else if (compoundButton == switchServerOpen) {
            intent.putExtra(WifiMulBroadCast.KEY_SERVER_OPEN, checked ? 1 : 0);
        } else if (compoundButton == switchServerLockEnable) {
            intent.putExtra(WifiMulBroadCast.KEY_ACQ_ENABLE, checked ? 1 : 0);
        }
        sendBroadcast(intent);
    }

    @Override
    public void onClick(View view) {
        CharSequence msg =  etClientMsg.getText();
        Log.d(TAG, "onClick: " +msg);
        if (TextUtils.isEmpty(msg)){
            msg = "default msg";
        }
        Intent intent = new Intent(ACTION_WIFI_MUL_BROADCAST);
        intent.putExtra(WifiMulBroadCast.KEY_CLIENT_MSG, msg.toString());
        sendBroadcast(intent);
    }
}
