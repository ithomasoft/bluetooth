package com.ithomasoft.bluetooth.demo;

import android.app.Application;

import com.ithomasoft.bluetooth.Bluetooth;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bluetooth.init(this);
    }

    @Override
    public void onTerminate() {
        Bluetooth.getInstance().stopService();
        super.onTerminate();
    }
}
