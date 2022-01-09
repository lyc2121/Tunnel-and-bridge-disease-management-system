package com.example.android.BTBLE905;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public class myorientionlistenerlisten implements SensorEventListener {
    private SensorManager mService;
    private Sensor mSensor;
    private Sensor sensor_jia;
    private Sensor sensor_di;
    private Context mContext;
    private float lastX;
    private OnorientionListener mOnorientionListener;
    private float[] jia_Values = new float[3];
    private float[] di_Values = new float[3];


    public myorientionlistenerlisten(Context context) {
        this.mContext = context;
    }
    public void start(){
        mService = (SensorManager) mContext.getSystemService(mContext.SENSOR_SERVICE);
        mSensor = mService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensor_jia=mService.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensor_di=mService.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mService.registerListener(this, mSensor,SensorManager.SENSOR_DELAY_UI);
        mService.registerListener(this,sensor_jia,SensorManager.SENSOR_DELAY_GAME);//延迟两十毫秒
        mService.registerListener(this,sensor_di,SensorManager.SENSOR_DELAY_GAME);
    }
    public void stop(){
        mService.unregisterListener(this);
        mService.unregisterListener(this,sensor_jia);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION) {
        //            float x = sensorEvent.values[SensorManager.DATA_X];
        //            if (Math.abs(x - lastX) >= 1) {
        //                if (mOnorientionListener!=null){
        //                    mOnorientionListener.OnorientionChanged(x);
        //                }
        //                lastX = x;
        //            }
        //        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
        {                jia_Values = sensorEvent.values;            }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {                di_Values = sensorEvent.values;            }
        float x= calculateOrientation();
        System.out.println(x);
        if (Math.abs(x - lastX) >= 0.1) {
                            if (mOnorientionListener!=null){
                                mOnorientionListener.OnorientionChanged(x);
                            }
                            lastX = x;
                        }
         }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void setOnorientionListener(OnorientionListener onorientionListener) {
        mOnorientionListener = onorientionListener;
    }

    public interface OnorientionListener{
        void OnorientionChanged(float x);
    }
    private float calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, jia_Values, di_Values);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);//转换为角度
        return values[0];
    }
}
