package com.ruziniu.phonelive.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.util.NetUtils;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.KSYStreamerConfig;
import com.ksyun.media.streamer.kit.RecorderConstants;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.util.audio.KSYBgmPlayer;
import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ShowLiveActivityBase;
import com.ruziniu.phonelive.bean.ChatBean;
import com.ruziniu.phonelive.bean.SendGiftBean;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.fragment.MusicPlayerDialogFragment;
import com.ruziniu.phonelive.fragment.SearchMusicDialogFragment;
import com.ruziniu.phonelive.interf.ChatServerInterface;
import com.ruziniu.phonelive.interf.DialogInterface;
import com.ruziniu.phonelive.ui.other.ChatServer;
import com.ruziniu.phonelive.ui.other.LiveStream;
import com.ruziniu.phonelive.utils.DialogHelp;
import com.ruziniu.phonelive.utils.InputMethodUtils;
import com.ruziniu.phonelive.utils.LiveUtils;
import com.ruziniu.phonelive.utils.TLog;
import com.ruziniu.phonelive.utils.ThreadManager;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.music.DefaultLrcBuilder;
import com.ruziniu.phonelive.widget.music.ILrcBuilder;
import com.ruziniu.phonelive.widget.music.LrcRow;
import com.ruziniu.phonelive.widget.music.LrcView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * 直播页面
 * 本页面包括点歌 分享 直播 聊天 僵尸粉丝 管理 点亮 歌词等功能详细参照每个方法的注释
 * 本页面继承基类和观看直播属于同一父类
 */
public class StartLiveActivity extends ShowLiveActivityBase implements  SearchMusicDialogFragment.SearchMusicFragmentInterface{

    //渲染视频
    @InjectView(R.id.camera_preview)
    GLSurfaceView mCameraPreview;

    //歌词显示控件
    @InjectView(R.id.lcv_live_start)
    LrcView mLrcView;

    //歌词显示控件
    @InjectView(R.id.rl_live_music)
    LinearLayout mViewShowLiveMusicLrc;

    private String stream;

    //直播结束映票数量
    private int mLiveEndYpNum;

    private Timer mTimer;

    private TimerTask mTask;

    //是否开启直播
    private boolean IS_START_LIVE = true;

    public LiveStream mStreamer;

    private final static String TAG = "StartLiveActivity";

    private boolean mBeauty = false;

    private int mPlayTimerDuration = 1000;

    private int pauseTime = 0;

