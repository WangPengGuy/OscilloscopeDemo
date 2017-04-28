package com.android.wangpeng.oscilloscopedemo_layout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


/**
 * Created by Mr_wang on 2017/4/7.
 */

public class SurfaceViewOnDraw {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private int[] data_Channel1_rising = new int[300];    //通道1上升沿之后的数据
    private int[] data_Channel1_dropping = new int[300];    //通道1下降沿之后的数据
    private int[] data_Channel2_rising = new int[300];    //通道2上升沿之后的数据
    private int[] data_Channel2_dropping = new int[300];    //通道2下降沿之后的数据
    private int data_num_temp = 0;

    private int oldX = 0, oldY = 0;
    float oldY1 = 0.0f, oldY2 = 0.0f;
    int data_Channel1_max = 0, data_Channel1_min = 0;
    int data_Channel2_max = 0, data_Channel2_min = 0;

    public SurfaceViewOnDraw(SurfaceView sfv){
        this.surfaceView = sfv;
        this.surfaceHolder = sfv.getHolder();
    }

    /**
     * 根据Y轴的分辨率确定最大值的位置
     * @param y_v_div   Y轴分辨率
     * @return
     */
    private float getRateY(int y_v_div){
        float rateY = 0.0f;
        switch (y_v_div){
            case 5000:
                rateY = 4095.0f/(3300*90/5000);
                break;
            case 2000:
                rateY = 4095.0f/(3300*90/2000);
                break;
            case 1000:
                rateY = 4095.0f/(3300*90/1000);
                break;
            case 500:
                rateY = 4095.0f/(3300*90/500);
                break;
            case 200:
                rateY = 4095.0f/(3300*90/200);
                break;
            case 100:
                rateY = 4095.0f/(3300*90/100);
                break;
            case 50:
                rateY = 4095.0f/(3300*90/50);
                break;
            case 20:
                rateY = 4095.0f/(3300*90/20);
                break;
            case 10:
                rateY = 4095.0f/(3300*90/10);
                break;
            default:
                rateY = 4095.0f/(3300*90/1000);
                break;
        }
        //System.out.println("rateY = "+rateY);
        return rateY;
    }

    /**
     * 通道1、通道2的上升沿触发方式后剩下的数据
     * @param data_Channel_1    通道1的数据
     * @param data_Channel_2    通道2的数据
     * @param data_num    数据长度
     */
    public void triggerMode_rising(final int[] data_Channel_1, final int[] data_Channel_2, final int data_num){
        final int data_trigger_Channel1 = (data_Channel1_max - data_Channel1_min) / 2;
        final int data_trigger_Channel2 = (data_Channel2_max - data_Channel2_min) / 2;
        final int[] index_trigger_Channel1 = {0};
        final int[] index_trigger_Channel2 = {0};
        new Thread(new Runnable() {
            @Override
            public void run() {
                //计算通道1的上升沿之后的数据
                for(int i = 0; i<data_num; i++){
                    if( (data_Channel_1[i] <= data_trigger_Channel1) && (data_Channel_1[i+1] >= data_trigger_Channel1)){
                        index_trigger_Channel1[0] = i;
                        break;
                    }
                    if(index_trigger_Channel1[0]>60){
                        break;
                    }
                }
                for(int i = 0; i<data_num- index_trigger_Channel1[0]; i++){
                    data_Channel1_rising[i] = data_Channel_1[i+ index_trigger_Channel1[0]];
                }
                //计算通道2的上升沿之后的数据
                for(int i = 0; i<data_num; i++){
                    if( (data_Channel_2[i] <= data_trigger_Channel2) && (data_Channel_2[i+1] >= data_trigger_Channel2)){
                        index_trigger_Channel2[0] = i;
                        break;
                    }
                }
                for(int i = 0; i<data_num- index_trigger_Channel2[0]; i++){
                    data_Channel2_rising[i] = data_Channel_2[i+ index_trigger_Channel2[0]];
                }
            }
        }).start();
    }

