package com.example.medi.stoolurine;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Set;

public class RecordUrineActivity extends BaseActivity {
    boolean connected = false;
    static EditText print_weight;
    /*
     * Notifications from UsbService will be received here.
     */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "측정 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
                    connected = true;
                    break;

                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    connected = false;
                    Toast.makeText(context, "저울과 연결이 필요합니다", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    connected = false;
                    Toast.makeText(context, "저울과 연결이 필요합니다", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    connected = false;
                    Toast.makeText(context, "저울과 연결이 필요합니다", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    connected = false;
                    Toast.makeText(context, "저울과 연결이 필요합니다", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private MyHandler mHandler;

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        immersiveMode();
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        immersiveMode();
                    }
                });
    }

    public void immersiveMode() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    @Override
    public void onResume() {
        super.onResume();
        immersiveMode();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<RecordUrineActivity> mActivity;

        public MyHandler(RecordUrineActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().print_weight.append(data);
                    break;
            }
        }
    }

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.activity_record_urine);

        Intent intent = getIntent();
        final String pid = intent.getStringExtra("pid");
        final String  name = MediValues.patientData.get(pid).get("name");
        TextView title_pname = findViewById(R.id.p_name);
        title_pname.setText(name+" 님");
        TextView tv = findViewById(R.id.tv);

        tv.setText("무게를 직접 입력하거나, 측정 버튼을 눌러 무게를 측정하세요\n( 영점 조절버튼은 저울에 아무것도 올려놓지 않은 상태에서 눌러주세요 )");

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(uiOptions);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mHandler = new MyHandler(this);

        Button bt_start = findViewById(R.id.askWeight);
        print_weight = findViewById(R.id.weightPrint);
        Button bt_zero = findViewById(R.id.setZero);
        Button bt_next = findViewById(R.id.Btn_next);
        Button bt_prev = findViewById(R.id.Btn_prev);

        bt_zero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usbService.isServiceConnected()) {
                    String data = "0";
                    usbService.write(data.getBytes());
                } else
                    Toast.makeText(getApplicationContext(), "저울과 연결이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        });

        //측정시작
        bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                print_weight.setText("");

                if (usbService.isServiceConnected()) {
                    String data = "1";
                    usbService.write(data.getBytes());
                } else
                    Toast.makeText(getApplicationContext(), "저울과 연결이 필요합니다", Toast.LENGTH_SHORT).show();
            }
        });

        bt_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tmp = (print_weight.getText().toString());
                tmp.replace("ready", "");

                if (tmp.matches(""))
                    Toast.makeText(getApplicationContext(), "잘못된 무게값입니다", Toast.LENGTH_SHORT).show();

                else if(Float.parseFloat(tmp)<=0)
                    Toast.makeText(getApplicationContext(), "잘못된 무게값입니다", Toast.LENGTH_SHORT).show();

                else {
                    Toast.makeText(getApplicationContext(), tmp + "cc의 소변이 등록되었습니다", Toast.LENGTH_SHORT).show();
                    MediPostRequest postRequest = new MediPostRequest(v.getContext(), pid, name,MediValues.OUTPUT, MediValues.URINE, Float.parseFloat(tmp), null );
                    Intent intent = new Intent(RecordUrineActivity.this, ReportActivity.class);
                    intent.putExtra("pid", pid);
                    startActivity(intent);
                }
            }
        });

        bt_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecordUrineActivity.this, ContainerSelectActivity.class);
                intent.putExtra("pid", pid);
                startActivity(intent);
            }
        });
    }
}
