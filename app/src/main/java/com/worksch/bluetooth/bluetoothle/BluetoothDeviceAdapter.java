package com.worksch.bluetooth.bluetoothle;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDeviceEntity> {
    private int resource;

    private List<BluetoothDeviceEntity> bleDevices = new ArrayList<>();

    public BluetoothDeviceAdapter(Context context, int resource) {
        super(context, resource);
        this.resource = resource;
    }

    public BluetoothDeviceAdapter(Context context, int resource, List<BluetoothDeviceEntity> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.bleDevices = objects;
    }

    public void setBleDevices(List<BluetoothDeviceEntity> bleDevices) {
        this.bleDevices = bleDevices;
    }

    /**
     * 只更新单条数据且不更新界面
     */
    public void updateChangedItemBean(int index, BluetoothDeviceEntity entity ) {
        bleDevices.set(index, entity);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BluetoothDeviceEntity entity = getItem(position);//获取当前项的实例

        View view = LayoutInflater.from(getContext()).inflate(resource, parent, false);

        ((TextView) view.findViewById(R.id.name)).setText(entity.getDevice().getName());
        ((TextView) view.findViewById(R.id.address)).setText(entity.getDevice().getAddress());
        //((TextView) view.findViewById(R.id.rssi)).setText(entity.getDevice()());

        /*BluetoothLEDeviceEntity entity = getItem(position);//获取当前项的实例
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resource, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.name = (TextView) view.findViewById(R.id.name);
            viewHolder.address = (TextView) view.findViewById(R.id.address);
            viewHolder.rssi = (TextView) view.findViewById(R.id.rssi);

            view.setTag(viewHolder);//保存
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();//取出
        }

        viewHolder.name.setText(entity.getName());
        viewHolder.address.setText(entity.getAddress());
        viewHolder.rssi.setText(entity.getRssi());*/

        return view;
    }


    private class ViewHolder {
        TextView name;
        TextView address;
        TextView rssi;
    }
}
