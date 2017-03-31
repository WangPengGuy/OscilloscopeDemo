package com.android.wangpeng.oscilloscopedemo_layout;

import android.app.Activity;
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
    private String[] strings_X_axis_resolution = {"2S/div", "1S/div", "500mS/div", "200mS/div", "100mS/div", "50mS/div", "20mS/div",
            "10mS/div", "5mS/div", "2mS/div", "1mS/div", "500uS/div", "200uS/div", "100uS/div", "50uS/div", "20uS/div", "10uS/div",
            "5uS/div", "2uS/div", "1uS/div"};
    //将X轴的分辨率换算成 毫秒级
    private float[] value_X_axis_resolution = {2000, 1000, 500, 200, 100, 50, 20, 10, 5, 2, 1, 0.5f, 0.2f, 0.1f, 0.05f, 0.02f, 0.01f, 0.005f, 0.002f, 0.001f};
    private int clickTimes = 0; //点击次数

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

    @Override
    public void onClick(View view) {
        if(view == btn_X_add){
            clickTimes--;
            if (clickTimes <= 0){
                clickTimes = 0;
            }else if(clickTimes >=19){
                clickTimes = 19;
            }else {}
            String s = getText_XAxisResolution(clickTimes);
            float f = getValue_XAxisResolution(clickTimes);
            text_X_axis_resolution.setText(s);
            System.out.println(clickTimes);
        }else if(view == btn_x_reduce){
            clickTimes++;
            if (clickTimes <= 0){
                clickTimes = 0;
            }else if(clickTimes >=19){
                clickTimes = 19;
            }else {}
            String s = getText_XAxisResolution(clickTimes);
            float f = getValue_XAxisResolution(clickTimes);
            text_X_axis_resolution.setText(s);
            System.out.println(clickTimes);
        }
    }
}
