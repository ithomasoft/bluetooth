package com.ithomasoft.bluetooth.listener;

/**
 * 蓝牙连接状态改变监听
 */
public interface BluetoothStateListener {
    /**
     * 连接状态改变结果回调
     * */
    void onConnectStateChanged(int state);
}
