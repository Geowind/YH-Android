package com.intfocus.yh_android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.github.moduth.blockcanary.BlockCanary;
import com.intfocus.yh_android.screen_lock.ConfirmPassCodeActivity;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.LogUtil;
import com.intfocus.yh_android.util.URLs;
import com.pgyersdk.crash.PgyCrashManager;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.UmengNotificationClickHandler;
import com.umeng.message.entity.UMessage;

import org.OpenUDID.OpenUDID_manager;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by lijunjie on 16/1/15.
 */
public class YHApplication extends Application {
    private Context mContext;
    private RefWatcher refWatcher;
    private static YHApplication instance;

    /*
     *  手机待机再激活时发送解屏广播
     */
    private final BroadcastReceiver broadcastScreenOnAndOff = new BroadcastReceiver() {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            if(!intent.getAction().equals(Intent.ACTION_SCREEN_ON)) return;
            Log.i("BroadcastReceiver", "Screen On");


            String currentActivityName = null;
            Activity currentActivity = ((YHApplication)context.getApplicationContext()).getCurrentActivity();
            if(currentActivity != null) {
                currentActivityName = currentActivity.getClass().getSimpleName();
                Log.i("currentActivityName", currentActivityName.trim().equals("ConfirmPassCodeActivity") ? "YES" : "NO");
            }
            Log.i("currentActivityName", "[" + currentActivityName + "]");
            if ((currentActivityName != null && !currentActivityName.trim().equals("ConfirmPassCodeActivity")) && // 当前活动的Activity非解锁界面
                FileUtil.checkIsLocked(mContext)) { // 应用处于登录状态，并且开启了密码锁

                Intent i = new Intent(getApplicationContext(), ConfirmPassCodeActivity.class);
                i.putExtra("is_from_login", false);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        mContext = YHApplication.this;
        String sharedPath = FileUtil.sharedPath(mContext), basePath = FileUtil.basePath(mContext);

        /*
         *  蒲公英平台，收集闪退日志
         */
        PgyCrashManager.register(this);

        /*
         *  初始化OpenUDID, 设备唯一化
         */
        OpenUDID_manager.sync(getApplicationContext());

        /*
         *  基本目录结构
         */
        makeSureFolderExist(URLs.CACHED_DIRNAME);
        makeSureFolderExist(URLs.SHARED_DIRNAME);

        /**
         *  新安装、或升级后，把代码包中的静态资源重新拷贝覆盖一下
         *  避免再从服务器下载更新，浪费用户流量
         */
        copyAssetFiles(basePath, sharedPath);

        /*
         *  校正静态资源
         *
         *  sharedPath/filename.zip md5值 <=> user.plist中filename_md5
         *  不一致时，则删除原解压后文件夹，重新解压zip
         */
        FileUtil.checkAssets(mContext, "loading", false);
        FileUtil.checkAssets(mContext, "assets", false);
        FileUtil.checkAssets(mContext, "fonts", true);
        FileUtil.checkAssets(mContext, "images", true);
        FileUtil.checkAssets(mContext, "stylesheets", true);
        FileUtil.checkAssets(mContext, "javascripts", true);
        FileUtil.checkAssets(mContext, "BarCodeScan", false);
        FileUtil.checkAssets(mContext, "advertisement", false);

        /*
         *  手机待机再激活时发送开屏广播
         */
        registerReceiver(broadcastScreenOnAndOff, new IntentFilter(Intent.ACTION_SCREEN_ON));
        // registerReceiver(broadcastScreenOnAndOff, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        /*
         *  监测内存泄漏
         */
        refWatcher = LeakCanary.install(this);
        BlockCanary.install(this, new AppBlockCanaryContext()).start();

        PushAgent mPushAgent = PushAgent.getInstance(mContext);
        //开启推送并设置注册的回调处理
        mPushAgent.enable(new IUmengRegisterCallback() {
            @Override
            public void onRegistered(final String registrationId) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(mContext == null) {
                                LogUtil.d("PushAgent", "mContext is null");
                                return;
                            }
                            // onRegistered方法的参数registrationId即是device_token
                            String pushConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.PUSH_CONFIG_FILENAME);
                            JSONObject pushJSON = FileUtil.readConfigFile(pushConfigPath);
                            pushJSON.put("push_valid", false);
                            pushJSON.put("push_device_token", registrationId);
                            FileUtil.writeFile(pushConfigPath, pushJSON.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        mPushAgent.onAppStart();

        mPushAgent.setNotificationClickHandler(handler);

        mPushAgent.setPushCheck(true);
    }

    UmengNotificationClickHandler handler = new UmengNotificationClickHandler() {
        @Override
        public void dealWithCustomAction(Context context, UMessage uMessage) {
            super.dealWithCustomAction(context, uMessage);
            try {
                String pushMessageConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.PUSH_MESSAGE_FILENAME);
                JSONObject jsonObject = new JSONObject(uMessage.custom);
                FileUtil.writeFile(pushMessageConfigPath, jsonObject.toString());
                Intent intent;
                if ((mCurrentActivity == null)) {
                    intent = new Intent (mContext, com.intfocus.yh_android.LoginActivity.class);
                }
                else {
                    String activityName = mCurrentActivity.getClass().getSimpleName();
                    intent = new Intent (mContext,DashboardActivity.class);
                    if (activityName.equals("LoginActivity")) {
                        return;
                    }
                    ActivityCollector.finishAll();
                    if (activityName.equals("GuideActivity")) {
                        intent = new Intent (mContext,LoginActivity.class);
                    }
                    else if (activityName.equals("DashboardActivity")) {
                        mCurrentActivity.finish();
                    }
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public static RefWatcher getRefWatcher(Context context) {
        YHApplication application = (YHApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    private void makeSureFolderExist(String folderName) {
        String cachedPath = String.format("%s/%s", FileUtil.basePath(mContext), folderName);
        FileUtil.makeSureFolderExist(cachedPath);
    }

    /**
     *  新安装、或升级后，把代码包中的静态资源重新拷贝覆盖一下
     *  避免再从服务器下载更新，浪费用户流量
     */
    private void copyAssetFiles(String basePath, String sharedPath) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionConfigPath = String.format("%s/%s", basePath, URLs.CURRENT_VERSION_FILENAME);

            boolean isUpgrade = true;
            String localVersion = "new-installer";
            if ((new File(versionConfigPath)).exists()) {
                localVersion = FileUtil.readFile(versionConfigPath);
                isUpgrade = !localVersion.equals(packageInfo.versionName);
            }
            if (!isUpgrade) return;
            Log.i("VersionUpgrade", String.format("%s => %s remove %s/%s", localVersion, packageInfo.versionName, basePath, URLs.CACHED_HEADER_FILENAME));

            String assetZipPath;
            File assetZipFile;
            String[] assetsName = {"assets.zip", "loading.zip", "fonts.zip", "images.zip", "stylesheets.zip", "javascripts.zip"};
            for (String string : assetsName) {
                assetZipPath = String.format("%s/%s", sharedPath, string);
                assetZipFile = new File(assetZipPath);
                if (!assetZipFile.exists()) { assetZipFile.delete(); }
                FileUtil.copyAssetFile(mContext, string, assetZipPath);
            }
            FileUtil.writeFile(versionConfigPath, packageInfo.versionName);
        }
        catch (PackageManager.NameNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private Activity mCurrentActivity = null;
    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity) {
        Log.i("setCurrentActivity", mCurrentActivity == null ? "null" : mCurrentActivity.getClass().getSimpleName());
        this.mCurrentActivity = mCurrentActivity;
    }
}
