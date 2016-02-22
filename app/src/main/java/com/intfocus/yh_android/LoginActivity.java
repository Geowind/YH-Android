package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.intfocus.yh_android.screen_lock.ConfirmPassCodeActivity;
import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class LoginActivity extends BaseActivity {

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pullToRefreshWebView = (PullToRefreshWebView) findViewById(R.id.webview);
        initRefreshWebView();
        setPullToRefreshWebView(false);

        mWebView.requestFocus();
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");

        /*
         * 显示加载中...界面
         */
        urlStringForLoading = String.format("file:///%s/loading/login.html", FileUtil.sharedPath(mContext));
        mWebView.loadUrl(urlStringForLoading);

        /*
         *  是否启用锁屏
         */
        if (FileUtil.checkIsLocked(mContext)) {
            Log.i("screen_lock", "lock it");

            Intent intent = new Intent(this, ConfirmPassCodeActivity.class);
            intent.putExtra("is_from_login", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);
        } else {
            Log.i("screen_lock", "not lock");

            /*
             *  检测版本更新
             *    1. 与锁屏界面互斥；取消解屏时，返回登录界面，则不再检测版本更新；
             *    2. 原因：如果解屏成功，直接进入MainActivity,会在BaseActivity#finishLoginActivityWhenInMainAcitivty中结束LoginActivity,若此时有AlertDialog，会报错误:Activity has leaked window com.android.internal.policy.impl.PhoneWindow$DecorView@44f72ff0 that was originally added here
             */
            checkUpgrade(false);
        }

        urlString = URLs.LOGIN_PATH;
        urlStringForDetecting = URLs.HOST;
        assetsPath = FileUtil.sharedPath(mContext);
        relativeAssetsPath = "assets";

        /*
         * 检测登录界面，版本是否升级
         */
        checkVersionUpgrade(assetsPath);

        /*
         *  加载服务器网页
         */
        new Thread(mRunnableForDetecting).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void checkVersionUpgrade(String assetsPath) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String versionConfigPath = String.format("%s/%s", assetsPath, URLs.CURRENT_VERSION__FILENAME);

            boolean isUpgrade = false;
            if ((new File(versionConfigPath)).exists()) {
                String localVersion = FileUtil.readFile(versionConfigPath);
                if (localVersion.compareTo(packageInfo.versionName) != 0) {
                    isUpgrade = true;
                    Log.i("VersionUpgrade", String.format("%s => %s remove %s/%s", localVersion, packageInfo.versionName, assetsPath, URLs.CACHED_HEADER_FILENAME));
                }
            } else {
                isUpgrade = true;
            }
            if (isUpgrade) {
                ApiHelper.clearResponseHeader(URLs.LOGIN_PATH, assetsPath);
                FileUtil.writeFile(versionConfigPath, packageInfo.versionName);

                String userConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.USER_CONFIG_FILENAME);
                JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);
                userJSON.remove("local_loading_md5");
                userJSON.remove("local_assets_md5");
                FileUtil.writeFile(userConfigPath, userJSON.toString());

                /*
                 * 用户报表数据js文件存放在公共区域
                 */
                String headerPath = String.format("%s/%s", FileUtil.sharedPath(mContext), URLs.CACHED_HEADER_FILENAME);
                new File(headerPath).delete();

                FileUtil.checkAssets(mContext, "loading");
                FileUtil.checkAssets(mContext, "assets");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class JavaScriptInterface extends JavaScriptBase {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void login(final String username, String password) {
            if (username.length() > 0 && password.length() > 0) {
                try {
                    String info = ApiHelper.authentication(mContext, username, URLs.MD5(password));
                    if (info.compareTo("success") == 0) {
                        // 检测用户空间，版本是否升级
                        assetsPath = FileUtil.dirPath(mContext, URLs.HTML_DIRNAME);
                        checkVersionUpgrade(assetsPath);

                        // 跳转至主界面
                        Intent intent = new Intent(mContext, MainActivity.class);
                        intent.putExtra("fromActivity", this.getClass().toString());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mContext.startActivity(intent);

                        finish();
                    } else {
                        Toast.makeText(mContext, info, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                /*
                 * 用户行为记录, 单独异常处理，不可影响用户体验
                 */
                try {
                    logParams = new JSONObject();
                    logParams.put("action", "登录");
                    new Thread(mRunnableForLogger).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(mContext, "请输入用户名与密码", Toast.LENGTH_SHORT).show();
            }
        }

        @JavascriptInterface
        public void jsException(final String ex) {
            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "JS异常");
                logParams.put("obj_title", String.format("登录页面/%s", ex));
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
