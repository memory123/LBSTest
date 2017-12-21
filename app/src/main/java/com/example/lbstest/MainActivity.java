package com.example.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView positionTest;
    private MapView mapView;
    private LocationClient mLocationClient;
    private BaiduMap baiduMap;
    private boolean isFristLocate = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        positionTest = (TextView) findViewById(R.id.position_text_view);
        mapView = (MapView) findViewById(R.id.bdmapview);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!=
                PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            requestLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    public void requestLocation(){
        initLocation();
        mLocationClient.start();
    }

    private void navigateTo(BDLocation location){
        if (isFristLocate){
            Log.e("xxx",location.getLatitude()+","+location.getLongitude());
            LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
//            LatLng ll = new LatLng(22.602442,114.057343);
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            update = MapStatusUpdateFactory.zoomTo(16f);
            baiduMap.animateMapStatus(update);
            isFristLocate = false;
        }
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData myLocationData = locationBuilder.build();
        baiduMap.setMyLocationData(myLocationData);
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(3000);
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        Toast.makeText(MainActivity.this,"必须同意所以权限才能使用本程序",Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    requestLocation();;
                }else{
                    Toast.makeText(MainActivity.this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(final BDLocation bdLocation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    navigateTo(bdLocation);
                    StringBuilder currentPosition = new StringBuilder();
                    currentPosition.append("纬度:").append(bdLocation.getLatitude()).append("\n");
                    currentPosition.append("经度:").append(bdLocation.getLongitude()).append("\n");
                    currentPosition.append("国家:").append(bdLocation.getCountry()).append("\n");
                    currentPosition.append("省:").append(bdLocation.getProvince()).append("\n");
                    currentPosition.append("市:").append(bdLocation.getCity()).append("\n");
                    currentPosition.append("区:").append(bdLocation.getDistrict()).append("\n");
                    currentPosition.append("街道:").append(bdLocation.getStreet()).append("\n");
                    currentPosition.append("定位方式：");
                    if(bdLocation.getLocType() == BDLocation.TypeGpsLocation){
                        currentPosition.append("GPS");
                    }else if(bdLocation.getLocType() == BDLocation.TypeNetWorkLocation){
                        currentPosition.append("网络");
                    }
                    positionTest.setText(currentPosition.toString());

                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }
}
