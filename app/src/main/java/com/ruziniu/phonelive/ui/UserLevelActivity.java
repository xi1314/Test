package com.ruziniu.phonelive.ui;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;

import butterknife.InjectView;

/**
 * 等级
 */
public class UserLevelActivity extends ToolBarBaseActivity {
    @InjectView(R.id.wv_level)
    WebView mWbView;
    @InjectView(R.id.pb_loading)
    ProgressBar mProgressBar;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_level;
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {
        setActionBarTitle(getString(R.string.myleve));
        mProgressBar.setMax(100);
        mWbView.setWebChromeClient(new WebViewClient());
        WebSettings settings = mWbView.getSettings();
        settings.setJavaScriptEnabled(true);
        mWbView.loadUrl(AppConfig.MAIN_URL2 + "/index.php?g=user&m=level&a=index&uid="+getIntent().getStringExtra("USER_ID"));
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    private class WebViewClient extends WebChromeClient{
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            mProgressBar.setProgress(newProgress);
            if(newProgress == 100){
                mProgressBar.setVisibility(View.GONE);
            }
            super.onProgressChanged(view, newProgress);

        }
    }

    @Override
    protected void onDestroy() {
        mWbView.destroy();
        super.onDestroy();

    }

    public void onResume() {
        super.onResume();
    }
    public void onPause() {
        super.onPause();
    }

}
