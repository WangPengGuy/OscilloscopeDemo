package com.android.wangpeng.oscilloscopedemo_layout;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Mr_wang on 2017/3/4.
 */

public class XAxisResolutionChanged implements View.OnClickListener{

    private TextView text_X_axis_resolution;
    private Button btn_X_add, btn_x_reduce;
    //将被显示的X轴的分辨率
    private String[] strings_X_axis_resolution = {"20mS/div", "10mS/div", "5mS/div", "2mS/div", "1mS/div", "500uS/div"};
    //将X轴的分辨率换算成 毫秒级
    private float[] value_X_axis_resolution = {20, 10, 5, 2, 1, 0.5f, 0.2f, 0.1f, 0.05f};
    static int clickTimes = 0; //点击次数

    public XAxisResolutionChanged(Button button_add, Button button_reduce, TextView textView) {
        this.btn_X_add = button_add;
        this.btn_x_reduce = button_reduce;
        this.text_X_axis_resolution = textView;
    }


    /**
     * 获得X轴的分辨率的字符串
     * @param i
     * @return
     */
    public String getText_XAxisResolution(int i){
        return strings_X_axis_resolution[i];
    }

    /**
     * 获得X轴分辨率的值
     * @param i
     * @return
     */
    public float getValue_XAxisResolution(int i){
        return value_X_axis_resolution[i];
    }

    /**
     * 计算时间差
     * @param line_1_x    时间前标的x坐标
     * @param line_2_x    时间后标的x坐标
     * @return  时间差
     */
    public void getDeltaTime(final int line_1_x, final int line_2_x, final Handler handler){
        final int x1 = line_1_x, x2 = line_2_x;
        final Handler handler1 = handler;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int dx = Math.abs(x1 - x2);
                float dt = (dx/90.0f)*value_X_axis_resolution[clickTimes];
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putFloat("deltaTime",dt);
                bundle.putInt("code",2);
                message.setData(bundle);
                handler1.sendMessage(message);
            }
        }).start();
    }

    @Override
    public void onClick(View view) {
        if(view == btn_X_add){
            clickTimes--;
            if (clickTimes <= 0){
                clickTimes = 0;
            }else if(clickTimes >=5){
                clickTimes = 5;
            }else {}
            String s = getText_XAxisResolution(clickTimes);
            float f = getValue_XAxisResolution(clickTimes);
            text_X_axis_resolution.setText(s);
            System.out.println(clickTimes);
        }else if(view == btn_x_reduce){
            clickTimes++;
            if (clickTimes <= 0){
                clickTimes = 0;
            }else if(clickTimes >=5){
                clickTimes = 5;
            }else {}
            String s = getText_XAxisResolution(clickTimes);
            float f = getValue_XAxisResolution(clickTimes);
            text_X_axis_resolution.setText(s);
            System.out.println(clickTimes);
        }
    }
}
