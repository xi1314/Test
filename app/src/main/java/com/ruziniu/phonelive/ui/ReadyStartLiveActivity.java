package com.ruziniu.phonelive.ui;

import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.utils.InputMethodUtils;
import com.ruziniu.phonelive.utils.ShareUtils;
import com.ruziniu.phonelive.utils.StringUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;

import butterknife.InjectView;
import butterknife.OnClick;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import okhttp3.Call;

public class ReadyStartLiveActivity extends ToolBarBaseActivity {
    //填写直播标题
    @InjectView(R.id.et_start_live_title)
    EditText mStartLiveTitle;

    //开始直播遮罩层
    @InjectView(R.id.rl_start_live_bg)
    RelativeLayout mStartLiveBg;

    //开始直播btn
    @InjectView(R.id.btn_start_live)
    Button mStartLive;

    //分享模式 7为不分享任何平台
    private int shareType = 7;

    private UserBean mUser;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ready_start_live;
    }

    @Override
    public void initView() {
//默认新浪微博share
        ImageView mSinnaWeiBoShare = (ImageView) findViewById(R.id.iv_live_share_weibo);
        mSinnaWeiBoShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveShare(v,0);
                shareType = 0 == shareType?7:0;
            }
        });
        findViewById(R.id.iv_live_share_timeline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveShare(v,2);
                shareType = 2 == shareType?7:2;
            }
        });
        findViewById(R.id.iv_live_share_wechat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveShare(v,1);
                shareType = 1 == shareType?7:1;
            }
        });

        findViewById(R.id.iv_live_share_qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveShare(v,3);
                shareType = 3 == shareType?7:3;
            }
        });
        findViewById(R.id.iv_live_share_qqzone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLiveShare(v,4);
                shareType = 4 == shareType?7:4;
            }
        });
    }

    @Override
    public void initData() {
         mUser = AppContext.getInstance().getLoginUser();
    }

    @OnClick({R.id.iv_live_exit,R.id.btn_start_live})
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_start_live://创建房间
                //请求服务端存储记录
                createRoom();
                break;
            case R.id.iv_live_exit:
                finish();
                break;
        }
    }
    /**
     * @dw 创建直播房间
     * 请求服务端添加直播记录,分享直播
     * */
    private void createRoom() {
        if(shareType != 7){
            ShareUtils.share(ReadyStartLiveActivity.this, shareType, mUser,
                    new PlatformActionListener() {
                        @Override
                        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
                            readyStart();
                        }

                        @Override
                        public void onError(Platform platform, int i, Throwable throwable) {
                            readyStart();
                        }

                        @Override
                        public void onCancel(Platform platform, int i) {
                            readyStart();
                        }
                    });

        }else {
            readyStart();
        }
        InputMethodUtils.closeSoftKeyboard(this);
        mStartLive.setEnabled(false);
        mStartLive.setTextColor(getResources().getColor(R.color.white));
    }
    /**
     * @dw 准备直播
     * */
    private void readyStart() {

        //拼接流地址
        final String stream = mUser.getId() + "_" + System.currentTimeMillis();
        //请求服务端
        PhoneLiveApi.createLive(mUser.getId(),stream, StringUtils.getNewString(mStartLiveTitle.getText().toString()),
                new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        AppContext.showToastAppMsg(ReadyStartLiveActivity.this,"开启直播失败,请退出重试- -!");
                    }

                    @Override
                    public void onResponse(String s) {
                        String res = ApiUtils.checkIsSuccess(s);
                        if(res != null){
                            StartLiveActivity.startLiveActivity(ReadyStartLiveActivity.this,stream);
                            finish();
                        }
                    }
                },mUser.getToken());
    }

    @Override
    protected boolean hasActionBar() {
        return false;
    }

    /**
     * @dw 开始直播分享
     * @param v 点击按钮
     * @param type 分享平台
     * */
    private void startLiveShare(View v,int type){
        String titleStr = "";
        if(type == shareType){
            String titlesClose[] = getResources().getStringArray(R.array.live_start_share_close);
            titleStr = titlesClose[type];
        }else {
            String titlesOpen[] = getResources().getStringArray(R.array.live_start_share_open);
            titleStr = titlesOpen[type];
        }

        View popView  =  getLayoutInflater().inflate(R.layout.pop_view_share_start_live,null);
        TextView title = (TextView) popView.findViewById(R.id.tv_pop_share_start_live_prompt);
        title.setText(titleStr);
        PopupWindow pop = new PopupWindow(popView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
        popView.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
        pop.setBackgroundDrawable(new BitmapDrawable());
        pop.setOutsideTouchable(true);
        int location[] = new int[2];
        v.getLocationOnScreen(location);
        pop.setFocusable(false);

        pop.showAtLocation(v, Gravity.NO_GRAVITY,location[0] + v.getWidth()/2 - popView.getMeasuredWidth()/2,location[1]- popView.getMeasuredHeight());

    }
}
