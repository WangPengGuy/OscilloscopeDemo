package com.android.wangpeng.oscilloscopedemo_layout;

import android.app.ActionBar;
import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    ImageView img_time_line_left, img_time_line_right; //时间差前标，时间差后标
    ImageView img_CH1_horizontal_line, img_CH2_horizontal_line; //通道1波形水平基线，通道2波形水平基线
    SurfaceView surfaceView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        location_img_time_line_left = new int[2];
        location_img_time_line_right = new int[2];
        location_img_CH1_horizontal_line = new int[2];
        location_img_CH2_horizontal_line = new int[2];

        img_time_line_left = (ImageView) findViewById(R.id.img_time_left);
        img_time_line_right = (ImageView) findViewById(R.id.img_time_right);
        img_CH1_horizontal_line = (ImageView) findViewById(R.id.img_wave_CH1_horizontal_line);
        img_CH2_horizontal_line = (ImageView) findViewById(R.id.img_wave_CH2_horizontal_line);

        surfaceView.setOnTouchListener(this);

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
            case R.id.fullscreen:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.refresh:
                Toast.makeText(this,"您点击了“刷新”",Toast.LENGTH_SHORT).show();
                break;
            case R.id.pause:
                Toast.makeText(this,"您点击了“暂停”",Toast.LENGTH_SHORT).show();
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
     * @param touvh_y   //触摸点Y坐标
     * @param img_time_line_left   //时间差左标线
     * @param img_time_line_right  //时间差右标线
     * @param img_CH1_horizontal_line  //通道1波形基线
     * @param img_CH2_horizontal_line  //通道2波形基线
     * @param location_img_time_line_left  //时间差左标线 坐标 二维数列
     * @param location_img_time_line_right  //时间差右标线 坐标 二维数列
     * @param location_img_CH1_horizontal_line  //通道1波形基线 坐标 二维数列
     * @param location_img_CH2_horizontal_line  //通道2波形基线 坐标 二维数列
     */
    public void judgeViewLocation(int touch_x, int touvh_y,
                                  View img_time_line_left, View img_time_line_right, View img_CH1_horizontal_line, View img_CH2_horizontal_line,
                                  int[] location_img_time_line_left, int[] location_img_time_line_right,
                                  int[] location_img_CH1_horizontal_line, int[] location_img_CH2_horizontal_line) {
        img_time_line_left.getLocationInWindow(location_img_time_line_left);
        img_time_line_right.getLocationInWindow(location_img_time_line_right);
        img_CH1_horizontal_line.getLocationInWindow(location_img_CH1_horizontal_line);
        img_CH2_horizontal_line.getLocationInWindow(location_img_CH2_horizontal_line);
        int dx_time_left = Math.abs(touch_x - location_img_time_line_left[0]);
        int dx_time_right = Math.abs(touch_x - location_img_time_line_right[0]);
        int dy_ch1_horizontal = Math.abs(touvh_y - location_img_CH1_horizontal_line[1]);
        int dy_ch2_horizontal = Math.abs(touvh_y - location_img_CH2_horizontal_line[1]);
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
                    case TIME_LEFT: img_time_line_left.offsetLeftAndRight(offsetX);break;
                    case TIME_RIGHT: img_time_line_right.offsetLeftAndRight(offsetX);break;
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
}
