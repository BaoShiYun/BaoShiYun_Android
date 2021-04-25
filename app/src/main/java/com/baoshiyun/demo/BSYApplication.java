package com.baoshiyun.demo;

import android.app.Application;

import com.baoshiyun.warrior.core.BSYSdk;
import com.baoshiyun.warrior.core.evn.RunMode;

/**
 * 抱石云 application
 * Created by ljt on 2020/10/27.
 */
public class BSYApplication extends Application {
    public static BSYApplication application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        // 初始化抱石云sdk
        BSYSdk.BSYSdkConfig bsySdkConfig = new BSYSdk.BSYSdkConfig(this)
                .setRunMode(RunMode.TEST)
                .debug(true);
        BSYSdk.init(bsySdkConfig);
    }

}
