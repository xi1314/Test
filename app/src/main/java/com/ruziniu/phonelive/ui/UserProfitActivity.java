package com.ruziniu.phonelive.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.ProfitBean;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 收益
 */
public class UserProfitActivity extends ToolBarBaseActivity {
    private TextView mTvTitle;
    @InjectView(R.id.iv_back)
    ImageView mIvBack;
    @InjectView(R.id.tv_text)
    TextView mSaveInfo;
    @InjectView(R.id.tv_votes)
    TextView mVotes;
    @InjectView(R.id.tv_profit_canwithdraw)
    TextView mCanwithDraw;
    @InjectView(R.id.tv_profit_withdraw)
    TextView mWithDraw;
    private ProfitBean mProfitBean;
    private UserBean mUser;




    @Override
    protected int getLayoutId() {
        return R.layout.activity_profit;
    }

    @Override
    public void initView() {
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void initData() {
        mUser = AppContext.getInstance().getLoginUser();
        mTvTitle.setText(getString(R.string.myprofit));
        mSaveInfo.setVisibility(View.GONE);//待开发功能提现记录
        mSaveInfo.setText("提现记录");

        Bundle bundle = getIntent().getBundleExtra("USERINFO");
        mVotes.setText(bundle.getString("votes"));
        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);

                if(null != res){
                    mProfitBean = new Gson().fromJson(res,ProfitBean.class);
                    fillUI();
                }
            }
        };
        PhoneLiveApi.getWithdraw(AppContext.getInstance().getLoginUid(), AppContext.getInstance().getToken(), callback);

    }

    private void fillUI() {
        mCanwithDraw.setText(mProfitBean.getCanwithdraw());
        mWithDraw.setText(mProfitBean.getWithdraw());
        mVotes.setText(mProfitBean.getVotes());
    }



    @OnClick({R.id.ll_profit_cash,R.id.TextView})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_profit_cash:
                PhoneLiveApi.requestCash(mUser.getId(),mUser.getToken(),
                        new StringCallback(){

                            @Override
                            public void onError(Call call, Exception e) {

                            }

                            @Override
                            public void onResponse(String response) {
                                String res = ApiUtils.checkIsSuccess(response);
                                if(null != res){
                                    AppContext.showToastAppMsg(UserProfitActivity.this,res);
                                    initData();
                                }
                            }
                        });
                break;

            case R.id.TextView:  //添加  zxy 2016-04-19
                UIHelper.showWebView(this, AppConfig.MAIN_URL2 + "/index.php?g=portal&m=page&a=newslist", "");
                break;
        }


    }
    @Override
    protected void initActionBar(ActionBar actionBar) {
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.view_actionbar_title);
        mTvTitle = (TextView) actionBar.getCustomView().findViewById(R.id.tv_actionBarTitle);

    }
    public void onResume() {
        super.onResume();
    }
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag("getWithdraw");
    }
}
