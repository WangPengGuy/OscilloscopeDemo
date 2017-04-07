package com.android.wangpeng.oscilloscopedemo_layout;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

public class GetBluetoothDataService extends Service {

    private InputStream inputStreamFromBluetooth;    //输入流，用来接收蓝牙数据
    private byte[] buffer = new byte[1024];  //inputStream的输入缓冲
    private int[] data_adc_8bit = new int[1024];  //STM32的ADC对波形抽样转换的数据，从STM32发送上来的是8bit的数据
    private int[] data_adc_Channel1_8bit = new int[513];    //存储通道1的8bit的数据
    private int[] data_adc_Channel2_8bit = new int[513];    //存储通道2的8bit的数据
    private int[] data_adc_Channel1_16bit = new int[256];    //把通道1的8bit的数据转换成16bit存在此数组中
    private int[] data_adc_Channel2_16bit = new int[256];    //把通道2的8bit的数据转换成16bit存在此数组中
    private int[] data_wave_1 = new int[512];   //要在surfaceView上显示的Channel_1数据
    private int[] data_wave_2 = new int[512];   //要在surfaceView上显示的Channel_1数据
    private int number_buffer = 0;   //存储蓝牙接收到的数据长度
    int data_num=0;
    int data_num_temp = 0;
    public final static String ACTION_OSC_DATA_SEND = "com.android.getBluetoothDataService.SEND_OSC_DATA";
                                                            // 给MainActivity发送广播的action
    public final static String DATA_CHANNEL_1 = "Channel_1_data";   //发送通道1的数据的数据标签
    public final static String DATA_CHANNEL_2 = "Channel_2_data";   //发送通道2的数据的数据标签
    public final static String DATA_NUM_TEMP = "data_num_temp";

    private DeviceConnectStateReceiver deviceConnectStateReceiver;

