package com.android.wangpeng.oscilloscopedemo_layout;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by Mr_wang on 2017/3/5.
 */

public class YAxisResolutionChanged implements View.OnClickListener {

    private TextView text_Y_axis_resolution;
    private Button btn_Y_add, btn_Y_reduce;
    //将被显示的Y轴的分辨率
    private String[] strings_Y_axis_resolution = {"5V/div", "1V/div", "500mV/div", "200mV/div", "100mV/div", "50mV/div", "20mV/div",
            "10mV/div"};
    //将Y轴的分辨率换算成 伏特（V）
    private float[] value_Y_axis_resolution = {5, 1, 0.5f, 0.2f, 0.1f, 0.05f, 0.02f, 0.01f};
    private int clickTimes = 0; //点击次数

    public YAxisResolutionChanged(Button button_add, Button button_reduce, TextView textView){
        this.btn_Y_add = button_add;
        this.btn_Y_reduce = button_reduce;
        this.text_Y_axis_resolution = textView;
    }

    /**
     * 获得X轴的分辨率的字符串
     * @param i
     * @return
     */
    public String getText_YAxisResolution(int i){
        return strings_Y_axis_resolution[i];
    }

    /**
     * 获得X轴分辨率的值
     * @param i
     * @return
     */
    public float getValue_YAxisResolution(int i){
        return value_Y_axis_resolution[i];
    }

    @Override
    public void onClick(View view) {
        if(view == btn_Y_add){
            clickTimes--;
            if (clickTimes <= 0){
                clickTimes = 0;
            }else if(clickTimes >=7){
                clickTimes = 7;
            }else {}
            String s = getText_YAxisResolution(clickTimes);
            float f = getValue_YAxisResolution(clickTimes);
            text_Y_axis_resolution.setText(s);
            System.out.println(clickTimes);
        }else if(view == btn_Y_reduce){
            clickTimes++;
            if (clickTimes <= 0){
                clickTimes = 0;
            }else if(clickTimes >=7){
                clickTimes = 7;
            }else {}
            String s = getText_YAxisResolution(clickTimes);
            float f = getValue_YAxisResolution(clickTimes);
            text_Y_axis_resolution.setText(s);
            System.out.println(clickTimes);
        }
    }
}
