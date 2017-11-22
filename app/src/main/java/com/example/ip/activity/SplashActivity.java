package com.example.ip.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.example.ip.R;
import com.example.ip.base.AppConstants;
import com.example.ip.base.BaseActivity;
import com.example.ip.base.BaseHandler;
import com.example.ip.service.BridgeService;
import com.example.ip.util.PermissionsChecker;

import butterknife.BindView;
import vstc2.nativecaller.NativeCaller;

public class SplashActivity extends BaseActivity implements BaseHandler.HandleMessage {

    @BindView(R.id.iv_splash)
    ImageView ivSplash;

    static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA

    };

    private PermissionsChecker mPermissionsChecker; // 权限检测器
    private static final int PERMISSION_REQUEST_CODE = 0; // 系统权限管理页面的参数
    private static final int REFRESH_COMPLETE = 0X153;

    private Handler handler = new BaseHandler<>(SplashActivity.this);

    @Override
    protected int getContentViewId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        AppConstants.displayWidth = getWindowManager().getDefaultDisplay().getWidth();
        AppConstants.displayHeight = getWindowManager().getDefaultDisplay().getHeight();
        mPermissionsChecker = new PermissionsChecker(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {

            if (mPermissionsChecker.lacksPermissions(PERMISSIONS)) {
                //请求权限
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

            } else {
                startAnim(); // 全部权限都已获取
            }
        } else {
            startAnim();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            startAnim();
        } else {
            handler.sendEmptyMessageDelayed(REFRESH_COMPLETE, 1000);
        }
    }

    /**
     * 启动动画
     */
    private void startAnim() {
        AlphaAnimation alpha = getAlphaAnimation();

        startThread();
        ivSplash.startAnimation(alpha);
    }

    @NonNull
    private AlphaAnimation getAlphaAnimation() {
        AnimationSet set = new AnimationSet(false); //设置动画集合；
        //缩放动画；
        ScaleAnimation scale = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f);
        scale.setDuration(2000);
        scale.setFillAfter(true);

        //淡入淡出动画；
        AlphaAnimation alpha = new AlphaAnimation(0, 1);
        alpha.setDuration(2000);
        alpha.setFillAfter(true);

        set.addAnimation(scale);
        set.addAnimation(alpha);
        alpha.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                jumpNextPage();
            }
        });
        return alpha;
    }

    private void startThread() {
        Intent intent = new Intent(SplashActivity.this, BridgeService.class);
        startService(intent);

        new Thread(new Runnable() {
            @Override
            public void run() {
                NativeCaller.PPPPInitialOther("ADCBBFAOPPJAHGJGBBGLFLAGDBJJHNJGGMBFBKHIBBNKOKLDHOBHCBOEHOKJJJKJBPMFLGCPPJMJAPDOIPNL");
            }
        }).start();
    }

    private void jumpNextPage() {
        startActivity(new Intent(this, SearchActivity.class));
        finish();
    }

    /**
     * 含有全部的权限
     *
     * @param grantResults
     * @return
     */
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case REFRESH_COMPLETE:
                finish();
                break;
        }
    }
}
