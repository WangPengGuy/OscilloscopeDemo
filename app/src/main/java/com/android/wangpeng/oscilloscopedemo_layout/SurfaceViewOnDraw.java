package com.android.wangpeng.oscilloscopedemo_layout;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Mr_wang on 2017/4/7.
 */

public class SurfaceViewOnDraw {

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    private int[] data_adc_Channel1_16bit = new int[256];    //把通道1的8bit的数据转换成16bit存在此数组中
    private int[] data_adc_Channel2_16bit = new int[256];    //把通道2的8bit的数据转换成16bit存
    private int data_num_temp = 0;

    private int oldX = 0, oldY = 0;
    float oldY1 = 0.0f, oldY2 = 0.0f;

    public SurfaceViewOnDraw(SurfaceView sfv, int[] data_Channel_1, int[] data_Channel_2, int data_num){
        this.surfaceView = sfv;
        this.surfaceHolder = sfv.getHolder();
    }

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
        System.out.println("rateY = "+rateY);
        return rateY;
    }

    public void simpleDraw(int[] data_Channel_1, int[] data_Channel_2, int data_num, int line_1_y, int line_2_y) {

        //******************************画背景网格******************************
        Canvas canvas_background = surfaceHolder.lockCanvas();    //获取画布
        canvas_background.drawColor(Color.BLACK);
        Paint paint = new Paint();     //定义画笔
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        ///画横线
        oldY = 94;  //110+90
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
        rateY = getRateY(1000);
        int baseLine1_dy = line_1_y - 474;
        System.out.println(line_1_y);
        int baseLine2_dy = line_2_y - 474;
        oldX = 0;
        oldY1 = 364 - (int)(data_Channel_1[0] / rateY) + baseLine1_dy;
        oldY2 = 364 - (int)(data_Channel_2[0] / rateY) + baseLine2_dy;
        int x = 0;
        float y1 = 0.0f, y2 = 0.0f;
        for (int i = 0; i <= data_num; i++) {
            y1 = 364-(data_Channel_1[i] / rateY) +  baseLine1_dy;;
            y2 = 364-(data_Channel_2[i] / rateY) +  baseLine2_dy;;
            x = i * 3;
            //canvas_wave_1 = surfaceHolder.lockCanvas(new Rect(oldX, y1 + 2, x+1, y1 - 2));
            canvas_background.drawLine(oldX,oldY1,x,y1,paint_wave_1);
            //canvas_background.drawPoint(x,y1,paint_wave_1);
            //canvas_wave_1.drawPoint(x, y1, paint_wave_1);
            //Canvas canvas_wave_2 = surfaceHolder.lockCanvas(new Rect(oldX+1, y2+2, x+1, y2-2));    //获取画布1
            canvas_background.drawLine(oldX, oldY2, x, y2, paint_wave_2);
            oldX = x;
            oldY1 = y1;
            oldY2 = y2;

            //surfaceHolder.unlockCanvasAndPost(canvas_wave_2);
        }
        surfaceHolder.unlockCanvasAndPost(canvas_background);
    }

}
