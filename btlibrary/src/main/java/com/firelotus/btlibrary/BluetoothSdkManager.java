package com.firelotus.btlibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.firelotus.btlibrary.constant.ConstantDefine;
import com.firelotus.btlibrary.listener.BluetoothConnectListener;
import com.firelotus.btlibrary.listener.BluetoothStateListener;
import com.firelotus.btlibrary.listener.DiscoveryDevicesListener;
import com.firelotus.btlibrary.listener.IReceiveDataListener;
import com.firelotus.btlibrary.service.BtService;
import com.firelotus.btlibrary.util.PrintQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Description: 接口管理类
 */
public enum BluetoothSdkManager {
    INSTANCE;
    private static final String TAG = BluetoothSdkManager.class.getSimpleName();
    public static final String BT_PREFERENCE = "bt_preference";
    public static final String KEY_BT_ADDRESS = "bt_address";
    public static final String KEY_BT_NAME = "bt_name";

    //保存在本地sp中的数据
    public static String btAddress;
    public static String btName;

    private Context mContext;
    private PrintQueue printQueue;
    private ArrayList<byte[]> mQueue;

    private BluetoothAdapter mBluetoothAdapter;
    private String mConnectDeviceName;
    private String mConnectDeviceAddress;

    //listener
    private BluetoothConnectListener mConnectListener = null;
    private BluetoothStateListener mStateListener = null;
    private IReceiveDataListener mReceiveDataListener = null;
    private DiscoveryDevicesListener mDiscoveryDevicesListener = null;

    private BtService mBTService;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isServiceRunning = false;

    private List<BluetoothDevice> mDeviceList = null;
    private DiscoveryReceiver mReceiver;
    private boolean isRegister = false;

    public void init(Context context) {
        this.mContext = context;
        printQueue = PrintQueue.getQueue(mContext);
        mQueue = printQueue.getmQueue();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //获取本地保存的已连接的设备信息
        btName = (String) SFSharedPreferences.get(mContext, BluetoothSdkManager.BT_PREFERENCE, BluetoothSdkManager.KEY_BT_NAME, "");
        btAddress = (String) SFSharedPreferences.get(mContext, BluetoothSdkManager.BT_PREFERENCE, BluetoothSdkManager.KEY_BT_ADDRESS, "");
    }

    /**
     * 判断设备是否支持蓝牙
     */
    public boolean isBluetoothSupported() {
        return mBluetoothAdapter != null;
    }

