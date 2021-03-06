package com.cretin.collegehelper;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.WindowManager;
import android.widget.Toast;

import com.cretin.collegehelper.constants.LocalKeysStorage;
import com.cretin.collegehelper.model.LocationModel;
import com.cretin.collegehelper.model.UserModel;
import com.cretin.collegehelper.net.NetChangeObserver;
import com.cretin.collegehelper.net.NetWorkUtil;
import com.cretin.collegehelper.net.NetworkStateReceiver;
import com.cretin.collegehelper.services.LocationService;
import com.orhanobut.hawk.Hawk;
import com.orhanobut.hawk.HawkBuilder;
import com.orhanobut.hawk.LogLevel;

import cn.bmob.push.BmobPush;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;


/**
 * Created by ycdyus on 2015/10/9.
 */
public class BaseApp extends Application {
    private static BaseApp app;
    private UserModel userModel;
    private int windowWidth;

    public LocationModel getLocationModel() {
        if (locationModel == null) {
            locationModel = Hawk.get(LocalKeysStorage.LOCATION_DATA);
        }

        return locationModel;
    }

    public void setLocationModel(LocationModel locationModel) {
        this.locationModel = locationModel;
    }

    private LocationModel locationModel;
    /**
     * 网络状态  observer
     */
    private static NetChangeObserver observer = new NetChangeObserver() {
        @Override
        public void onConnect(NetWorkUtil.NetType type) {
            super.onConnect(type);
            switch (type) {
                case WIFI:
                    showToast("WIFI已连接");
                    break;
                case GNET_3:
                    showToast("移动3G网络已连接!!!");
                    break;
                default:
                    showToast("网络已连接");
                    break;
            }
        }

        @Override
        public void onDisConnect() {
            super.onDisConnect();
            showToast("网络未连接");
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        initNetState();

        Bmob.initialize(this, "ee3cfb7f92e0ffc42dc42ba799079182");
        // 使用推送服务时的初始化操作
        BmobInstallation.getCurrentInstallation(this).save();
        // 启动推送服务
        BmobPush.startWork(this);

        Hawk.init(this)
                .setEncryptionMethod(HawkBuilder.EncryptionMethod.NO_ENCRYPTION)
                .setStorage(HawkBuilder.newSqliteStorage(this))
                .setLogLevel(LogLevel.FULL)
                .build();

        startLocationService();

//        o.sg.zm.yaie.vkao.MzYm.np(this, "01kFbt30");
//        o.sg.zm.yaie.vkao.MzYm.ef(3, 60, 60);
    }

    public static BaseApp getInstance() {
        return app;
    }

    /**
     * 初始化网络监听
     */
    private void initNetState() {
        NetworkStateReceiver.registerNetworkStateReceiver(this);
        NetworkStateReceiver.registerObserver(observer);
    }

    //启动定位服务
    private void startLocationService() {
        Intent startLocationServiceIntent = new Intent(this,
                LocationService.class);
        startService(startLocationServiceIntent);
    }

    public UserModel getUserModel() {
        if (userModel == null) {
            userModel = Hawk.get(LocalKeysStorage.LOCAL_USERINFO);
        }
        return userModel;
    }

    public void setUserModel(UserModel user) {
        userModel = user;
        Hawk.put(LocalKeysStorage.LOCAL_USERINFO, user);
    }

    /**
     * 注销网络监听
     */
    public static void unegisterNetworkStateReceiver() {
        NetworkStateReceiver.removeRegisterObserver(observer);
        NetworkStateReceiver.unRegisterNetworkStateReceiver(app);
    }

    /**
     * 弹出Toast
     */
    public static void showToast(String msg) {
        Toast.makeText(app, msg, Toast.LENGTH_SHORT).show();
    }

    public int getWindowWidth() {
        if (windowWidth <= 0) {
            WindowManager wm = (WindowManager) this
                    .getSystemService(Context.WINDOW_SERVICE);
            windowWidth = wm.getDefaultDisplay().getWidth();
        }
        return windowWidth;
    }
}