    /**
     * 通道1、通道2的下降沿触发方式后剩下的数据
     * @param data_Channel_1    通道1的数据
     * @param data_Channel_2    通道2的数据
     * @param data_num    数据长度
     */
    public void triggerMode_dropping(final int[] data_Channel_1, final int[] data_Channel_2, final int data_num){
        final int data_trigger_Channel1 = (data_Channel1_max - data_Channel1_min) / 2;
        final int data_trigger_Channel2 = (data_Channel2_max - data_Channel2_min) / 2;
        final int[] index_trigger_Channel1 = {0};
        final int[] index_trigger_Channel2 = {0};
        new Thread(new Runnable() {
            @Override
            public void run() {
                //计算通道1的下降沿之后的数据
                for(int i = 0; i<data_num; i++){
                    if((data_Channel_1[i] >= data_trigger_Channel1) && (data_Channel_1[i+1] <= data_trigger_Channel1)){
                        index_trigger_Channel1[0] = i;
                        break;
                    }
                }
                for(int i = 0; i<data_num- index_trigger_Channel1[0]; i++){
                    data_Channel1_dropping[i] = data_Channel_1[i+ index_trigger_Channel1[0]];
                }
                //计算通道2的下降沿之后的数据
                for(int i = 0; i<data_num; i++){
                    if( (data_Channel_2[i] >= data_trigger_Channel2) && (data_Channel_2[i+1] <= data_trigger_Channel2)){
                        index_trigger_Channel2[0] = i;
                        break;
                    }
                }
                for(int i = 0; i<data_num- index_trigger_Channel2[0]; i++){
                    data_Channel2_dropping[i] = data_Channel_2[i+ index_trigger_Channel2[0]];
                }
            }
        }).start();
    }

    /**
     * 画波形
     * @param data_Channel_1   通道1的数据
     * @param data_Channel_2   通道2的数据
     * @param data_num   数据长度
     * @param line_1_y   通道1水平基线的位置
     * @param line_2_y   通道2水平基线的位置
     * @param rate_y   Y轴的分辨率
     */
    public void simpleDraw(int[] data_Channel_1, int[] data_Channel_2, int data_num, int line_1_y, int line_2_y, int rate_y) {

        //******************************画背景网格******************************
        Canvas canvas_background = surfaceHolder.lockCanvas();    //获取画布
        canvas_background.drawColor(Color.BLACK);
        Paint paint = new Paint();     //定义画笔
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        ///画横线
        oldY = 86;  //110+90
        for (int i = 0; i < 7; i++) {
            canvas_background.drawLine(0, oldY, 720, oldY, paint);
            oldY = oldY + 90;
        }
        ///画竖线
        oldX = 90;
        for (int i = 0; i < 8; i++) {
            canvas_background.drawLine(oldX, 0, oldX, 830, paint);
            oldX = oldX + 90;
        }
        //提交画布
//        surfaceHolder.unlockCanvasAndPost(canvas_background);
//        canvas_background = surfaceHolder.lockCanvas(new Rect(0,0,0,0));
//        surfaceHolder.unlockCanvasAndPost(canvas_background);

        //*****************************画通道1波形*******************************
        Paint paint_wave_1 = new Paint();   //定义通道1画笔
        Paint paint_wave_2 = new Paint();   //定义通道2画笔
        paint_wave_1.setColor(Color.RED);
        paint_wave_2.setColor(Color.YELLOW);
        paint_wave_1.setStrokeWidth(3);
        paint_wave_2.setStrokeWidth(3);
        float rateY = 0.0f;
        rateY = getRateY(rate_y);
        int baseLine1_dy = line_1_y - 474;
        //System.out.println(line_1_y);
        int baseLine2_dy = line_2_y - 474;

        if(MainActivity.triggerMode) {
            triggerMode_rising(data_Channel_1, data_Channel_2, data_num);
            oldX = 0;
            oldY1 = 364 - (int) (data_Channel1_rising[1] / rateY) + baseLine1_dy;
            oldY2 = 364 - (int) (data_Channel2_rising[1] / rateY) + baseLine2_dy;
            int x = 0;
            float y1 = 0.0f, y2 = 0.0f;
            for (int i = 1; i <= data_num; i++) {
                y1 = 364 - (data_Channel1_rising[i] / rateY) + baseLine1_dy;
                y2 = 364 - (data_Channel2_rising[i] / rateY) + baseLine2_dy;
                x = i * 3;
                if (x > 720) break;
                //canvas_wave_1 = surfaceHolder.lockCanvas(new Rect(oldX, y1 + 2, x+1, y1 - 2));
                canvas_background.drawLine(oldX, oldY1, x, y1, paint_wave_1);
                //canvas_background.drawPoint(x,y1,paint_wave_1);
                //canvas_wave_1.drawPoint(x, y1, paint_wave_1);
                //Canvas canvas_wave_2 = surfaceHolder.lockCanvas(new Rect(oldX+1, y2+2, x+1, y2-2));    //获取画布1
                canvas_background.drawLine(oldX, oldY2, x, y2, paint_wave_2);
                oldX = x;
                oldY1 = y1;
                oldY2 = y2;

                //surfaceHolder.unlockCanvasAndPost(canvas_wave_2);
            }
        }else{
            oldX = 0;
            oldY1 = 364 - (int) (data_Channel_1[1] / rateY) + baseLine1_dy;
            oldY2 = 364 - (int) (data_Channel_2[1] / rateY) + baseLine2_dy;
            int x = 0;
            float y1 = 0.0f, y2 = 0.0f;
            for (int i = 1; i <= data_num; i++) {
                y1 = 364 - (data_Channel_1[i] / rateY) + baseLine1_dy;
                y2 = 364 - (data_Channel_2[i] / rateY) + baseLine2_dy;
                x = i * 3;
                if (x > 720) break;
                //canvas_wave_1 = surfaceHolder.lockCanvas(new Rect(oldX, y1 + 2, x+1, y1 - 2));
                canvas_background.drawLine(oldX, oldY1, x, y1, paint_wave_1);
                //canvas_background.drawPoint(x,y1,paint_wave_1);
                //canvas_wave_1.drawPoint(x, y1, paint_wave_1);
                //Canvas canvas_wave_2 = surfaceHolder.lockCanvas(new Rect(oldX+1, y2+2, x+1, y2-2));    //获取画布1
                canvas_background.drawLine(oldX, oldY2, x, y2, paint_wave_2);
                oldX = x;
                oldY1 = y1;
                oldY2 = y2;

                //surfaceHolder.unlockCanvasAndPost(canvas_wave_2);
            }
        }
        surfaceHolder.unlockCanvasAndPost(canvas_background);
    }