    /**
     * 判断蓝牙是否可用
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    //判断蓝牙服务是否可用
    public boolean isServiceAvailable() {
        return mBTService != null;
    }

    /**
     * 检查蓝牙是否可用,可用时是否开启,未开启自动开启
     */
    public void checkBluetooth() {
        if (!isBluetoothSupported()) {
            Toast.makeText(mContext, "此设备不支持蓝牙...", Toast.LENGTH_SHORT).show();
        } else {
            if (!isBluetoothEnabled()) {
                Toast.makeText(mContext, "蓝牙没有开启，正在强制开启...", Toast.LENGTH_SHORT).show();
                getBluetoothAdapter().enable();
            }
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    //开启远程蓝牙设备扫描: 不需要独自调用，在进行搜索回调中已经被调用
    public boolean startDiscovery() {

        Log.d(TAG, "startDiscovery");
        return mBluetoothAdapter.startDiscovery();
    }

    //判断发现蓝牙设备进程是否正在运行
    public boolean isDiscoverying() {
        return mBluetoothAdapter.isDiscovering();
    }

    //取消设备发现进程
    public boolean cancelDiscovery() {
        return mBluetoothAdapter.cancelDiscovery();
    }

    public void setupService() {
        Log.d(TAG, "setupService");
        mBTService = new BtService(mHandler);
        if (mBTService.getState() == ConstantDefine.CONNECT_STATE_NONE) {
            mBTService.start();
            isServiceRunning = true;
        }
    }

    public int getServiceState() {
        if (mBTService != null) {
            return mBTService.getState();
        } else {
            return -1;
        }
    }

    public void stopService() {
        Log.d(TAG, "stopService");
        if (mBTService != null) {
            mBTService.stop();
            isServiceRunning = false;
        }
        if (isRegister) {
            mContext.unregisterReceiver(mReceiver);
            isRegister = false;
        }

        mDeviceList = null;
    }

    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    //连接蓝牙设备通过MAC地址
    public void connect(String address) {
        Log.d(TAG, "connect");
        //直到蓝牙可用才进行连接
        while (!isBluetoothEnabled()) {
            SystemClock.sleep(10);
        }
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (mBTService != null) {
            mBTService.connect(device);
        }
    }

    public void connect(BluetoothDevice device) {
        Log.d(TAG, "connect");
        //直到蓝牙可用才进行连接
        while (!isBluetoothEnabled()) {
            SystemClock.sleep(10);
        }

        if (mBTService != null) {
            mBTService.connect(device);
        }
    }

    //断开连接
    public void disconnect() {
        Log.d(TAG, "disconnect");
        if (mBTService != null) {
            mBTService.stop();
            isServiceRunning = false;
            if (mBTService.getState() == ConstantDefine.CONNECT_STATE_NONE) {
                mBTService.start();
                isServiceRunning = true;
            }
        }
    }

    // TODO: 2018/1/22 私有化write方法,使所有命令都从print方法加入队列后进行处理
    private void write(byte[] data) {
        if (mBTService.getState() == ConstantDefine.CONNECT_STATE_CONNECTED) {
            mBTService.write(data);
        }
    }

    //返回当前命令的返回值
    public byte[] ReadData() {
        return mBTService.getCurrentCMDReadData();
    }

    //得到配对成功的设备集合
    public Set<BluetoothDevice> getPairingDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    //得到连接成功的设备名称
    public String getConnectDeviceName() {
        return mConnectDeviceName;
    }

    //得到连接成功的设备Mac地址
    public String getConnectDeviceAddress() {
        return mConnectDeviceAddress;
    }

    public void setBlueStateListener(BluetoothStateListener listener) {
        this.mStateListener = listener;
    }

    public void setReceiveDataListener(IReceiveDataListener listener) {
        this.mReceiveDataListener = listener;
    }

    public void setBluetoothConnectListener(BluetoothConnectListener listener) {
        this.mConnectListener = listener;
    }

    public void setDiscoveryDeviceListener(DiscoveryDevicesListener listener) {
        this.mDiscoveryDevicesListener = listener;

        mDeviceList = new ArrayList<>();
        if (mReceiver == null) {
            mReceiver = new DiscoveryReceiver();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, intentFilter);
        isRegister = true;
        mDeviceList.clear();

        if (isDiscoverying()) {
            cancelDiscovery();
        }

        //扫描
        int intStartCount = 0;
        while (!startDiscovery() && intStartCount < 5) {
            Log.e(TAG, "扫描尝试失败");
            intStartCount++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mDiscoveryDevicesListener != null) {
            mDiscoveryDevicesListener.startDiscovery();
        }

    }

    //发现蓝牙设备广播(已对蓝牙进行过滤,只显示打印机蓝牙)
    public class DiscoveryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                    ////int deviceType = device.getBluetoothClass().getMajorDeviceClass(); 不同设备类型该值不同，比如computer蓝牙为256、phone 蓝牙为512、打印机蓝牙为1536搜索等等。
                    if (device.getBluetoothClass().getMajorDeviceClass() == 1536) {
                        if (!mDeviceList.contains(device)) {
                            if (mDiscoveryDevicesListener != null) {
                                Log.d(TAG, "onReceive --- device.toString: " + device.getName() + ":" + device.getAddress());
                                mDiscoveryDevicesListener.discoveryNew(device);
                            }
                            mDeviceList.add(device);
                        }
                    }
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mDiscoveryDevicesListener != null) {
                    Log.d(TAG, "onReceive --- mDeviceList.size() = " + mDeviceList.size());
                    mDiscoveryDevicesListener.discoveryFinish(mDeviceList);
                }
            }
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //读取数据
                case ConstantDefine.MESSAGE_STATE_READ:
                    byte[] data = (byte[]) msg.obj;
                    if (data != null && data.length > 0) {
                        if (mReceiveDataListener != null) {
                            mReceiveDataListener.onReceiveData(data);
                        }
                    }
                    break;
                case ConstantDefine.MESSAGE_STATE_WRITE:
                    break;
                case ConstantDefine.MESSAGE_DEVICE_INFO:
                    mConnectDeviceName = msg.getData().getString(ConstantDefine.KEY_DEVICE_NAME);
                    mConnectDeviceAddress = msg.getData().getString(ConstantDefine.KEY_DEVICE_ADDRESS);

