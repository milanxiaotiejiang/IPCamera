package com.example.ip.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SwitchCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.ip.R;
import com.example.ip.base.BaseActivity;
import com.example.ip.base.ContentCommon;
import com.example.ip.ipcamera.MyRender;
import com.example.ip.modle.Tele;
import com.example.ip.service.BridgeService;
import com.example.ip.util.GpsUtils;
import com.seabreeze.log.Print;

import butterknife.BindView;
import butterknife.OnClick;
import vstc2.nativecaller.NativeCaller;

public class TaskActivity extends BaseActivity implements
        BridgeService.AddCameraInterface,
        BridgeService.CallBackMessageInterface,
        BridgeService.IpcamClientInterface,
        BridgeService.PlayInterface
         {

    private static final String STR_MSG_PARAM = "msgparam";
    private static final String STR_DID = "did";

    @BindView(R.id.gl_surface_view)
    GLSurfaceView glSurfaceView;

    private MyRender myRender;

    private LocalBroadcastManager mLbmManager;
    private boolean isAccept;
    //数据视频流回掉
    private boolean bDisplayFinished = true;
    private byte[] mVideodata = null;
    public int nVideoWidths = 0;
    public int nVideoHeights = 0;
    public boolean isH264 = false;//是否是H264格式标志

    private Handler mDrawHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                NativeCaller.PPPPGetSystemParams(Tele.getInstance().getDid(), ContentCommon.MSG_TYPE_GET_CAMERA_PARAMS);
            }

            switch (msg.what) {
                case 1: // h264
                    myRender.writeSample(mVideodata, nVideoWidths, nVideoHeights);
                    break;
            }
            if (msg.what == 1) {
                bDisplayFinished = true;
            }
        }
    };

    private int tag = 0;

    private Handler PPPPMsgHandler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle bd = msg.getData();
            int msgParam = bd.getInt(STR_MSG_PARAM);
            int msgType = msg.what;
            String did = bd.getString(STR_DID);
            switch (msgType) {
                case ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS:
                    switch (msgParam) {
                        case ContentCommon.PPPP_STATUS_CONNECTING://0
                            Print.e(getString(R.string.pppp_status_connecting));
                            showToast(getString(R.string.pppp_status_connecting));
                            tag = 2;
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_FAILED://3
                            Print.e(getString(R.string.pppp_status_connect_failed));
                            showToast(getString(R.string.pppp_status_connect_failed));
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_DISCONNECT://4
                            Print.e(getString(R.string.pppp_status_disconnect));
                            showToast(getString(R.string.pppp_status_disconnect));
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_INITIALING://1
                            Print.e(getString(R.string.pppp_status_initialing));
                            showToast(getString(R.string.pppp_status_initialing));
                            tag = 2;
                            break;
                        case ContentCommon.PPPP_STATUS_INVALID_ID://5
                            Print.e(getString(R.string.pppp_status_invalid_id));
                            showToast(getString(R.string.pppp_status_invalid_id));
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_ON_LINE://2 在线状态
                            //摄像机在线之后读取摄像机类型
                            String cmd = "get_status.cgi?loginuse=admin&loginpas=" + Tele.getInstance().getPwd() + "&user=admin&pwd=" + Tele.getInstance().getPwd();
                            NativeCaller.TransferMessage(did, cmd, 1);
                            Print.e(getString(R.string.pppp_status_online));
                            showToast(getString(R.string.pppp_status_online));
                            priviewIpcamera();
                            tag = 1;
                            break;
                        case ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE://6
                            Print.e(getString(R.string.device_not_on_line));
                            showToast(getString(R.string.device_not_on_line));
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT://7
                            Print.e(getString(R.string.pppp_status_connect_timeout));
                            showToast(getString(R.string.pppp_status_connect_timeout));
                            tag = 0;
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_ERRER://8
                            Print.e(getString(R.string.pppp_status_pwd_error));
                            showToast(getString(R.string.pppp_status_pwd_error));
                            tag = 0;
                            break;
                        default:
                            Print.e(getString(R.string.pppp_status_unknown));
                            showToast(getString(R.string.pppp_status_unknown));
                    }
                    if (msgParam == ContentCommon.PPPP_STATUS_ON_LINE) {
                        NativeCaller.PPPPGetSystemParams(did, ContentCommon.MSG_TYPE_GET_PARAMS);
                    }
                    if (msgParam == ContentCommon.PPPP_STATUS_INVALID_ID
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_FAILED
                            || msgParam == ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT
                            || msgParam == ContentCommon.PPPP_STATUS_CONNECT_ERRER) {
                        NativeCaller.StopPPPP(did);
                    }
                    break;
                case ContentCommon.PPPP_MSG_TYPE_PPPP_MODE:
                    break;

            }

        }
    };


    @Override
    protected int getContentViewId() {
        return R.layout.activity_task;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

        mLbmManager = LocalBroadcastManager.getInstance(this);

        initCamera();

        myRender = new MyRender(glSurfaceView);
        glSurfaceView.setRenderer(myRender);

        if (!GpsUtils.isOPen(this)) {
            GpsUtils.openGPS(this);
        }
    }


    @Override
    protected void initData() {
        Tele.getInstance().setTele3();
    }

    @Override
    protected void setListener() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectIpcamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        NativeCaller.PPPPPTZControl(Tele.getInstance().getDid(), ContentCommon.CMD_PTZ_LEFT_RIGHT_STOP);
        NativeCaller.PPPPPTZControl(Tele.getInstance().getDid(), ContentCommon.CMD_PTZ_UP_DOWN_STOP);
    }

    @Override
    protected void onDestroy() {
        stopIpcamera();
        super.onDestroy();
    }


    /**
     * 初始化camera
     */
    private void initCamera() {
        BridgeService.setAddCameraInterface(this);
        BridgeService.setCallBackMessage(this);
        BridgeService.setIpcamClientInterface(this);
        NativeCaller.Init();
    }

    private void connectIpcamera() {
        Print.e("连接 " + Tele.getInstance().getPwd());
        new Thread(new StartPPPPThread(Tele.getInstance().getUser(), Tele.getInstance().getPwd(), Tele.getInstance().getDid())).start();
    }

    private void priviewIpcamera() {
        Print.e("预览相机");
        BridgeService.setPlayInterface(this);
        NativeCaller.StartPPPPLivestream(Tele.getInstance().getDid(), 10, 1);//确保不能重复start
        NativeCaller.PPPPGetSystemParams(Tele.getInstance().getDid(), ContentCommon.MSG_TYPE_GET_CAMERA_PARAMS);
    }

    private void stopIpcamera() {
        Print.e("stop相机");
        NativeCaller.StopPPPPLivestream(Tele.getInstance().getDid());
        NativeCaller.StopPPPP(Tele.getInstance().getDid());
    }


    @OnClick({})
    public void onClick(View view) {
        switch (view.getId()) {
//            case R.id.ib_hori_tour:
//                if (isLeftRight) {
//                    ibHoriTour.setBackgroundColor(getResources().getColor(R.color.task_deepblue));
//                    isLeftRight = false;
//                    NativeCaller.PPPPPTZControl(Tele.getInstance().getDid(), ContentCommon.CMD_PTZ_LEFT_RIGHT_STOP);
//                } else {
//                    ibHoriTour.setBackgroundColor(getResources().getColor(R.color.navajowhite));
//                    isLeftRight = true;
//                    NativeCaller.PPPPPTZControl(Tele.getInstance().getDid(), ContentCommon.CMD_PTZ_LEFT_RIGHT);
//                }
//                break;
//            case R.id.ib_vert_tour:
//                if (isUpDown) {
//                    ibVertTour.setBackgroundColor(getResources().getColor(R.color.task_deepblue));
//                    isUpDown = false;
//                    NativeCaller.PPPPPTZControl(Tele.getInstance().getDid(), ContentCommon.CMD_PTZ_UP_DOWN_STOP);
//                } else {
//                    ibVertTour.setBackgroundColor(getResources().getColor(R.color.navajowhite));
//                    isUpDown = true;
//                    NativeCaller.PPPPPTZControl(Tele.getInstance().getDid(), ContentCommon.CMD_PTZ_UP_DOWN);
//
//                }
//                break;
        }
    }


    //******************************相机连接后回调*******************
    @Override
    public void callBackCameraParamNotify(String did, int resolution, int brightness, int contrast, int hue, int saturation, int flip, int mode) {
//        Print.e("设备返回的参数" + resolution + "," + brightness + "," + contrast + "," + hue + "," + saturation + "," + flip + "," + mode);

    }


    @Override
    public void callBackVideoData(byte[] videobuf, int h264Data, int len, int width, int height) {
//        Print.e("callBackVideoData 视频数据流回调");
//        Print.e("底层返回数据" + "videobuf:" + videobuf + "--" + "h264Data" + h264Data + "len" + len + "width" + width + "height" + height);
        if (!bDisplayFinished)
            return;
        bDisplayFinished = false;

        mVideodata = videobuf;
        Message msg = new Message();
        if (h264Data == 1) { // H264

            nVideoWidths = width;//1920
            nVideoHeights = height;//1080
            isH264 = true;
            msg.what = 1;
        }
        mDrawHandler.sendMessage(msg);
    }

    @Override
    public void callBackMessageNotify(String did, int msgType, int param) {

        Print.e("连接通知 did: " + did + " msgType: " + msgType + " param: " + param);
    }

    @Override
    public void callBackAudioData(byte[] pcm, int len) {
        Print.e("callBackAudioData");
        Print.e("AudioData: len :+ " + len);
    }

    @Override
    public void callBackH264Data(byte[] h264, int type, int size) {
        Print.e("callBackH264Data");
        Print.e("CallBack_H264Data" + " type:" + type + " size:" + size);
    }


    //**************************连接*********************
    @Override
    public void BSMsgNotifyData(String did, int type, int param) {
        Print.e("连接状态回调... type : " + type);
        Bundle bd = new Bundle();
        Message msg = PPPPMsgHandler.obtainMessage();
        msg.what = type;
        bd.putInt(STR_MSG_PARAM, param);
        bd.putString(STR_DID, did);
        msg.setData(bd);
        PPPPMsgHandler.sendMessage(msg);
    }

    @Override
    public void BSSnapshotNotify(String did, byte[] bImage, int len) {
        Print.e("相机连接后 did : " + did);
    }

    @Override
    public void callBackUserParams(String did, String user1, String pwd1, String user2, String pwd2, String user3, String pwd3) {
        Print.e("did : " + did + " ,user1 : " + user1 + " ,pwd1 : " + pwd1 + " ,user2 : " + user2 + " ,pwd : " + pwd2 +
                " ,user3 : " + user3 + " ,pwd3 : " + pwd3);
    }

    @Override
    public void CameraStatus(String did, int status) {
        Print.e("相机连接状态 did ： " + did + " status : " + status);
    }

    @Override
    public void callBackSearchResultData(int cameraType, String strMac, String strName, String strDeviceID, String strIpAddr, int port) {
        Print.e("搜索出来的 strDeviceID : " + strDeviceID);
    }

    //*********************************setCallBackMessage***************
    @Override
    public void CallBackGetStatus(String did, String resultPbuf, int cmd) {
        Print.e("setCallBackMessage的回调 did : " + did + " 连接相机的信息参数 ");
    }


    class StartPPPPThread implements Runnable {

        private String mStrUser, mStrPwd, mStrDID;

        public StartPPPPThread(String strUser, String strPwd, String strDID) {
            this.mStrUser = strUser;
            this.mStrPwd = strPwd;
            this.mStrDID = strDID;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(100);

                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mStrDID.toLowerCase().startsWith("vsta")) {
                    NativeCaller.StartPPPPExt(mStrDID, mStrUser, mStrPwd, 1, "",
                            "EFGFFBBOKAIEGHJAEDHJFEEOHMNGDCNJCDFKAKHLEBJHKEKMCAFCDLLLHAOCJPPMBHMNOMCJKGJEBGGHJHIOMFBDNPKNFEGCEGCBGCALMFOHBCGMFK");
                } else {
                    NativeCaller.StartPPPP(mStrDID, mStrUser, mStrPwd, 1, "");
                }

            } catch (Exception e) {

            }
        }
    }

}
