package com.example.ip.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.example.ip.R;
import com.example.ip.adapter.FullyLinearLayoutManager;
import com.example.ip.adapter.SearchAdapter;
import com.example.ip.base.BaseActivity;
import com.example.ip.base.ContentCommon;
import com.example.ip.modle.Tele;
import com.example.ip.service.BridgeService;
import com.seabreeze.log.Print;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import vstc2.nativecaller.NativeCaller;

/**
 * Created by zhangyuanyuan on 2017/11/22.
 */

public class SearchActivity extends BaseActivity implements BridgeService.AddCameraInterface, BridgeService.CallBackMessageInterface,
        BridgeService.IpcamClientInterface {

    private static final String STR_MSG_PARAM = "msgparam";
    private static final String STR_DID = "did";

    @BindView(R.id.done)
    Button done;
    @BindView(R.id.undone)
    Button undone;
    @BindView(R.id.play)
    Button play;
    @BindView(R.id.setting)
    Button setting;
    @BindView(R.id.btn_searchCamera)
    Button btnSearchCamera;
    @BindView(R.id.btn_linkcamera)
    Button btnLinkcamera;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private boolean isSearched;

    private List<String> divList;
    private SearchAdapter searchAdapter;

    private Handler updateListHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    isSearched = false;
                    searchAdapter.loadMoreData(divList);
                    showToast("搜索完毕");
                    break;
                case 2:
                    btnLinkcamera.setVisibility(View.VISIBLE);
                    break;
            }
        }
    };

    private int tag;

    private Handler PPPPMsgHandler = new Handler() {
        public void handleMessage(Message msg) {

            Bundle bd = msg.getData();
            int msgParam = bd.getInt(STR_MSG_PARAM);
            int msgType = msg.what;
            String did = bd.getString(STR_DID);
            switch (msgType) {
                case ContentCommon.PPPP_MSG_TYPE_PPPP_STATUS:
                    int resid;
                    switch (msgParam) {
                        case ContentCommon.PPPP_STATUS_CONNECTING://0
                            resid = R.string.pppp_status_connecting;
                            tag = 2;
                            showToast(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_FAILED://3
                            resid = R.string.pppp_status_connect_failed;
                            tag = 0;
                            showToast(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_DISCONNECT://4
                            resid = R.string.pppp_status_disconnect;
                            tag = 0;
                            showToast(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_INITIALING://1
                            resid = R.string.pppp_status_initialing;
                            tag = 2;
                            showToast(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_INVALID_ID://5
                            resid = R.string.pppp_status_invalid_id;
                            tag = 0;
                            showToast(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_ON_LINE://2 在线状态
                            resid = R.string.pppp_status_online;
                            //摄像机在线之后读取摄像机类型
                            String cmd = "get_status.cgi?loginuse=admin&loginpas=" + Tele.getInstance().getPwd() + "&user=admin&pwd=" + Tele.getInstance().getPwd();
                            NativeCaller.TransferMessage(did, cmd, 1);
                            tag = 1;
                            showToast(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_DEVICE_NOT_ON_LINE://6
                            resid = R.string.device_not_on_line;
                            tag = 0;
                            showToast(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_TIMEOUT://7
                            resid = R.string.pppp_status_connect_timeout;
                            tag = 0;
                            showToast(resid);
                            break;
                        case ContentCommon.PPPP_STATUS_CONNECT_ERRER://8
                            resid = R.string.pppp_status_pwd_error;
                            tag = 0;
                            showToast(resid);
                            break;
                        default:
                            resid = R.string.pppp_status_unknown;
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
        return R.layout.activity_search;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        BridgeService.setAddCameraInterface(this);
        BridgeService.setCallBackMessage(this);
    }

    @Override
    protected void initData() {
        Tele.getInstance().setTele3();
        divList = new ArrayList<>();
        setAdapter();
    }

    @Override
    protected void setListener() {

    }

    @Override
    protected void onStop() {
        super.onStop();
        NativeCaller.StopSearch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NativeCaller.Free();
        Intent intent = new Intent();
        intent.setClass(this, BridgeService.class);
        stopService(intent);
    }

    @OnClick({R.id.done, R.id.undone, R.id.play, R.id.setting, R.id.btn_searchCamera, R.id.btn_linkcamera})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.done:
                if (tag == 1) {
                    showToast("设备已经是在线状态了");
                } else if (tag == 2) {
                    showToast("设备不在线");
                } else {
                    done();
                }
                break;
            case R.id.undone:
                NativeCaller.StopPPPPLivestream(Tele.getInstance().getDid());
                NativeCaller.StopPPPP(Tele.getInstance().getDid());
                tag = 0;
                break;
            case R.id.play:
                Intent intent = new Intent(SearchActivity.this, TaskActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_searchCamera:
                if (!isSearched) {
                    divList.clear();
                    searchAdapter.clear();
                    isSearched = true;
                    showToast("开始搜索");
                    new Thread(new SearchThread()).start();
                    updateListHandler.postDelayed(updateThread, 5000);
                }
                break;
            case R.id.btn_linkcamera:
                Intent it = new Intent(SearchActivity.this, LinkCameraSettingActivity.class);
                startActivity(it);
                break;
            case R.id.setting:
                Intent intent1 = new Intent(SearchActivity.this, SettingActivity.class);
                startActivity(intent1);
                overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                break;
        }

    }

    private void setAdapter() {
        searchAdapter = new SearchAdapter(this, divList);
        recyclerView.setAdapter(searchAdapter);

        recyclerView.setLayoutManager(new FullyLinearLayoutManager(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void done() {
        BridgeService.setIpcamClientInterface(this);
        NativeCaller.Init();
        new Thread(new StartPPPPThread(Tele.getInstance().getUser(), Tele.getInstance().getPwd(), Tele.getInstance().getDid())).start();
    }

    @Override
    public void BSMsgNotifyData(String did, int type, int param) {
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

    }

    @Override
    public void callBackUserParams(String did, String user1, String pwd1, String user2, String pwd2, String user3, String pwd3) {

    }

    @Override
    public void CameraStatus(String did, int status) {

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


    private class SearchThread implements Runnable {
        @Override
        public void run() {
            NativeCaller.StartSearch();
        }
    }

    Runnable updateThread = new Runnable() {

        public void run() {
            NativeCaller.StopSearch();
            Message msg = updateListHandler.obtainMessage();
            msg.what = 1;
            updateListHandler.sendMessage(msg);
        }
    };

    //添加
    @Override
    public void callBackSearchResultData(int cameraType, String strMac, String strName, String strDeviceID, String strIpAddr, int port) {
        if (!divList.contains(strDeviceID)) {
            divList.add(strDeviceID);
        }
    }


    @Override
    public void CallBackGetStatus(String did, String resultPbuf, int cmd) {
        if (cmd == ContentCommon.CGI_IEGET_STATUS) {
            String cameraType = spitValue(resultPbuf, "upnp_status=");
            int intType = Integer.parseInt(cameraType);
            int type14 = (int) (intType >> 16) & 1;// 14位 来判断是否报警联动摄像机
            if (intType == 2147483647) {// 特殊值
                type14 = 0;
            }

            if (type14 == 1) {
                updateListHandler.sendEmptyMessage(2);
            } else {
                showToast("没有联动");
            }

        }
    }

    private String spitValue(String name, String tag) {
        String[] strs = name.split(";");
        for (int i = 0; i < strs.length; i++) {
            String str1 = strs[i].trim();
            if (str1.startsWith("var")) {
                str1 = str1.substring(4, str1.length());
            }
            if (str1.startsWith(tag)) {
                String result = str1.substring(str1.indexOf("=") + 1);
                return result;
            }
        }
        return -1 + "";
    }

}
