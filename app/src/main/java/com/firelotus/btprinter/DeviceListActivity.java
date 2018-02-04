package com.firelotus.btprinter;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firelotus.btlibrary.BluetoothSdkManager;
import com.firelotus.btlibrary.constant.ConstantDefine;
import com.firelotus.btlibrary.listener.DiscoveryDevicesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DeviceListActivity extends Activity {
    public static final String TAG = DeviceListActivity.class.getSimpleName();
    private BluetoothSdkManager manager;
    // 成员字段
    public BluetoothAdapter mBtAdapter;
    public ArrayAdapter<String> mPairedDevicesArrayAdapter;
    public ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //启用窗口拓展功能，方便调用
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_devicelist);
        manager = BluetoothSdkManager.INSTANCE;
        setResult(Activity.RESULT_CANCELED);
        progress = findViewById(R.id.progress);
        Button scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // 初始化 arryadapter 已经配对的设备和新扫描到得设备
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getPairedData());
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ListView pairedListView = findViewById(R.id.paired_devices);
        ListView newDevicesListView = findViewById(R.id.new_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        try {
            pairedListView.setOnItemClickListener(mDeviceClickListener);
            newDevicesListView.setOnItemClickListener(mDeviceClickListener);
        } catch (Exception excpt) {
            Toast.makeText(this, "获取设备失败:" + excpt, Toast.LENGTH_LONG).show();
        }
    }

    //取得已经配对的蓝牙信息,用来加载到ListView中去
    public List<String> getPairedData() {
        List<String> data = new ArrayList<String>();
        //默认的蓝牙适配器
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // 得到当前的一个已经配对的蓝牙设备
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                data.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = "未配对设备";
            data.add(noDevices);
        }
        return data;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.cancelDiscovery();
        }
    }

    /**
     * 启动装置发现的BluetoothAdapter
     */
    public void doDiscovery() {
        Log.d(TAG, "doDiscovery()");
        // 在标题中注明扫描
        setTitle("扫描中......");
        // 打开子标题的新设备
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        //如果正在扫描的话先取消掉当前的扫描
        if (manager.isDiscoverying()) {
            manager.cancelDiscovery();
        } else {
            //扫描蓝牙设备回调
            manager.setDiscoveryDeviceListener(new DiscoveryDevicesListener() {
                @Override
                public void startDiscovery() {
                    setProgressBarIndeterminateVisibility(true);
                    Toast.makeText(DeviceListActivity.this, "开始搜索蓝牙设备...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void discoveryNew(BluetoothDevice device) {
                    Toast.makeText(DeviceListActivity.this, "发现新的蓝牙设备...", Toast.LENGTH_SHORT).show();
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }

                @Override
                public void discoveryFinish(List<BluetoothDevice> list) {
                    setTitle("设备列表");
                    setProgressBarIndeterminateVisibility(false);
                    Log.d(TAG, "startSearchBT --- discoveryFinish() --- list.size(): " + list.size());
                    Toast.makeText(DeviceListActivity.this, "搜索完成，共发现 <" + list.size() + ">" + "个蓝牙设备", Toast.LENGTH_SHORT).show();
                    if (list.size() == 0) {
                        findViewById(R.id.button_scan).setVisibility(View.VISIBLE);
                        findViewById(R.id.title_new_devices).setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    // 给列表的中的蓝牙设备创建监听事件
    public AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            try {
                if (manager.isDiscoverying()) {
                    manager.cancelDiscovery();
                    progress.setVisibility(View.GONE);
                }

                //取得蓝牙mvc地址
                String info = ((TextView) v).getText().toString();
                String btName = info.substring(0, info.indexOf("\n"));
                String btAddress = info.substring(info.length() - 17);
                if (!btAddress.contains(":")) {
                    return;
                }

                //如果切换连接设备,先断开之前的.
                if (manager.getConnectDeviceAddress() != null) {
                    //不同设备切换
                    if (!manager.getConnectDeviceAddress().equals(btAddress)) {
                        manager.disconnect();
                        manager.connect(btAddress);
                    }
                } else {
                    //正在进行连接操作的设备不可操作
                    if (manager.getServiceState() != ConstantDefine.CONNECT_STATE_CONNECTING) {
                        //当前无连接
                        manager.connect(btAddress);
                    }
                }

                finish();
            } catch (Exception e) {
                progress.setVisibility(View.GONE);
                e.printStackTrace();
            }
        }
    };

    private ProgressBar progress;
}
