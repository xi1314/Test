package com.ruziniu.phonelive.ui;


import android.app.ActivityManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.GridViewAdapter;
import com.ruziniu.phonelive.adapter.ViewPageGridViewAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ShowLiveActivityBase;
import com.ruziniu.phonelive.bean.ChatBean;
import com.ruziniu.phonelive.bean.GiftBean;
import com.ruziniu.phonelive.bean.SendGiftBean;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.interf.ChatServerInterface;
import com.ruziniu.phonelive.ui.other.ChatServer;
import com.ruziniu.phonelive.utils.DialogHelp;
import com.ruziniu.phonelive.utils.QosThread;
import com.ruziniu.phonelive.utils.ShareUtils;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.TLog;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.LoadUrlImageView;
import com.ruziniu.phonelive.widget.VideoSurfaceView;
import com.tandong.bottomview.view.BottomView;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;

/*

* 直播播放页面
* */
public class VideoPlayerActivity extends ShowLiveActivityBase implements View.OnLayoutChangeListener {

    public final static String USER_INFO = "USER_INFO";
    @InjectView(R.id.video_view)
    VideoSurfaceView mVideoSurfaceView;

    @InjectView(R.id.view_live_content)
    RelativeLayout mLiveContent;

    //加载中的背景图
    @InjectView(R.id.iv_live_look_loading_bg)
    LoadUrlImageView mIvLoadingBg;

    private static final String TAG = "VideoPlayerActivity";

    public static final int UPDATE_QOS = 2;

    private KSYMediaPlayer ksyMediaPlayer;

    private QosThread mQosThread;

    private Surface mSurface = null;

    //private boolean mPause = false;

    //视频流宽度
    private int mVideoWidth = 0;

    //视频流高度
    private int mVideoHeight = 0;

    private List<GiftBean> mGiftList = new ArrayList<>();

    private ViewPageGridViewAdapter mVpGiftAdapter;

    //礼物view
    private ViewPager mVpGiftView;

    //礼物服务端返回数据
    private String mGiftResStr;

    //当前选中的礼物
    private GiftBean mSelectedGiftItem;

    //赠送礼物按钮
    private Button mSendGiftBtn;

    private int mShowGiftSendOutTime = 5;

    private RelativeLayout mSendGiftLian;

    private TextView mUserCoin;

    //主播信息
    private UserBean mEmceeInfo;

    private BottomView mGiftSelectView;

    //是否是禁言状态
    private boolean mIsShutUp = false;

    private long mLitLastTime = 0;

    private View mLoadingView;

