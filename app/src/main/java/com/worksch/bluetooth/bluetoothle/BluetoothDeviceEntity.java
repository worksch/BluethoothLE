package com.worksch.bluetooth.bluetoothle;

import android.bluetooth.BluetoothDevice;

public class BluetoothDeviceEntity {

    private BluetoothDevice device;
    private int rssi;

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public BluetoothDeviceEntity(BluetoothDevice device, int rssi) {
        this.device = device;
        this.rssi = rssi;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
