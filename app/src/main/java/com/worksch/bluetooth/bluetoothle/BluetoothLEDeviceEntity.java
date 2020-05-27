package com.worksch.bluetooth.bluetoothle;

public class BluetoothLEDeviceEntity {
    /**
     * name
     */
    private String name;

    /**
     * @param address
     */
    private String address;

    public BluetoothLEDeviceEntity(String name, String address, String rssi) {
        this.name = name;
        this.address = address;
        this.rssi = rssi;
    }

    /**
     * rssi
     */
    private String rssi;

    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    /**
     *
     * @param address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    public String getRssi() {
        return rssi;
    }

    /**
     *
     * @param rssi
     */
    public void setRssi(String rssi) {
        this.rssi = rssi;
    }
}
