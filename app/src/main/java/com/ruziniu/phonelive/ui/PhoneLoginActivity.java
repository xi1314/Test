package com.ruziniu.phonelive.ui;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.LoginUtils;
import com.ruziniu.phonelive.utils.TDevice;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.callback.StringCallback;

import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 *手机登陆
 */
public class PhoneLoginActivity extends ToolBarBaseActivity {
    @InjectView(R.id.et_loginphone)
    EditText mEtUserPhone;
    @InjectView(R.id.et_logincode)
    EditText mEtCode;
    @InjectView(R.id.btn_phone_login_send_code)
    Button mBtnSendCode;
    private int waitTime = 30;


    private String mUserName = "";
    private String mPassword = "";
    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    public void initView() {
        mBtnSendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode();
            }
        });
    }

    private void sendCode() {
        mUserName = mEtUserPhone.getText().toString();
        if(!mUserName.equals("") && mUserName.length() == 11) {
            AppContext.showToastAppMsg(PhoneLoginActivity.this, getString(R.string.codehasbeensend));
            PhoneLiveApi.getMessageCode(mUserName);
            mBtnSendCode.setEnabled(false);
            mBtnSendCode.setTextColor(getResources().getColor(R.color.white));
            mBtnSendCode.setText("重新获取验证码(" + waitTime + ")");
            handler.postDelayed(runnable,1000);
        }
        else{
            AppContext.showToastAppMsg(PhoneLoginActivity.this, getString(R.string.plasecheckyounumiscorrect));
        }

    }


    @Override
    public void initData() {
        setActionBarTitle("使用手机号码登陆");
    }
    @OnClick(R.id.btn_dologin)
    @Override
    public void onClick(View v) {
        if(prepareForLogin()){
            return;
        }
        mUserName = mEtUserPhone.getText().toString();
        mPassword = mEtCode.getText().toString();
        showWaitDialog(R.string.loading);
        PhoneLiveApi.login(mUserName, mPassword,callback);
    }
    //登录回调
    private final StringCallback callback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {
            AppContext.showToast("网络请求出错!");
        }

        @Override
        public void onResponse(String s) {

           hideWaitDialog();
           String requestRes = ApiUtils.checkIsSuccess(s);
           if(requestRes != null){
               Gson gson = new Gson();
               UserBean user = gson.fromJson(requestRes, UserBean.class);

               handler.removeCallbacks(runnable);
               //保存用户信息
               AppContext.getInstance().saveUserInfo(user);

               LoginUtils.getInstance().OtherInit(PhoneLoginActivity.this);

           }
        }
    };

    private boolean prepareForLogin() {
        if (!TDevice.hasInternet()) {
            AppContext.showToastShort(R.string.tip_no_internet);
            return true;
        }
        if (mEtUserPhone.length() == 0) {
            mEtUserPhone.setError("请输入手机号码/用户名");
            mEtUserPhone.requestFocus();
            return true;
        }

        if (mEtCode.length() == 0) {
            mEtCode.setError("请输入验证码");
            mEtCode.requestFocus();
            return true;
        }

        return false;
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            waitTime -- ;
            mBtnSendCode.setText("重新获取验证码(" + waitTime + ")");
            if(waitTime == 0) {
                handler.removeCallbacks(runnable);
                mBtnSendCode.setText("发送验证码");
                mBtnSendCode.setEnabled(true);
                waitTime = 30;
                return;
            }
            handler.postDelayed(this,1000);

        }
    };

    @Override
    protected boolean hasBackButton() {
        return true;
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);
        }
    };
    public void onResume() {
        super.onResume();
    }
    public void onPause() {
        super.onPause();
    }
}
