package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.util.Log;
import java.io.File;
import java.io.InputStream;

import java.io.IOException;
import java.util.Map;
import android.os.Handler;
import android.os.Message;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.FileOutputStream;
import android.content.Context;

import com.intfocus.yh_android.util.ApiUtil;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.URLs;
import com.intfocus.yh_android.util.HttpUtil;

import org.OpenUDID.OpenUDID_manager;

public class LoginActivity extends Activity {

	private WebView mWebView = null;
    private Thread mThread;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            String htmlName = HttpUtil.UrlToFileName(URLs.LOGIN_PATH);
            String htmlPath = String.format("%s/assets/%s", FileUtil.sharedPath(), htmlName);
            //mWebView.loadDataWithBaseURL(String.format("file:///%s/assets/", FileUtil.sharedPath()), FileUtil.readFile(htmlPath), "text/html", "UTF-8", null);
            mWebView.loadUrl(String.format("file:///" + htmlPath));
        }

    };

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
        mWebView.loadUrl(URLs.LOGIN_PATH);

        /*
         *  初始化OpenUDID, 设备唯一化
         */
        OpenUDID_manager.sync(getApplicationContext());

        try {
            File file = new File(String.format("%s/assets", FileUtil.sharedPath()));
            if(!file.exists()) {
                unZip("assets.zip", FileUtil.sharedPath(), true);
            }
            File[] files = file.listFiles();
            for(int i = 0; i < files.length; i ++) {
                Log.i("FileInShared", files[i].getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        String[] assets = new String[0];
//        try {
//            assets = getApplicationContext().getAssets().list("");
//            for(int i = 0; i < assets.length; i ++) {
//                Log.i("assets", assets[i]);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        new Thread(runnable).start();
    }

    /**
     * 解压assets的zip压缩文件到指定目录
     * @param context上下文对象
     * @param assetName压缩文件名
     * @param outputDirectory输出目录
     * @param isReWrite是否覆盖
     * @throws IOException
     */
    public void unZip(String assetName, String outputDirectory, boolean isReWrite) throws IOException {
        // 创建解压目标目录
        File file = new File(outputDirectory);
        // 如果目标目录不存在，则创建
        if (!file.exists()) {
            file.mkdirs();
        }
        // 打开压缩文件
        InputStream inputStream = getApplicationContext().getAssets().open(assetName);
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        // 读取一个进入点
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        // 使用1Mbuffer
        byte[] buffer = new byte[10*1024 * 1024];
        // 解压时字节计数
        int count = 0;
        // 如果进入点为空说明已经遍历完所有压缩包中文件和目录
        while (zipEntry != null) {
            // 如果是一个目录
            if (zipEntry.isDirectory()) {
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                // 文件需要覆盖或者是文件不存在
                if (isReWrite || !file.exists()) {
                    file.mkdir();
                }
            } else {
                // 如果是文件
                file = new File(outputDirectory + File.separator + zipEntry.getName());
                // 文件需要覆盖或者文件不存在，则解压文件
                if (isReWrite || !file.exists()) {
                    file.createNewFile();
                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    while ((count = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, count);
                    }
                    fileOutputStream.close();
                }
            }
            // 定位到下一个文件入口
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.close();
    }



    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                Map<String, String> response = HttpUtil.httpGet(URLs.LOGIN_PATH);
                if (response.get("code").toString().compareTo("200") == 0) {
                    String htmlName = HttpUtil.UrlToFileName(URLs.LOGIN_PATH);
                    String htmlPath = String.format("%s/assets/%s", FileUtil.sharedPath(), htmlName);
                    String htmlContent = response.get("body").toString();
                    String assetsPath = String.format("file:///%s/assets/", FileUtil.sharedPath());
                    htmlContent = htmlContent.replace("javascript/", String.format("%s/javascript/", assetsPath));
                    htmlContent = htmlContent.replace("stylesheets/", String.format("%s/stylesheets/", assetsPath));
                    htmlContent = htmlContent.replace("images/", String.format("%s/images/", assetsPath));
                    Log.i("HTML", htmlContent);
                    FileUtil.writeFile(htmlPath, response.get("body").toString());

                    mHandler.obtainMessage().sendToTarget();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    };

    private class JavaScriptInterface {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void login(final String username, String password) {
            if(username.length() > 0 && password.length() > 0) {
                try {
                    String info = ApiUtil.authentication(username, URLs.MD5(password));
                    if (info.compareTo("success") == 0) {
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, info, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(LoginActivity.this, "请输入用户名与密码", Toast.LENGTH_SHORT).show();
            }
        }

        public String HtmlcallJava() {
            return "Html call Java";
        }

        public String HtmlcallJava2(final String param) {
            return "Html call Java : " + param;
        }

        public void JavacallHtml() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript: showFromHtml()");
                    Toast.makeText(LoginActivity.this, "clickBtn", Toast.LENGTH_SHORT).show();
                }
            });
        }

        public void JavacallHtml2() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl("javascript: showFromHtml2('IT-homer blog')");
                    Toast.makeText(LoginActivity.this, "clickBtn2", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @JavascriptInterface
        public void callHandler(String tag, Object obj, Object cb) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            LoginActivity.this.startActivity(intent);
        }
    }
}
