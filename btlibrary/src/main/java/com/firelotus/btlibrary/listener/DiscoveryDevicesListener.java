package com.firelotus.btlibrary.listener;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * Description: 发现蓝牙设备结果监听
 */
public interface DiscoveryDevicesListener {

    void startDiscovery();

    void discoveryNew(BluetoothDevice device);

    void discoveryFinish(List<BluetoothDevice> list);
}
