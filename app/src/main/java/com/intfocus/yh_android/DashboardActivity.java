package com.intfocus.yh_android;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshWebView;
import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.LogUtil;
import com.intfocus.yh_android.util.OtherUtil;
import com.intfocus.yh_android.util.URLs;
import com.readystatesoftware.viewbadger.BadgeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DashboardActivity extends BaseActivity implements View.OnClickListener {
    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private int objectType;
    private TabView mCurrentTab;
    private ImageView imgSetting;
    private BadgeView bvKpi, bvAnalyse, bvApp, bvMessage, bvSetting;
    private BadgeView bvUser, bvVoice;
    private String postNotificationUrl = "";
    private String getNotificationUrl = "";
    private PopupWindow popupWindow;
    private int appValue, tabKpiValue, tabAnalyseValue, tabAppValue, tabMessageValue, settingValue;
    private LinearLayout linearUserInfo, linearScan, linearVoice, linearSearch;
    private TabView mTabKPI, mTabAnalyse, mTabAPP, mTabMessage;
    private WebView browserAd;

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        pullToRefreshWebView = (PullToRefreshWebView) findViewById(R.id.browser);
        initWebView();
        setPullToRefreshWebView(true);

        mWebView.requestFocus();
        mWebView.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");
        mWebView.loadUrl(urlStringForLoading);

        browserAd = (WebView) findViewById(R.id.browserAd);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, OtherUtil.dip2px(this, 100));
        browserAd.setLayoutParams(layoutParams);
        browserAd.getSettings().setJavaScriptEnabled(true);
        browserAd.getSettings().setDefaultTextEncodingName("utf-8");
        browserAd.addJavascriptInterface(new JavaScriptInterface(), "AndroidJSBridge");
        browserAd.setWebViewClient(new WebViewClient());
        browserAd.setWebChromeClient(new WebChromeClient() {
        });
        browserAd.getSettings().setUseWideViewPort(true);
        browserAd.getSettings().setLoadWithOverviewMode(true);

        try {
            objectType = 1;
            urlString = String.format(URLs.KPI_PATH, URLs.kBaseUrl, currentUIVersion(), user.getString("group_id"), user.getString("role_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        initView();

        initTab();

        checkNotification();

        Intent intent = getIntent();
        if (intent.hasExtra("from_activity")) {
            checkVersionUpgrade(assetsPath);
            checkPgyerVersionUpgrade(false);

            new Thread(new Runnable() {
                @Override
                public synchronized void run() {
                    try {
                        String userConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.USER_CONFIG_FILENAME);
                        JSONObject userJSON = FileUtil.readConfigFile(userConfigPath);

                        String info = ApiHelper.authentication(mContext, userJSON.getString("user_num"), userJSON.getString("password"));
                        if (!info.isEmpty() && info.contains("用户") || info.contains("密码")) {
                            userJSON.put("is_login", false);
                            FileUtil.writeFile(userConfigPath, userJSON.toString());
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            mWebView.clearCache(true);
        }

        /*
         * 检测服务器静态资源是否更新，并下载
         */
        checkAssetsUpdated(true);
        if (mCurrentTab == mTabKPI) {
            browserAd.setVisibility(View.VISIBLE);
            browserAd.loadUrl(String.format("file:///%s/%s.html", FileUtil.sharedPath(this) + "/advertisement", "index_android"));
        }
        String pushMsgConfigPath = String.format("%s/%s", FileUtil.basePath(mContext), URLs.PUSH_MESSAGE_FILENAME);
        if (new File(pushMsgConfigPath).exists()) {
            try {
                JSONObject pushMsgConfigJson = new JSONObject(FileUtil.readFile(pushMsgConfigPath));
                if (!pushMsgConfigJson.getBoolean("state")) {
                    pushMsgConfigJson.put("state", true);
                    FileUtil.writeFile(pushMsgConfigPath, pushMsgConfigJson.toString());
                    JSONObject pushMsgJson = new JSONObject(pushMsgConfigJson.getString("push_message"));
                    String type = pushMsgJson.getString("type");
                    switch (type){
                        case "report":
                            Intent subjectIntent = new Intent(this, SubjectActivity.class);
                            subjectIntent.putExtra("link", pushMsgJson.getString("link"));
                            subjectIntent.putExtra("bannerName", pushMsgJson.getString("title"));
                            startActivity(subjectIntent);
                            break;
                        case "analyse":
                            jumpTab(mTabAnalyse);
                            urlString = String.format(URLs.ANALYSE_PATH, URLs.kBaseUrl, currentUIVersion(), user.getString("role_id"));
                            break;
                        case "app":
                            jumpTab(mTabAPP);
                            urlString = String.format(URLs.APPLICATION_PATH, URLs.kBaseUrl, currentUIVersion(), user.getString("role_id"));
                            break;
                        case "message":
                            jumpTab(mTabMessage);
                            urlString = String.format(URLs.MESSAGE_PATH, URLs.kBaseUrl, currentUIVersion(), user.getString("role_id"), user.getString("group_id"), user.getString("user_id"));
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        new Thread(mRunnableForDetecting).start();
    }

    public void initView() {
        ImageView bannerCodeScan = (ImageView) findViewById(R.id.bannerCodeScan);
        bannerCodeScan.setVisibility(URLs.kDashboardDisplayScanCode ? View.VISIBLE : View.INVISIBLE);

        imgSetting = (ImageView) findViewById(R.id.imgSetting);
        bvSetting = new BadgeView(DashboardActivity.this, imgSetting);

        List<ImageView> colorViews = new ArrayList<>();
        colorViews.add((ImageView) findViewById(R.id.colorView0));
        colorViews.add((ImageView) findViewById(R.id.colorView1));
        colorViews.add((ImageView) findViewById(R.id.colorView2));
        colorViews.add((ImageView) findViewById(R.id.colorView3));
        colorViews.add((ImageView) findViewById(R.id.colorView4));

        initColorView(colorViews);

        View contentView = LayoutInflater.from(this).inflate(R.layout.activity_dashboard_dialog, null);

        linearUserInfo = (LinearLayout) contentView.findViewById(R.id.linearUserInfo);
        linearScan = (LinearLayout) contentView.findViewById(R.id.linearScan);
        linearSearch = (LinearLayout) contentView.findViewById(R.id.linearSearch);
        linearVoice = (LinearLayout) contentView.findViewById(R.id.linearVoice);

        bvUser = new BadgeView(DashboardActivity.this, linearUserInfo);
        bvVoice = new BadgeView(DashboardActivity.this, linearVoice);

        setRedDot(bvUser, false);
        setRedDot(bvVoice, false);

        linearUserInfo.setOnClickListener(this);
        linearScan.setOnClickListener(this);
        linearSearch.setOnClickListener(this);
        linearVoice.setOnClickListener(this);

        popupWindow = new PopupWindow(this);

        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(contentView);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0x00000000));
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true);
    }

    protected void onResume() {
        mMyApp.setCurrentActivity(this);
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        popupWindow.dismiss();
    }

    protected void onDestroy() {
        mContext = null;
        mWebView = null;
        user = null;
        super.onDestroy();
    }

    public void jumpTab(TabView tabView) {
        browserAd.setVisibility(View.GONE);
        mCurrentTab.setActive(false);
        mCurrentTab = tabView;
        mCurrentTab.setActive(true);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @JavascriptInterface
    private void initTab() {
        mTabKPI = (TabView) findViewById(R.id.tabKPI);
        mTabAnalyse = (TabView) findViewById(R.id.tabAnalyse);
        mTabAPP = (TabView) findViewById(R.id.tabApp);
        mTabMessage = (TabView) findViewById(R.id.tabMessage);

        if (URLs.kDashboardTabBarDisplay) {
            mTabKPI.setVisibility(URLs.kDashboardTabBarDisplayKPI ? View.VISIBLE : View.GONE);
            mTabAnalyse.setVisibility(URLs.kDashboardTabBarDisplayAnalyse ? View.VISIBLE : View.GONE);
            mTabAPP.setVisibility(URLs.kDashboardTabBarDisplayApp ? View.VISIBLE : View.GONE);
            mTabMessage.setVisibility(URLs.kDashboardTabBarDisplayMessage ? View.VISIBLE : View.GONE);
        } else {
            findViewById(R.id.toolBar).setVisibility(View.GONE);
        }

        bvKpi = new BadgeView(DashboardActivity.this, mTabKPI);
        bvAnalyse = new BadgeView(DashboardActivity.this, mTabAnalyse);
        bvApp = new BadgeView(DashboardActivity.this, mTabAPP);
        bvMessage = new BadgeView(DashboardActivity.this, mTabMessage);

        mTabKPI.setOnClickListener(mTabChangeListener);
        mTabAnalyse.setOnClickListener(mTabChangeListener);
        mTabAPP.setOnClickListener(mTabChangeListener);
        mTabMessage.setOnClickListener(mTabChangeListener);

        mCurrentTab = mTabKPI;
        mCurrentTab.setActive(true);
    }

    /*
     * OBJ_TYPE_KPI = 1
     * OBJ_TYPE_ANALYSE = 2
     * OBJ_TYPE_APP = 3
     * OBJ_TYPE_REPORT = 4
     * OBJ_TYPE_MESSAGE = 5
     */
    @SuppressLint("SetJavaScriptEnabled")
    private final View.OnClickListener mTabChangeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mCurrentTab) return;

            mCurrentTab.setActive(false);
            mCurrentTab = (TabView) v;
            mCurrentTab.setActive(true);

            mWebView.loadUrl(loadingPath("loading"));
            String currentUIVersion = currentUIVersion();
            if (mCurrentTab == mTabKPI) {
                browserAd.setVisibility(View.VISIBLE);
            } else {
                browserAd.setVisibility(View.GONE);
            }
            try {
                switch (v.getId()) {
                    case R.id.tabKPI:
                        objectType = 1;
                        urlString = String.format(URLs.KPI_PATH, URLs.kBaseUrl, currentUIVersion, user.getString("group_id"), user.getString("role_id"));
                        browserAd.loadUrl(String.format("file:///%s/%s.html", FileUtil.sharedPath(mContext) + "/advertisement", "index_android"));
                        break;
                    case R.id.tabAnalyse:
                        objectType = 2;
                        urlString = String.format(URLs.ANALYSE_PATH, URLs.kBaseUrl, currentUIVersion, user.getString("role_id"));
                        if (tabAnalyseValue > 0) {
                            clickDeal("dengwenbin", "tab_analyse", tabAnalyseValue, bvAnalyse);
                        }
                        break;
                    case R.id.tabApp:
                        objectType = 3;
                        urlString = String.format(URLs.APPLICATION_PATH, URLs.kBaseUrl, currentUIVersion, user.getString("role_id"));
                        if (tabAppValue > 0) {
                            clickDeal("dengwenbin", "tab_app", tabAppValue, bvApp);
                        }
                        break;
                    case R.id.tabMessage:
                        objectType = 5;
                        urlString = String.format(URLs.MESSAGE_PATH, URLs.kBaseUrl, currentUIVersion, user.getString("role_id"), user.getString("group_id"), user.getString("user_id"));
                        if (tabMessageValue > 0) {
                            clickDeal("dengwenbin", "tab_message", tabMessageValue, bvMessage);
                        }
                        break;
                    default:
                        objectType = 1;
                        urlString = String.format(URLs.KPI_PATH, URLs.kBaseUrl, currentUIVersion, user.getString("group_id"), user.getString("role_id"));
                        if (tabKpiValue > 0) {
                            clickDeal("dengwenbin", "tab_kpi", tabKpiValue, bvKpi);
                        }
                }

                new Thread(mRunnableForDetecting).start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "点击/主页面/标签栏");
                logParams.put("obj_type", objectType);
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void clickDeal(String notifications, String tag, int messageValue, BadgeView badgeView) {
        postNotificationUrl = String.format(URLs.POST_NOTIFICATION, notifications, tag);
        messageValue = 0;
        postNotification(postNotificationUrl);
        badgeView.setVisibility(View.INVISIBLE);
    }

    public void launchSettingActivity(View v) {
        if (settingValue > 0) {
            clickDeal("dengwenbin", "setting", settingValue, bvSetting);
        }

        popupWindow.showAsDropDown(imgSetting, OtherUtil.dip2px(this, -87), OtherUtil.dip2px(this, 10));
//        Intent intent = new Intent(mContext, SettingActivity.class);
//        mContext.startActivity(intent);

        /*
         * 用户行为记录, 单独异常处理，不可影响用户体验
         */
        try {
            logParams = new JSONObject();
            logParams.put("action", "点击/主页面/设置");
            new Thread(mRunnableForLogger).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void launchBarCodeScannerActivity(View v) throws JSONException {
        if (user.has("store_ids") || user.getString("store_ids").equals("") || user.getString("store_ids").equals(null)) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ZBAR_CAMERA_PERMISSION);
            } else {
                Intent intent = new Intent(mContext, BarCodeScannerActivity.class);
                mContext.startActivity(intent);
            }
        } else {
            Toast.makeText(this, "您无门店权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        Intent testIntent = new Intent(DashboardActivity.this, TestDialogActivity.class);
        switch (v.getId()) {
            case R.id.linearUserInfo:
                popupWindow.dismiss();
                Intent settingIntent = new Intent(mContext, SettingActivity.class);
                mContext.startActivity(settingIntent);
                break;

            case R.id.linearScan:
                popupWindow.dismiss();
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, ZBAR_CAMERA_PERMISSION);
                }
                else {
                    Intent barCodeScannerIntent = new Intent(mContext, BarCodeScannerActivity.class);
                    mContext.startActivity(barCodeScannerIntent);
                }
                break;

            case R.id.linearSearch:
                popupWindow.dismiss();
                testIntent.putExtra("linearStr", "linearAddFrd");
                startActivity(testIntent);
                break;

            case R.id.linearVoice:
                popupWindow.dismiss();
                testIntent.putExtra("linearStr", "linearHelp");
                startActivity(testIntent);
                break;

        }
    }

    //@Override
    //public void onRequestPermissionsResult(int requestCode,  String permissions[], int[] grantResults) {
    //    switch (requestCode) {
    //        case ZBAR_CAMERA_PERMISSION:
    //            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    //
    //                Intent intent = new Intent(this, BarCodeScannerActivity.class);
    //                startActivity(intent);
    //            } else {
    //                toast("Please grant camera permission to use the QR Scanner");
    //            }
    //            return;
    //    }
    //}

    private class JavaScriptInterface extends JavaScriptBase {
        /*
         * JS 接口，暴露给JS的方法使用@JavascriptInterface装饰
         */
        @JavascriptInterface
        public void pageLink(final String bannerName, final String link, final int objectID) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String message = String.format("%s\n%s\n%d", bannerName, link, objectID);
                    LogUtil.d("JSClick", message);

                    Intent intent = new Intent(DashboardActivity.this, SubjectActivity.class);
                    intent.putExtra("bannerName", bannerName);
                    intent.putExtra("link", link);
                    intent.putExtra("objectID", objectID);
                    intent.putExtra("objectType", objectType);
                    mContext.startActivity(intent);
                }
            });

            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "点击/主页面/浏览器");
                logParams.put("obj_id", objectID);
                logParams.put("obj_type", objectType);
                logParams.put("obj_title", bannerName);
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void adLink(final String openType, final String openLink) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        switch (openType) {
                            case "browser":
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                Uri content_url = Uri.parse(openLink);
                                intent.setData(content_url);
                                startActivity(intent);
                                break;
                            case "tab_kpi":
                                break;
                            case "tab_analyse":
                                jumpTab(mTabMessage);
                                urlString = String.format(URLs.ANALYSE_PATH, URLs.kBaseUrl, currentUIVersion(), user.getString("role_id"));
                                new Thread(mRunnableForDetecting).start();
                                break;
                            case "tab_app":
                                jumpTab(mTabAPP);
                                urlString = String.format(URLs.APPLICATION_PATH, URLs.kBaseUrl, currentUIVersion(), user.getString("role_id"));
                                new Thread(mRunnableForDetecting).start();
                                break;
                            case "tab_message":
                                jumpTab(mTabMessage);
                                urlString = String.format(URLs.MESSAGE_PATH, URLs.kBaseUrl, currentUIVersion(), user.getString("role_id"), user.getString("group_id"), user.getString("user_id"));
                                new Thread(mRunnableForDetecting).start();
                                break;
                            case "report":
                                Intent subjectIntent = new Intent(DashboardActivity.this, SubjectActivity.class);
                                subjectIntent.putExtra("link", openLink);
                                subjectIntent.putExtra("bannerName", openType);
                                startActivity(subjectIntent);
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

        @JavascriptInterface
        public void storeTabIndex(final String pageName, final int tabIndex) {
            try {
                String filePath = FileUtil.dirPath(mContext, URLs.CONFIG_DIRNAME, URLs.TABINDEX_CONFIG_FILENAME);

                JSONObject config = new JSONObject();
                if ((new File(filePath).exists())) {
                    String fileContent = FileUtil.readFile(filePath);
                    config = new JSONObject(fileContent);
                }
                config.put(pageName, tabIndex);

                FileUtil.writeFile(filePath, config.toString());
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public int restoreTabIndex(final String pageName) {
            int tabIndex = 0;
            try {
                String filePath = FileUtil.dirPath(mContext, URLs.CONFIG_DIRNAME, URLs.TABINDEX_CONFIG_FILENAME);

                JSONObject config = new JSONObject();
                if ((new File(filePath).exists())) {
                    String fileContent = FileUtil.readFile(filePath);
                    config = new JSONObject(fileContent);
                }
                tabIndex = config.getInt(pageName);

            } catch (JSONException e) {
                // e.printStackTrace();
            }

            return tabIndex < 0 ? 0 : tabIndex;
        }

        @JavascriptInterface
        public void jsException(final String ex) {
            /*
             * 用户行为记录, 单独异常处理，不可影响用户体验
             */
            try {
                logParams = new JSONObject();
                logParams.put("action", "JS异常");
                logParams.put("obj_type", objectType);
                logParams.put("obj_title", String.format("主页面/%s", ex));
                new Thread(mRunnableForLogger).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void checkNotification() {
        getNotificationUrl = String.format(URLs.GET_NOTIFICATION, "dengwenbin");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> response = HttpUtil.httpGet(getNotificationUrl,
                        new HashMap<String, String>());
                if (response.get("code").equals("200")) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.get("body"));
                        appValue = jsonObject.getInt("app");
                        tabKpiValue = jsonObject.getInt("tab_kpi");
                        tabAnalyseValue = jsonObject.getInt("tab_analyse");
                        tabAppValue = jsonObject.getInt("tab_app");
                        tabMessageValue = jsonObject.getInt("tab_message");
                        settingValue = jsonObject.getInt("setting");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkNotificationValue(appValue, tabKpiValue, tabAnalyseValue, tabAppValue, tabMessageValue, settingValue);
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public void postNotification(final String urlString) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Map<String, String> retMap = HttpUtil.httpPost(urlString, new HashMap<>());
            }
        }).start();
    }

    public void checkNotificationValue(int appValue, int tabKpiValue, int tabAnalyseValue, int tabAppValue, int tabMessageValue, int settingValue) {

        if (tabAnalyseValue == 1) {
            setRedDot(bvAnalyse, true);
        } else if (tabAnalyseValue > 1) {
            setRedDot(bvAnalyse, tabAnalyseValue);
        }

        if (tabAppValue == 1) {
            setRedDot(bvApp, true);
        } else if (tabAppValue > 1) {
            setRedDot(bvApp, tabAppValue);
        }

        if (tabMessageValue == 1) {
            setRedDot(bvMessage, true);
        } else if (tabMessageValue > 1) {
            setRedDot(bvMessage, tabMessageValue);
        }

        if (settingValue == 1) {
            setRedDot(bvSetting, true);
        } else if (settingValue > 1) {
            setRedDot(bvSetting, settingValue);
        }
    }

    public void setRedDot(BadgeView badgeView, boolean flag) {
        badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        badgeView.setWidth(OtherUtil.dip2px(this, 7));
        badgeView.setHeight(OtherUtil.dip2px(this, 7));
        //是否为最右上角
        if (flag) {
            badgeView.setBadgeMargin(0, 0);
        }

        badgeView.show();
    }

    public void setRedDot(BadgeView badgeView, int value) {
        badgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        badgeView.setText("" + value);
        badgeView.setTextSize(10);
        badgeView.setBadgeMargin(0, 0);

        badgeView.show();
    }

}
