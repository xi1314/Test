package com.ruziniu.phonelive.ui;

import android.view.View;

import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.utils.TDevice;
import com.ruziniu.phonelive.utils.UIHelper;
import com.zhy.http.okhttp.OkHttpUtils;

import butterknife.OnClick;

/**
 * 设置
 */
public class SettingActivity extends ToolBarBaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        setActionBarTitle(getString(R.string.setting));
    }

    @OnClick({R.id.ll_clearCache,R.id.ll_push_manage,R.id.ll_about,R.id.ll_feedback,R.id.ll_blank_list})
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.ll_clearCache:
                clearCache();
                break;
            case R.id.ll_push_manage:
                UIHelper.showPushManage(this);
                break;
            case R.id.ll_about:
                UIHelper.showWebView(this,AppConfig.MAIN_URL2 + "/index.php?g=portal&m=page&a=lists","服务条款");
                break;
            //用户反馈
            case R.id.ll_feedback:
                String model = android.os.Build.MODEL;
                String release = android.os.Build.VERSION.RELEASE;
                UIHelper.showWebView(this, AppConfig.MAIN_URL2 + "/index.php?g=portal&m=page&a=newslist&uid="
                        + AppContext.getInstance().getLoginUid() + "&version=" + release + "&model=" + model,"");
                break;
            case R.id.ll_blank_list:
                UIHelper.showBlackList(SettingActivity.this);
                break;

        }
    }

    private void clearCache() {
        AppContext.showToastAppMsg(this,"缓存清理成功");
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }
    public void onResume() {
        super.onResume();
    }
    public void onPause() {
        super.onPause();
    }
}
