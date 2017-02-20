package com.ruziniu.phonelive.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ruziniu.phonelive.utils.ImageUtils;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.utils.TLog;

import butterknife.InjectView;


//浏览器打开url
public class WebViewActivity extends ToolBarBaseActivity implements View.OnClickListener {
    @InjectView(R.id.iv_webview_back)
    ImageView mBack;
    @InjectView(R.id.tv_webview_title)
    TextView mTitle;
    private WebView wv = null;
    private String url;
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mUploadMessageForAndroid5;
    public final static int FILECHOOSER_RESULTCODE = 1;
    public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_webview;
    }

    @Override
    public void initView() {

        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE|WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        findViewById(R.id.toolbar_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(wv.getTitle().equals("提交成功")){
                    finish();
                    return;
                }
                if(wv.getTitle().equals("支付成功")){
                    finish();
                    return;
                }
                if(null != wv){
                    if(wv.canGoBack()){
                        wv.goBack();
                    }else {
                        finish();
                    }
                }
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != wv){
                    if(wv.canGoBack()){
                        wv.goBack();
                    }else {
                        finish();
                    }
                }
            }
        });

        wv = (WebView) this.findViewById(R.id.webView);
        WebSettings webSettings = wv.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(true);
        webSettings.setSupportZoom(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        //webSettings.setBuiltInZoomControls(true);
        WebChromeClient chromeClient = new WebChromeClient(){
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                mTitle.setText(title);
                setActionBarTitle(title);
            }

            //扩展浏览器上传文件
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                Log.d("1", "openFileChoose(ValueCallback<Uri> uploadMsg)");
                openFileChooserImpl(uploadMsg);
            }
            public void openFileChooser( ValueCallback uploadMsg, String acceptType ) {
                Log.d("1", "openFileChoose( ValueCallback uploadMsg, String acceptType )");
                openFileChooserImpl(uploadMsg);
            }
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
                Log.d("1", "openFileChoose(ValueCallback<Uri> uploadMsg, String acceptType, String capture)");
                openFileChooserImpl(uploadMsg);
            }

            // For Android > 5.0
            public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> uploadMsg, WebChromeClient.FileChooserParams fileChooserParams) {

                openFileChooserImplForAndroid5(uploadMsg);
                return true;
            }


        };


        wv.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(mTitle != null){
                    mTitle.setText(view.getTitle());
                }

                setActionBarTitle(view.getTitle());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if(url.startsWith("weixin://wap/pay/back")){
                    finish();
                    return true;
                }
                if(url.startsWith("weixin://wap/pay?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);

                    return true;
                }

                if (url.startsWith("tel:")) {
                    if (wv.canGoBack()) {
                        wv.goBack();
                    } else {
                        wv.clearHistory();
                        finish();
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return false;
                }

                if (url.startsWith("close:")) {
                    finish();
                    return false;
                }

                if (url.startsWith("login:")) {
                    if (wv.canGoBack()) {
                        wv.goBack();
                    } else {
                        wv.clearHistory();
                        finish();
                    }
                    return false;
                }
                view.loadUrl(url);
                return false;
            }
        });
        wv.setWebChromeClient(chromeClient);
    }
    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) {
        mUploadMessageForAndroid5 = uploadMsg;
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        contentSelectionIntent.setType("image/*");

        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");

        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE_FOR_ANDROID_5);
    }
    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {
        /*mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image*//*");
        startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);*/

        mUploadMessage = uploadMsg;
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image*//*");
        startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != RESULT_OK ? null: intent.getData();
            TLog.log("result1"+result+"...");
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5){
            if (null == mUploadMessageForAndroid5)
                return;
            Uri result = (intent == null || resultCode != RESULT_OK) ? null: intent.getData();
            TLog.log("result2"+result);
            if (result != null) {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{result});
            } else {
                mUploadMessageForAndroid5.onReceiveValue(new Uri[]{});
            }
            mUploadMessageForAndroid5 = null;
        }
    }

    @Override
    public void initData() {
        Bundle _Extras = getIntent().getBundleExtra("URL_INFO");
        url = _Extras.getString("url");
        wv.loadUrl(url);
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }

    @Override
    protected boolean hasActionBar() {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK  && wv.getTitle().equals("提交成功") || keyCode == KeyEvent.KEYCODE_BACK  && wv.getTitle().equals("支付成功")){
            finish();
            return true;
        }
        if(keyCode == KeyEvent.KEYCODE_BACK && wv.canGoBack()){
            wv.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == 16908332){
            if(wv.canGoBack()){
                wv.goBack();
                return true;
            }else {
                finish();
            }

        }
        return super.onOptionsItemSelected(item);
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
        wv.destroy();
    }
}
