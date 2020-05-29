package com.worksch.bluetooth.bluetoothle;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    //获取系统蓝牙适配器管理类
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothLeScanner mBluetoothScanner;
    private List<BluetoothGattService> mBluetoothServicesList;

    //BLE工作子线程
    private Handler mBleHandler = new Handler();
    private Handler mMainHandler;
    private Button startScanBtn;
    private Button stopScanBtn;

    //扫描时间
    private int mScanTime = 2000;
    private BluetoothDeviceAdapter bleDeviceAdapter;
    private List<BluetoothDeviceEntity> bleDeviceList = new ArrayList<>();
    private ListView bleDeviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch(msg.what){
                    case 1:
                        BluetoothDeviceEntity entity = (BluetoothDeviceEntity) msg.obj;
                        //Toast.makeText(MainActivity.this, entity.getAddress(), Toast.LENGTH_SHORT).show();
                        refreshBleDeviceList(entity);
                        break;
                }
            }
        };

        mBluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();

        /* 初始化蓝牙显示列表 */
        bleDeviceAdapter = new BluetoothDeviceAdapter(MainActivity.this, R.layout.bluetoothle_layout, bleDeviceList);
        bleDeviceListView = findViewById(R.id.bledevicelistview);
        bleDeviceListView.setAdapter(bleDeviceAdapter);
        bleDeviceListView.setOnItemClickListener( (parent, view, position, id) ->  {
                BluetoothDeviceEntity entity = bleDeviceAdapter.getItem(position);
                //bleDeviceAdapter.updateChangedItemBean(position, entity);
                mBluetoothGatt = entity.getDevice().connectGatt(this, false, gattCallback);
                Toast.makeText(MainActivity.this, entity.getDevice().getName(), Toast.LENGTH_SHORT).show();
        });

        // > 23 (6.0) 检查权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        startScanBtn = findViewById(R.id.startScanBtn);
        startScanBtn.setOnClickListener(event -> {
            startBleScan();
        });

        stopScanBtn = findViewById(R.id.stopScanBtn);
        stopScanBtn.setOnClickListener(event -> {
            stopBleScan();
        });
        stopScanBtn.setVisibility(View.INVISIBLE);

        // 询问打开蓝牙
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.e(TAG, "设备连接上 开始扫描服务");
                // 开始扫描服务，安卓蓝牙开发重要步骤之一
                mBluetoothGatt.discoverServices();
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                // 连接断开
                /*连接断开后的相应处理*/
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "发送成功");
            }
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {

        };

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //获取服务列表
            mBluetoothServicesList = mBluetoothGatt.getServices();
            Log.d(TAG, ""+mBluetoothServicesList.size());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // value为设备发送的数据，根据数据协议进行解析
            byte[] value = characteristic.getValue();
        }
    };

    private void refreshBleDeviceList(BluetoothDeviceEntity entity){
        for (int i = 0; i < bleDeviceList.size(); i++)
        {
            BluetoothDeviceEntity oldEntity = bleDeviceList.get(i);
            if (null != oldEntity && oldEntity.getDevice().getAddress() == entity.getDevice().getAddress()) {
                bleDeviceList.set(i, entity);
                return;
            }
        }
        //添加  刷新
        bleDeviceList.add(entity);
        bleDeviceAdapter.notifyDataSetChanged();
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        //当一个蓝牙ble广播被发现时回调
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //扫描类型有开始扫描时传入的ScanSettings相关
            //对扫描到的设备进行操作。如：获取设备信息。
            Log.d(TAG, result.getDevice().getName());

            BluetoothDeviceEntity entity = new BluetoothDeviceEntity(result.getDevice(), result.getRssi());

            Message msg = new Message();
            msg.what = 1;
            msg.obj = entity;
            mMainHandler.sendMessage(msg);
        }

        //批量返回扫描结果
        //@param results 以前扫描到的扫描结果列表。
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        //当扫描不能开启时回调
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            //扫描太频繁会返回ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED，表示app无法注册，无法开始扫描。

        }
    };

    private boolean startBleScan() {
        startScanBtn.setVisibility(View.INVISIBLE);
        stopScanBtn.setVisibility(View.VISIBLE);

        bleDeviceList.clear();
        bleDeviceAdapter.notifyDataSetChanged();    // 清空数据表

        mBluetoothScanner.startScan(mScanCallback);
        //设置结束扫描 //关闭ble扫描
        mBleHandler.postDelayed(() -> { stopBleScan(); }, mScanTime);

        return true;
    }

    private boolean stopBleScan() {
        Log.d(TAG,"Stopped Scanning");
        startScanBtn.setVisibility(View.VISIBLE);
        stopScanBtn.setVisibility(View.INVISIBLE);
        mBluetoothScanner.stopScan(mScanCallback);

        return true;
    }

    // 申请打开蓝牙请求的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "蓝牙已经开启", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "没有蓝牙权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }
}
