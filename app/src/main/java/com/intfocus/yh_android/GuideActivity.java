package com.intfocus.yh_android;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.intfocus.yh_android.util.OtherUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 40284 on 2016/8/18.
 */
public class GuideActivity extends BaseActivity implements ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private List<View> views = new ArrayList<>();
    private LinearLayout linearDot;
    private List<ImageView> dots = new ArrayList<>();
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide);
        inflater = LayoutInflater.from(this);
//        if (isGuide()) {
//            Intent intent = new Intent(this, LoginActivity.class);
//            startActivity(intent);
//            OtherUtil.writeShared(this, "Guide", true);
//
//            finish();
//        }

        linearDot = (LinearLayout) findViewById(R.id.linearDot);

        init();
        initDot();
    }

    public void init() {
        View view1 = setViewAttributes(R.drawable.bg_guide1, "", 0, "", 0, "", 0, 0,
                R.drawable.text_guide1, R.color.white, "登录", Color.BLACK,
                "立即尝试 传真", Color.WHITE);
        View view2 = setViewAttributes(0, "数据跟着你走", R.color.guide_big_tv_color,
                "出差途中，也可以实时掌握企业经营状况", R.color.guide_small_tv_color, "",
                0, R.drawable.img_guide2, 0, R.color.guide_big_tv_color, "登录", Color.WHITE,
                "立即尝试 传真", Color.BLACK);
        View view3 = setViewAttributes(0, "可动口可动手", R.color.guide_big_tv_color,
                "文字、语言、扫码，多种查询方式，", R.color.guide_small_tv_color,
                "只为让您身在不同环境时多一些选择", R.color.guide_small_tv_color,
                R.drawable.img_guide3, 0, R.color.guide_big_tv_color, "登录",
                Color.WHITE, "立即尝试 传真", Color.BLACK);

        views.add(view1);
        views.add(view2);
        views.add(view3);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        adapter = new ViewPagerAdapter(this,views);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(this);
    }

    public View setViewAttributes(int guideLayoutBg, String guideBigTvStr, int guideBigTvFontColor, String guideSmallTvStr,
                                  int guideSmallTvFontColor, String guideSmallTv1Str, int guideSmallTv1FontColor,
                                  int guideImgBg, int guideImg1Bg, int guideBtnBg, String guideBtnStr,
                                  int guideBtnFontColor, String guideClickStr, int guideClickFontColor) {
        View view = inflater.inflate(R.layout.activity_guide_adapter, null);
        TextView guideBigText = (TextView) view.findViewById(R.id.guideBigTv);
        TextView guideSmallText = (TextView) view.findViewById(R.id.guideSmallTv);
        TextView guideSmallText1 = (TextView) view.findViewById(R.id.guideSmallTv1);
        TextView guideClickTv = (TextView) view.findViewById(R.id.guideClickTv);
        Button guideBtn = (Button) view.findViewById(R.id.guideBtn);
        ImageView guideImg = (ImageView) view.findViewById(R.id.guideImg);
        ImageView guideImg1 = (ImageView) view.findViewById(R.id.guideImg1);
        RelativeLayout guideLayout = (RelativeLayout) view.findViewById(R.id.guideLayout);

        if (guideLayoutBg != 0) {
            guideLayout.setBackgroundResource(guideLayoutBg);
        }

        setTextViewAttributes(guideBigText, guideBigTvStr, guideBigTvFontColor);

        setTextViewAttributes(guideSmallText, guideSmallTvStr, guideSmallTvFontColor);

        setTextViewAttributes(guideSmallText1, guideSmallTv1Str, guideSmallTv1FontColor);

        setTextViewAttributes(guideClickTv, guideClickStr, guideClickFontColor);

        setImageViewAttributes(guideImg, guideImgBg);

        setImageViewAttributes(guideImg1, guideImg1Bg);


        if (!guideBtnStr.equals("")) {
            guideBtn.setText(guideBtnStr);
            guideBtn.setTextColor(guideBtnFontColor);
            guideBtn.setBackgroundResource(guideBtnBg);
            guideBtn.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public void setTextViewAttributes(TextView tv, String tvStr, int tvFontColor) {
        if (!tvStr.equals("")) {
            if (tv.getId() == R.id.guideClickTv) {
                tv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            }

            tv.setText(tvStr);
            tv.setTextColor(tvFontColor);
            tv.setVisibility(View.VISIBLE);
        }
    }

    public void setImageViewAttributes(ImageView iv, int ivBg) {
        if (ivBg != 0) {
            iv.setBackgroundResource(ivBg);
            iv.setVisibility(View.VISIBLE);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void initDot() {
        for (int i=0; i<views.size(); i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.image_shape);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(OtherUtil.dip2px(this, 10), OtherUtil.dip2px(this, 10));
            layoutParams.setMargins(OtherUtil.dip2px(this, 5), 0, 0, 0);
            imageView.setLayoutParams(layoutParams);
            dots.add(imageView);
            linearDot.addView(imageView);
        }
        dots.get(0).setImageResource(R.drawable.image_shape1);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int j=0; j<views.size(); j++) {
            if (position == j) {
                dots.get(j).setImageResource(R.drawable.image_shape1);
            }
            else {
                dots.get(j).setImageResource(R.drawable.image_shape);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public boolean isGuide() {
        SharedPreferences sharedPreferences = getSharedPreferences("guide",MODE_PRIVATE);
        return sharedPreferences.getBoolean("boolean",false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ActivityCollector.removeActivity(this);
        finish();
    }
}
