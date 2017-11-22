package com.example.ip.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.ip.R;
import com.example.ip.base.BaseActivity;
import com.example.ip.modle.Tele;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vstc2.nativecaller.NativeCaller;

/**
 * demo中只演示了部分功能，其他需要可参考sdk开发文档中的说明
 */
public class LinkCameraSettingActivity extends BaseActivity {


    @BindView(R.id.open_alarm)
    Button openAlarm;
    @BindView(R.id.close_alarm)
    Button closeAlarm;
    @BindView(R.id.get_sensor)
    Button getSensor;

    private String did;
    private String pwd;

    @Override
    protected int getContentViewId() {
        return R.layout.activity_link_camera_setting;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void initData() {
        did = Tele.getInstance().getDid();
        pwd = Tele.getInstance().getPwd();
    }

    @Override
    protected void setListener() {

    }

    @OnClick({R.id.open_alarm, R.id.close_alarm, R.id.get_sensor})
    public void onClick(View view){
        switch (view.getId()){
            case R.id.open_alarm:
                NativeCaller.TransferMessage(did,
                        "set_sensorstatus.cgi?cmd=0&loginuse=admin&loginpas=" + pwd
                                + "&user=admin&pwd=" + pwd, 1);
                break;
            case R.id.close_alarm:
                NativeCaller.TransferMessage(did,
                        "set_sensorstatus.cgi?cmd=1&loginuse=admin&loginpas=" + pwd
                                + "&user=admin&pwd=" + pwd, 1);
                break;
            case R.id.get_sensor:
                NativeCaller.TransferMessage(did,
                        "get_sensorlist.cgi?loginuse=admin&loginpas=" + pwd
                                + "&user=admin&pwd=" + pwd, 1);
                break;
        }
    }
}
