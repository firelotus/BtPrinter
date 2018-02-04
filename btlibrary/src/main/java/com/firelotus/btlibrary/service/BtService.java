package com.firelotus.btlibrary.service;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.firelotus.btlibrary.constant.ConstantDefine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

/**
 * Description: 连接蓝牙设备
 */
@SuppressLint("NewApi")
public class BtService {

    private static final String TAG = BtService.class.getSimpleName();

    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    private int mState;

    //当前命令返回数据
    private byte[] currentCMDReadData;

    //连接所需的UUID
    private final UUID UUID_DEVICE_PRINTER = UUID.fromString(ConstantDefine.STRING_DEVICE_PRINTER);
    private final static String MY_NAME = "BluetoothSDK";

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    public BtService(Handler handler) {
        this.mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = ConstantDefine.CONNECT_STATE_NONE;
    }

    public synchronized void setState(int state) {
        Log.d(TAG, "setState " + mState + " -> " + state);

        this.mState = state;
        //连接状态改变
        mHandler.obtainMessage(ConstantDefine.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    public byte[] getCurrentCMDReadData() {
        return currentCMDReadData;
    }

    public void setCurrentCMDReadData(byte[] currentCMDReadData) {
        this.currentCMDReadData = currentCMDReadData;
    }

    //开启线程，监听来自打印机的数据
    public synchronized void start() {
        Log.d(TAG, "start");
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(ConstantDefine.CONNECT_STATE_LISTENER);

        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

    }

    //开启线程，连接远程的蓝牙设备
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        // Cancel any thread attempting to make a connection
        if (mState == ConstantDefine.CONNECT_STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(ConstantDefine.CONNECT_STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message message = mHandler.obtainMessage(ConstantDefine.MESSAGE_DEVICE_INFO);
        Bundle bundle = new Bundle();
        bundle.putString(ConstantDefine.KEY_DEVICE_NAME, device.getName());
        bundle.putString(ConstantDefine.KEY_DEVICE_ADDRESS, device.getAddress());
        message.setData(bundle);
        mHandler.sendMessage(message);

        setState(ConstantDefine.CONNECT_STATE_CONNECTED);

    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(ConstantDefine.CONNECT_STATE_NONE);

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread.kill();
            mAcceptThread = null;
        }

    }

    //向远程蓝牙设备发送数据
    public synchronized void write(byte[] data) {
        ConnectedThread thread;

        synchronized (this) {
            if (mState != ConstantDefine.CONNECT_STATE_CONNECTED) {
                return;
            }
            thread = mConnectedThread;
        }

        thread.write(data);
    }

    //连接失败时重新开启
    private void connectionFailed() {
        Log.d(TAG, "connectionFailed");
        /*setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BTPrinterDemo.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BTPrinterDemo.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
        setState(ConstantDefine.CONNECT_STATE_NONE);
        BtService.this.start();
    }

    //连接断开时重新开启
    private void connectionLost() {
        Log.d(TAG, "connectionLost");
        /*//setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BTPrinterDemo.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BTPrinterDemo.TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);*/
        setState(ConstantDefine.CONNECT_STATE_NONE);

        BtService.this.start();
    }

    //--------相关操作线程类---------//
    //连接为服务器
    public class AcceptThread extends Thread {
        private BluetoothServerSocket mBluetoothServerSocket;
        private boolean isRunning = true;

        public AcceptThread() {
            BluetoothServerSocket tempServerSocket = null;
            try {
                tempServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(MY_NAME, UUID_DEVICE_PRINTER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            mBluetoothServerSocket = tempServerSocket;
        }

        @Override
        public void run() {
            Log.d(TAG, "BEGIN mAcceptThread" + this);
            BluetoothSocket socket;

            while (mState != ConstantDefine.CONNECT_STATE_CONNECTED && isRunning) {
                try {
                    socket = mBluetoothServerSocket.accept();
                } catch (Exception ex) {
                    //ex.printStackTrace();
                    break;
                }

                if (socket != null) {
                    synchronized (BtService.this) {
                        switch (mState) {
                            case ConstantDefine.CONNECT_STATE_LISTENER:
                            case ConstantDefine.CONNECT_STATE_CONNECTING:
                                //进行连接
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case ConstantDefine.CONNECT_STATE_NONE:
                            case ConstantDefine.CONNECT_STATE_CONNECTED:
                                try {
                                    socket.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        }

        public void cancel() {
            try {
                mBluetoothServerSocket.close();
                mBluetoothServerSocket = null;
                isRunning = false;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public void kill() {
            isRunning = false;
        }
    }

    //与远程蓝牙设备进行连接
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tempSocket = null;
            this.mmDevice = device;

            try {
                tempSocket = device.createRfcommSocketToServiceRecord(UUID_DEVICE_PRINTER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mmSocket = tempSocket;
        }

        @Override
        public void run() {
            Log.d(TAG, "BEGIN mConnectThread" + this);
            mBluetoothAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    mmSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                connectionFailed();
                return;
            }

            synchronized (BtService.this) {
                mConnectThread = null;
            }

            //连接完成后，两个设备之间进行数据传输
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    //两个连接成功的设备之间进行数据传输
    public class ConnectedThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final InputStream mmInputStream;
        private final OutputStream mmOutputStream;
        //完整的数据
        private byte[] fullDatas = new byte[0];
        //当前接收到的数据
        private byte[] perDatas = new byte[0];
        //总的字节数
        private int totalLen = 0;
        //当前读取到的字节数
        private int perLen = 0;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mmInputStream = inputStream;
            mmOutputStream = outputStream;
        }

        @Override
        public void run() {
            Log.d(TAG, "BEGIN mConnectedThread");
            //此处需做判断,有效数据的最后一位是否是0,如果不是0,要等到接收到有效数据最后一位是0为止,然后组合在一起返回.调试过程中发现有时发送一条命令,响应分两次返回.
            while (true) {
                try {
                    if (mmSocket.isConnected() && mState == ConstantDefine.CONNECT_STATE_CONNECTED) {
                        int len;
                        //按需分配大小进行数据读取
                        if ((len = mmInputStream.available()) > 0) {
                            perDatas = new byte[len];
                            perLen = mmInputStream.read(perDatas);

                            //数据完整性处理
                            fullDatas = addBytes(fullDatas, perDatas);
                            totalLen += perLen;

                            if (perDatas[len - 1] == 0) {//完整的数据(此处针对打印机进行特殊处理)
                                //接收到的数据分两条线路获取,一条是通过回调的mHandler,另一条是通过setCurrentCMDReadData用于命令返回.
                                mHandler.obtainMessage(ConstantDefine.MESSAGE_STATE_READ, totalLen, -1, fullDatas).sendToTarget();
                                //保存当前命令的返回数据
                                setCurrentCMDReadData(fullDatas);

                                //复位
                                fullDatas = new byte[0];
                                totalLen = 0;
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // 读取数据失败处理
                    //connectionLost();
                    break;
                }
            }

            /*byte[] bufferDatas = new byte[1024];
            //读取到的字节数
            int bytes;
            while(true){
                try{
                    bytes = mmInputStream.read(bufferDatas);

                    //保存当前命令的返回数据
                    setCurrentCMDReadData(bufferDatas);

                    mHandler.obtainMessage(ConstantDefine.MESSAGE_STATE_READ, bytes, -1, bufferDatas).sendToTarget();
                }catch (Exception ex){
                    ex.printStackTrace();
                    // 读取数据失败处理
                    connectionLost();
                    break;
                }
            }*/
        }

        public void write(byte[] data) {
            try {
                //清空上次返回值
                currentCMDReadData = null;
                //SNBC清除禁止打印状态
                if (data != null && data.length > 1 && data[0] == 0x21) {
                    Log.d(TAG, "**SNBC**");
                    mmOutputStream.write(new byte[]{0x1b, 0x41});
                }
                Log.d(TAG, "write  " + Arrays.toString(data));
                mmOutputStream.write(data);
                mmOutputStream.flush();
                mHandler.obtainMessage(ConstantDefine.MESSAGE_STATE_WRITE, -1, -1, data).sendToTarget();
            } catch (Exception ex) {
                try {
                    mmOutputStream.flush();
                    mmOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ex.printStackTrace();
            }
        }

        public void cancel() {
            try {
                fullDatas = null;
                perDatas = null;
                mmSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * byte[]组合
     *
     * @param data1
     * @param data2
     * @return data1 与 data2拼接的结果
     */
    public static byte[] addBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    public static byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }
}
