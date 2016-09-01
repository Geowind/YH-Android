package com.intfocus.yh_android;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.intfocus.yh_android.util.FileUtil;
import com.intfocus.yh_android.util.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by lijunjie on 16/7/20.
 */
public class BarCodeSelectorActivity extends BaseActivity {
    private ListView mListView;
    private String selectedItem;
    private ArrayList<String> searchItems = new ArrayList<String>();
    private String bannerName;
    private HashMap<String,String> hashMap = new HashMap<>();
    private JSONObject jsonObject;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_selector);

        Intent intent = getIntent();
        bannerName = intent.getStringExtra("bannerName");
        try {
            jsonObject = new JSONObject(FileUtil.readFile(getFilesDir() + "/Cached/barcode.json"));

            if (user.has("store_ids") ) {
                JSONArray storeList = new JSONArray(user.getString("store_ids"));
                for (int i = 0; i < storeList.length(); i++) {
                    searchItems.add(new JSONObject(storeList.getString(i)).getString("name"));
                    hashMap.put(new JSONObject(storeList.getString(i)).getString("name"), new JSONObject(storeList.getString(i)).getString("id"));
                }

                if (jsonObject.has("store")) {
                    selectedItem = new JSONObject(jsonObject.getString("store")).getString("name");
                }
                else {
                    selectedItem = new JSONObject(storeList.getString(0)).getString("name");
                }
            }
            else {
                searchItems.add("门店的名称为-1");
                selectedItem = "门店的名称为-1";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView mTitle = (TextView) findViewById(R.id.bannerTitle);
        mTitle.setText(bannerName);

        /**
         *  筛选项列表按字母排序，以便于用户查找
         */
        Collections.sort(searchItems, new Comparator<String>() {
            @Override
            public int compare(String one, String two) {
                return Collator.getInstance(Locale.CHINESE).compare(one, two);
            }
        });

        mListView = (ListView) findViewById(R.id.listSearchItems);
        ListArrayAdapter mArrayAdapter = new ListArrayAdapter(this, R.layout.list_item_report_selector, searchItems);
        mListView.setAdapter(mArrayAdapter);

        /**
         *  用户点击项写入本地缓存文件
         */
        mListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                try {
                    JSONObject storeJson = new JSONObject();
                    storeJson.put("id", hashMap.get(searchItems.get(arg2)));
                    storeJson.put("name", searchItems.get(arg2));

                    jsonObject.put("store", storeJson);
                    FileUtil.writeFile(getFilesDir() + "/Cached/barcode.json", jsonObject.toString());
                }catch (Exception e){
                    e.printStackTrace();
                }
                dismissActivity(null);
            }
        });
    }

    protected void onResume() {
        mMyApp.setCurrentActivity(this);
        super.onResume();
    }

    protected void onDestroy() {
        mContext = null;
        mWebView = null;
        user = null;
        super.onDestroy();
    }

    public class ListArrayAdapter extends ArrayAdapter<String> {
        private int resourceId;

        public ListArrayAdapter(Context context, int textViewResourceId, List<String> items) {
            super(context, textViewResourceId, items);
            this.resourceId = textViewResourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String item = getItem(position).trim();
            LinearLayout listItem = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(inflater);
            vi.inflate(resourceId, listItem, true);
            TextView viewItem = (TextView) listItem.findViewById(R.id.reportSelectorItem);
            viewItem.setText(item);
            /**
             *  上次选中项显示选中状态
             */
            boolean isSelected = (item != null && selectedItem != null && item.compareTo(selectedItem) == 0);
            LogUtil.d("getView", String.format("%s %s %s", item, selectedItem, isSelected ? "==" : "!="));
            viewItem.setBackgroundColor(isSelected ? Color.GREEN : Color.WHITE);

            return listItem;
        }
    }

    /*
     * 返回
     */
    public void dismissActivity(View v) {
        BarCodeSelectorActivity.this.onBackPressed();
        finish();
    }

}
