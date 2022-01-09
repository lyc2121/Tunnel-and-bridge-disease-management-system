/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BTBLE905;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class BluetoothLeService extends Service  {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private int mConnectionState = STATE_DISCONNECTED;
    private File mFile;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";

    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
//   // 存放三轴数据
//    float[] oriValues = new float[3];
//    final int ValueNum = 4;
//    //用于存放计算阈值的波峰波谷差值
//    float[] tempValue = new float[ValueNum];
//    int tempCount = 0;
//    //是否上升的标志位
//    boolean isDirectionUp = false;
//    //持续上升次数
//    int continueUpCount = 0;
//    //上一点的持续上升的次数，为了记录波峰的上升次数
//    int continueUpFormerCount = 0;
//    //上一点的状态，上升还是下降
//    boolean lastStatus = false;
//    //波峰值
//    float peakOfWave = 0;
//    //波谷值
//    float valleyOfWave = 0;
//    //此次波峰的时间
//    long timeOfThisPeak = 0;
//    //上次波峰的时间
//    long timeOfLastPeak = 0;
//    //当前的时间
//    long timeOfNow = 0;
//    //当前传感器的值
//    float gravityNew = 0;
//    //上次传感器的值
//    float gravityOld = 0;
//    //动态阈值需要动态的数据，这个值用于这些动态数据的阈值
//    final float InitialValue = (float) 1.3;
//    //初始阈值
//    float ThreadValue = (float) 2.0;
//    //波峰波谷时间差
//    int TimeInterval = 250;
//    private StepCountListener mStepListeners;
//    float []fData= fData=new float[9];

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                BluetoothLeService.this.getWorkableGattServices(getSupportedGattServices());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.i(TAG, "--onCharacteristicRead called--");
                byte[] sucString=characteristic.getValue();
                String string= new String(sucString);
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
        @Override
        public void onCharacteristicWrite (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){

        }
    };
    private FileOutputStream mFos;
    private long mStarttime;
    private BluetoothLeService mLeService;
    private float floIMU;

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private Queue<Byte> queueBuffer = new LinkedList<Byte>();
    private byte[] packBuffer = new byte[20];
    float d=0;

    String strIMU="",strACK="",strIO="";

    public float fAngleRef;
    float Yaw;
    public void SetAngleRef(boolean bRef)
    {
        if (bRef) fAngleRef = Yaw;
        else fAngleRef = 0;
    };
    public boolean getRssiVal() {
        if (mBluetoothGatt == null)
            return false;
        return mBluetoothGatt.readRemoteRssi();

    }
    byte byteIO=0;
    float fPressure=0;
    private void broadcastUpdate(final String action,final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        final byte[] packBuffer = characteristic.getValue();
        float fHeight=0;
        float []fData= fData=new float[9];

        if (packBuffer != null && packBuffer.length == 20) {
            switch (packBuffer[1]) {
                case 0x61:
                    for (int i=0;i<9;i++) fData[i] = (((short) packBuffer[i*2+3]) << 8) | ((short) packBuffer[i*2+2] & 0xff);
                    for (int i=0;i<3;i++) fData[i] = (float)(fData[i] /32768.0*16.0*9.8);
                    for (int i=3;i<6;i++) fData[i] = (float)(fData[i] /32768.0*2000.0);
                      for (int i=6;i<9;i++) fData[i] = (float)(fData[i] /32768.0*180.0);
                    strIMU =(String.format("Normal\r\n加速度:%.2f %.2f %.2f \r\n角速度:%.2f %.2f %.2f \r\n角度:%.0f %.0f %.0f ",fData[0],fData[1],fData[2],fData[3],fData[4],fData[5],fData[6],fData[7],fData[8]));
                    intent.putExtra(EXTRA_DATA, "RSSI  :"+strIMU);
                    long finaltime = System.currentTimeMillis();
                    long time=finaltime-mStarttime;
                    try {
                        FileOutputStream mFos = new FileOutputStream(mFile, true);
 //                       mFos.write((String.valueOf(fData[0])+"  "+String.valueOf(fData[1])+"  "+String.valueOf(fData[2])+"  "+String.valueOf(time/1000)+"."+String.valueOf(time%1000)+"\n").getBytes());
//                        mFos.write(String.valueOf(fData[8]+"\n").getBytes());
                        mFos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    sendBroadcast(intent);
                    break;
                case 0x70:
                    strACK = String.format("Text:%c%c%c%c\r\n",packBuffer[2],packBuffer[3],packBuffer[4],packBuffer[5]);
                    break;
                case 0x71:
                    byteIO = packBuffer[3];
                    fPressure = ((((long) packBuffer[7]) << 24)&0xff000000) |((((long) packBuffer[6]) << 16)&0xff0000) |((((long) packBuffer[5]) << 8)&0xff00) | ((((long) packBuffer[4])&0xff));
                    fHeight = (((((long) packBuffer[11]) << 24)&0xff000000) |((((long) packBuffer[10]) << 16)&0xff0000) |((((long) packBuffer[9]) << 8)&0xff00) | ((((long) packBuffer[8])&0xff)))/100.0f;

                    strIO = String.format("Rssi:%d\r\nIO     :  %x\nPress:  %.0fPa\nHeight:  %.2fm\n",packBuffer[2],packBuffer[3],fPressure,fHeight);
                    break;
                case 0x72:
                    for (int i=0;i<9;i++) fData[i] = (((short) packBuffer[i*2+3]) << 8) | ((short) packBuffer[i*2+2] & 0xff);
                    for (int i=0;i<3;i++) fData[i] = (float)(fData[i] /32768.0*16.0);
                    for (int i=3;i<6;i++) fData[i] = (float)(fData[i] /32768.0*2000.0);
                    byte byteCheck = DataCheck(byteIO,strACK,fPressure,fData);
                    //strIMU =(String.format("Check:%d\r\na      :  %.2f %.2f %.2f \r\nw     :  %.2f %.2f %.2f \r\nH     :  %.0f %.0f %.0f ",byteCheck,fData[0],fData[1],fData[2],fData[3],fData[4],fData[5],fData[6],fData[7],fData[8]));
                    floIMU=fData[6];
                    intent.putExtra("CHECK", byteCheck);
                    intent.putExtra(EXTRA_DATA, "Test\r\n"+strACK+strIO+strIMU);
                    sendBroadcast(intent);
                    strACK = "";
                    byteIO=0;
                    fPressure = 0;
                    fHeight = 0;
                    strIO = "";
                    break;
                default:
                    break;
            }

        }
    }
    public byte DataCheck(byte byteIO,String strACK,float fPressrue,float [] fIMU)
    {
        byte byteResult = 0;
        if (byteIO!=0x0f) byteResult = -2;
        if (strACK.compareTo("Text:wtzn\r\n")!=0) byteResult = -6;
        double a = Math.sqrt(fIMU[0]*fIMU[0]+fIMU[1]*fIMU[1]+fIMU[2]*fIMU[2]);
        double w = Math.sqrt(fIMU[3]*fIMU[3]+fIMU[4]*fIMU[4]+fIMU[5]*fIMU[5]);
        double h = Math.sqrt(fIMU[6]*fIMU[6]+fIMU[7]*fIMU[7]+fIMU[8]*fIMU[8]);
        if ((a<0.6)|(a>1.4)) byteResult = -5;
        if ((w>500)|(w==0)) byteResult = -4;
        if ((h>1000)|(h<10)) byteResult = -3;
        if (fPressrue<90000) byteResult = -1;
        return  byteResult;
    }
    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mFile=new File(Environment.getExternalStorageDirectory(),"acceleration.txt");
        mStarttime=System.currentTimeMillis();
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public boolean initialize() {
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void setCharacteristicNotification(
            BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic
                    .getDescriptor(UUID
                            .fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor
                    .setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }
    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    public boolean writeByes(byte[] bytes) {
        if (mNotifyCharacteristic != null) {
            Log.d("BLE","WriteByte");
            mNotifyCharacteristic.setValue(bytes);
            return mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
        } else {
            Log.d("BLE","NOCharacter");
            return false;
        }
    }

    public boolean writeString(String s) {
        if (mNotifyCharacteristic != null) {
            mNotifyCharacteristic.setValue(s);
            return mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
        } else {
            return false;
        }
    }

   private void getWorkableGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null)
            return;
        String uuid = null;
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                uuid = gattCharacteristic.getUuid().toString();
                if (uuid.toLowerCase().contains("ffe9")) {//Write
                    mNotifyCharacteristic = gattCharacteristic;
                    setCharacteristicNotification(mNotifyCharacteristic, true);
                }
                if (uuid.toLowerCase().contains("ffe4")) {//Read
                    setCharacteristicNotification(gattCharacteristic, true);
                    BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(CCCD);
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }
    }

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID DATA_UUID = UUID.fromString("0000ffe5-0000-1000-8000-00805f9634fb");
    public static final UUID WRITE_UUID = UUID.fromString("0000ffe9-0000-1000-8000-00805f9634fb");
    public static final UUID READ_UUID = UUID.fromString("0000ffe4-0000-1000-8000-00805f9634fb");
//    public void initListener(StepCountListener listener) {
//        this.mStepListeners = listener;
//    }

//    /*
//    * 检测步子，并开始计步
//    * 1.传入sersor中的数据
//    * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
//    * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
//    * */
//    public void detectorNewStep(float values) {
//        if (gravityOld == 0) {
//            gravityOld = values;
//        } else {
//            if (detectorPeak(values, gravityOld)) {
//                timeOfLastPeak = timeOfThisPeak;
//                timeOfNow = System.currentTimeMillis();
//                if (timeOfNow - timeOfLastPeak >= TimeInterval
//                        && (peakOfWave - valleyOfWave >= ThreadValue)) {
//                    timeOfThisPeak = timeOfNow;
//                    /*
//                     * 更新界面的处理，不涉及到算法
//                     * 一般在通知更新界面之前，增加下面处理，为了处理无效运动：
//                     * 1.连续记录10才开始计步
//                     * 2.例如记录的9步用户停住超过3秒，则前面的记录失效，下次从头开始
//                     * 3.连续记录了9步用户还在运动，之前的数据才有效
//                     * */
//                    mStepListeners.countStep();
//                }
//                if (timeOfNow - timeOfLastPeak >= TimeInterval
//                        && (peakOfWave - valleyOfWave >= InitialValue)) {
//                    timeOfThisPeak = timeOfNow;
//                    ThreadValue = peakValleyThread(peakOfWave - valleyOfWave);
//                }
//            }
//        }
//        gravityOld = values;
//    }
//
//    /*
//     * 检测波峰
//     * 以下四个条件判断为波峰：
//     * 1.目前点为下降的趋势：isDirectionUp为false
//     * 2.之前的点为上升的趋势：lastStatus为true
//     * 3.到波峰为止，持续上升大于等于2次
//     * 4.波峰值大于20
//     * 记录波谷值
//     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
//     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
//     * */
//    public boolean detectorPeak(float newValue, float oldValue) {
//        lastStatus = isDirectionUp;
//        if (newValue >= oldValue) {
//            isDirectionUp = true;
//            continueUpCount++;
//        } else {
//            continueUpFormerCount = continueUpCount;
//            continueUpCount = 0;
//            isDirectionUp = false;
//        }
//
//        if (!isDirectionUp && lastStatus
//                && (continueUpFormerCount >= 2 || oldValue >= 20)) {
//            peakOfWave = oldValue;
//            return true;
//        } else if (!lastStatus && isDirectionUp) {
//            valleyOfWave = oldValue;
//            return false;
//        } else {
//            return false;
//        }
//    }
//
//    /*
//     * 阈值的计算
//     * 1.通过波峰波谷的差值计算阈值
//     * 2.记录4个值，存入tempValue[]数组中
//     * 3.在将数组传入函数averageValue中计算阈值
//     * */
//    public float peakValleyThread(float value) {
//        float tempThread = ThreadValue;
//        if (tempCount < ValueNum) {
//            tempValue[tempCount] = value;
//            tempCount++;
//        } else {
//            tempThread = averageValue(tempValue, ValueNum);
//            for (int i = 1; i < ValueNum; i++) {
//                tempValue[i - 1] = tempValue[i];
//            }
//            tempValue[ValueNum - 1] = value;
//        }
//        return tempThread;
//
//    }
//
//    /*
//     * 梯度化阈值
//     * 1.计算数组的均值
//     * 2.通过均值将阈值梯度化在一个范围里
//     * */
//    public float averageValue(float value[], int n) {
//        float ave = 0;
//        for (int i = 0; i < n; i++) {
//            ave += value[i];
//        }
//        ave = ave / ValueNum;
//        if (ave >= 8)
//            ave = (float) 4.3;
//        else if (ave >= 7 && ave < 8)
//            ave = (float) 3.3;
//        else if (ave >= 4 && ave < 7)
//            ave = (float) 2.3;
//        else if (ave >= 3 && ave < 4)
//            ave = (float) 2.0;
//        else {
//            ave = (float) 1.3;
//        }
//        return ave;
//    }

}