    private MediaPlayer mPlayer;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_live_show;
    }

    @Override
    public void initView() {
        super.initView();

        //防止聊天软键盘挤压屏幕
        mRoot.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(bottom > oldBottom && InputMethodUtils.isShowSoft(StartLiveActivity.this)){
                    hideEditText();
                }
            }
        });


    }

    @Override
    public void initData() {
        super.initData();

        mUser = AppContext.getInstance().getLoginUser();
        mRoomNum = mUser.getId();
        mTvLiveNumber.setText("孺子牛号:" + mUser.getId());
        stream = getIntent().getStringExtra("stream");
        //连接聊天服务器
        initChatConnection();
        initLivePlay();
    }

    /**
     * @dw 初始化连接聊天服务器
     *
     * */
    private void initChatConnection() {
        //连接socket服务器
        try {
            mChatServer = new ChatServer(new ChatListenUIRefresh(),this,mUser.getId());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * @dw 初始化直播播放器
     * */
    private void initLivePlay() {

        //直播参数配置start

        KSYStreamerConfig.Builder builder = new KSYStreamerConfig.Builder();
        builder.setmUrl(AppConfig.RTMP_URL + stream + "?vhost=testruziniu.yunbaozhibo.com");
        builder.setFrontCameraMirror(true);
        mStreamer = new LiveStream(this);
        mStreamer.setConfig(builder.build());
        mStreamer.setDisplayPreview(mCameraPreview);

        mStreamer.setOnInfoListener(mOnInfoListener);
        mStreamer.setOnErrorListener(mOnErrorListener);
        //默认美颜关闭
        //mStreamer.setBeautyFilter(RecorderConstants.FILTER_BEAUTY_DENOISE);
        //直播参数配置end
        mEmceeHead.setAvatarUrl(mUser.getAvatar());
        startAnimation(3);
    }

    //开始直播
    private void startLiveStream() {
        if (IS_START_LIVE){
            /*mStreamer.startStream();
            mStreamer.setEnableReverb(true);
            mStreamer.setReverbLevel(4);*/
            //连接到socket服务端
            mChatServer.connectSocketServer(mUser, mUser.getId());
            //mLvChatList.setAdapter(mChatListAdapter);
        }
    }

    @OnClick({R.id.btn_live_sound,R.id.iv_live_emcee_head,R.id.tglbtn_danmu_setting,R.id.ll_live_room_info,R.id.btn_live_end_music,R.id.iv_live_music,R.id.iv_live_meiyan,R.id.iv_live_switch_camera,R.id.camera_preview,R.id.iv_live_privatechat,R.id.iv_live_back,R.id.ll_yp_labe,R.id.iv_live_chat,R.id.bt_send_chat})
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //音效
            case R.id.btn_live_sound:
                showSoundEffectsDialog();
                break;
            //展示主播信息弹窗
            case R.id.iv_live_emcee_head:
                showUserInfoDialog(mUser,mUser,mRoomNum);
                break;
            //展示主播信息弹窗
            case R.id.ll_live_room_info:
                showUserInfoDialog(mUser,mUser,mUser.getId());
                break;
            //展示点歌菜单
            case R.id.iv_live_music:
                showSearchMusicDialog();

                break;
            //美颜
            case R.id.iv_live_meiyan:
                if(!mBeauty){
                    mBeauty = true;
                    mStreamer.setBeautyFilter(RecorderConstants.FILTER_BEAUTY_DENOISE);
                }else{
                    mBeauty = false;
                    mStreamer.setBeautyFilter(RecorderConstants.FILTER_BEAUTY_DISABLE);
                }
                break;
            //摄像头反转
            case R.id.iv_live_switch_camera:
                mStreamer.switchCamera();
                break;
            //开启关闭弹幕
            case R.id.tglbtn_danmu_setting:
                mDanMuIsOpen = mDanMuIsOpen?false:true;
                mBtnDanMu.setBackgroundResource(mDanMuIsOpen?R.drawable.tuanmubutton1:R.drawable.tanmubutton);
                break;
            case R.id.camera_preview:
                hideEditText();
                break;
            //私信
            case R.id.iv_live_privatechat:
                showPrivateChat();
                break;
            case R.id.iv_live_back:
                clickBack();
                break;

            //映票排行榜
            case R.id.ll_yp_labe:
                showDedicateOrder();
                break;
            //聊天输入框
            case R.id.iv_live_chat://chat gone or visble
                showEditText();
                break;
            case R.id.bt_send_chat://send chat
                //sendChat();
                if(mDanMuIsOpen)
                {
                    sendBarrage();
                }else{
                    sendChat();
                }
                break;
            case R.id.iv_live_exit:
                finish();
                break;
            case R.id.btn_live_end_music:
                stopMusic();
                break;
        }
    }

    //音效调教菜单
    private void showSoundEffectsDialog() {
        MusicPlayerDialogFragment musicPlayerDialogFragment = new MusicPlayerDialogFragment();
        musicPlayerDialogFragment.show(getSupportFragmentManager(),"MusicPlayerDialogFragment");
    }


    //打开映票排行
    private void showDedicateOrder() {
        DialogHelp.showPromptDialog(getLayoutInflater(),StartLiveActivity.this,"正在直播点击排行会影响直播,是否继续",new DialogInterface(){

            @Override
            public void cancelDialog(View v, Dialog d) {
                d.dismiss();
            }

            @Override
            public void determineDialog(View v, Dialog d) {
                d.dismiss();
                UIHelper.showDedicateOrderActivity(StartLiveActivity.this, mUser.getId());
            }
        });
    }
    /**
     * @dw 当每个聊天被点击显示该用户详细信息弹窗
     * */
    public void chatListItemClick(ChatBean chat) {
        showUserInfoDialog(mUser,chat,mUser.getId());
    }
    /**
     * @dw 显示搜索音乐弹窗
     * */
    private void showSearchMusicDialog(){

        SearchMusicDialogFragment musicFragment = new SearchMusicDialogFragment();
        musicFragment.setStyle(SearchMusicDialogFragment.STYLE_NO_TITLE,0);
        musicFragment.show(getSupportFragmentManager(),"SearchMusicDialogFragment");
    }

    /**
     *@dw  @艾特用户
     *@param toUser 被@用户
     * */
    @Override
    public void dialogReply(UserBean toUser) {
        ACE_TEX_TO_USER = toUser.getId();
        mChatInput.setText("@" + toUser.getUser_nicename() + " ");
        mChatInput.setSelection(mChatInput.getText().length());
        showEditText();
    }

    //当主播选中了某一首歌,开始播放
    @Override
    public void onSelectMusic(Intent data) {
        startMusicStrem(data);
    }

    @Override
    protected void sendBarrageOnResponse(String response) {
        String s = ApiUtils.checkIsSuccess(response);
        if(s != null){
            try {

                JSONObject tokenJson = new JSONObject(s);
                mChatServer.doSendBarrage(tokenJson.getString("barragetoken"),mUser);
                mChatInput.setText("");
                mChatInput.setHint("开启大喇叭，1钻石/条");

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //socket客户端事件监听处理
    private class ChatListenUIRefresh implements ChatServerInterface {

        @Override
        public void onMessageListen(final int type,final ChatBean c) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if(type ==1){
                        addDanmu(c);
                    }else if(type == 2){
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
        //用户列表
        @Override
        public void onUserList(List<UserBean> uList, String votes) {//用户列表
            mUserList = uList;
            if(mRvUserList != null){
                mLiveNum.setText(ChatServer.LIVE_USER_NUMS + "观众");
                mYpNum.setText(votes);
                sortUserList();
            }
        }
        //用户状态改变
        @Override
        public void onUserStateChange(final UserBean user, final boolean state) {//用户状态改变
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onUserStatusChange(user,state);
                }
            });

        }
        //系统通知
        @Override
        public void onSystemNot(final int code) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(code == 1){//后台关闭直播
                        DialogHelp.showPromptDialog(getLayoutInflater(),StartLiveActivity.this,"直播内容涉嫌违规",new DialogInterface(){

                            @Override
                            public void cancelDialog(View v, Dialog d) {

                            }

                            @Override
                            public void determineDialog(View v, Dialog d) {
                                d.dismiss();
                            }
                        });
                        videoPlayerEnd();
                        showLiveEndDialog(mUser.getId(),mLiveEndYpNum);
                    }
                }
            });

        }
        //送礼物
        @Override
        public void onShowSendGift(final SendGiftBean giftInfo,final ChatBean chatBean) {//送礼物展示
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLiveEndYpNum += giftInfo.getTotalcoin();
                    showGiftInit(giftInfo);
                    addChatMessage(chatBean);
                }
            });

        }
        //设置管理员
        @Override
        public void setManage(final JSONObject contentJson,final ChatBean chat) {//设置为管理员
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if(contentJson.getInt("touid") == mUser.getId()){
                            //AppContext.showToastAppMsg(StartLiveActivity.this,"您已被设为管理员");
                        }
                        addChatMessage(chat);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });


        }
        //特权动作
        @Override
        public void onPrivilegeAction(final ChatBean c, final JSONObject contentJson) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
                    showLit(mRandom.nextInt(4));
                }
            });

        }

        //添加僵尸粉
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
                    AppContext.showToastAppMsg(StartLiveActivity.this,"服务器连接错误");
                }
            });
        }
    }

    //播放音乐
    private void startMusicStrem(Intent data) {

        //停止音乐
        mStreamer.stopMixMusic();

        mViewShowLiveMusicLrc.setVisibility(View.VISIBLE);


        //获取音乐路径
        String musicPath = data.getStringExtra("filepath");

        //获取歌词字符串
        String lrcStr = LiveUtils.getFromFile(musicPath.substring(0,musicPath.length() - 3) + "lrc");


        mStreamer.getAudioPlayerCapture().getBgmPlayer()
                .setOnCompletionListener(new KSYBgmPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(KSYBgmPlayer bgmPlayer) {
                        TLog.log("音乐初始化完毕");
                    }
                });
        mStreamer.getAudioPlayerCapture().getBgmPlayer()
                .setOnErrorListener(new KSYBgmPlayer.OnErrorListener() {
                    @Override
                    public void onError(KSYBgmPlayer bgmPlayer, int what, int extra) {
                        TLog.log("喝彩初始化失败");
                    }
                });


        mStreamer.getAudioPlayerCapture().getBgmPlayer().setVolume(1);
        mStreamer.startBgm(musicPath, true);
        mStreamer.setHeadsetPlugged(true);

        //插入耳机
        //mStreamer.setHeadsetPlugged(true)
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(musicPath);
            mPlayer.setLooping(true);
            mPlayer.setVolume(0,0);
            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {

                    mp.start();
                    if(mTimer == null){
                        mTimer = new Timer();
                        mTask = new LrcTask();
                        mTimer.scheduleAtFixedRate(mTask, 0, mPlayTimerDuration);
                    }
                }
            });
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    stopLrcPlay();
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(lrcStr);

        //设置歌词
        mLrcView.setLrc(rows);
    }

    //停止歌词滚动
    public void stopLrcPlay(){
        if(mTimer != null){
            mTimer.cancel();
            mTimer = null;
        }
    }
    //停止播放音乐
    private void stopMusic() {
        if(mPlayer != null && null != mStreamer){
            mStreamer.stopMixMusic();
            mPlayer.stop();
            mViewShowLiveMusicLrc.setVisibility(View.GONE);
        }
    }
    private class LrcTask extends TimerTask{

        long beginTime = -1;

        @Override
        public void run() {
            if(beginTime == -1) {
                beginTime = System.currentTimeMillis();
            }

            if(null != mPlayer){
                final long timePassed = mPlayer.getCurrentPosition();
                StartLiveActivity.this.runOnUiThread(new Runnable() {

                    public void run() {
                        if (mLrcView!=null) {
                            mLrcView.seekLrcToTime(timePassed);
                        }
                    }
                });
            }

        }
    };

    //返回键监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            if((!IS_START_LIVE)){
                return super.onKeyDown(keyCode,event);
            }else{
                clickBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode,event);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // 判断权限请求是否通过
                if((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) && (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED)){
                    showSoundEffectsDialog();
                }else if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast3("您拒绝写入文件权限,无法保存歌曲,请到设置中修改",0);
                }else if(grantResults.length > 0 && grantResults[1] != PackageManager.PERMISSION_GRANTED){
                    showToast3("您拒绝读取文件权限,无法读取歌曲,请到设置中修改",0);
                }
                break;
            }
        }
    }
    //主播点击退出
    private void clickBack(){

        DialogHelp.getConfirmDialog(this, getString(R.string.iscloselive), new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialogInterface, int i) {
                videoPlayerEnd();
                showLiveEndDialog(mUser.getId(),mLiveEndYpNum);
            }
        }).show();
    }
    //关闭直播
    private void videoPlayerEnd() {
        IS_START_LIVE = false;
        if(mGiftView != null){
            mRoot.removeView(mGiftView);
        }
        //请求接口改变直播状态
        PhoneLiveApi.closeLive(mUser.getId(), mUser.getToken(), new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                showToast3("关闭直播失败" ,0);
            }

            @Override
            public void onResponse(String response) {

            }
        });
        mChatServer.closeLive();
        //停止播放音乐
        stopMusic();
        //停止直播
        mStreamer.stopStream();

        ThreadManager.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                mStreamer.release();
            }
        });

        if(null != mHandler){
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
        //mLvChatList.setVisibility(View.GONE);
        mDanmuControl.hide();//关闭弹幕

        //mLiveChatEdit.setVisibility(View.VISIBLE);

        //mButtonMenuFrame.setVisibility(View.GONE);
        //mCameraPreview.setBackgroundResource(R.drawable.create_room_bg);
    }

    private KSYStreamer.OnInfoListener mOnInfoListener =  new KSYStreamer.OnInfoListener() {
        public void onInfo(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE:
                    TLog.log("初始化完成");
                    mStreamer.startStream();
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_STREAM_SUCCESS:
                    TLog.log("推流成功");
                    PhoneLiveApi.changeLiveState(String.valueOf(mUser.getId()),mUser.getToken(),stream,"1",null);
                    break;
                case StreamerConstants.KSY_STREAMER_FRAME_SEND_SLOW:

                    showToast3("网络状况不好",0);
                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_RAISE:

                    break;
                case StreamerConstants.KSY_STREAMER_EST_BW_DROP:

                    break;
                default:
                    TLog.log("OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                    break;
            }
        }
    };

    //直播错误监听
    private KSYStreamer.OnErrorListener mOnErrorListener = new KSYStreamer.OnErrorListener() {
        //
        @Override
        public void onError(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_ERROR_DNS_PARSE_FAILED:
                    //url域名解析失败
                    TLog.log( "url域名解析失败");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_FAILED:
                    //网络连接失败，无法建立连接
                    TLog.log("网络连接失败");
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_PUBLISH_FAILED:
                    //跟RTMP服务器完成握手后,向{streamname}推流失败)
                    TLog.log("跟RTMP服务器完成握手后,向{streamname}推流失败)");

                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_CONNECT_BREAKED:
                    //网络连接断开
                    TLog.log("网络连接断开");
                    if(!NetUtils.hasNetwork(StartLiveActivity.this)){
                        mStreamer.stopStream();
                        mStreamer.release();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(StartLiveActivity.this)
                                        .setTitle("提示")
                                        .setMessage("网络断开连接,请检查网络后重新开始直播")
                                        .setNegativeButton("确定", new android.content.DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(android.content.DialogInterface dialogInterface, int i) {
                                                videoPlayerEnd();
                                                showLiveEndDialog(mUser.getId(),mLiveEndYpNum);
                                            }
                                        })
                                        .create()
                                        .show();
                            }
                        });
                        return;

                    }
                    break;
                case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                    //音视频采集pts差值超过5s
                    TLog.log("KSY_STREAMER_ERROR_AV_ASYNC " + msg1 + "ms");
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                    //编码器初始化失败
                    TLog.log("编码器初始化失败");
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                    //视频编码失败
                    TLog.log("视频编码失败");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED:
                    //音频初始化失败
                    TLog.log("音频初始化失败");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN:
                    //音频编码失败
                    TLog.log("音频编码失败");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                    //录音开启失败
                    TLog.log("录音开启失败");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    //录音开启未知错误
                    TLog.log("录音开启未知错误");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                    //摄像头未知错误
                    TLog.log("摄像头未知错误");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                    //打开摄像头失败
                    TLog.log( "打开摄像头失败");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    //系统Camera服务进程退出
                    TLog.log("系统Camera服务进程退出");
                    break;
                default:

                    break;
            }

            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    mStreamer.stopCameraPreview();
                    if (mHandler!=null) {
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mStreamer.startCameraPreview();
                            }
                        }, 5000);
                    }
                    break;
                //重连
                default:
                    mStreamer.startStream();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mStreamer.startStream();
                        }
                    }, 3000);
                    break;
            }

        }
    };

    public static void startLiveActivity(Context context,String stream){
        Intent intent = new Intent(context,StartLiveActivity.class);
        intent.putExtra("stream",stream);
        context.startActivity(intent);
    }

    @Override
    public void onPause() {
        super.onPause();

        mStreamer.onPause();
        mStreamer.stopCameraPreview();
        mStreamer.stopStream();
        if(IS_START_LIVE && mHandler != null){
            mHandler.postDelayed(pauseRunnable,1000);
        }
        //提示
        mChatServer.doSendSystemMessage("主播暂时离开一下,马上回来!",mUser);

    }
    //定时10秒钟,如果主播未能如约而来,提示结束主播
    private Runnable pauseRunnable = new Runnable() {
        @Override
        public void run() {
            pauseTime ++ ;
            if(pauseTime >= 60){
                mHandler.removeCallbacks(this);
                videoPlayerEnd();
                return;
            }
            TLog.log(pauseTime + "定时器");
            mHandler.postDelayed(this,1000);
        }
    };

    public void onResume() {
        super.onResume();

        // 一般可以在onResume中开启摄像头预览
        mStreamer.startCameraPreview();
        // 调用KSYStreamer的onResume接口
        mStreamer.onResume();
        //重置时间,如果超过预期则关闭直播

        if(pauseTime  >=  60){
            showLiveEndDialog(mUser.getId(),mLiveEndYpNum);
        }else if(mHandler != null){
            mHandler.removeCallbacks(pauseRunnable);
        }
        pauseTime = 0;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mChatServer.close();
        if(mStreamer != null){
            mStreamer.release();
        }
        mChatServer = null;
        ButterKnife.reset(this);
        OkHttpUtils.getInstance().cancelTag("closeLive");
    }


    /**
     * @dw 开始直播倒数计时
     * @param num 倒数时间
     * */
    private void startAnimation(final int num){
        final TextView tvNum = new TextView(this);
        tvNum.setTextColor(getResources().getColor(R.color.white));
        tvNum.setText(num +"");
        tvNum.setTextSize(30);
        mRoot.addView(tvNum);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvNum.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvNum.setLayoutParams(params);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvNum,"scaleX",5f,1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvNum,"scaleY",5f,1f);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX,scaleY);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                if(mRoot == null)return;
                mRoot.removeView(tvNum);
                if(num == 1){
                    startLiveStream();
                    return;
                }
                startAnimation(num == 3?2:1);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.setDuration(1000);
        animatorSet.start();

    }
}
