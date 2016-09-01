package com.intfocus.yh_android;

import android.content.Intent;
import android.os.Bundle;
<<<<<<< HEAD
import android.util.Log;
=======
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
<<<<<<< HEAD
import com.intfocus.yh_android.util.ApiHelper;
import com.intfocus.yh_android.util.FileUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
=======

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.HttpUtil;
import com.intfocus.yh_android.util.URLs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9

/**
 * Created by lijunjie on 16/6/10.
 */
public class BarCodeResultActivity extends BaseActivity {
<<<<<<< HEAD
  private String htmlContent, htmlPath, cachedPath;
  private String codeInfo, codeType, groupID, roleID, userNum;
  private String storeID;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    setContentView(R.layout.activity_bar_code_result);

    mWebView = (WebView) findViewById(R.id.browser);
    WebSettings webSettings = mWebView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDefaultTextEncodingName("utf-8");
    webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

    List<ImageView> colorViews = new ArrayList<>();
    colorViews.add((ImageView) findViewById(R.id.colorView0));
    colorViews.add((ImageView) findViewById(R.id.colorView1));
    colorViews.add((ImageView) findViewById(R.id.colorView2));
    colorViews.add((ImageView) findViewById(R.id.colorView3));
    colorViews.add((ImageView) findViewById(R.id.colorView4));
    initColorView(colorViews);

    String htmlOriginPath = sharedPath + "/BarCodeScan/scan_bar_code.html";
    htmlContent = FileUtil.readFile(htmlOriginPath);
    cachedPath = FileUtil.dirPath(mContext, "Cached", "barcode.json");
    htmlPath = String.format("%s.tmp", htmlOriginPath);

    try {
      Intent intent = getIntent();
      codeInfo = intent.getStringExtra("code_info");
      codeType = intent.getStringExtra("code_type");
      groupID = user.getString("group_id");
      roleID = user.getString("role_id");
      userNum = user.getString("user_num");

      /*
       * 初始化默认选中门店（第一家）
       */
      JSONObject cachedJSON = FileUtil.readConfigFile(cachedPath);
      if((!cachedJSON.has("store") || !cachedJSON.getJSONObject("store").has("id")) &&
          user.has("store_ids") && user.getJSONArray("store_ids").length() > 0) {
        cachedJSON.put("store", user.getJSONArray("store_ids").get(0));
        FileUtil.writeFile(cachedPath, cachedJSON.toString());
      }

      /*
       * 商品条形码写入缓存
       */
      JSONObject cachedCodeJSON = new JSONObject();
      cachedCodeJSON.put("code_info", codeInfo);
      cachedCodeJSON.put("code_type", codeType);
      cachedJSON.put("barcode", cachedCodeJSON);
      FileUtil.writeFile(cachedPath, cachedJSON.toString());

      storeID = cachedJSON.getJSONObject("store").getString("id");
    } catch (JSONException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    String loadingString = "{\"chart\": \"[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]\", \"tabs\": [{ title: \"提示\", table: { length: 1, \"1\": [\"加载中...\"]}}]}";
    FileUtil.barCodeScanResult(mContext, loadingString);
    updateHtmlContentTimetamp();
    mWebView.loadUrl(String.format("file:///%s", htmlPath));

    new Thread(new Runnable() {
      @Override public void run() {
        ApiHelper.barCodeScan(mContext, groupID, roleID, userNum, storeID, codeInfo, codeType);
        updateHtmlContentTimetamp();

        runOnUiThread(new Runnable() {
          @Override public void run() {
            mWebView.loadUrl(String.format("file:///%s", htmlPath));
          }
        });
      }
    }).start();
  }

  /*
   * 返回
   */
  public void dismissActivity(View v) {
    BarCodeResultActivity.this.onBackPressed();
  };

  public void updateHtmlContentTimetamp() {
    try {
      String newHtmlContent = htmlContent.replaceAll("TIMESTAMP", String.format("%d", new Date().getTime()));
      Log.i("HtmlContentTimetamp", newHtmlContent);
      FileUtil.writeFile(htmlPath, newHtmlContent);
    } catch(IOException e) {
      e.printStackTrace();
    }
  }

