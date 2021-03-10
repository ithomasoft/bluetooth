package com.ithomasoft.bluetooth.listener;

import android.bluetooth.BluetoothDevice;

import java.util.List;

/**
 * 发现蓝牙设备结果监听
 */
public interface DiscoveryDevicesListener {
    void startDiscovery();

    void discoveryNew(BluetoothDevice device);

    void discoveryFinish(List<BluetoothDevice> list);
}
