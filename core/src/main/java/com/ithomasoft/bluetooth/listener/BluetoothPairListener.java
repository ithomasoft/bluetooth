package com.ithomasoft.bluetooth.listener;

import android.bluetooth.BluetoothDevice;

/**
 * 蓝牙配对监听
 */
public interface BluetoothPairListener {
    void onPairRemoved(BluetoothDevice bluetoothDevice);
    void onPairing(BluetoothDevice bluetoothDevice);
    void onPairSuccess(BluetoothDevice bluetoothDevice);
}
