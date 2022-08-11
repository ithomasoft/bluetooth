package com.ithomasoft.bluetooth.demo;

import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import com.allen.library.SuperTextView;
import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.ithomasoft.bluetooth.Bluetooth;

public class DeviceAdapter extends BaseSectionQuickAdapter<DeviceInfo, BaseViewHolder> {
    public DeviceAdapter() {
        super(R.layout.item_device_head);
        setNormalLayout(R.layout.item_device_content);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, DeviceInfo deviceInfo) {
        SuperTextView stvDevice = baseViewHolder.getView(R.id.root_view);

        BluetoothDevice bluetoothDevice = deviceInfo.getBluetoothDevice();
        stvDevice.setCenterTopString(bluetoothDevice.getName() + "==" + (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED ? "已配对" : "未配对"))
                .setCenterString(bluetoothDevice.getAddress());
        if (Bluetooth.getInstance().getServiceState() == Bluetooth.CONNECT_STATE_CONNECTED) {
            if (bluetoothDevice.getAddress().equals(Bluetooth.getInstance().getConnectDeviceAddress())) {
                stvDevice.setRightString("已连接");
            } else {
                stvDevice.setRightString("");
            }
        } else {
            stvDevice.setRightString("");
        }
        stvDevice.setCenterTopString(bluetoothDevice.getName())
                .setCenterString(bluetoothDevice.getAddress());
    }

    @Override
    protected void convertHeader(BaseViewHolder baseViewHolder, DeviceInfo deviceInfo) {
        baseViewHolder.setText(R.id.tv_title, deviceInfo.getTitle());
        baseViewHolder.setGone(R.id.loading_progress, !deviceInfo.isShowLoading());
    }

    public boolean containDevice(BluetoothDevice bluetoothDevice) {
        for (DeviceInfo deviceInfo : getData()) {
            if (deviceInfo.getBluetoothDevice() != null) {
                if (TextUtils.equals(deviceInfo.getBluetoothDevice().getAddress(),
                        bluetoothDevice.getAddress())) {
                    return true;
                }
            }
        }
        return false;
    }

    public DeviceInfo getDevice(BluetoothDevice bluetoothDevice) {
        for (DeviceInfo deviceInfo : getData()) {
            if (deviceInfo.getBluetoothDevice() != null) {
                if (TextUtils.equals(deviceInfo.getBluetoothDevice().getAddress(),
                        bluetoothDevice.getAddress())) {
                    return deviceInfo;
                }
            }
        }
        return null;
    }
}
