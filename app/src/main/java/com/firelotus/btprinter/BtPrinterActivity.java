package com.firelotus.btprinter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firelotus.btlibrary.BluetoothSdkManager;
import com.firelotus.btlibrary.BtCmdService;
import com.firelotus.btlibrary.constant.ConstantDefine;
import com.firelotus.btlibrary.listener.BluetoothConnectListener;
import com.firelotus.btlibrary.listener.BluetoothStateListener;
import com.firelotus.btlibrary.listener.IReceiveDataListener;

import java.util.Arrays;

public class BtPrinterActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = BtPrinterActivity.class.getSimpleName();
    private Context mContext;

    private BluetoothSdkManager manager;

    private TextView tvPrinterTitle;
    private TextView tvPrinterSummary;
    private ImageView imgPrinter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);
        mContext = this;

        initViews();

        initListener();
    }

    public void initViews() {
        tvPrinterTitle = findViewById(R.id.txt_printer_setting_title);
        tvPrinterSummary = findViewById(R.id.txt_printer_setting_summary);
        if (!TextUtils.isEmpty(BluetoothSdkManager.btName) && !TextUtils.isEmpty(BluetoothSdkManager.btAddress)) {
            tvPrinterTitle.setText("已绑定蓝牙：" + BluetoothSdkManager.btName);
            tvPrinterSummary.setText(BluetoothSdkManager.btAddress);
        }
        imgPrinter = findViewById(R.id.img_printer_setting_icon);
        manager = BluetoothSdkManager.INSTANCE;
        manager.checkBluetooth();
    }

    public void initListener() {
        findViewById(R.id.ll_printer_setting_change_device).setOnClickListener(this);
        findViewById(R.id.btn_printTest).setOnClickListener(this);
        findViewById(R.id.btn_printSTOne).setOnClickListener(this);
        findViewById(R.id.btn_printSTTwo).setOnClickListener(this);

        findViewById(R.id.btn_getStatus).setOnClickListener(this);
        findViewById(R.id.btn_getPrintName).setOnClickListener(this);
        findViewById(R.id.btn_getPrintVersion).setOnClickListener(this);
        findViewById(R.id.btn_getPrintID).setOnClickListener(this);

        //接收蓝牙数据回调
        manager.setReceiveDataListener(new IReceiveDataListener() {
            @Override
            public void onReceiveData(byte[] data) {
                Log.d(TAG, "onReceiveData ==>> " + Arrays.toString(data));
            }
        });

        //连接状态结果回调
        manager.setBlueStateListener(new BluetoothStateListener() {
            @Override
            public void onConnectStateChanged(int state) {
                switch (state) {
                    case ConstantDefine.CONNECT_STATE_NONE:
                        Log.d(TAG, "  -----> none <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_LISTENER:
                        Log.d(TAG, "  -----> listener <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_CONNECTING:
                        Log.d(TAG, "  -----> connecting <----");
                        break;
                    case ConstantDefine.CONNECT_STATE_CONNECTED:
                        Log.d(TAG, "  -----> connected <----");
                        break;
                }
            }
        });

        manager.setBluetoothConnectListener(new BluetoothConnectListener() {
            @Override
            public void onBTDeviceConnected(String address, String name) {
                Toast.makeText(BtPrinterActivity.this, "已连接到名称为" + name + "的设备", Toast.LENGTH_SHORT).show();
                tvPrinterTitle.setText("已绑定蓝牙：" + name);
                tvPrinterSummary.setText(address);
                imgPrinter.setImageResource(R.drawable.ic_bluetooth_device_connected);
                //mBtnPrintTest.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBTDeviceDisconnected() {
                /*tvPrinterTitle.setText("蓝牙连接已断开");
                tvPrinterSummary.setText("");
                imgPrinter.setImageResource(R.drawable.ic_bluetooth_device_connected);
                Toast.makeText(MainActivity.this, "连接已经断开，请重新尝试连接...", Toast.LENGTH_SHORT).show();*/
            }

            @Override
            public void onBTDeviceConnectFailed() {
                Toast.makeText(BtPrinterActivity.this, "连接失败，请重新连接...", Toast.LENGTH_SHORT).show();
                tvPrinterTitle.setText("蓝牙连接失败");
                tvPrinterSummary.setText("");
                imgPrinter.setImageResource(R.drawable.ic_bluetooth_device_connected);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (manager != null) {
            Log.d(TAG, "onStart");
            manager.setupService();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            Log.d(TAG, "onDestroy");
            manager.stopService();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_printer_setting_change_device:
                Intent serverIntent = new Intent(BtPrinterActivity.this, DeviceListActivity.class);
                startActivity(serverIntent);
                break;
            case R.id.btn_printTest:
                printTest();
                break;
            case R.id.btn_printSTOne:
                printSTOne();
                break;
            case R.id.btn_printSTTwo:
                printSTTwo();
                break;
            case R.id.btn_getStatus:
                startBtCmdService(ConstantDefine.ACTION_GET_STATUS);
                break;
            case R.id.btn_getPrintName:
                startBtCmdService(ConstantDefine.ACTION_GET_PRINT_NAME);
                break;
            case R.id.btn_getPrintVersion:
                startBtCmdService(ConstantDefine.ACTION_GET_PRINT_VERSION);
                break;
            case R.id.btn_getPrintID:
                startBtCmdService(ConstantDefine.ACTION_GET_PRINT_ID);
                break;
            default:
                break;
        }
    }

    //打印测试
    public void printTest() {
        /*manager.printText("可以正常打印出这句话吗？\n");
        manager.printText("Hello World.\n");

        manager.printImage(markImage());*/

        Intent intent = new Intent(getApplicationContext(), BtCmdService.class);
        intent.setAction(ConstantDefine.ACTION_PRINT_TEST);
        startService(intent);
    }

    private void printSTOne() {
        Intent intent = new Intent(getApplicationContext(), BtCmdService.class);
        intent.setAction(ConstantDefine.ACTION_PRINT_ST_ONE);
        startService(intent);
    }

    private void printSTTwo() {
        Intent intent = new Intent(getApplicationContext(), BtCmdService.class);
        intent.setAction(ConstantDefine.ACTION_PRINT_ST_TWO);
        startService(intent);
    }

    private void startBtCmdService(String action) {
        Intent intent = new Intent(getApplicationContext(), BtCmdService.class);
        intent.setAction(action);
        startService(intent);
    }

}
