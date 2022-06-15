package com.ithomasoft.bluetooth.demo;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemLongClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.ithomasoft.bluetooth.Bluetooth;
import com.ithomasoft.bluetooth.listener.BluetoothConnectListener;
import com.ithomasoft.bluetooth.listener.BluetoothPairListener;
import com.ithomasoft.bluetooth.listener.BluetoothStateListener;
import com.ithomasoft.bluetooth.listener.DiscoveryDevicesListener;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private Button btnOpen;
    private Button btnClose;
    private Button btnStart;
    private Button btnStop;
    private TextView tvState;
    private RecyclerView rvDevice, rvLocation;
    private DeviceAdapter mAdapter;
    private DeviceInfo pairedTitle = new DeviceInfo("已配对设备", false);
    private DeviceInfo unpairedTitle = new DeviceInfo("可用设备", false);
    private BaseQuickAdapter<String, BaseViewHolder> locationAdapter = new BaseQuickAdapter<String, BaseViewHolder>(R.layout.item_location) {
        @Override
        protected void convert(BaseViewHolder baseViewHolder, String s) {
            baseViewHolder.setText(R.id.tv_title, s);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpen = findViewById(R.id.btn_open);
        btnClose = findViewById(R.id.btn_close);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        tvState = findViewById(R.id.tv_state);
        rvDevice = findViewById(R.id.rv_device);
        rvLocation = findViewById(R.id.rv_location);
        mAdapter = new DeviceAdapter();
        rvDevice.setLayoutManager(new LinearLayoutManager(this));
        rvDevice.setAdapter(mAdapter);
        rvLocation.setLayoutManager(new LinearLayoutManager(this));
        rvLocation.setAdapter(locationAdapter);

        Bluetooth.getInstance().setBlueStateListener(new BluetoothStateListener() {

            @Override
            public void onOpening() {
                Toast.makeText(MainActivity.this, "打开蓝牙中", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClosing() {
                Toast.makeText(MainActivity.this, "关闭蓝牙中", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onOpened() {
                resetBluetoothState();
                Toast.makeText(MainActivity.this, "打开蓝牙成功", Toast.LENGTH_SHORT).show();
                getPairedDevices();

            }

            @Override
            public void onClosed() {
                resetBluetoothState();
                Toast.makeText(MainActivity.this, "关闭蓝牙成功", Toast.LENGTH_SHORT).show();
            }
        });

        Bluetooth.getInstance().setBluePairListener(new BluetoothPairListener() {
            @Override
            public void onPairRemoved(BluetoothDevice bluetoothDevice) {
                DeviceInfo deviceInfo = mAdapter.getDevice(bluetoothDevice);
                if (deviceInfo != null) {
                    deviceInfo.setPair(false);
                }
                mAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "取消配对", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPairing(BluetoothDevice bluetoothDevice) {
                Toast.makeText(MainActivity.this, "配对中", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPairSuccess(BluetoothDevice bluetoothDevice) {
                DeviceInfo deviceInfo = mAdapter.getDevice(bluetoothDevice);
                if (deviceInfo != null) {
                    deviceInfo.setPair(true);
                }
                mAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "配对成功", Toast.LENGTH_SHORT).show();
            }
        });

        Bluetooth.getInstance().setBluetoothConnectListener(new BluetoothConnectListener() {
            @Override
            public void onBTDeviceConnected(String address, String name) {
                mAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                registerSouthGnss();
            }

            @Override
            public void onBTDeviceDisconnected() {
                Toast.makeText(MainActivity.this, "取消连接", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBTDeviceConnectFailed() {
                Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
            }
        });

        if (Bluetooth.getInstance().isBluetoothSupported()) {
            btnOpen.setEnabled(true);
            btnClose.setEnabled(true);
            btnStart.setEnabled(true);
            btnStop.setEnabled(true);
            resetBluetoothState();
        } else {
            btnOpen.setEnabled(false);
            btnClose.setEnabled(false);
            btnStart.setEnabled(false);
            btnStop.setEnabled(false);
        }
        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.getInstance().openBluetooth();
            }
        });
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.getInstance().closeBluetooth();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPairedDevices();
                Bluetooth.getInstance().startDiscovery();
                Bluetooth.getInstance().setDiscoveryDeviceListener(new DiscoveryDevicesListener() {
                    @Override
                    public void startDiscovery() {
                        btnStart.setEnabled(false);
                        btnStop.setEnabled(true);
                        unpairedTitle.setShowLoading(true);
                        mAdapter.notifyItemChanged(mAdapter.getItemPosition(unpairedTitle));
                    }

                    @SuppressLint("MissingPermission")
                    @Override
                    public void discoveryNew(BluetoothDevice device) {
                        if (device != null) {
                            if (!TextUtils.isEmpty(device.getName())) {
                                if (!mAdapter.containDevice(device)) {
                                    mAdapter.addData(new DeviceInfo(device, false));
                                }
                            }
                        }
                    }

                    @Override
                    public void discoveryFinish(List<BluetoothDevice> list) {
                        btnStart.setEnabled(true);
                        btnStop.setEnabled(false);
                        unpairedTitle.setShowLoading(false);
                        mAdapter.notifyItemChanged(mAdapter.getItemPosition(unpairedTitle));
                        if (list.isEmpty()) {
                            Toast.makeText(MainActivity.this, "没有发现新的设备", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bluetooth.getInstance().cancelDiscovery();
            }
        });
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            DeviceInfo info = mAdapter.getData().get(position);
            if (!info.isHeader()) {
                pairDevice(info.getBluetoothDevice());
            }
        });
        mAdapter.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {

                DeviceInfo info = mAdapter.getData().get(position);
                if (!info.isHeader()) {
                    unPairDevice(info.getBluetoothDevice());
                }
                return false;
            }
        });

    }

    private LocationManager mLocationManager;

    @SuppressLint("MissingPermission")
    private void registerSouthGnss() {
        XXPermissions.with(this).permission(Permission.ACCESS_FINE_LOCATION, Permission.ACCESS_COARSE_LOCATION).request(new OnPermissionCallback() {
            @Override
            public void onGranted(List<String> permissions, boolean all) {
                mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                        1000, 0, MainActivity.this);
                mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, MainActivity.this, Looper.getMainLooper());
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void unPairDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            Bluetooth.getInstance().disBoundDevice(bluetoothDevice);
            Bluetooth.getInstance().disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    private void pairDevice(BluetoothDevice bluetoothDevice) {
        Bluetooth.getInstance().cancelDiscovery();
        if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_NONE) {
            Bluetooth.getInstance().bondDevice(bluetoothDevice);
        } else if (bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            if (Bluetooth.getInstance().getServiceState() == Bluetooth.CONNECT_STATE_CONNECTED) {
                if (!Bluetooth.getInstance().getConnectDeviceAddress().equals(bluetoothDevice.getAddress())) {
                    Bluetooth.getInstance().connect(bluetoothDevice);
                } else {
//                    showDisconnectDialog(bluetoothDevice);
                }
            } else {
                Bluetooth.getInstance().connect(bluetoothDevice);
            }


        }
    }

    private void getPairedDevices() {
        mAdapter.getData().clear();
        mAdapter.addData(pairedTitle);
        Iterator<BluetoothDevice> it = Bluetooth.getInstance().getPairedDevices().iterator();
        while (it.hasNext()) {
            BluetoothDevice bluetoothDevice = it.next();
            mAdapter.addData(new DeviceInfo(bluetoothDevice, true));
        }
        mAdapter.addData(unpairedTitle);
    }

    public void resetBluetoothState() {
        if (Bluetooth.getInstance().isBluetoothEnabled()) {
            btnOpen.setEnabled(false);
            btnClose.setEnabled(true);
            btnStart.setEnabled(true);
        } else {
            btnOpen.setEnabled(true);
            btnClose.setEnabled(false);
            btnStart.setEnabled(false);
            btnStop.setEnabled(false);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.e("location", location.toString());
        locationAdapter.addData("onLocationChanged:" + location.toString());
        if (location != null) {
            onStatusChanged(LocationManager.GPS_PROVIDER, 111, location.getExtras());
        }
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
//        Log.e("location", location.toString());
    }

    @Override
    public void onFlushComplete(int requestCode) {
//        Log.e("location", location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("location", "onStatusChanged:" + provider + "--" + status + "--");
        locationAdapter.addData("onStatusChanged:" + provider + "--" + status + "--");
        Set<String> set = extras.keySet();
        for (String key : set) {
            locationAdapter.addData("onStatusChanged:" + key + "--" + extras.get(key));
        }

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        Log.e("location", "onProviderEnabled:" + provider);
        locationAdapter.addData("onProviderEnabled:" + provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        Log.e("location", "onProviderDisabled:" + provider);
        locationAdapter.addData("onProviderDisabled:" + provider);
    }
}