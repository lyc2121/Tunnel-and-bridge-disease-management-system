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

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.example.android.BTBLE905.bmobdata.brige_acount;
import com.example.android.BTBLE905.bmobdata.brige_disease;
import com.example.android.BTBLE905.bmobdata.brige_loaction;
import com.example.android.BTBLE905.bmobdata.brige_photo;
import com.example.android.BTBLE905.marker.markerinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends Activity implements View.OnClickListener{

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    //byte[] WriteBytes = null;
    byte[] WriteBytes = new byte[20];
    // Code to manage Service lifecycle.
    private Button mBt_rate;
    private MapView mMapView = null;
    private BitmapDescriptor mMarker;
    private boolean isFirstLoc = true;
    private LocationClient mLocationClient;
    private myorientionlistenerlisten mMyorientionlistenerlisten;
    public static final String TAG = "dddjjjccc";
    private float currentx;
    private TextView tv_step;
    private Button btn_reset;
    private StepService mService;
    private boolean mIsRunning;
    private SharedPreferences mySharedPreferences;
    int laststep = 0;
    double distance;
    private BDLocation mBDLocation;
    private LatLng startlatlng;
    double PI = 3.14159265358979323;
    private ArrayList<Float> orition;
    boolean isfirst = true;
    private Location startloction;
    private BDLocation startbdloction;
    public static double pi = 3.1415926535897932384626;
    public static double a = 6378245.0;
    public static double ee = 0.00669342162296594323;
    private ArrayList<String> list;
    private PopupWindow PopupWindow;
    private ListView mLv_disease;



    map mMap;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                tv_step.setText(mySharedPreferences.getString("steps", "0"));
                int newstep = Integer.valueOf(mySharedPreferences.getString("steps", "0"));
                int step = newstep - laststep;
                if (step >= 1) {
                    float ange;
                    distance = step * 0.5*1.15;
                    float sum = 0;
                    for (float b : orition) {
                        sum = sum + b;
                    }
                    ange = sum / orition.size();
                    orition.clear();
                    double longdistance = distance * Math.sin(ange * PI / 180);
                    double latudedistance = distance * Math.cos(ange * PI / 180);
//                    BDLocation bdLocation_gc02 = LocationClient.getBDLocationInCoorType(mBDLocation, BDLocation.BDLOCATION_BD09LL_TO_GCJ02);
//                    Gps gps = gcj_To_Gps84(bdLocation_gc02.getLatitude(), bdLocation_gc02.getLongitude());
//                    double lat = mMap.getLat(gps.getLongtitude(), gps.getLatitude(),latudedistance);
//                    double longt = mMap.getLongt(gps.getLongtitude(), gps.getLatitude(), longdistance);
//                    Gps gps1 = new Gps(gps.latitude + lat, gps.longtitude + longt);
//                    mBDLocation.setLongitude(gps1.getLongtitude());
//                    mBDLocation.setLatitude(gps1.getLatitude());
//                    bdLocation_gc02=LocationClient.getBDLocationInCoorType(mBDLocation,BDLocation.BDLOCATION_WGS84_TO_GCJ02);
//                    mBDLocation=LocationClient.getBDLocationInCoorType(bdLocation_gc02,BDLocation.BDLOCATION_GCJ02_TO_BD09LL);
                    mBDLocation.setLatitude(mBDLocation.getLatitude()+mMap.getLat(mBDLocation.getLongitude(),mBDLocation.getLatitude(),latudedistance));
                    mBDLocation.setLongitude(mBDLocation.getLongitude()+mMap.getLongt(mBDLocation.getLongitude(),mBDLocation.getLatitude(),longdistance));
                   // startloction.setLongitude(startloction.getLongitude()+longt);
                    //startloction.setLatitude(startloction.getLatitude()+lat);
//                    startlatlng = new LatLng(startlatlng.longitude + longt, startlatlng.latitude + lat);
//                    startbdloction.setLatitude(startloction.getLatitude());
//                    startbdloction.setLongitude(startloction.getLongitude());
//                    mBDLocation=LocationClient.getBDLocationInCoorType(startbdloction,BDLocation.BDLOCATION_WGS84_TO_GCJ02);
//                    mBDLocation=LocationClient.getBDLocationInCoorType(mBDLocation,BDLocation.BDLOCATION_GCJ02_TO_BD09LL);
//                    mBDLocation.setLongitude(startlatlng.longitude);
//                    mBDLocation.setLatitude(startlatlng.latitude);
//                    LocationClient.getBDLocationInCoorType(mBDLocation,BDLocation.BDLOCATION_BD09LL_TO_GCJ02);
                }
                laststep = newstep;
            }
        }
    };
    private File mFile;
    private RelativeLayout mRelat2;
    private ImageView mSearch;
    private ImageView mSerch_list;
    private RelativeLayout mRelat1;
    private RelativeLayout mRelat4;
    private EditText mEtsearch;
    private ImageView mIv_plan_down;
    private ImageView mrelat4_search;
    private brigesysteminfodao mBrigesystemdao;
    private ImageView mUpdate;
    private tool mTool;
    private boolean relat2ishow=true;
    private boolean relat4ishow=false;
    private ListView listView;
    private myadapt myadapt;
    private PoiSearch mPoiSearch;
    private LatLng mDesloctionData;
    private OnGetPoiSearchResultListener poiListener;
    private ArrayList<String> mdisease;
    private mylistdisease_adapt mMylistdisease_adapt;
    private PopupWindow diseasePopupWindow;
    private LatLng latLng;
    private ImageView mIv_disease;

    public static Gps gcj_To_Gps84(double lat, double lon) {
        Gps gps = transform(lat, lon);
        double lontitude = lon * 2 - gps.getLongtitude();
        double latitude = lat * 2 - gps.getLatitude();
        return new Gps(latitude, lontitude);
    }
    public static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347) return true;
        if (lat < 0.8293 || lat > 55.8271) return true;
        return false;
    }
    private static Gps transform(double lat, double lon) {
        if (outOfChina(lat, lon)) return new Gps(lat, lon);
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Gps(mgLat, mgLon);
    }
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }
    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0 * pi)) * 2.0 / 3.0;
        return ret;
    }
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            Log.d(TAG, "SUCCESS: ");
            //定位成功后回调该方法
            //BDLocation封装了定位相关的各种信息
            //构造定位数据
            if (isfirst) {
                mBDLocation = bdLocation;
                startlatlng = new LatLng(mBDLocation.getLongitude(), mBDLocation.getLatitude());
                mService.resetValues();
                isfirst = false;
            }else {
                bdLocation.setLatitude(mBDLocation.getLatitude());
                bdLocation.setLongitude(mBDLocation.getLongitude());
                try {
//                    FileOutputStream fos = new FileOutputStream(mFile,true);
//                    fos.write((mBDLocation.getLatitude()+"  "+mBDLocation.getLongitude()+"\n").getBytes());
//                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            MyLocationData data = new MyLocationData.Builder()
                    .accuracy(bdLocation.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(currentx).latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude()).build();
            //设置定位数据
            mBaiduMap.setMyLocationData(data);

            //配置定位图层显示方式
            //有两个不同的构造方法重载 分别为三个参数和五个参数的
            //这里主要讲一下常用的三个参数的构造方法
            //三个参数：LocationMode(定位模式：罗盘，跟随),enableDirection（是否允许显示方向信息）
            // ,customMarker（自定义图标）
            MyLocationConfiguration configuration = new MyLocationConfiguration(
                    MyLocationConfiguration.LocationMode.NORMAL, true, mMarker);

            mBaiduMap.setMyLocationConfiguration(configuration);

            mLl = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            //第一次定位需要更新下地图显示状态
            if (isFirstLoc) {
                isFirstLoc = false;
                MapStatus.Builder builder = new MapStatus.Builder()
                        .target(mLl)//地图缩放中心点
                        .zoom(18f);//缩放倍数 百度地图支持缩放21级 部分特殊图层为20级
                //改变地图状态
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA), intent.getByteExtra("CHECK", (byte) -3));
                mBluetoothLeService.writeByes(new byte[]{'w', 't', 'z', 'n'});
            }

        }
    };

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
              /*      final EditText et ;  //申明变量

                    et = new EditText(parent.getContext()); //创建对象
                    et.setSingleLine(true);  //设置属性

                    final EditText etHex ;  //申明变量
                    etHex = new EditText(parent.getContext()); //创建对象
                    etHex.setSingleLine(true);  //设置属性

                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();

                        //如果该char可写
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {


                            LayoutInflater factory = LayoutInflater.from(parent.getContext());
                            final View textEntryView = factory.inflate(R.layout.dialog, null);
                            final EditText editTextName = (EditText) textEntryView.findViewById(R.id.editTextName);
                            final EditText editTextNumEditText = (EditText)textEntryView.findViewById(R.id.editTextNum);
                            AlertDialog.Builder ad1 = new AlertDialog.Builder(parent.getContext());
                            ad1.setTitle("WriteCharacteristic");
                            ad1.setView(textEntryView);
                            ad1.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int i) {
                                    byte[] value = new byte[20];
                                    value[0] = (byte) 0x00;
                                    if(editTextName.getText().length() > 0){
                                        //write string
                                        WriteBytes= editTextName.getText().toString().getBytes();
                                    }else if(editTextNumEditText.getText().length() > 0){
                                        WriteBytes= hex2byte(editTextNumEditText.getText().toString().getBytes());
                                    }
                                    characteristic.setValue(value[0],
                                            BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                                    characteristic.setValue(WriteBytes);

                                    mBluetoothLeService.writeCharacteristic(characteristic);
                                }
                            });
                            ad1.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int i) {

                                }
                            });
                            ad1.show();

                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }*/
                    return false;
                }
            };
    private BaiduMap mBaiduMap;
    private Button mMyposiotion;
    private LatLng mLl;
    private TextView fangxiang;
    private LocationManager mLocman;
    private boolean isloctionfirest=true;

    public void onClickAccCali(View view) {
        mBluetoothLeService.writeByes(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x01, (byte) 0x00});
    }

    public void onClickAccCaliL(View view) {
        mBluetoothLeService.writeByes(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x05, (byte) 0x00});
    }

    public void onClickAccCaliR(View view) {
        mBluetoothLeService.writeByes(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x06, (byte) 0x00});
    }

    public void onClickReset(View view) {
        mBluetoothLeService.writeByes(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x00, (byte) 0x01, (byte) 0x00});
    }

    public void onClickMagCali(View view) {
        Button btnMagCali = ((Button) findViewById(R.id.btnMagCali));
        if (btnMagCali.getText() == "磁场校准") {
            mBluetoothLeService.writeByes(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x07, (byte) 0x00});
            btnMagCali.setText("完成");
        } else {
            mBluetoothLeService.writeByes(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x01, (byte) 0x00, (byte) 0x00});
            btnMagCali.setText("磁场校准");
        }
    }

    public void onClickSave(View view) {
        mBluetoothLeService.writeByes(new byte[]{(byte) 0xff, (byte) 0xaa, (byte) 0x00, (byte) 0x00, (byte) 0x00});
    }


    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.gatt_services_characteristics);



        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
        mGattServicesList.setOnChildClickListener(servicesListClickListner);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        mDataField = (TextView) findViewById(R.id.data_value);
        mBt_rate = (Button) findViewById(R.id.btrate);
        mMyposiotion = (Button) findViewById(R.id.position);
        mBt_rate.setOnClickListener(this);
        mMyposiotion.setOnClickListener(this);
        mMapView = (MapView) findViewById(R.id.map_view);
        mBaiduMap = mMapView.getMap();
        fangxiang = (TextView) findViewById(R.id.fangxiang);
        mMap = new map();
        mLocman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        orition = new ArrayList<Float>();
        mFile=new File(Environment.getExternalStorageDirectory(),"positions.txt");
        mRelat2 = (RelativeLayout) findViewById(R.id.relat2);
        mUpdate = (ImageView) findViewById(R.id.update);
        mSearch = (ImageView) findViewById(R.id.search);
        mSerch_list = (ImageView) findViewById(R.id.search_list);
        mRelat1 = (RelativeLayout) findViewById(R.id.relat1);
        mRelat4 = (RelativeLayout) findViewById(R.id.relat4);
        mEtsearch = (EditText) findViewById(R.id.Etsearch);
        mIv_plan_down = (ImageView) findViewById(R.id.Iv_plan_down);
        mrelat4_search = (ImageView) findViewById(R.id.relat4_search);
        mBrigesystemdao = new brigesysteminfodao(getApplicationContext(),1);
        mPoiSearch = PoiSearch.newInstance();
        mdisease=new ArrayList<String>();
        mLv_disease=new ListView(getApplicationContext());
        mLv_disease.setBackgroundColor(Color.WHITE);
        mLv_disease.setMinimumHeight(40);
        mIv_disease=(ImageView) findViewById(R.id.iv_disease);

        listView=new ListView(getApplicationContext());
        listView.setBackgroundColor(Color.WHITE);
        mTool =new tool();
        list=new ArrayList<String>();
        list=mBrigesystemdao.query();
        initview();
        initLoc();
        judgePermission();
        myadapt=new myadapt();
        listView.setAdapter(myadapt);
        mUpdate.setOnClickListener(this);
        mRelat1.setOnClickListener(this);
        mSearch.setOnClickListener(this);
        mIv_plan_down.setOnClickListener(this);
        mrelat4_search.setOnClickListener(this);
        mSerch_list.setOnClickListener(this);
        mLv_disease.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long id) {
                if (mEtsearch.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"请选择查询的桥梁", Toast.LENGTH_SHORT).show();
                }else {
                    mFile=null;
                    brige_loaction brige_loaction = new brige_loaction();
                    BmobQuery<brige_loaction> query = new BmobQuery<>("brige_loaction");
                    query.addWhereEqualTo("name", mEtsearch.getText());
                    query.findObjects(getApplicationContext(), new FindListener<com.example.android.BTBLE905.bmobdata.brige_loaction>() {
                        @Override
                        public void onSuccess(List<brige_loaction> list) {
                            double[] doubles = list.get(0).get("D" + String.valueOf(position));
                            latLng = new LatLng(doubles[0], doubles[1]);
                            brige_photo brige_photo = new brige_photo();
                            mFile = new File(Environment.getExternalStorageDirectory(), mdisease.get(position)+"D"+String.valueOf(position)+".jpg");
                            if (mFile.exists()&&mFile!=null){
                                markerinfo info = new markerinfo(latLng, mFile,mdisease.get(position));
                                addoverlay(info);
                                Bitmap bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());
                                mIv_disease.setImageBitmap(bitmap);
                                InfoWindow mInfoWindow;
                                //生成一个TextView用户在地图中显示InfoWindow
                                Button location = new Button(getApplicationContext());
                                location.setPadding(20, 20, 30, 20);
                                location.setBackgroundColor(Color.WHITE);
                                location.setText(mdisease.get(position));
                                Point point = mBaiduMap.getProjection().toScreenLocation(latLng);
                                point.y-=20;
                                LatLng llinfo = mBaiduMap.getProjection().fromScreenLocation(point);
                                mInfoWindow=new InfoWindow(location, llinfo, -20) {
                                };
                                mBaiduMap.showInfoWindow(mInfoWindow);
                                MapStatus.Builder builder = new MapStatus.Builder()
                                        .target(latLng)//地图缩放中心点
                                        .zoom(18f);//缩放倍数 百度地图支持缩放21级 部分特殊图层为20级
                                //改变地图状态
                                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                            }else {
                                BmobQuery<brige_photo> query = new BmobQuery<>("brige_photo");
                                query.addWhereEqualTo("name", mEtsearch.getText());
                                query.findObjects(getApplicationContext(), new FindListener<com.example.android.BTBLE905.bmobdata.brige_photo>() {
                                    @Override
                                    public void onSuccess(List<brige_photo> list) {
                                        BmobFile bmobFile = list.get(0).get("D" + String.valueOf(position));
                                        File file = new File(Environment.getExternalStorageDirectory(), mdisease.get(position) + "D" + String.valueOf(position)+".jpg");
                                       bmobFile.download(getApplicationContext(), file, new DownloadFileListener() {
                                           @Override
                                           public void onSuccess(String s) {
                                               mFile=new File(s);
                                               Bitmap bitmap = BitmapFactory.decodeFile(mFile.getAbsolutePath());
                                               mIv_disease.setImageBitmap(bitmap);
                                               InfoWindow mInfoWindow;
                                               //生成一个TextView用户在地图中显示InfoWindow
                                               Button location = new Button(getApplicationContext());
                                               location.setPadding(20, 20, 30, 20);
                                               location.setBackgroundColor(Color.WHITE);
                                               location.setText(mdisease.get(position));
                                               Point point = mBaiduMap.getProjection().toScreenLocation(latLng);
                                               point.y-=50;
                                               LatLng llinfo = mBaiduMap.getProjection().fromScreenLocation(point);
                                               mInfoWindow=new InfoWindow(location, llinfo, -50) {
                                               };
                                               mBaiduMap.showInfoWindow(mInfoWindow);
                                               markerinfo info = new markerinfo(latLng, mFile,mdisease.get(position));
                                               addoverlay(info);
                                               MapStatus.Builder builder = new MapStatus.Builder()
                                                       .target(latLng)//地图缩放中心点
                                                       .zoom(18f);//缩放倍数 百度地图支持缩放21级 部分特殊图层为20级
                                               //改变地图状态
                                               mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                                           }

                                           @Override
                                           public void onFailure(int i, String s) {
                                               Toast.makeText(getApplicationContext(), "获取病害图片不成功，请检查网络信息", Toast.LENGTH_SHORT).show();
                                           }
                                       });
                                    }

                                    @Override
                                    public void onError(int i, String s) {
                                        Toast.makeText(getApplicationContext(), "获取病害图片对象不成功，请检查网络信息", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            }
                        }

                        @Override
                        public void onError(int i, String s) {
                            Toast.makeText(getApplicationContext(),"获取病害位置不成功，请检查网络信息", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String msg = list.get(position);
                mEtsearch.setText(msg);
                if (PopupWindow!=null&&PopupWindow.isShowing()){
                    PopupWindow.dismiss();
                    PopupWindow=null;
                }
            }
        });

        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        tv_step = (TextView) findViewById(R.id.step_tv);
        btn_reset = (Button) findViewById(R.id.reset_btn);
        mySharedPreferences = getSharedPreferences("relevant_data", Activity.MODE_PRIVATE);
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.resetValues();
                tv_step.setText(mySharedPreferences.getString("steps", "0"));
            }
        });
        startStepService();
        mBaiduMap.setOnMapLongClickListener(new BaiduMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                startlatlng = latLng;
                mBDLocation.setLongitude(latLng.longitude);
                mBDLocation.setLatitude(latLng.latitude);
                Toast.makeText(getApplicationContext(), "设置起点成功！", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addoverlay(markerinfo info) {
        mBaiduMap.clear();
        OverlayOptions ovlayoption=null;
        Marker marker=null;
        ovlayoption=new MarkerOptions().position(info.getLatLng())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.biaoji))
                .zIndex(5)
                .scaleX(2)
                .scaleY(2);
        marker = (Marker) mBaiduMap.addOverlay(ovlayoption);
        Bundle bundle = new Bundle();
        bundle.putSerializable("marker",info);
        marker.setExtraInfo(bundle);
    }
    private void judgePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查该权限是否已经获取
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝

            // sd卡权限
            String[] SdCardPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, SdCardPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, SdCardPermission, 100);
            }

            //手机状态权限
            String[] readPhoneStatePermission = {Manifest.permission.READ_PHONE_STATE};
            if (ContextCompat.checkSelfPermission(this, readPhoneStatePermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, readPhoneStatePermission, 200);
            }

            //定位权限
            String[] locationPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (ContextCompat.checkSelfPermission(this, locationPermission[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, locationPermission, 300);
            }

            String[] ACCESS_COARSE_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION};
            if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, ACCESS_COARSE_LOCATION, 400);
            }


            String[] READ_EXTERNAL_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, READ_EXTERNAL_STORAGE, 500);
            }

            String[] WRITE_EXTERNAL_STORAGE = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE[0]) != PackageManager.PERMISSION_GRANTED) {
                // 如果没有授予该权限，就去提示用户请求
                ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_STORAGE, 600);
            }

        } else {
            //doSdCardResult();
        }
        mLocationClient.start();
    }

    private void initLoc() {
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //定位相关参数设置
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系
        //共有三种坐标可选
        //1. gcj02：国测局坐标；
        //2. bd09：百度墨卡托坐标；
        //3. bd09ll：百度经纬度坐标；

        int span = 1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集
        option.setWifiCacheTimeOut(5 * 60 * 1000);

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        //加载设置
        mLocationClient.setLocOption(option);
    }

    private void initview() {
        mLocationClient = new LocationClient(getApplicationContext());
        //注册定位回调
        mLocationClient.registerLocationListener(mListener);
        mMarker = BitmapDescriptorFactory.fromResource(R.drawable.fangxiang);
        mMyorientionlistenerlisten = new myorientionlistenerlisten(getApplicationContext());
        mMyorientionlistenerlisten.setOnorientionListener(new myorientionlistenerlisten.OnorientionListener() {
            @Override
            public void OnorientionChanged(float x) {
                currentx = x;
                if (orition.size() < 60) {
                    orition.add(x);
                } else {
                    orition.clear();
                    orition.add(x);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMyorientionlistenerlisten.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMyorientionlistenerlisten.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        tv_step.setText(mySharedPreferences.getString("steps", "0"));
        if (this.mIsRunning) {
            bindStepService();
        }
//        if (mLocman != null) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                // TODO: Consider calling
//                //    ActivityCompat#requestPermissions
//                // here to request the missing permissions, and then overriding
//                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                //                                          int[] grantResults)
//                // to handle the case where the user grants the permission. See the documentation
//                // for ActivityCompat#requestPermissions for more details.
//                return;
//            }
//            mLocman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
//        }
    }

    @Override
    protected void onPause() {
        unbindStepService();
        super.onPause();
        mMapView.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        mPoiSearch.destroy();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    private void displayData(String data,byte byteCheck) {
        if (data != null) {
            mDataField.setText(data);
         //   TextView tvResult = ((TextView)findViewById(R.id.tvTestResult));
         /*   switch (byteCheck)
            {
                case 0:
                    tvResult.setBackgroundColor(Color.GREEN);tvResult.setText("测试通过！");
                    break;
                case -1:
                    tvResult.setBackgroundColor(Color.YELLOW);tvResult.setText("测试通过！无气压计");
                    break;
                default:
                    tvResult.setBackgroundColor(Color.RED);tvResult.setText("测试失败！");
                    break;
            }*/
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btrate:
                mBluetoothLeService.writeByes(new byte[]{(byte)0xff,(byte)0xaa,(byte)0x03,(byte)0x08,(byte)0x00});
                Toast.makeText(this,"检测速率改为50HZ", Toast.LENGTH_SHORT).show();
                break;
            case R.id.position:
                MapStatus.Builder builder = new MapStatus.Builder()
                        .target(mLl)//地图缩放中心点
                        .zoom(18f);//缩放倍数 百度地图支持缩放21级 部分特殊图层为20级
                //改变地图状态
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            case R.id.update:
                if (mBrigesystemdao.query().size()>0){
                    for(int i=0;i<mBrigesystemdao.query().size();i++){
                        mBrigesystemdao.delete(mBrigesystemdao.query().get(i));
                        Toast.makeText(this,"删除旧数据库！", Toast.LENGTH_SHORT).show();
                    }
                }
                final BmobQuery<person> query = new BmobQuery<>("person");
                query.addQueryKeys("name");
                query.findObjects(getApplicationContext(), new FindListener<com.example.android.BTBLE905.person>() {
                    @Override
                    public void onSuccess(List<person> list) {
                        for (int i = 0; i < list.size(); i++) {
                            person pnew=list.get(i);
                            final BmobQuery<person> query1 = new BmobQuery<>("person");
                            query1.addWhereEqualTo("name",pnew.getName());
                            query1.findObjects(getApplicationContext(), new FindListener<com.example.android.BTBLE905.person>() {
                                @Override
                                public void onSuccess(List<person> list) {
                                    for (int i = 0; i < list.size(); i++) {
                                        person p = list.get(i);
                                        mBrigesystemdao.add(p.getName(), p.getBaseinformation(), Double.valueOf(p.getLocation_X()), Double.valueOf(p.getLocation_Y()), p.getReport(), p.getResult());
                                    }
                                }
                                @Override
                                public void onError(int i, String s) {

                                }
                            });

                        }
                        Toast.makeText(getApplicationContext(),"数据库更新成功!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int i, String s) {
                        Toast.makeText(getApplicationContext(),"数据库更新失败！", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.relat1:
                if (relat2ishow) {
                    relat2ishow=false;
                    mTool.hideview(mRelat2);
                    mTool.hideview(mRelat4);
                }else {
                    relat2ishow=true;
                    mTool.showview(mRelat2);
                }
                break;
            case R.id.search:
                if (relat4ishow){
                    relat4ishow=false;
                    mTool.hideview(mRelat4);
                }else {
                    relat4ishow=true;
                    mTool.showview(mRelat4);
                    mRelat4.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.Iv_plan_down:
                if (PopupWindow == null){
                    PopupWindow=new PopupWindow(getApplicationContext());
                    PopupWindow.setWidth(mEtsearch.getWidth());
                    PopupWindow.setHeight(400);

                    PopupWindow.setContentView(listView);
                    PopupWindow.setFocusable(true);

                }
                PopupWindow.showAsDropDown(mEtsearch,0,0);
                break;
            case R.id.relat4_search:
                if (mEtsearch.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"请选择查询的桥梁", Toast.LENGTH_SHORT).show();
                }else {
                    boolean isindata=false;
                    for (int i = 0; i < list.size(); i++) {
                        String brige_name = mEtsearch.getText().toString();
                        if (brige_name.equals(list.get(i))){
                            ArrayList query1 = mBrigesystemdao.query(brige_name);
                            double longitude = (double) query1.get(2);
                            double lingitude = (double) query1.get(3);
                            LatLng brige_loction = new LatLng(lingitude,longitude);
                            addDestionifolay(brige_loction);
                            mDesloctionData=brige_loction;
                            builder = new MapStatus.Builder()
                                    .target(mDesloctionData)//地图缩放中心点
                                    .zoom(15f);
                            //改变地图状态
                            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                            isindata=true;
                            Toast.makeText(getApplicationContext(),"成功设置目的地", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    if (!isindata){
                        mPoiSearch.searchInCity((new PoiCitySearchOption())
                                .city("广州")
                                .keyword(mEtsearch.getText().toString())
                                .pageNum(20));
                        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
                    }
                }
                break;
            case R.id.search_list:
                mdisease.clear();
                if (mEtsearch.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"请选择维修的桥梁", Toast.LENGTH_SHORT).show();
                }else {
                    final String brige_name=mEtsearch.getText().toString();
                    final brige_acount brige_acount = new brige_acount();
                    BmobQuery<brige_acount> query1 = new BmobQuery<>("brige_acount");
                    query1.addWhereEqualTo("name",brige_name);
                    query1.findObjects(getApplicationContext(), new FindListener<com.example.android.BTBLE905.bmobdata.brige_acount>() {
                        @Override
                        public void onSuccess(List<brige_acount> list) {
                            final Integer acount = list.get(0).getAcount();
                            brige_disease brige_disease = new brige_disease();
                            BmobQuery<brige_disease> query=new BmobQuery<brige_disease>("brige_disease");
                            query.addWhereEqualTo("name",brige_name);
                            query.findObjects(getApplicationContext(), new FindListener<com.example.android.BTBLE905.bmobdata.brige_disease>() {
                                @Override
                                public void onSuccess(List<brige_disease> list) {
                                    brige_disease brige_disease1 = list.get(0);
                                    for (int i = 0; i < acount; i++) {
                                        String s="D"+String.valueOf(i);
                                        mdisease.add(brige_disease1.get(s));
                                    }
                                    mMylistdisease_adapt = new mylistdisease_adapt();
                                    mLv_disease.setAdapter(mMylistdisease_adapt);
                                    if (diseasePopupWindow == null){
                                        diseasePopupWindow=new PopupWindow(getApplicationContext());
                                        Display display = getWindow().getWindowManager().getDefaultDisplay();
                                        DisplayMetrics metrics = new DisplayMetrics();
                                        display.getMetrics(metrics);
                                        diseasePopupWindow.setWidth(metrics.widthPixels*7/8);
                                        diseasePopupWindow.setHeight(metrics.heightPixels*1/3);
                                        diseasePopupWindow.setContentView(mLv_disease);
                                        diseasePopupWindow.setFocusable(true);
                                    }
                                    diseasePopupWindow.showAtLocation(mMapView, Gravity.RIGHT,10,10);
                                }

                                @Override
                                public void onError(int i, String s) {
                                    Toast.makeText(getApplicationContext(),"获取病害失败,请检查网络信息", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(int i, String s) {
                            Toast.makeText(getApplicationContext(),"统计病害数量失败,请检查网络信息!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
        }
    }
    private void addDestionifolay(LatLng desloctionData) {
        mBaiduMap.clear();
        OverlayOptions option=new MarkerOptions().position(desloctionData)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.biaoji))
                .zIndex(5)
                .scaleX(2)
                .scaleY(2);

        mBaiduMap.addOverlay(option);
    }
    private UpdateUiCallBack mUiCallback = new UpdateUiCallBack() {
        @Override
        public void updateUi() {
            Message message = mHandler.obtainMessage();
            message.what = 1;
            mHandler.sendMessage(message);
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            StepService.StepBinder binder = (StepService.StepBinder) service;
            mService = binder.getService();
            mService.registerCallback(mUiCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void bindStepService() {
        bindService(new Intent(this, StepService.class), this.mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindStepService() {
        unbindService(this.mConnection);
    }

    private void startStepService() {
        this.mIsRunning = true;
        startService(new Intent(this, StepService.class));
    }

    private void stopStepService() {
        this.mIsRunning = false;
        if (this.mService != null)
            stopService(new Intent(this, StepService.class));
    }
    class myadapt extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceScanActivity.ViewHolder viewHolder ;
            if (convertView==null){
                convertView=View.inflate(getApplicationContext(),R.layout.item_down,null);
                viewHolder = new  DeviceScanActivity.ViewHolder();
                viewHolder.msg_text = (TextView) convertView.findViewById(R.id.tv_list);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= ( DeviceScanActivity.ViewHolder) convertView.getTag();
            }
            String mgs_text = list.get(position);
            viewHolder.msg_text.setText(mgs_text);

            return convertView;
        }
    }
    class mylistdisease_adapt extends BaseAdapter {

        @Override
        public int getCount() {
            return mdisease.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            DeviceScanActivity.ViewHolder viewHolder ;
            if (convertView==null){
                convertView=View.inflate(getApplicationContext(),R.layout.item_down,null);
                viewHolder = new DeviceScanActivity.ViewHolder();
                viewHolder.msg_text = (TextView) convertView.findViewById(R.id.tv_list);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= ( DeviceScanActivity.ViewHolder) convertView.getTag();
            }
            String mgs_text = mdisease.get(position);
            viewHolder.msg_text.setText(mgs_text);

            return convertView;
        }
    }
}