    @Override
    public void onCreate() {
        System.out.println("service is onCreate()");
        deviceConnectStateReceiver = new DeviceConnectStateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothHandleActivity.ACTION_DEVICE_CONNECTED);
        intentFilter.addAction(BluetoothHandleActivity.ACTION_DEVICE_DISCONNECTED);
        GetBluetoothDataService.this.registerReceiver(deviceConnectStateReceiver, intentFilter);
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GetBluetoothDataService.this.unregisterReceiver(deviceConnectStateReceiver);   //注销BroadcastReceiver
        stopSelf();  //停止service
    }

    public class DeviceConnectStateReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case BluetoothHandleActivity.ACTION_DEVICE_CONNECTED:

                        //System.out.println("bluetooth socket is connected. ");
                        try {
                            inputStreamFromBluetooth = BluetoothHandleActivity.mmSocket.getInputStream();    //得到蓝牙输入数据流
                        } catch (IOException e) {
                            Toast.makeText(GetBluetoothDataService.this, "接收数据失败！", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        readThread.start();    //打开数据接收线程
                        dataSwitchAndDrawThread.start();   //打开数据转换和绘图线程
//                    }else{
//                        System.out.println("bluetooth socket is disconnected. ");
//                    }
                    break;
                case BluetoothHandleActivity.ACTION_DEVICE_DISCONNECTED:
                    Toast.makeText(GetBluetoothDataService.this, "未连接设备,请重新尝试连接！", Toast.LENGTH_LONG).show();
                    break;

            }
        }
    }

    /**
     * 接收蓝牙数据的线程
     */
    Thread readThread = new Thread(){
        public void run(){
            while(true){
                try {
                    number_buffer = inputStreamFromBluetooth.read(buffer);
                    System.out.println("接收了"+number_buffer+"个数据：");
                    for(int i=0; i<number_buffer; i++) {
                        if(buffer[i] < 0)
                            data_adc_8bit[data_num+i]= buffer[i] + 256;
                        else
                            data_adc_8bit[data_num+i]=buffer[i];
                    }
                    data_num = data_num + number_buffer;
                    System.out.println(data_num);
                    if(data_num >= 500){
                        data_num_temp = data_num;
                        data_num = 0;
                        //判断数据处理线程是否终结（terminated），即上一次的run()函数是否执行完成
                        if(dataSwitchAndDrawThread.getState() == State.TERMINATED){
                            dataSwitchAndDrawThread.run();
                        }
//                        for(int i=0; i<data_num_temp; i++){
//                            System.out.printf("%d  ",data_adc_8bit[i]);
//                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                try {
//                    if(inputStreamFromBluetooth.available() == 0)
//                        break;   //短时间没有数据才跳出进行显示
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }
    };

    /**
     * 对接收到的蓝牙数据进行转换并绘图的线程
     */
    Thread dataSwitchAndDrawThread = new Thread() {
        short i=0;
        int j1=0, j2=0;
        int check_bit_7_4 = 0;       //校验第7、4比特位：00 --> high_8_bit  ADC_Channel_0
                                       //                 01 --> high_8_bit  ADC_Channel_2
                                       //                 10 --> low_8_bit  ADC_Channel_0
                                       //                 11 --> low_8_bit  ADC_Channel_2
        int check_bit_6 = 0;         //通过校验第7、4比特位，如果是 high_8_bit，则第6位存储的是low_8_bit的第7位
        int check_bit_5 = 0;         //通过校验第7、4比特位，如果是 high_8_bit，则第5位存储的是low_8_bit的第4位
        int recover_bit_7_4 = 0;    //通过 check_bit_6 和 check_bit_5 恢复 low_8_bit 的第7、4比特位
        public void run(){
            for(i=0; i<data_num_temp; i++){
                check_bit_7_4 = data_adc_8bit[i] & 0x90;  //0x90 = 10010000,取出第7、4比特位
                check_bit_6 = data_adc_8bit[i] & 0x40;    //0x40 = 01000000,取出第6比特位
                check_bit_5 = data_adc_8bit[i] & 0x20;    //0x20 = 00100000,取出第5比特位
                switch (check_bit_7_4) {
                    case 0x00:     // high_8_bit  ADC_Channel_0
                        if (i % 2 == 0) {   //如果在偶数位
                            data_adc_Channel1_8bit[j1++] = data_adc_8bit[i] & 0x0f;      //将高4位置0，还原数据
                        } else {
                            if (j1 == 0) j1 = j1 + 1;
                            data_adc_Channel1_8bit[--j1] = data_adc_8bit[i] & 0x0f;
                            j1 = ++j1;
                        }
                        break;
                    case 0x80:     // low_8_bit  ADC_Channel_0
                        recover_bit_7_4 = (check_bit_6 << 1) | (check_bit_5 >> 1);
                        if (i % 2 == 0) {
                            if (i != 0) {     //如果不是第0位的数
                                if (j1 == 0) j1 = j1 + 1;
                                data_adc_Channel1_8bit[--j1] = (data_adc_8bit[i] & 0x6f) | recover_bit_7_4;
                                //0x6f=01101111,先将第7、4位置0，再将原数据给进去
                                j1 = ++j1;
                            } else {
                                j1 = ++j1;
                                j2 = ++j2;
                                break;
                            }
                        } else {
                            data_adc_Channel1_8bit[j1++] = (data_adc_8bit[i] & 0x6f) | recover_bit_7_4;
                        }
                        break;
                    case 0x10:    // high_8_bit  ADC_Channel_2
                        if (i % 2 == 0) {   //如果在偶数位
                            data_adc_Channel2_8bit[j2++] = data_adc_8bit[i] & 0x0f;
                        } else {
                            if (j2 == 0) j2 = j2 + 1;
                            data_adc_Channel2_8bit[--j2] = data_adc_8bit[i] & 0x0f;
                            j2 = ++j2;
                        }
                        break;
                    case 0x90:    // low_8_bit  ADC_Channel_2
                        recover_bit_7_4 = (check_bit_6 << 1) | (check_bit_5 >> 1);
                        if (i % 2 == 0) {
                            if (i != 0) {     //如果不是第0位的数
                                if (j2 == 0) j2 = j2 + 1;
                                data_adc_Channel2_8bit[--j2] = (data_adc_8bit[i] & 0x6f) | recover_bit_7_4;
                                j2 = ++j2;
                            } else {
                                j2 = ++j2;
                                j1 = ++j1;
                                break;
                            }
                        } else {
                            data_adc_Channel2_8bit[j2++] = (data_adc_8bit[i] & 0x6f) | recover_bit_7_4;
                        }
                        break;
                    default:
                        break;
                }
            }

            // 将高8位和低8位的数据合在一起
            for(i=0; i<data_num_temp/4; i++){
                j1 = i*2;
                j2 = i*2;
                data_adc_Channel1_16bit[i] = data_adc_Channel1_8bit[j1] << 8 | data_adc_Channel1_8bit[j1+1];
                data_adc_Channel2_16bit[i] = data_adc_Channel2_8bit[j2] << 8 | data_adc_Channel2_8bit[j2+1];
            }

            // 给MainActivity发送广播：包含action和数据
            Intent intent = new Intent();
            intent.setAction(GetBluetoothDataService.ACTION_OSC_DATA_SEND);
            intent.putExtra(DATA_CHANNEL_1, data_adc_Channel1_16bit);
            intent.putExtra(DATA_CHANNEL_2, data_adc_Channel2_16bit);
            intent.putExtra(DATA_NUM_TEMP, data_num_temp/4);
            sendBroadcast(intent);


        /*    System.out.println("\r\n");
            for(i=0; i<data_num_temp/4; i++){
                System.out.printf("%d  ",data_adc_Channel1_16bit[i]);
            }

            System.out.println("\r\n");
            for(i=0; i<data_num_temp/2; i++){
                System.out.printf("%d  ",data_adc_Channel1_8bit[i]);
            }

            System.out.println("\r\n");
            for(i=0; i<data_num_temp/4; i++){
                System.out.printf("%d  ",data_adc_Channel2_16bit[i]);
            }

            System.out.println("\r\n");
            for(i=0; i<data_num_temp/2; i++){
                System.out.printf("%d  ",data_adc_Channel2_8bit[i]);
            }
            System.out.println("\r\n");*/
            j1 = 0;
            j2 = 0;
        }
    };

}
