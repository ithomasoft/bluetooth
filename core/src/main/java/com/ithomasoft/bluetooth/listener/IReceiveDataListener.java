package com.ithomasoft.bluetooth.listener;

/**
 * 接收远程设备数据
 */
public interface IReceiveDataListener {
    /**
     * 接收到的数据回调
     * */
    void onReceiveData(byte[] data);
}
