package com.ithomasoft.bluetooth.demo;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.chad.library.adapter.base.entity.JSectionEntity;

public class DeviceInfo extends JSectionEntity {
    private String title;

    private BluetoothDevice bluetoothDevice;

    private boolean isPair;
    private boolean showLoading;


    public DeviceInfo(String title, boolean showLoading) {
        this.title = title;
        this.showLoading = showLoading;
    }

    public DeviceInfo(BluetoothDevice bluetoothDevice, boolean isPair) {
        this.bluetoothDevice = bluetoothDevice;
        this.isPair = isPair;
    }

    public boolean isShowLoading() {
        return showLoading;
    }

    public void setShowLoading(boolean showLoading) {
        this.showLoading = showLoading;
    }

    public boolean isPair() {
        return isPair;
    }

    public void setPair(boolean pair) {
        isPair = pair;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public boolean isHeader() {
        return !TextUtils.isEmpty(title);
    }
}
