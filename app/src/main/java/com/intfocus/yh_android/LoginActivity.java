package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
<<<<<<< HEAD
=======

>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
import com.intfocus.yh_android.screen_lock.ConfirmPassCodeActivity;
import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;

<<<<<<< HEAD
public class LoginActivity extends BaseActivity {
    private EditText usernameEditText, passwordEditText;
    private String usernameString, passwordString;
=======
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class LoginActivity extends BaseActivity {
    private EditText kEtUsername, kEtPassword;
    private String kUsernameStr, kPasswordStr,notificationPath;
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

<<<<<<< HEAD
=======
        initView();

>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
        /*
         *  如果是从触屏界面过来，则直接进入主界面
         *  不是的话，相当于直接启动应用，则检测是否有设置锁屏
         */
        Intent intent = getIntent();
        if (intent.hasExtra("from_activity") && intent.getStringExtra("from_activity").equals("ConfirmPassCodeActivity")) {
            Log.i("getIndent", intent.getStringExtra("from_activity"));
            intent = new Intent(LoginActivity.this, DashboardActivity.class);
            intent.putExtra("from_activity", intent.getStringExtra("from_activity"));
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            LoginActivity.this.startActivity(intent);

            finish();
<<<<<<< HEAD
        } else if (FileUtil.checkIsLocked(mContext)) {
=======
        }
        else if (FileUtil.checkIsLocked(mContext)) {
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
            intent = new Intent(this, ConfirmPassCodeActivity.class);
            intent.putExtra("is_from_login", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startActivity(intent);

            finish();
<<<<<<< HEAD
        } else {
=======
        }
        else {
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
            /*
             *  检测版本更新
             *    1. 与锁屏界面互斥；取消解屏时，返回登录界面，则不再检测版本更新；
             *    2. 原因：如果解屏成功，直接进入MainActivity,会在BaseActivity#finishLoginActivityWhenInMainAcitivty中结束LoginActivity,若此时有AlertDialog，会报错误:Activity has leaked window com.android.internal.policy.impl.PhoneWindow$DecorView@44f72ff0 that was originally added here
             */
            checkPgyerVersionUpgrade(false);
        }

<<<<<<< HEAD
        usernameEditText = (EditText) findViewById(R.id.etUsername);
        passwordEditText = (EditText) findViewById(R.id.etPassword);
=======
//        if (intent.hasExtra("link") && intent.hasExtra("bannerName")) {
//            Intent subjectIntent = new Intent (this, SubjectActivity.class);
//            subjectIntent.putExtra("link", intent.getStringExtra("link"));
//            subjectIntent.putExtra("bannerName", intent.getStringExtra("bannerName"));
//            startActivity(subjectIntent);
//        }

        notificationPath = getFilesDir()+"/Cached/local_notifition.json";
        if (!(new File(notificationPath).exists())) {
            writeLocalNotification();
        }
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9

        /*
         * 检测登录界面，版本是否升级
         */
        checkVersionUpgrade(assetsPath);
    }

<<<<<<< HEAD
    protected void onResume() {
        mMyApp.setCurrentActivity(this);
        if(mProgressDialog != null)  {
            mProgressDialog.dismiss();
        }
=======
    public void initView() {
        kEtUsername = (EditText) findViewById(R.id.etUsername);
        kEtPassword = (EditText) findViewById(R.id.etPassword);
    }

    protected void onResume() {
        mMyApp.setCurrentActivity(this);
        if (mProgressDialog != null) mProgressDialog.dismiss();
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9

        super.onResume();
    }

    protected void onDestroy() {
        mContext = null;
        mWebView = null;
        user = null;
        super.onDestroy();
    }

<<<<<<< HEAD
    public void actionSubmit(View v) {
        try {
            usernameString = usernameEditText.getText().toString();
            passwordString = passwordEditText.getText().toString();
            if (usernameString.isEmpty() || passwordString.isEmpty()) {
=======
    public void login(View v) {
        try {
            kUsernameStr = kEtUsername.getText().toString();
            kPasswordStr = kEtPassword.getText().toString();
            if (kUsernameStr.isEmpty() || kPasswordStr.isEmpty()) {
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
                toast("请输入用户名与密码");
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog = ProgressDialog.show(LoginActivity.this, "稍等", "验证用户信息...");
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
<<<<<<< HEAD
                    final String info = ApiHelper.authentication(mContext, usernameString, URLs.MD5(passwordString));
=======
                    final String info = ApiHelper.authentication(mContext, kUsernameStr, URLs.MD5(kPasswordStr));
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (info.compareTo("success") > 0 || info.compareTo("success") < 0) {
<<<<<<< HEAD
                                if (mProgressDialog != null) {
                                    mProgressDialog.dismiss();
                                }
=======
                                if (mProgressDialog != null) mProgressDialog.dismiss();
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
                                toast(info);
                                return;
                            }

                            // 检测用户空间，版本是否升级
                            assetsPath = FileUtil.dirPath(mContext, URLs.HTML_DIRNAME);
                            checkVersionUpgrade(assetsPath);

                            // 跳转至主界面
                            Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            LoginActivity.this.startActivity(intent);

<<<<<<< HEAD
                            if (mProgressDialog != null) {
                                mProgressDialog.dismiss();
                            }
=======
                            if (mProgressDialog != null) mProgressDialog.dismiss();
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9

                            finish();
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
            if (mProgressDialog != null) mProgressDialog.dismiss();
            toast(e.getLocalizedMessage());
        }
    }
<<<<<<< HEAD
=======

    public void writeLocalNotification() {
        JSONObject notificationJson = new JSONObject();
        try {
            notificationJson.put("app", -100);
            notificationJson.put("tab_kpi_last", -100);
            notificationJson.put("tab_kpi", -1);
            notificationJson.put("tab_analyse_last", -1);
            notificationJson.put("tab_analyse", -1);
            notificationJson.put("tab_app_last", -1);
            notificationJson.put("tab_app", -1);
            notificationJson.put("tab_message_last", -1);
            notificationJson.put("tab_message", -1);
            notificationJson.put("setting", -1);

            FileUtil.writeFile(notificationPath, notificationJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
}
