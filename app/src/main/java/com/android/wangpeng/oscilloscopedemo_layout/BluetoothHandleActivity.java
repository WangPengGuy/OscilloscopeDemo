package com.android.wangpeng.oscilloscopedemo_layout;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothHandleActivity extends Activity {

    private ListView bluetoothDeviceListView;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothReceiver bluetoothReceive;
    private ArrayAdapter<String> arrayAdapter;
    private List<BluetoothDevice> deviceList;    //存储已配对的和搜索到的蓝牙设备
    private List<String> deviceNameList;  //存储已配对的和搜索到的蓝牙设备名称
    private List<String> deviceAddressList;  //存储已配对的和搜索到的蓝牙设备地址
    private Intent resultIntent = new Intent();
    static BluetoothSocket mmSocket = null;  //蓝牙连接后返回的socket
    static BluetoothDevice mmDevice = null;  //已连接的蓝牙设备

    private final int REQUEST_ENABLE_BT = 1;
    private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB" ;   //SPP(串口)服务UUID号
    public final static String ACTION_DEVICE_CONNECTED = "com.android.bluetooth.device.CONNECTED";
    public final static String ACTION_DEVICE_DISCONNECTED = "com.android.bluetooth.device.DISCONNECTED";
    final static String EXTRA_DEVICE_ADDRESS = "设备地址";  //返回数据时的数据标签

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // 创建并显示窗口,并设置窗口显示模式为窗口方式
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_bluetooth_handle);
        //初始化显示列表、存储队列
        bluetoothDeviceListView = (ListView) findViewById(R.id.bluetoothDeviceListView);
        deviceList = new ArrayList<BluetoothDevice>();
        deviceNameList = new ArrayList<String>();
        deviceAddressList = new ArrayList<>();
        //配置显示列表
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.bluetooth_devices_list, deviceNameList);
        bluetoothDeviceListView.setAdapter(arrayAdapter);
        bluetoothDeviceListView.setOnItemClickListener(mOnItemClickListener);
        //得到本地蓝牙适配器
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //新建一个BroadcastReceiver,用于监听蓝牙的ACTION
        bluetoothReceive = new BluetoothReceiver();
        //注册接收查找到设备action接收器
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceive, intentFilter);
        //注册查找结束的ACTION接收器
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceive, intentFilter);
        //注册连接设备成功的action接收器
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        registerReceiver(bluetoothReceive, intentFilter);
        //注册连接断开的action接收器
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothReceive, intentFilter);
        //如果蓝牙没有打开，先打开蓝牙
        openBluetooth();
        //进行搜索
        doStartDiscovery();
        //得到本地蓝牙适配器已经配对过的设备
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceList.add(device);
                deviceNameList.add(device.getName());
                deviceAddressList.add(device.getAddress());
            }
        }else{
            deviceList.add(null);
            deviceNameList.add("没有已配对的蓝牙设备");
            deviceAddressList.add(" ");
        }
    }

    //选择要配对的设备
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            System.out.println("点击了设备");
            //关闭服务查找，准备连接设备
            bluetoothAdapter.cancelDiscovery();
            BluetoothDevice bluetoothDevice = deviceList.get(position);
            connectBluetoothDevice(bluetoothDevice);
        }
    };

    //蓝牙设备进行连接的方法
    private void connectBluetoothDevice(BluetoothDevice bluetoothDevice){
        ConnectThread connectThread = new ConnectThread(bluetoothDevice);
        connectThread.start();
    }

    //蓝牙设备进行连接的线程
    private class ConnectThread extends Thread{
        public ConnectThread(BluetoothDevice bluetoothDevice){
            BluetoothSocket tmp = null;
            mmDevice = bluetoothDevice;
            try{
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mmSocket = tmp;
        }
        public void run(){
            try {
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    mmSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                return;
            }
        }
        public void cannel(){
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //开启蓝牙
    private void openBluetooth(){
        if(bluetoothAdapter == null){
            Toast.makeText(this, "您的设备不支持蓝牙（Bluetooth），无法使用本应用！", Toast.LENGTH_LONG).show();
        }
        if(!bluetoothAdapter.isEnabled()){
            setTitle("正在打开蓝牙...");
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    //开始服务和设备查找
    private void doStartDiscovery(){
        // 在窗口显示查找中信息
        setProgressBarIndeterminateVisibility(true);
        setTitle("查找设备中...");
        //关闭正在进行的服务查找
        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        //重新开始查找
        bluetoothAdapter.startDiscovery();
    }

    //查找到设备和搜索完成action监听器
    private class BluetoothReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothDevice.ACTION_FOUND:
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        deviceList.add(device);
                        //必须要判断蓝牙设备是否命名，否则会出现bug
                        if(device.getName() != null)
                            deviceNameList.add(device.getName());
                        else
                            deviceNameList.add("此设备未命名");
                        deviceAddressList.add(device.getAddress());
                        arrayAdapter.notifyDataSetChanged();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    setTitle("请选择设备");
                    setProgressBarIndeterminateVisibility(false);
                    Toast.makeText(BluetoothHandleActivity.this, "搜索设备完成", Toast.LENGTH_SHORT).show();
                    if(arrayAdapter.getCount() != bluetoothAdapter.getBondedDevices().size()){
                        int numberNewDevice = 0;
                        numberNewDevice = arrayAdapter.getCount() - bluetoothAdapter.getBondedDevices().size();
                        Toast.makeText(BluetoothHandleActivity.this, "共找到"+numberNewDevice+"个新设备", Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(BluetoothHandleActivity.this, "没有找到新设备", Toast.LENGTH_LONG).show();
                    }
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    BluetoothDevice device2 = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    setTitle("连接成功: "+device2.getName());
                    //设置返回数据
                    resultIntent.putExtra(EXTRA_DEVICE_ADDRESS, device2.getAddress());
                    //设置广播的action
                    resultIntent.setAction(BluetoothHandleActivity.ACTION_DEVICE_CONNECTED);
                    //设置返回值
                    while(!mmSocket.isConnected());
                        sendBroadcast(resultIntent);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    setTitle("连接已断开");
                    //设置广播的action
                    resultIntent.setAction(BluetoothHandleActivity.ACTION_DEVICE_DISCONNECTED);
                    //设置返回值
                    sendBroadcast(resultIntent);
                    break;
                default:
                    break;

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //System.out.println("onPause()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭服务查找
        if(bluetoothAdapter != null){
            bluetoothAdapter.cancelDiscovery();
        }
        //注销action接收器
        BluetoothHandleActivity.this.unregisterReceiver(bluetoothReceive);
    }
}
