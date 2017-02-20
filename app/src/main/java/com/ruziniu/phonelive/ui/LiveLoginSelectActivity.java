package com.ruziniu.phonelive.ui;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.LoginUtils;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import butterknife.InjectView;
import butterknife.OnClick;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.PlatformDb;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import okhttp3.Call;


//登录选择页面
public class LiveLoginSelectActivity extends ToolBarBaseActivity implements PlatformActionListener {
    private String[] names = {QQ.NAME,Wechat.NAME, SinaWeibo.NAME};
    private String type;
    @InjectView(R.id.iv_select_login_bg)
    ImageView mBg;
    private Bitmap bmp;

    @InjectView(R.id.tv_yinsitiaokuan)
    TextView mTiaoKusn;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_show_login;
    }


    @Override
    public void initView() {
        getSupportActionBar().hide();
        bmp = null;
        bmp = BitmapFactory.decodeResource(getResources(),R.drawable.ruziniulogin);
        mBg.setImageBitmap(bmp);
    }

    @Override
    public void initData() {
        mTiaoKusn.setOnClickListener(this);
    }

    @Override
    @OnClick({R.id.iv_qqlogin,R.id.iv_sllogin,R.id.iv_wxlogin,R.id.iv_mblogin})
    public void onClick(View v) {
        int id = v.getId();
        ShareSDK.initSDK(this);
        switch (id){
            case R.id.iv_qqlogin:
                type = "qq";
                otherLogin(names[0]);
                break;
            case R.id.iv_sllogin:
                type = "sina";
                AppContext.showToastAppMsg(this,"微博");
                otherLogin(names[2]);
                break;
            case R.id.iv_wxlogin:
                type = "wx";
                AppContext.showToastAppMsg(this,"微信");
                otherLogin(names[1]);
                break;
            case R.id.iv_mblogin:
                UIHelper.showMobilLogin(this);
                break;
            case R.id.tv_yinsitiaokuan:
                UIHelper.showWebView(this, AppConfig.MAIN_URL2 + "/index.php?g=portal&m=page&a=index&id=3","服务条款");
                break;
        }
    }
    private void otherLogin(String name){
        //AppContext.showToastAppMsg(LiveLoginSelectActivity.this,"正在准备登录..");
        Platform other = ShareSDK.getPlatform(name);
        other.showUser(null);//执行登录，登录后在回调里面获取用户资料
        other.SSOSetting(false);  //设置false表示使用SSO授权方式
        other.setPlatformActionListener(this);
        other.removeAccount(true);
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AppContext.showToastAppMsg(LiveLoginSelectActivity.this,"授权成功正在登录....");
            }
        });
        //用户资源都保存到res
        //通过打印res数据看看有哪些数据是你想要的
        if (i == Platform.ACTION_USER_INFOR) {
            //showWaitDialog("正在登录...");
            PlatformDb platDB = platform.getDb();//获取数平台数据DB
            //通过DB获取各种数据
            PhoneLiveApi.otherLogin(type,platDB,callback);
        }
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        //AppContext.showToastAppMsg(LiveLoginSelectActivity.this,"授权登录失败");
        String jsonObject = throwable.getMessage();
        try {
            JSONObject resJson = new JSONObject(jsonObject);
            String error = resJson.get("error").toString();
            AppContext.showToastAppMsg(LiveLoginSelectActivity.this,"授权登录失败"+error);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCancel(Platform platform, int i) {
        AppContext.showToastAppMsg(LiveLoginSelectActivity.this,"授权已取消");
    }
    StringCallback callback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {
            AppContext.showToastAppMsg(LiveLoginSelectActivity.this,"登录失败");
            //hideWaitDialog();
        }

        @Override
        public void onResponse(String response) {
            //hideWaitDialog();
            String requestRes = ApiUtils.checkIsSuccess(response);
            if(requestRes != null){
                Gson gson = new Gson();
                UserBean user = gson.fromJson(requestRes, UserBean.class);
                //保存用户信息
                AppContext.getInstance().saveUserInfo(user);

                LoginUtils.getInstance().OtherInit(LiveLoginSelectActivity.this);

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bmp!=null)
        bmp.recycle();
    }
    public void onResume() {
        super.onResume();       //统计时长
    }
    public void onPause() {
        super.onPause();
    }
}