    /**
     * 计算最大最小值
     * @param data_Channel_1    通道1的数据
     * @param data_Channel_2    通道2的数据
     * @param data_num      数据长度
     * @param handler       传送到主线程的handler（handler在主线程中创建）
     */
    public void getMaxMin(final int[] data_Channel_1, final int[] data_Channel_2, final int data_num, final Handler handler){

        new Thread(new Runnable() {
            int max_adc1_value = data_Channel_1[0], min_adc1_value = data_Channel_1[0];
            int max_adc2_value = data_Channel_2[0], min_adc2_value = data_Channel_2[0];
            int max_adc1_v = 0, min_adc1_v = 0;
            int max_adc2_v = 0, min_adc2_v = 0;
            int adc1_Vpp = 0, adc2_Vpp = 0;
            int max_adc1_index = 0, min_adc1_index = 0, max_adc2_index = 0, min_adc2_index = 0;
            @Override
            public void run() {
                for(int i=0; i<data_num; i++){
                    //计算通道1的最大最小值
                    if(max_adc1_value<=data_Channel_1[i]){
                        max_adc1_value = data_Channel_1[i];
                        max_adc1_index = i;
                    }else if(min_adc1_value>=data_Channel_1[i]){
                        min_adc1_value = data_Channel_1[i];
                        min_adc1_index = i;
                    }else {}
                    //计算通道2的最大最小值
                    if(max_adc2_value<=data_Channel_2[i]){
                        max_adc2_value = data_Channel_2[i];
                        max_adc2_index = i;
                    }else if(min_adc2_value>=data_Channel_2[i]){
                        min_adc2_value = data_Channel_2[i];
                        min_adc2_index = i;
                    }else {}
                }
                data_Channel1_max = max_adc1_value;
                data_Channel1_min = min_adc1_value;
                data_Channel2_max = max_adc2_value;
                data_Channel2_min = min_adc2_value;
                //计算相对应的电压值
                max_adc1_v = max_adc1_value * 3300 /4095;
                min_adc1_v = min_adc1_value * 3300 /4095;
                max_adc2_v = max_adc2_value * 3300 /4095;
                min_adc2_v = min_adc2_value * 3300 /4095;
                if(max_adc1_v>3300 || min_adc1_v>3300 || max_adc2_v>3300 || min_adc2_v>3300){
                    max_adc1_v = 3300;
                    min_adc1_v = 3300;
                    max_adc2_v = 3300;
                    min_adc2_v = 3300;
                }else if(max_adc1_v<0 || min_adc1_v<0 || max_adc2_v<0 || min_adc2_v<0){
                    max_adc1_v = 0;
                    min_adc1_v = 0;
                    max_adc2_v = 0;
                    min_adc2_v = 0;
                }
                //计算频率

                System.out.println(max_adc1_index+","+min_adc1_index+","+max_adc2_index+","+min_adc2_index);
                //计算峰峰值
                adc1_Vpp = max_adc1_v - min_adc1_v;
                adc2_Vpp = max_adc2_v - min_adc2_v;
                //通过handle发送
                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putInt("max_adc1_v", max_adc1_v);
                bundle.putInt("min_adc1_v", min_adc1_v);
                bundle.putInt("max_adc2_v", max_adc2_v);
                bundle.putInt("min_adc2_v", min_adc2_v);
                bundle.putInt("adc1_Vpp", adc1_Vpp);
                bundle.putInt("adc2_Vpp", adc2_Vpp);
                bundle.putInt("code",1);
                msg.setData(bundle);
                handler.sendMessage(msg);
            }
        }).start();

    }

}