    private String mrl;
    private float lastX1;
    private boolean isLeftOrRight;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_look;
    }

    @Override
    public void initView() {
        super.initView();
        mLiveChat.setVisibility(View.VISIBLE);
        //startLoadingAnimation();
        mVideoSurfaceView.addOnLayoutChangeListener(this);
        mRoot.addOnLayoutChangeListener(this);
        mLvChatList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hitLight();
                return true;
            }
        });
        findViewById(R.id.danmakuView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hitLight();
                return true;
            }
        });
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (v.getId() == R.id.video_view) {
            if (bottom != 0) {
                //防止聊天软键盘挤压屏幕导致视频变形
                //mVideoSurfaceView.setVideoDimension(mScreenWidth,mScreenHeight);
            }
        } else if (v.getId() == R.id.rl_live_root) {
            if (bottom > oldBottom) {
                //如果聊天窗口开启,收起软键盘时关闭聊天输入框
                hideEditText();
            }
        }
    }

    @Override
    public void initData() {
        super.initData();
        mGson = new Gson();
        Bundle bundle = getIntent().getBundleExtra(USER_INFO);
        //获取用户登陆信息
        mUser = AppContext.getInstance().getLoginUser();
        mEmceeInfo = (UserBean) bundle.getSerializable("USER_INFO");

        //设置背景图
        mIvLoadingBg.setNull_drawable(R.drawable.create_room_bg);
        mIvLoadingBg.setImageLoadUrl(mEmceeInfo.getAvatar());

        mRoomNum = mEmceeInfo.getId();
        mTvLiveNumber.setText("孺子牛号:" + mEmceeInfo.getId() + "");
        //初始化直播播放器参数配置
        initLive();
        mEmceeHead.setAvatarUrl(mEmceeInfo.getAvatar());
        getRoomInfo();
    }

    /**
     * @dw 获取房间信息
     */
    private void getRoomInfo() {

        //请求服务端获取房间基本信息
        PhoneLiveApi.initRoomInfo(AppContext.getInstance().getLoginUid()
                , mEmceeInfo.getId()
                , AppContext.getInstance().getToken()
                , AppContext.address
                , new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        AppContext.showToastAppMsg(VideoPlayerActivity.this, getString(R.string.initDataError));
                    }

                    @Override
                    public void onResponse(String s) {
                        String res = ApiUtils.checkIsSuccess(s);
                        if (res != null) {
                            UserBean u = mGson.fromJson(res, UserBean.class);
                            mUser.setCoin(u.getCoin());
                            mUser.setLevel(u.getLevel());
                            fillUI(res);
                        }
                    }
                });

        //禁言状态初始化
        PhoneLiveApi.isShutUp(mUser.getId(), mEmceeInfo.getId(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);
                if (null != res && res.equals("1")) {
                    mIsShutUp = true;
                }
            }
        });
        //获取礼物列表
        PhoneLiveApi.getGiftList(new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                AppContext.showToastAppMsg(VideoPlayerActivity.this, "获取礼物信息失败");
            }

            @Override
            public void onResponse(String s) {
                mGiftResStr = ApiUtils.checkIsSuccess(s);
            }
        });

    }


    private void initLive() {
        SurfaceHolder mSurfaceHolder = mVideoSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);
        mVideoSurfaceView.setOnTouchListener(mTouchListener);
        mVideoSurfaceView.setKeepScreenOn(true);
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);


        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        mQosThread = new QosThread(activityManager, mHandler);

        //视频流播放地址
        mrl = AppConfig.RTMP_URL2 + mEmceeInfo.getStream();
        //视频播放器init
        ksyMediaPlayer = new KSYMediaPlayer.Builder(this).build();
        ksyMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        ksyMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        ksyMediaPlayer.setOnPreparedListener(mOnPreparedListener);

        ksyMediaPlayer.setOnInfoListener(mOnInfoListener);
        ksyMediaPlayer.setOnVideoSizeChangedListener(mOnVideoSizeChangeListener);
        ksyMediaPlayer.setOnErrorListener(mOnErrorListener);
        //参数：screenOn 值为true时，播放时屏幕保持常亮，反之则否
        ksyMediaPlayer.setScreenOnWhilePlaying(true);
        //参数：直播音频缓存最大值，单位为秒
        ksyMediaPlayer.setBufferTimeMax(5);
        try {
            ksyMediaPlayer.setDataSource(mrl);
            ksyMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.iv_live_emcee_head, R.id.tglbtn_danmu_setting, R.id.iv_live_shar, R.id.iv_live_privatechat, R.id.iv_live_back, R.id.ll_yp_labe, R.id.ll_live_room_info, R.id.iv_live_chat, R.id.iv_live_look_loading_bg, R.id.bt_send_chat, R.id.iv_live_gift})
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_live_emcee_head:
                showUserInfoDialog(mUser, mEmceeInfo, mRoomNum);
                break;
            case R.id.iv_live_shar:
                ShareUtils.showSharePopWindow(this, v);
                break;
            //私信
            case R.id.iv_live_privatechat:
                showPrivateChat();
                break;
            //退出直播间
            case R.id.iv_live_back:
                finish();
                break;
            //票数排行榜
            case R.id.ll_yp_labe:
                UIHelper.showDedicateOrderActivity(this, mEmceeInfo.getId());
                break;
            //发言框
            case R.id.iv_live_chat:
                if (mIsShutUp) {
                    isShutUp();
                } else {
                    showEditText();
                }
                break;
            //开启关闭弹幕
            case R.id.tglbtn_danmu_setting:
                mDanMuIsOpen = mDanMuIsOpen ? false : true;
                mBtnDanMu.setBackgroundResource(mDanMuIsOpen ? R.drawable.tuanmubutton1 : R.drawable.tanmubutton);
                break;
            case R.id.bt_send_chat:
                //弹幕判断 HHH
                if (mDanMuIsOpen) {
                    sendBarrage();
                } else {
                    sendChat();
                }
                break;
            case R.id.iv_live_look_loading_bg:
                hideEditText();
                break;
            case R.id.iv_live_gift:
                showGiftList();
                break;
            case R.id.ll_live_room_info://左上角点击主播信息
                showUserInfoDialog(mUser, mEmceeInfo, mRoomNum);
                break;
        }
    }

    //分享操作
    public void share(View v) {
        ShareUtils.share(this, v.getId(), mEmceeInfo);
    }


    //连送按钮隐藏
    private void recoverySendGiftBtnLayout() {
        ((TextView) mSendGiftLian.findViewById(R.id.tv_show_gift_outtime)).setText("");
        mSendGiftLian.setVisibility(View.GONE);
        mSendGiftBtn.setVisibility(View.VISIBLE);
        mShowGiftSendOutTime = 5;
    }

    //展示礼物列表
    private void showGiftList() {
        if (mYpNum == null) {
            return;
        }
        mGiftSelectView = new BottomView(this, R.style.BottomViewTheme_Transparent, R.layout.view_show_viewpager);
        mGiftSelectView.setAnimation(R.style.BottomToTopAnim);
        mGiftSelectView.showBottomView(true);
        View mGiftView = mGiftSelectView.getView();
        mUserCoin = (TextView) mGiftView.findViewById(R.id.tv_show_select_user_coin);
        mUserCoin.setText(mUser.getCoin());
        //点击底部跳转充值页面
        mGiftView.findViewById(R.id.rl_show_gift_bottom).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("diamonds", mUser.getCoin());
                UIHelper.showMyDiamonds(VideoPlayerActivity.this, bundle);
            }
        });
        mVpGiftView = (ViewPager) mGiftView.findViewById(R.id.vp_gift_page);
        mSendGiftLian = (RelativeLayout) mGiftView.findViewById(R.id.iv_show_send_gift_lian);

        mSendGiftLian.setOnClickListener(new View.OnClickListener() {//礼物连送
            @Override
            public void onClick(View v) {
                sendGift("y");//礼物发送
                mShowGiftSendOutTime = 5;
                ((TextView) mSendGiftLian.findViewById(R.id.tv_show_gift_outtime)).setText(String.valueOf(mShowGiftSendOutTime));
            }
        });
        mSendGiftBtn = (Button) mGiftView.findViewById(R.id.btn_show_send_gift);
        mSendGiftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSendGift(v);
            }
        });
        if (mSelectedGiftItem != null) {
            mSendGiftBtn.setBackgroundColor(getResources().getColor(R.color.global));
        }
        //表示已经请求过数据不再向下执行
        if (mGiftViews != null) {
            fillGift();
            return;
        }

    }

    /**
     * @param v btn
     * @dw 点击赠送礼物按钮
     */
    private void onClickSendGift(View v) {//赠送礼物
        if (!mConnectionState) {//没有连接ok
            return;
        }
        if ((mSelectedGiftItem != null) && (mSelectedGiftItem.getType() == 1)) {//连送礼物
            v.setVisibility(View.GONE);
            if (mHandler == null) return;
            mHandler.postDelayed(new Runnable() {//开启连送定时器
                @Override
                public void run() {
                    if (mShowGiftSendOutTime == 1) {
                        recoverySendGiftBtnLayout();
                        mHandler.removeCallbacks(this);
                        return;
                    }
                    mHandler.postDelayed(this, 1000);
                    mShowGiftSendOutTime--;
                    ((TextView) mSendGiftLian.findViewById(R.id.tv_show_gift_outtime)).setText(String.valueOf(mShowGiftSendOutTime));

                }
            }, 1000);
            sendGift("y");//礼物发送
        } else {
            sendGift("n");//礼物发送
        }
    }

    //礼物列表填充
    private void fillGift() {
        if (null == mVpGiftAdapter && null != mGiftResStr) {
            if (mGiftList.size() == 0) {
                try {
                    JSONArray giftListJson = new JSONArray(mGiftResStr);
                    for (int i = 0; i < giftListJson.length(); i++) {
                        mGiftList.add(mGson.fromJson(giftListJson.getString(i), GiftBean.class));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //礼物item填充
            List<View> mGiftViewList = new ArrayList<>();
            int index = 0;

            for (int i = 0; i < 3; i++) {
                View v = getLayoutInflater().inflate(R.layout.view_show_gifts_gv, null);
                mGiftViewList.add(v);
                List<GiftBean> l = new ArrayList<>();
                for (int j = 0; j < 8; j++) {
                    if (index >= mGiftList.size()) {
                        break;
                    }
                    l.add(mGiftList.get(index));
                    index++;
                }
                mGiftViews.add((GridView) v.findViewById(R.id.gv_gift_list));
                mGiftViews.get(i).setAdapter(new GridViewAdapter(l));
                mGiftViews.get(i).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        giftItemClick(parent, view, position);
                    }
                });
            }
            mVpGiftAdapter = new ViewPageGridViewAdapter(mGiftViewList);
        }
        mVpGiftView.setAdapter(mVpGiftAdapter);
        //mVpGiftView.setCurrentItem(0);
    }

    //赠送礼物单项被选中
    private void giftItemClick(AdapterView<?> parent, View view, int position) {
        if ((GiftBean) parent.getItemAtPosition(position) != mSelectedGiftItem) {
            recoverySendGiftBtnLayout();
            mSelectedGiftItem = (GiftBean) parent.getItemAtPosition(position);
            //点击其他礼物
            changeSendGiftBtnStatue(true);
            for (int i = 0; i < mGiftViews.size(); i++) {
                for (int j = 0; j < mGiftViews.get(i).getChildCount(); j++) {
                    if (((GiftBean) mGiftViews.get(i).getItemAtPosition(j)).getType() == 1) {
                        mGiftViews.get(i).getChildAt(j).findViewById(R.id.iv_show_gift_selected).setBackgroundResource(R.drawable.icon_continue_gift);
                    } else {
                        mGiftViews.get(i).getChildAt(j).findViewById(R.id.iv_show_gift_selected).setBackgroundResource(0);
                    }

                }
            }
            view.findViewById(R.id.iv_show_gift_selected).setBackgroundResource(R.drawable.icon_continue_gift_chosen);

        } else {
            if (((GiftBean) parent.getItemAtPosition(position)).getType() == 1) {
                view.findViewById(R.id.iv_show_gift_selected).setBackgroundResource(R.drawable.icon_continue_gift);
            } else {
                view.findViewById(R.id.iv_show_gift_selected).setBackgroundResource(0);
            }
            mSelectedGiftItem = null;
            changeSendGiftBtnStatue(false);

        }
    }

    /**
     * @param statue 开启or关闭
     * @dw 赠送礼物按钮状态修改
     */
    private void changeSendGiftBtnStatue(boolean statue) {
        if (statue) {
            mSendGiftBtn.setBackgroundColor(getResources().getColor(R.color.global));
            mSendGiftBtn.setEnabled(true);
        } else {
            mSendGiftBtn.setBackgroundColor(getResources().getColor(R.color.light_gray2));
            mSendGiftBtn.setEnabled(false);
        }
    }

    /**
     * @param isOutTime 是否连送超时(如果是连送礼物的情况下)
     * @dw 赠送礼物, 请求服务端获取数据扣费
     */
    private void sendGift(final String isOutTime) {
        if (mSelectedGiftItem != null) {
            if (mSelectedGiftItem.getType() == 1) {
                mSendGiftLian.setVisibility(View.VISIBLE);
            } else {
                changeSendGiftBtnStatue(true);
            }
            StringCallback callback = new StringCallback() {
                @Override
                public void onError(Call call, Exception e) {
                    AppContext.showToastAppMsg(VideoPlayerActivity.this, getString(R.string.senderror));
                }

                @Override
                public void onResponse(String response) {
                    String s = ApiUtils.checkIsSuccess(response);
                    if (s != null) {
                        try {
                            ((TextView) mSendGiftLian.findViewById(R.id.tv_show_gift_outtime)).setText(String.valueOf(mShowGiftSendOutTime));
                            JSONObject tokenJson = new JSONObject(s);
                            //获取剩余金额,重新赋值
                            mUser.setCoin(tokenJson.getString("coin"));
                            mUserCoin.setText(mUser.getCoin());//重置余额
                            mUser.setLevel(tokenJson.getInt("level"));
                            mChatServer.doSendGift(tokenJson.getString("gifttoken"), mUser, isOutTime);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            PhoneLiveApi.sendGift(mUser, mSelectedGiftItem, mEmceeInfo.getId(), callback);
        }
    }

    //弹幕发送
    @Override
    protected void sendBarrageOnResponse(String response) {
        String s = ApiUtils.checkIsSuccess(response);
        if (s != null) {
            try {

                if (mSendGiftLian != null) {
                    //重置余额
                    ((TextView) mSendGiftLian.findViewById(R.id.tv_show_gift_outtime)).setText(String.valueOf(mShowGiftSendOutTime));
                    mUser.setCoin(String.valueOf(StringUtils.toInt(mUser.getCoin(), 0) - 1));
                    mUserCoin.setText(mUser.getCoin());
                } else {
                    //重置余额
                    mUser.setCoin(String.valueOf(StringUtils.toInt(mUser.getCoin(), 0) - 1));
                }
                JSONObject tokenJson = new JSONObject(s);

                mUser.setLevel(tokenJson.getInt("level"));

                mChatServer.doSendBarrage(tokenJson.getString("barragetoken"), mUser);
                mChatInput.setText("");
                mChatInput.setHint("开启大喇叭，1钻石/条");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //更新ui
    private void fillUI(String res) {

        //连接socket服务器
        try {
            mChatServer = new ChatServer(new ChatListenUIRefresh(), this, mEmceeInfo.getId());
            mChatServer.connectSocketServer(res, AppContext.getInstance().getToken(), mEmceeInfo.getId());//连接到socket服务端
            //请求僵尸粉丝
            mChatServer.getZombieFans();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            TLog.log("connect error");
        }
    }

    //socket客户端事件监听处理start
    private class ChatListenUIRefresh implements ChatServerInterface {

        @Override
        public void onMessageListen(final int type, final ChatBean c) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (type == 1) {
                        addDanmu(c);
                    } else if (type == 2) {
                        addChatMessage(c);
                    }
                }
            });
        }

        @Override
        public void onConnect(final boolean res) {
            //连接结果
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onConnectRes(res);
                }
            });
        }

        @Override
        public void onUserList(List<UserBean> uList, String votes) {//用户列表
            mUserList = uList;
            if (mRvUserList != null) {
                mLiveNum.setText(ChatServer.LIVE_USER_NUMS + "观众");
                mYpNum.setText(votes);
                sortUserList();
            }
        }

        //用户状态改变
        @Override
        public void onUserStateChange(final UserBean user, final boolean state) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onUserStatusChange(user, state);
                }
            });
        }

        //主播关闭直播
        @Override
        public void onSystemNot(final int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (code == 1) {
                        DialogHelp.getMessageDialog(VideoPlayerActivity.this, "直播内容涉嫌违规").show();
                    }
                    videoPlayerEnd();
                    showLiveEndDialog(mUser.getId(), 0);
                }
            });

        }

        //送礼物展示
        @Override
        public void onShowSendGift(final SendGiftBean giftInfo, final ChatBean chatBean) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showGiftInit(giftInfo);
                    addChatMessage(chatBean);
                }
            });

        }

        //设置为管理员
        @Override
        public void setManage(final JSONObject contentJson, final ChatBean chat) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (contentJson.getInt("touid") == mUser.getId()) {
                            //AppContext.showToastAppMsg(VideoPlayerActivity.this,"您已被设为管理员");
                        }
                        addChatMessage(chat);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        //特权操作
        @Override
        public void onPrivilegeAction(final ChatBean c, final JSONObject contentJson) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (contentJson.getInt("touid") == mUser.getId()) {
                            //AppContext.showToastAppMsg(VideoPlayerActivity.this,"您已被禁言");
                            mIsShutUp = true;
                            hideEditText();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    addChatMessage(c);
                }
            });
        }

        //点亮
        @Override
        public void onLit() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showLit(mRandom.nextInt(3));
                }
            });

        }

        //添加僵尸粉丝
        @Override
        public void onAddZombieFans(final String ct) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addZombieFans(ct);
                }
            });
        }

        //服务器连接错误
        @Override
        public void onError() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    AppContext.showToastAppMsg(VideoPlayerActivity.this, "服务器连接错误");
                }
            });
        }
    }

    //socket客户端事件监听处理end

    public void dialogReply(UserBean toUser) {
        if (mIsShutUp) {
            isShutUp();
        } else {
            ACE_TEX_TO_USER = toUser.getId();
            mChatInput.setText("@" + toUser.getUser_nicename() + " ");
            mChatInput.setSelection(mChatInput.getText().length());
            showEditText();
        }

    }


    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            //直播开始
            if (null != mLoadingView) {
                mRoot.removeView(mLoadingView);
                mLoadingView = null;
            }
            mIvLoadingBg.setVisibility(View.GONE);
            mIvLoadingBg = null;
            ksyMediaPlayer.start();
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            long duration = ksyMediaPlayer.getDuration();
            long progress = duration * percent / 100;

        }
    };

    private IMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangeListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
            if (mVideoWidth > 0 && mVideoHeight > 0) {
                if (width != mVideoWidth || height != mVideoHeight) {
                    mVideoWidth = mp.getVideoWidth();
                    mVideoHeight = mp.getVideoHeight();

                    // maybe we could call scaleVideoView here.
                    if (mVideoSurfaceView != null) {
                        mVideoSurfaceView.setVideoDimension(mVideoWidth, mVideoHeight);
                        mVideoSurfaceView.requestLayout();
                    }
                }
            }
        }
    };
    //视频播放完成
    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {

            // 重新链接
            ksyMediaPlayer.reload(mrl, true);
        }
    };

    //错误异常监听
    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int what, int extra) {
            switch (what) {
                case KSYMediaPlayer.MEDIA_ERROR_UNKNOWN:
                    Log.e(TAG, "OnErrorListener, Error Unknown:" + what + ",extra:" + extra);
                    break;
                default:
                    Log.e(TAG, "OnErrorListener, Error:" + what + ",extra:" + extra);
            }

            return false;
        }
    };

    public IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int info, int i1) {
            if(info == IMediaPlayer.MEDIA_INFO_RELOADED)
                //重连成功
                TLog.log("重连成功");

            if(info == IMediaPlayer.MEDIA_INFO_SUGGEST_RELOAD){
                //建议此时重连
                ksyMediaPlayer.reload(mrl, true);

            }
            return false;
        }
    };

    //直播结束释放资源
    private void videoPlayerEnd() {

        if (mGiftView != null) {
            mRoot.removeView(mGiftView);
        }
        if (mGiftSelectView != null) {
            mGiftSelectView.dismissBottomView();
        }

        mButtonMenuFrame.setVisibility(View.GONE);//隐藏菜单栏
        mLvChatList.setVisibility(View.GONE);
        //mVideoSurfaceView.setBackgroundResource(R.drawable.create_room_bg);

        if (mChatServer != null) {
            mChatServer.close();
            mChatServer = null;
        }

        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.release();
            ksyMediaPlayer = null;
        }
        if (null != mHandler) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        if (mQosThread != null) {
            mQosThread.stopThread();
            mQosThread = null;
        }

        mDanmuControl.hide();//关闭弹幕 HHH
    }


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            hideEditText();
            //dealTouchEvent(v, event);
            return false;
        }
    };


    //获取当前用户是否被禁言
    private void isShutUp() {
        if (mIsShutUp) {
            PhoneLiveApi.isShutUp(mUser.getId(), mEmceeInfo.getId(),
                    new StringCallback() {

                        @Override
                        public void onError(Call call, Exception e) {

                        }

                        @Override
                        public void onResponse(String response) {
                            String res = ApiUtils.checkIsSuccess(response);

                            if (res == null) return;
                            if (Integer.parseInt(res) == 0) {
                                mIsShutUp = false;
                                showEditText();
                            } else {
                                AppContext.showToastAppMsg(VideoPlayerActivity.this, "您已被禁言");
                            }
                        }
                    });
        }
    }

    private final SurfaceHolder.Callback mSurfaceCallback = new Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (ksyMediaPlayer != null) {
                final Surface newSurface = holder.getSurface();
                ksyMediaPlayer.setDisplay(holder);
                ksyMediaPlayer.setScreenOnWhilePlaying(true);
                //设置视频缩放模式
                ksyMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
                if (mSurface != newSurface) {
                    mSurface = newSurface;
                    ksyMediaPlayer.setSurface(mSurface);
                }
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed");
            if (ksyMediaPlayer != null) {
                mSurface = null;
            }
        }
    };

    /**
     * @dw 当每个聊天被点击显示该用户详细信息弹窗
     */
    public void chatListItemClick(ChatBean chat) {
        showUserInfoDialog(mUser, chat, mRoomNum);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //按下并且当前屏幕不是清屏状态下
        if (event.getAction() == MotionEvent.ACTION_DOWN && !(mLiveContent.getLeft() > 10)) {
            hitLight();
        }
        //屏幕侧滑隐藏页面功能
        float rowX = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX1 = rowX;
                break;
            case MotionEvent.ACTION_MOVE:
                float offsetX = rowX - lastX1;

                if (offsetX > 0) {
                    isLeftOrRight = false;
                } else {
                    isLeftOrRight = true;
                }

                if (Math.abs(offsetX) > 10) {
                    mLiveContent.setX(mLiveContent.getX() + offsetX);
                }
                lastX1 = rowX;
                break;
            case MotionEvent.ACTION_UP:
                if (isLeftOrRight) {
                    if (mLiveContent.getX() < mScreenWidth / 2) {//如果left>当前屏幕宽度/2 则将该内容view隐藏
                        mLiveContent.setX(0);
                    } else {
                        mLiveContent.setX(mScreenWidth);
                    }
                } else {
                    if (mLiveContent.getX() > mScreenWidth / 2) {//如果left>当前屏幕宽度/2 则将该内容view隐藏
                        mLiveContent.setX(mScreenWidth);
                    } else {
                        mLiveContent.setX(0);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    //点亮
    private void hitLight() {
        if (mChatServer != null) {
            int index = mRandom.nextInt(3);
            if (mLitLastTime == 0 || (System.currentTimeMillis() - mLitLastTime) > 500) {
                if (mLitLastTime == 0) {
                    //第一次点亮请求服务端纪录
                    PhoneLiveApi.showLit(mUser.getId(), mUser.getToken(), mEmceeInfo.getId());
                    mChatServer.doSendLitMsg(mUser, index);
                }
                mLitLastTime = System.currentTimeMillis();
                if (mChatServer != null) {
                    mChatServer.doSendLit(index);
                }

            } else {
                showLit(mRandom.nextInt(3));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();         //统计时长
        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.start();
            //mPause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();         //统计时长

        if (ksyMediaPlayer != null) {
            ksyMediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {//释放
        videoPlayerEnd();
        //解除广播
        super.onDestroy();
        ButterKnife.reset(this);
    }
}
