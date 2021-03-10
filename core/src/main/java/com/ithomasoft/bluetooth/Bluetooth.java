package com.ithomasoft.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.ithomasoft.bluetooth.listener.BluetoothConnectListener;
import com.ithomasoft.bluetooth.listener.BluetoothStateListener;
import com.ithomasoft.bluetooth.listener.DiscoveryDevicesListener;
import com.ithomasoft.bluetooth.listener.IReceiveDataListener;
import com.ithomasoft.bluetooth.service.BluetoothService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Bluetooth {
    private Context mContext;

    private BluetoothAdapter mBluetoothAdapter;
    private String mConnectDeviceName;
    private String mConnectDeviceAddress;

    //listener
    private BluetoothConnectListener mConnectListener = null;
    private BluetoothStateListener mStateListener = null;
    private IReceiveDataListener mReceiveDataListener = null;
    private DiscoveryDevicesListener mDiscoveryDevicesListener = null;

    private BluetoothService mBTService;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isServiceRunning = false;

    private List<BluetoothDevice> mDeviceList = null;
    private DiscoveryReceiver mReceiver;
    private boolean isRegister = false;


    //定义当前的连接状态
    public static final int CONNECT_STATE_NONE = 0;         //什么都没有连接
    public static final int CONNECT_STATE_LISTENER = 1;     //侦听连接
    public static final int CONNECT_STATE_CONNECTING = 2;   //正在连接
    public static final int CONNECT_STATE_CONNECTED = 3;    //已经连接


    //Handler的消息类型
    public static final int MESSAGE_STATE_READ = 1;
    public static final int MESSAGE_STATE_WRITE = 2;
    public static final int MESSAGE_STATE_CHANGE = 3;
    public static final int MESSAGE_DEVICE_INFO = 4;

    //Intent请求码
    public static final int REQUEST_CONNECT_BT = 0x111;     //请求连接蓝牙
    public static final int REQUEST_ENABLED_BT = 0x222;     //请求开启蓝牙

    //需要的UUID
    public static final String DEFAULT_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    public static final String KEY_DEVICE_NAME = "device_name";
    public static final String KEY_DEVICE_ADDRESS = "device_address";

    public Bluetooth(Context context) {
        this.mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 判断设备是否支持蓝牙
     */
    public boolean isBluetoothSupported() {
        if (mBluetoothAdapter == null) {
            return false;
        }
        return true;
    }

    /**
     * 判断蓝牙是否可用
     */
    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    /**
     * 判断蓝牙服务是否可用
     */
    public boolean isServiceAvailable() {
        return mBTService != null;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * 开启远程蓝牙设备扫描: 不需要独自调用，在进行搜索回调中已经被调用
     */
    public boolean startDiscovery() {

        return mBluetoothAdapter.startDiscovery();
    }

    /**
     * 判断发现蓝牙设备进程是否正在运行
     */
    public boolean isDiscovering() {
        return mBluetoothAdapter.isDiscovering();
    }

    /**
     * 取消设备发现进程
     */
    public boolean cancelDiscovery() {
        return mBluetoothAdapter.cancelDiscovery();
    }

    public void setupService() {
        mBTService = new BluetoothService(mHandler);
        if (mBTService.getState() == CONNECT_STATE_NONE) {
            mBTService.start();
            isServiceRunning = true;
        }
    }

    public int getServiceState() {
        if (mBTService != null) {
            return mBTService.getState();
        } else {
            return -1;
        }
    }

    public void stopService() {
        if (mBTService != null) {
            mBTService.stop();
            isServiceRunning = false;
        }
        if (isRegister) {
            mContext.unregisterReceiver(mReceiver);
            isRegister = false;
        }

        mDeviceList = null;
    }

    public boolean isServiceRunning() {
        return isServiceRunning;
    }

    /**
     * 通过MAC地址连接蓝牙设备
     */
    public void connect(String address) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (mBTService != null) {
            mBTService.connect(device);
        }
    }

    public void connect(BluetoothDevice device) {
        if (mBTService != null) {
            mBTService.connect(device);
        }
    }

    /**
     * 断开连接
     */
    public void disconnect() {
        if (mBTService != null) {
            mBTService.stop();
            isServiceRunning = false;
            if (mBTService.getState() == CONNECT_STATE_NONE) {
                mBTService.start();
                isServiceRunning = true;
            }
        }
    }

    /**
     * 写入数据
     */
    public void write(byte[] data) {
        if (mBTService.getState() == CONNECT_STATE_CONNECTED) {
            mBTService.write(data);
        }
    }

    /**
     * 得到配对成功的设备集合
     */
    public Set<BluetoothDevice> getPairedDevices() {
        return mBluetoothAdapter.getBondedDevices();
    }

    /**
     * 得到连接成功的设备名称
     */
    public String getConnectDeviceName() {
        return mConnectDeviceName;
    }

    /**
     * 得到连接成功的设备Mac地址
     */
    public String getConnectDeviceAddress() {
        return mConnectDeviceAddress;
    }

    public void setBlueStateListener(BluetoothStateListener listener) {
        this.mStateListener = listener;
    }

    public void setReceiveDataListener(IReceiveDataListener listener) {
        this.mReceiveDataListener = listener;
    }

    public void setBluetoothConnectListener(BluetoothConnectListener listener) {
        this.mConnectListener = listener;
    }

    public void setDiscoveryDeviceListener(DiscoveryDevicesListener listener) {
        this.mDiscoveryDevicesListener = listener;

        mDeviceList = new ArrayList<>();
        if (mReceiver == null) {
            mReceiver = new DiscoveryReceiver();
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, intentFilter);
        isRegister = true;
        mDeviceList.clear();

        if (isDiscovering()) {
            cancelDiscovery();
        }

        startDiscovery();
    }

    /**
     * 发现蓝牙设备广播
     */
    public class DiscoveryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                if (mDiscoveryDevicesListener != null) {
                    mDiscoveryDevicesListener.startDiscovery();
                }
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (mDiscoveryDevicesListener != null) {
                    Log.e("haha >>>", "onReceive --- device.toString: " + device.getName() + ":" + device.getAddress());
                    mDiscoveryDevicesListener.discoveryNew(device);
                }
                mDeviceList.add(device);
            }
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mDiscoveryDevicesListener != null) {
                    Log.e("haha >>>", "onReceive --- mDeviceList.size() = " + mDeviceList.size());
                    mDiscoveryDevicesListener.discoveryFinish(mDeviceList);
                }
            }
        }
    }

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                //读取数据
                case MESSAGE_STATE_READ:
                    byte[] data = (byte[]) msg.obj;
                    if (data != null && data.length > 0) {
                        if (mReceiveDataListener != null) {
                            mReceiveDataListener.onReceiveData(data);
                        }
                    }
                    break;
                case MESSAGE_STATE_WRITE:

                    break;
                case MESSAGE_DEVICE_INFO:
                    mConnectDeviceName = msg.getData().getString(KEY_DEVICE_NAME);
                    mConnectDeviceAddress = msg.getData().getString(KEY_DEVICE_ADDRESS);
                    if (mConnectListener != null) {
                        mConnectListener.onBTDeviceConnected(mConnectDeviceAddress, mConnectDeviceName);
                    }
                    isConnected = true;
                    break;
                case MESSAGE_STATE_CHANGE:
                    if (mStateListener != null) {
                        mStateListener.onConnectStateChanged(msg.arg1);
                    }
                    if (isConnected && msg.arg1 != CONNECT_STATE_CONNECTED) {
                        if (mConnectListener != null) {
                            mConnectListener.onBTDeviceDisconnected();
                        }
                        isConnected = false;
                        mConnectDeviceName = null;
                        mConnectDeviceAddress = null;
                    }

                    if (!isConnecting && msg.arg1 == CONNECT_STATE_CONNECTING) {
                        isConnecting = true;
                    } else if (isConnecting) {
                        if (msg.arg1 != CONNECT_STATE_CONNECTED) {
                            if (mConnectListener != null) {
                                mConnectListener.onBTDeviceConnectFailed();
                            }
                        }
                        isConnecting = false;
                    }
                    break;
                default:
                    break;
            }
        }
    };
}
