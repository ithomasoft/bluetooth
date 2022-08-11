package com.ithomasoft.bluetooth.demo;

import android.app.Application;

import com.ithomasoft.bluetooth.Bluetooth;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bluetooth.getInstance().init();
    }

    @Override
    public void onTerminate() {
        Bluetooth.getInstance().unInit();
        super.onTerminate();
    }
}
