package com.android.wangpeng.oscilloscopedemo_layout;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener,View.OnClickListener {

    ImageView img_time_line_left, img_time_line_right; //时间差前标，时间差后标
    ImageView img_CH1_horizontal_line, img_CH2_horizontal_line; //通道1波形水平基线，通道2波形水平基线
    Button btn_x_axis_add, btn_x_axis_reduce;
    Button btn_y_axis_add, btn_y_axis_reduce;
    TextView text_x_axis_resolution, text_y_axis_resolution;
    SurfaceView surfaceView;

    XAxisResolutionChanged xAxisResolutionChanged;
    YAxisResolutionChanged yAxisResolutionChanged;
    SurfaceViewOnDraw surfaceViewOnDraw;

    private Intent intent1;
    private DataFromServiceReceiver dataFromServiceReceiver;
    private int[] data_adc_Channel1_16bit = new int[256];    //把通道1的8bit的数据转换成16bit存在此数组中
    private int[] data_adc_Channel2_16bit = new int[256];    //把通道2的8bit的数据转换成16bit存
    private int data_num_temp = 0;

    int[] location_img_time_line_left; //记录 时间差前标 在父容器中 的坐标
    int[] location_img_time_line_right; //记录 时间差后标 在父容器中 的坐标
    int[] location_img_CH1_horizontal_line; //记录 通道1波形的水平基线 的坐标
    int[] location_img_CH2_horizontal_line; //记录 通道2波形的水平基线 的坐标
    int lastX = 0;
    int lastY = 0;
    final static int NONE = 0;
    final static int TIME_LEFT = 1;
    final static int TIME_RIGHT = 2;
    final static int WAVE_CH1_MOVE = 3;
    final static int WAVE_CH2_MOVE = 4;
    final static int TIME = 5;
    final static int WAVE = 6;
    static int WHICH_VIEW_MOVE = NONE;
    static int WHICH_VIEW_GROUP_MOVE = NONE;
    private int clickTimes; //点击次数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //控件
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        img_time_line_left = (ImageView) findViewById(R.id.img_time_left);
        img_time_line_right = (ImageView) findViewById(R.id.img_time_right);
        img_CH1_horizontal_line = (ImageView) findViewById(R.id.img_wave_CH1_horizontal_line);
        img_CH2_horizontal_line = (ImageView) findViewById(R.id.img_wave_CH2_horizontal_line);
        btn_x_axis_add = (Button) findViewById(R.id.btn_x_axis_add);
        btn_x_axis_reduce = (Button) findViewById(R.id.btn_x_axis_reduce);
        btn_y_axis_add = (Button) findViewById(R.id.btn_y_axis_add);
        btn_y_axis_reduce = (Button) findViewById(R.id.btn_y_axis_reduce);
        text_x_axis_resolution = (TextView) findViewById(R.id.text_X_axis_resolution);
        text_y_axis_resolution = (TextView) findViewById(R.id.text_Y_axis_resolution);
        //新建的类
        xAxisResolutionChanged = new XAxisResolutionChanged(btn_x_axis_add, btn_x_axis_reduce, text_x_axis_resolution);
        yAxisResolutionChanged = new YAxisResolutionChanged(btn_y_axis_add, btn_y_axis_reduce, text_y_axis_resolution);
        dataFromServiceReceiver = new DataFromServiceReceiver();
        intent1 = new Intent(MainActivity.this, GetBluetoothDataService.class);
        surfaceViewOnDraw = new SurfaceViewOnDraw(surfaceView, data_adc_Channel1_16bit, data_adc_Channel2_16bit, data_num_temp);
        //注册广播接收者
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GetBluetoothDataService.ACTION_OSC_DATA_SEND);
        MainActivity.this.registerReceiver(dataFromServiceReceiver, intentFilter);
        //类的方法

        //需要的数据
        location_img_time_line_left = new int[2];
        location_img_time_line_right = new int[2];
        location_img_CH1_horizontal_line = new int[2];
        location_img_CH2_horizontal_line = new int[2];
        //绑定的事件
        surfaceView.setOnTouchListener(this);
        btn_x_axis_add.setOnClickListener(this);
        btn_x_axis_reduce.setOnClickListener(this);
        btn_y_axis_add.setOnClickListener(this);
        btn_y_axis_reduce.setOnClickListener(this);

        surfaceView.post(new Runnable() {
            @Override
            public void run() {
                System.out.println(surfaceView.getWidth());
                System.out.println(surfaceView.getHeight());
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.fullscreen:  //全屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.refresh:
                Toast.makeText(this,"您点击了“刷新”",Toast.LENGTH_SHORT).show();
                break;
            case R.id.pause:
                Toast.makeText(this,"您点击了“暂停”",Toast.LENGTH_SHORT).show();
                break;
            case R.id.connectDevice:     //连接蓝牙
                startService(intent1);
                Intent intent2 = new Intent(MainActivity.this, BluetoothHandleActivity.class);
                startActivity(intent2);
                break;
            case R.id.black_background:
                Toast.makeText(this,"您点击了“黑色背景”",Toast.LENGTH_SHORT).show();
                break;
            case R.id.white_background:
                Toast.makeText(this,"您点击了“白色背景”",Toast.LENGTH_SHORT).show();
                break;
            case R.id.aboutUs:
                Toast.makeText(this,"您点击了“about us”",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * FUNCTION: 判断准备让哪一个View移动
     * @param touch_x   //触摸点X坐标
     * @param touch_y   //触摸点Y坐标
     * @param img_time_line_left   //时间差左标线
     * @param img_time_line_right  //时间差右标线
     * @param img_CH1_horizontal_line  //通道1波形基线
     * @param img_CH2_horizontal_line  //通道2波形基线
     * @param location_img_time_line_left  //时间差左标线 坐标 二维数列
     * @param location_img_time_line_right  //时间差右标线 坐标 二维数列
     * @param location_img_CH1_horizontal_line  //通道1波形基线 坐标 二维数列
     * @param location_img_CH2_horizontal_line  //通道2波形基线 坐标 二维数列
     */
    public void judgeViewLocation(int touch_x, int touch_y,
                                  View img_time_line_left, View img_time_line_right, View img_CH1_horizontal_line, View img_CH2_horizontal_line,
                                  int[] location_img_time_line_left, int[] location_img_time_line_right,
                                  int[] location_img_CH1_horizontal_line, int[] location_img_CH2_horizontal_line) {
        img_time_line_left.getLocationInWindow(location_img_time_line_left);
        img_time_line_right.getLocationInWindow(location_img_time_line_right);
        img_CH1_horizontal_line.getLocationInWindow(location_img_CH1_horizontal_line);
        img_CH2_horizontal_line.getLocationInWindow(location_img_CH2_horizontal_line);
        int dx_time_left = Math.abs(touch_x - location_img_time_line_left[0]);
        int dx_time_right = Math.abs(touch_x - location_img_time_line_right[0]);
        int dy_ch1_horizontal = Math.abs(touch_y - location_img_CH1_horizontal_line[1]);
        int dy_ch2_horizontal = Math.abs(touch_y - location_img_CH2_horizontal_line[1]);
        if(dx_time_left <= 20 || dx_time_right <= 20){
            WHICH_VIEW_GROUP_MOVE = TIME;
            if(dx_time_left <= 20){
                WHICH_VIEW_MOVE = TIME_LEFT;
            }else if(dx_time_right <= 20){
                WHICH_VIEW_MOVE = TIME_RIGHT;
            }else
                WHICH_VIEW_MOVE = NONE;
        }else if(dy_ch1_horizontal <= 20 || dy_ch2_horizontal <=20){
            WHICH_VIEW_GROUP_MOVE = WAVE;
            if(dy_ch1_horizontal <= 20){
                WHICH_VIEW_MOVE = WAVE_CH1_MOVE;
            }else if(dy_ch2_horizontal <=20){
                WHICH_VIEW_MOVE = WAVE_CH2_MOVE;
            }else
                WHICH_VIEW_MOVE = NONE;
        }else
            WHICH_VIEW_GROUP_MOVE = NONE;

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int x = (int) motionEvent.getRawX();
        int y = (int) motionEvent.getRawY();
        int offsetX = 0;
        int offsetY = 0;
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN: //当手指按下时，记录触点坐标
                lastX = x;
                lastY = y;
                System.out.println(x + ", " + y);
                //判断哪个View获得焦点
                judgeViewLocation(lastX, lastY, img_time_line_left, img_time_line_right, img_CH1_horizontal_line, img_CH2_horizontal_line,
                        location_img_time_line_left, location_img_time_line_right, location_img_CH1_horizontal_line, location_img_CH2_horizontal_line);
                break;
            case MotionEvent.ACTION_MOVE:
                //计算偏移量
                offsetX = x - lastX;
                offsetY = y - lastY;
                //将每一次移动后的坐标值 赋给 前一坐标，以便计算下一次偏移量
                lastX = x;
                lastY = y;
                break;
        }
        switch(WHICH_VIEW_GROUP_MOVE){
            case TIME:
                switch (WHICH_VIEW_MOVE){
                    case TIME_LEFT: img_time_line_left.offsetLeftAndRight(offsetX);
                          surfaceViewOnDraw.simpleDraw(data_adc_Channel1_16bit, data_adc_Channel2_16bit, data_num_temp,
                                location_img_CH1_horizontal_line[1], location_img_CH2_horizontal_line[1] );
                          break;
                    case TIME_RIGHT: img_time_line_right.offsetLeftAndRight(offsetX);
                        surfaceViewOnDraw.simpleDraw(data_adc_Channel1_16bit, data_adc_Channel2_16bit, data_num_temp,
                                location_img_CH1_horizontal_line[1], location_img_CH2_horizontal_line[1] );
                        break;
                    case NONE: break;
                }
                break;
            case WAVE:
                switch (WHICH_VIEW_MOVE){
                    case WAVE_CH1_MOVE: img_CH1_horizontal_line.offsetTopAndBottom(offsetY);break;
                    case WAVE_CH2_MOVE: img_CH2_horizontal_line.offsetTopAndBottom(offsetY);break;
                    case NONE: break;
                }
                break;
            case NONE:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_x_axis_add:
                xAxisResolutionChanged.onClick(btn_x_axis_add);
                break;
            case R.id.btn_x_axis_reduce:
                xAxisResolutionChanged.onClick(btn_x_axis_reduce);
                break;
            case R.id.btn_y_axis_add:
                yAxisResolutionChanged.onClick(btn_y_axis_add);
                break;
            case R.id.btn_y_axis_reduce:
                yAxisResolutionChanged.onClick(btn_y_axis_reduce);
                break;
        }
    }

    /**
     * BroadcastReceiver：接收从service传过来的数据
     */
    public class DataFromServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                case GetBluetoothDataService.ACTION_OSC_DATA_SEND:
                    data_adc_Channel1_16bit = intent.getIntArrayExtra(GetBluetoothDataService.DATA_CHANNEL_1);
                    data_adc_Channel2_16bit = intent.getIntArrayExtra(GetBluetoothDataService.DATA_CHANNEL_2);
                    data_num_temp = intent.getIntExtra(GetBluetoothDataService.DATA_NUM_TEMP, 0);
                    img_CH1_horizontal_line.getLocationInWindow(location_img_CH1_horizontal_line);
                    img_CH2_horizontal_line.getLocationInWindow(location_img_CH2_horizontal_line);
                    surfaceViewOnDraw.simpleDraw(data_adc_Channel1_16bit, data_adc_Channel2_16bit, data_num_temp,
                                                    location_img_CH1_horizontal_line[1], location_img_CH2_horizontal_line[1] );
                    System.out.println("通道1的值");
                    for(int j =0; j<data_num_temp; j++){
                        System.out.printf("%d  ",data_adc_Channel1_16bit[j]);
                    }
                    System.out.println("通道2的值");
                    for(int j =0; j<data_num_temp; j++){
                        System.out.printf("%d  ",data_adc_Channel2_16bit[j]);
                    }

                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.this.unregisterReceiver(dataFromServiceReceiver);
        stopService(intent1);
    }
}
