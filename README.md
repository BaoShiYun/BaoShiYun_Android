# 1.概述

## 1.1 SDK简介

抱石云AndroidSDK是适用于Android平台的云直播点播功能SDK。使用SDK可以与抱石云直播点播服务进行对接，在Android端使用抱石云直播点播功能。

## 1.2 功能特性

| 功能    | 描述                                            |
|:--------|:----------------------------------------------|
| 直播    | 观看直播视频画面                                 |
| 线路    | 支持多线路切换                                   |
| 清晰度   | 支持多清晰度切换。目前支持清晰度类型:超清、高清、标清 |
| 连麦    | 支持纯语音或音视频连麦                            |
| 白板    | 支持讲师课件白板实时展示                          |
| 随堂问答 | 支持随堂题目下发及答题提交                        |
| 通知    | 支持下发模版式通知消息                            |
| 互动聊天 | 支持直播间文字图片互动聊天                        |
| 口令红包 | 支持配置口令红包                                 |
| 货架    | 支持配置货架                                     |
| 点赞    | 支持直播间点赞功能                                |
| 禁言    | 支持直播间禁言                                   |
| 在线人数 | 支持直播间在线人数及是否展示在线人数                |
| 广播    | 支持广播链接                                     |

# 2.集成开发

[最新 release 版本 sdk](https://github.com/BaoShiYun/bsySdkAndroid/packages)

## 前提条件

1. Android Studio
2. API 19+

## 获取所需要的信息

1. 阅读 应用与权限，获取 SDK Token

## 集成 SDK 到项目中

### build.gradle 配置

1.打开根目录下的 build.gradle 进行如下标准配置：

```groovy
allprojects {
    repositories {
        // github 库使用
        maven { url "https://jitpack.io" }
        maven { url "https://www.jitpack.io" }
        jcenter()
        google()

        // 远程sdk仓库
        maven {
            url = "http://maven.pkg.github.com/baoshiyun/BaoShiYun_Android/"
            credentials {
                username = "BaoShiYun"
                password = "ghp_0QxdoB" + "PlyiA4PQl" + "VAzvxZoBHyh" + "PpoP2WYb9h"
            }
        }
    }
}
```

2.打开 app 目录下的 build.gradle 进行如下配置：

```groovy
android {
    ** *
    defaultConfig {
        ** *
        // 根据自己项目需求添加动态库过滤配置
        // abiFilters 'armeabi', 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a', 'armeabi-v7a'
        ndk {
            abiFilters 'armeabi-v7a', 'arm64-v8a'
        }
    }
}
dependencies {
    // 添加依赖
    // 数字请根据最新版自行添加
    implementation('com.baoshiyun:bsy-sdk:1.2.6') {
        // 依赖库版本有冲突可以排除 相应依赖
        //        exclude module: 'okhttp'
        //        exclude module: 'gson'
    }
}
```

### Permission 配置

打开 app 目录下的 AndroidManifest.xml 进行如下配置：

``` xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<!--直播间连麦互动 如果使用蓝牙耳机 通话时需要 申请-->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BROADCAST_STICKY" />
```

### Proguard 配置

```bash
# 抱石云 sdk model
-keep class com.baoshiyun.**{*;}
# white SDK model
-keep class com.herewhite.** { *; }
-keepattributes  *JavascriptInterface*
-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson
-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *;
# 声网 sdk model
-keep class io.agora.** { *;}
# 腾讯 sdk model
-keep class com.tencent.**{*;}
# ijk 配置
-keep class tv.danmaku.ijk.media.player.**{*;}
```

### 初始化

在 Application onCreate() 方法中初始化 抱石云 sdk

``` java
BSYSdk.BSYSdkConfig bsySdkConfig = new BSYSdk.BSYSdkConfig(applicationContext)
    .debug(Boolean) // 是否打印调试日志 
    .setThreadPoolExecutor(ThreadPoolExecutor); // 线程池对象，可不设置 
BSYSdk.init(bsySdkConfig);
```