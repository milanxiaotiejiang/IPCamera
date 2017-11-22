package com.example.ip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ip.R;
import com.example.ip.base.BaseActivity;
import com.example.ip.modle.Tele;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.tv_camera_setting)
    TextView tvCameraSetting;
    @BindView(R.id.top)
    RelativeLayout top;
    @BindView(R.id.settingWifi)
    TextView settingWifi;
    @BindView(R.id.wifi_setting)
    RelativeLayout wifiSetting;
    @BindView(R.id.settingPwd)
    TextView settingPwd;
    @BindView(R.id.pwd_setting)
    RelativeLayout pwdSetting;
    @BindView(R.id.settingAlarm)
    TextView settingAlarm;
    @BindView(R.id.alarm_setting)
    RelativeLayout alarmSetting;
    @BindView(R.id.settingTime)
    TextView settingTime;
    @BindView(R.id.time_setting)
    RelativeLayout timeSetting;
    @BindView(R.id.settingSD)
    TextView settingSD;
    @BindView(R.id.sd_setting)
    RelativeLayout sdSetting;
    @BindView(R.id.updatefirmware)
    TextView updatefirmware;
    @BindView(R.id.update_firmware)
    RelativeLayout updateFirmware;
    @BindView(R.id.sensor_text)
    TextView sensorText;
    @BindView(R.id.setting_sensor)
    RelativeLayout settingSensor;
    @BindView(R.id.settingTF)
    TextView settingTF;
    @BindView(R.id.tf_setting)
    RelativeLayout tfSetting;
    @BindView(R.id.tv_move_inform)
    TextView tvMoveInform;
    @BindView(R.id.rl_move_inform)
    RelativeLayout rlMoveInform;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void initData() {
    }

    @Override
    protected void setListener() {

    }

    @OnClick({R.id.wifi_setting, R.id.pwd_setting, R.id.alarm_setting, R.id.time_setting, R.id.sd_setting,
            R.id.tf_setting, R.id.update_firmware, R.id.setting_sensor, R.id.rl_move_inform})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.wifi_setting:
                Intent intent1 = new Intent(SettingActivity.this, SettingWifiActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.pwd_setting:
                Intent intent2 = new Intent(SettingActivity.this, SettingUserActivity.class);
                startActivity(intent2);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.alarm_setting:
                Intent intent3 = new Intent(SettingActivity.this, SettingAlarmActivity.class);
                startActivity(intent3);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.time_setting:
                Intent intent4 = new Intent(SettingActivity.this, SettingDateActivity.class);
                startActivity(intent4);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.sd_setting:
                Intent intent5 = new Intent(SettingActivity.this, SettingSDCardActivity.class);
                startActivity(intent5);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.tf_setting:
                Intent intentVid = new Intent(SettingActivity.this, PlayBackTFActivity.class);
                startActivity(intentVid);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.update_firmware:
                Intent intentup = new Intent(SettingActivity.this, FirmwareUpdateActiviy.class);
                startActivity(intentup);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
            case R.id.setting_sensor:
                Intent intentsen = new Intent(SettingActivity.this, SensorListActivty.class);
                startActivity(intentsen);
                break;
            case R.id.rl_move_inform:
                Intent intentalam = new Intent(SettingActivity.this, MoveNotificationActivity.class);
                startActivity(intentalam);
                break;
        }
    }
}
