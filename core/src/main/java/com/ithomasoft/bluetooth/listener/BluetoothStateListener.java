package com.ithomasoft.bluetooth.listener;

/**
 * 蓝牙连接状态改变监听
 */
public interface BluetoothStateListener {

    /**
     * 蓝牙打开中回调
     */
    void onOpening();

    /**
     * 蓝牙关闭中回调
     */
    void onClosing();

    /**
     * 蓝牙已经打开回调
     */
    void onOpened();

    /**
     * 蓝牙已经关闭回调
     */
    void onClosed();
}