                    //连接成功后,保存本地
                    SFSharedPreferences.put(mContext, BT_PREFERENCE, KEY_BT_NAME, mConnectDeviceName);
                    SFSharedPreferences.put(mContext, BT_PREFERENCE, KEY_BT_ADDRESS, mConnectDeviceAddress);
                    //同步当前连接设备信息,避免中途切换蓝牙设备,断开再次打印时,自动连接的设备仍为上一次设备的问题
                    btName = mConnectDeviceName;
                    btAddress = mConnectDeviceAddress;

                    if (mConnectListener != null) {
                        mConnectListener.onBTDeviceConnected(mConnectDeviceAddress, mConnectDeviceName);
                    }
                    isConnected = true;

                    //连接成功后打印队列

                    if (null == mQueue || mQueue.size() <= 0) {
                        return;
                    }
                    while (mQueue.size() > 0) {
                        write(mQueue.get(0));
                        mQueue.remove(0);
                    }
                    break;
                case ConstantDefine.MESSAGE_STATE_CHANGE:
                    if (mStateListener != null) {
                        mStateListener.onConnectStateChanged(msg.arg1);
                    }
                    Log.d(TAG, "isConnected = " + isConnected + "  msg.arg1 = " + msg.arg1);
                    if (isConnected && msg.arg1 != ConstantDefine.CONNECT_STATE_CONNECTED) {
                        if (mConnectListener != null) {
                            mConnectListener.onBTDeviceDisconnected();
                        }
                        isConnected = false;
                        mConnectDeviceName = null;
                        mConnectDeviceAddress = null;
                    }

                    if (!isConnecting && msg.arg1 == ConstantDefine.CONNECT_STATE_CONNECTING) {
                        isConnecting = true;
                    } else if (isConnecting) {
                        if (msg.arg1 != ConstantDefine.CONNECT_STATE_CONNECTED) {
                            if (mConnectListener != null) {
                                mConnectListener.onBTDeviceConnectFailed();
                            }
                        }
                        isConnecting = false;
                    }
                    break;
                default:
                    break;
            }
        }
    };

    //************* 打印相关 ****************//

    /**
     * print queue
     */
    public synchronized void print(ArrayList<byte[]> bytes) {
        checkBluetooth();
        // TODO: 2018/1/22 打印命令必须加入printQueue,起到缓存的作用.以便在打印机连接成功后,打印之前发送的命令
        printQueue.add(bytes);

        if (null == mQueue || mQueue.size() <= 0) {
            return;
        }

        //正在进行连接操作和已经连接上要再次进行连接
        if (isConnecting) {
            return;
        }
        if (mBTService.getState() != ConstantDefine.CONNECT_STATE_CONNECTED) {
            //自动连接上一次已连通设备
            if (!TextUtils.isEmpty(btAddress)) {
                connect(btAddress);
                return;
            }
        }

        while (mQueue.size() > 0) {
            write(mQueue.get(0));
            mQueue.remove(0);
        }
    }

    public synchronized void print(byte[] bytes) {
        checkBluetooth();
        // TODO: 2018/1/22 打印命令必须加入printQueue,起到缓存的作用.以便在打印机连接成功后,打印之前发送的命令
        printQueue.add(bytes);

        if (null == mQueue || mQueue.size() <= 0) {
            return;
        }

        //正在进行连接操作和已经连接上的不需要再次进行连接
        if (isConnecting) {
            return;
        }
        if (mBTService.getState() != ConstantDefine.CONNECT_STATE_CONNECTED) {
            //自动连接上一次已连通设备
            if (!TextUtils.isEmpty(btAddress)) {
                connect(btAddress);
                return;
            }
        }

        while (mQueue.size() > 0) {
            write(mQueue.get(0));
            mQueue.remove(0);
        }

    }
}
