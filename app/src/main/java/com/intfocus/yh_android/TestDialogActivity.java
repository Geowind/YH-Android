package com.intfocus.yh_android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;


/**
 * Created by 40284 on 2016/8/7.
 */
public class TestDialogActivity extends BaseActivity {

    private TextView testTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_dialog);

        Intent intent = getIntent();
        testTv = (TextView) findViewById(R.id.testTv);
        testTv.setText("你好"+intent.getStringExtra("linearStr"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
