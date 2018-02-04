package com.firelotus.btlibrary.listener;

/**
 * Description: 蓝牙连接状态改变监听
 */
public interface BluetoothStateListener {

    /**
     * 连接状态改变结果回调
     */
    void onConnectStateChanged(int state);
}
