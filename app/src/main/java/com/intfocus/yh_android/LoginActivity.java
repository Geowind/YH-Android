package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

import java.io.File;
import java.io.IOException;

import com.pgyersdk.update.PgyUpdateManager;

public class LoginActivity extends BaseActivity {

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mWebView = (WebView) findViewById(R.id.webview);
        mWebView.initialize();
        mWebView.requestFocus();

        mWebView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return false;
            }
        });
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");


        /*
         * 显示加载中...界面
         */
        urlStringForLoading = String.format("file:///%s/loading/login.html", FileUtil.sharedPath());
        mWebView.loadUrl(urlStringForLoading);

        /*
         * 检测登录界面，版本是否升级
         */
        checkVersionUpgrade(FileUtil.sharedPath());

        /*
         *  加载服务器网页
         */
        urlString = URLs.LOGIN_PATH;

        new Thread(mRunnableForDetecting).start();

        /*
         *  检测版本更新
         */
        PgyUpdateManager.register(this);
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
                    String info = ApiHelper.authentication(username, URLs.MD5(password));
                    if (info.compareTo("success") == 0) {

                        // 检测用户空间，版本是否升级
                        assetsPath = FileUtil.dirPath(URLs.HTML_DIRNAME);
                        checkVersionUpgrade(assetsPath);

                        // 跳转至主界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        LoginActivity.this.startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, info, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(LoginActivity.this, "请输入用户名与密码", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
