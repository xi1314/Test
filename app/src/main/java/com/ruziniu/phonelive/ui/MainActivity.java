package com.ruziniu.phonelive.ui;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.AppManager;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.em.MainTab;
import com.ruziniu.phonelive.interf.BaseViewInterface;
import com.ruziniu.phonelive.utils.LoginUtils;
import com.ruziniu.phonelive.utils.TLog;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.utils.UpdateManager;
import com.ruziniu.phonelive.widget.MyFragmentTabHost;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

import butterknife.InjectView;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.api.TagAliasCallback;
import okhttp3.Call;


//主页面
public class MainActivity extends ToolBarBaseActivity implements
        TabHost.OnTabChangeListener, BaseViewInterface,
        View.OnTouchListener {
    @InjectView(android.R.id.tabhost)
    MyFragmentTabHost mTabHost;

    public boolean isStartingLive = true;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        AppManager.getAppManager().addActivity(this);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        if (android.os.Build.VERSION.SDK_INT > 10) {
            mTabHost.getTabWidget().setShowDividers(0);
        }
        getSupportActionBar().hide();
        initTabs();


        mTabHost.setCurrentTab(100);
        mTabHost.setOnTabChangedListener(this);
        mTabHost.setNoTabChangedTag("2");


    }

    private void initTabs() {
        MainTab[] tabs = MainTab.values();
        final int size = tabs.length;
        String[] title = new String[]{"首页", "搜索", "", "私信", "我"};

        for (int i = 0; i < size; i++) {
            MainTab mainTab = tabs[i];

            TabHost.TabSpec tab = mTabHost.newTabSpec(String.valueOf(mainTab.getResName()));
            View indicator = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.tab_indicator, null);
            ImageView tabImg = (ImageView) indicator.findViewById(R.id.tab_img);
            TextView tabTv = (TextView) indicator.findViewById(R.id.tv_wenzi);
            Drawable drawable = this.getResources().getDrawable(
                    mainTab.getResIcon());

            tabTv.setText(title[i]);

            if (i == 2) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 17, 2, 0);
                tabImg.setLayoutParams(params);
            }
            tabImg.setImageDrawable(drawable);
            tab.setIndicator(indicator);
            tab.setContent(new TabHost.TabContentFactory() {

                @Override
                public View createTabContent(String tag) {
                    return new View(MainActivity.this);
                }
            });
            mTabHost.addTab(tab, mainTab.getClz(), null);
            mTabHost.getTabWidget().getChildAt(i).setOnTouchListener(this);
        }
    }

    @Override
    public void initData() {
        initAMap();
        //检查token是否过期
        checkTokenIsOutTime();
        //注册极光推送
        registerJpush();
        //登录环信
        loginIM();
        //检查是否有最新版本
        checkNewVersion();
        mTabHost.setCurrentTab(0);

        Bundle bundle = getIntent().getBundleExtra("USER_INFO");

        if (bundle != null) {
            UIHelper.showLookLiveActivity(this, bundle);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 5: {
                // 判断权限请求是否通过
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults.length > 0 && grantResults[2] != PackageManager.PERMISSION_GRANTED && grantResults.length > 0 && grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    checkCreateRoom();
                } else if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AppContext.showToast("您已拒绝使用摄像头权限,将无法正常直播,请去设置中修改");
                } else if (grantResults.length > 0 && grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    AppContext.showToast("您已拒绝使用录音权限,将无法正常直播,请去设置中修改");
                } else if (grantResults.length > 0 && grantResults[2] != PackageManager.PERMISSION_GRANTED || grantResults.length > 0 && grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    showToast3("您没有同意使用读写文件权限,无法正常直播,请去设置中修改", 0);
                } else if (grantResults.length > 0 && grantResults[3] != PackageManager.PERMISSION_GRANTED) {
                    showToast3("定位权限未打开", 0);
                }
                return;
            }
        }
    }

    //请求服务端开始直播
    private void requestStartLive() {
        if (isStartingLive) {
            isStartingLive = false;
            PhoneLiveApi.getLevelLimit(AppContext.getInstance().getLoginUid(), new StringCallback() {

                @Override
                public void onError(Call call, Exception e) {
                    AppContext.showToastAppMsg(MainActivity.this, "开始直播失败");
                    isStartingLive = true;
                }

                @Override
                public void onResponse(String response) {
                    String res = ApiUtils.checkIsSuccess(response);
                    if (null != res) {
                        UIHelper.showStartLiveActivity(MainActivity.this);
                    }
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        isStartingLive = true;
    }

    //登录环信即时聊天
    private void loginIM() {
        String uid = String.valueOf(AppContext.getInstance().getLoginUid());

        EMClient.getInstance().login(uid,
                "fmscms" + uid, new EMCallBack() {//回调
                    @Override
                    public void onSuccess() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                EMClient.getInstance().groupManager().loadAllGroups();
                                EMClient.getInstance().chatManager().loadAllConversations();
                                TLog.log("环信[登录聊天服务器成功]");
                            }
                        });
                    }

                    @Override
                    public void onProgress(int progress, String status) {

                    }

                    @Override
                    public void onError(int code, String message) {
                        if (204 == code) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AppContext.showToastAppMsg(MainActivity.this, "聊天服务器登录和失败,请重新登录");
                                }
                            });
                        }
                        TLog.log("环信[主页登录聊天服务器失败" + "code:" + code + "MESSAGE:" + message + "]");
                    }
                });


    }

    /**
     * @dw 注册极光推送
     */
    private void registerJpush() {
        JPushInterface.setAlias(this, AppContext.getInstance().getLoginUid() + "PUSH",
                new TagAliasCallback() {
                    @Override
                    public void gotResult(int i, String s, Set<String> set) {
                        TLog.log("极光推送注册[" + i + "I" + "S:-----" + s + "]");
                    }
                });

    }

    /**
     * @dw 检查token是否过期
     */
    private void checkTokenIsOutTime() {
        LoginUtils.tokenIsOutTime(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);

                if (null == res) return;
                if (res.equals(ApiUtils.TOKEN_TIMEOUT)) {
                    AppContext.showToastAppMsg(MainActivity.this, "登陆过期,请重新登陆");
                    UIHelper.showLoginSelectActivity(MainActivity.this);
                    return;
                }
            }
        });
    }

    /**
     * @dw 检查是否有最新版本
     */
    private void checkNewVersion() {
        UpdateManager manager = new UpdateManager(this, false);
        manager.checkUpdate();

    }

    @Override
    protected void onResume() {
        super.onResume();          //统计时长
        int i = mTabHost.getCurrentTab();
        if (i != 4) {
            mTabHost.setCurrentTab(0);
        }
    }

    public void onPause() {
        super.onPause();
    }

    //开始直播初始化
    public void startLive() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            //摄像头权限检测
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_COARSE_LOCATION},
                        5);
                return;
            }
        }
        checkCreateRoom();
    }

    private void checkCreateRoom() {
        PhoneLiveApi.checkcreateroom(AppContext.getInstance().getLoginUid(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                AppContext.showToastAppMsg(MainActivity.this, "开始直播失败");
            }

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject resJson = new JSONObject(response);

                    if (Integer.parseInt(resJson.getString("ret")) == 200) {
                        JSONObject dataJson = resJson.getJSONObject("data");
                        String code = dataJson.getString("code");
                        if (code.equals("100")) {
                            Toast.makeText(AppContext.getInstance(), dataJson.get("msg").toString(), Toast.LENGTH_SHORT).show();
                        } else {
                            requestStartLive();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onTabChanged(String tabId) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }


    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*RefWatcher refWatcher = AppContext.getRefWatcher(this);
        refWatcher.watch(this);*/
    }
}