  public void actionLaunchStoreSelectorActivity(View v) throws InterruptedException {
    Intent intent = new Intent(mContext, StoreSelectorActivity.class);
    mContext.startActivity(intent);
  }
=======
    private String htmlContent, htmlPath, barcodePath, codeInfo, codeType, groupID, storeUrl, roleID, userNum, storeName, userId;
    private String barcodeContent;
    private JSONObject barcodeJson, storeJson, jsonObject;
    private JSONArray storeList;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_bar_code_result);

        initView();

        initData();

        writeBarcodeFile(codeInfo, codeType);
    }

    public void initView() {
        mWebView = (WebView) findViewById(R.id.browser);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        List<ImageView> colorViews = new ArrayList<>();
        colorViews.add((ImageView) findViewById(R.id.colorView0));
        colorViews.add((ImageView) findViewById(R.id.colorView1));
        colorViews.add((ImageView) findViewById(R.id.colorView2));
        colorViews.add((ImageView) findViewById(R.id.colorView3));
        colorViews.add((ImageView) findViewById(R.id.colorView4));
        initColorView(colorViews);
    }

    public void initData() {
        htmlPath = sharedPath + "/BarCodeScan/scan_bar_code.html";
        htmlContent = FileUtil.readFile(htmlPath);
        barcodePath = getFilesDir() + "/Cached/barcode.json";

        try {
            Intent intent = getIntent();
            if (intent.hasExtra("code_info")) {
                codeInfo = intent.getStringExtra("code_info");
            }
            else {
                barcodeContent = FileUtil.readFile(getFilesDir() + "/Cached/barcode.json");
                jsonObject = new JSONObject(barcodeContent);
                barcodeJson = (JSONObject) jsonObject.get("barcode");
                codeInfo = barcodeJson.getString("code_info");
            }

            if (intent.hasExtra("code_type")) {
                codeType = intent.getStringExtra("code_type");
            }
            else {
                codeType = barcodeJson.getString("code_type");
            }
            groupID = user.getString("group_id");
            roleID = user.getString("role_id");
            userNum = user.getString("user_num");
            userId = user.getString("user_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeBarcodeFile(String codeInfo, String codeType) {
        try {
            File file = new File(getFilesDir() + "/Cached/barcode.json");
            if (file.exists()) {
                jsonObject = new JSONObject(FileUtil.readFile(getFilesDir() + "/Cached/barcode.json"));
            }
            else {
                jsonObject = new JSONObject();
            }

            barcodeJson = new JSONObject();
            barcodeJson.put("code_info", codeInfo);
            barcodeJson.put("code_type", codeType);

            jsonObject.put("barcode", barcodeJson);
            FileUtil.writeFile(barcodePath, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getUrlString() {
        try {
            barcodeContent = FileUtil.readFile(getFilesDir() + "/Cached/barcode.json");
            jsonObject = new JSONObject(barcodeContent);

            if (jsonObject.has("store")) {
                storeJson = (JSONObject) jsonObject.get("store");
                setStoreInfo(storeJson.getString("id"), storeJson.getString("name"));
            }
            else {
                if (user.has("store_ids")) {
                    storeList = new JSONArray(user.getString("store_ids"));
                    setStoreInfo(new JSONObject(storeList.getString(0)).getString("id"), new JSONObject(storeList.getString(0)).getString("name"));
                }
                else {
                    setStoreInfo("-1", "门店名称为-1");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setStoreInfo(String id, String name) {
        storeUrl = String.format(URLs.POST_BARCODE, groupID, roleID, userId, id, codeInfo, codeType);
        storeName = name;
    }

    @Override
    protected void onResume () {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                getUrlString();

                Map<String, String> response = HttpUtil.httpGet(storeUrl, new HashMap());
                if (response.get("code").equals("200") || response.get("code").equals("201")) {
                    StringBuffer sb = new StringBuffer(response.get("body").replaceAll(",\"code\":200", ""));
                    sb.append(";");
                    FileUtil.barCodeScanResult(mContext, sb.toString());

                    updateHtmlContentTimetamp();

                    mWebView.post(new Runnable() {
                        @Override
                        public void run() {
                            mWebView.loadUrl(String.format("file:///%s", htmlPath));
                        }
                    });
                }
            }
        }).start();
    }

    /*
     * 返回
     */

    public void dismissActivity(View v) {
        BarCodeResultActivity.this.onBackPressed();
    }

    public void updateHtmlContentTimetamp() {
        try {
            String newHtmlContent = htmlContent.replaceAll("TIMESTAMP", String.format("%d", new Date().getTime()));
            FileUtil.writeFile(htmlPath, newHtmlContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionLaunchReportSelectorActivity(View v) throws InterruptedException {
        Intent intent = new Intent(mContext, BarCodeSelectorActivity.class);
        intent.putExtra("bannerName", storeName);
        mContext.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
>>>>>>> fa4ffd90cd3f6a0db797ede37e42becda41540e9
}
