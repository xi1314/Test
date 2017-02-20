package com.ruziniu.phonelive.base;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.adapter.ChatListAdapter;
import com.ruziniu.phonelive.adapter.UserListAdapter;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.bean.ChatBean;
import com.ruziniu.phonelive.bean.SendGiftBean;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.fragment.LiveEmceeEndFragmentDialog;
import com.ruziniu.phonelive.fragment.LiveEndFragmentDialog;
import com.ruziniu.phonelive.fragment.UserInfoDialogFragment;
import com.ruziniu.phonelive.interf.DialogInterface;
import com.ruziniu.phonelive.model.Danmu;
import com.ruziniu.phonelive.ui.other.ChatServer;
import com.ruziniu.phonelive.ui.other.PhoneLivePrivateChat;
import com.ruziniu.phonelive.utils.InputMethodUtils;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.TDevice;
import com.ruziniu.phonelive.utils.TLog;
import com.ruziniu.phonelive.utils.ThreadManager;
import com.ruziniu.phonelive.viewpagerfragment.PrivateChatCorePagerDialogFragment;
import com.ruziniu.phonelive.widget.AvatarView;
import com.ruziniu.phonelive.widget.StrokeTextView;
import com.ruziniu.phonelive.widget.danmu.DanmuControl;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.InjectView;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.IDanmakus;
import okhttp3.Call;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import com.ruziniu.phonelive.widget.SpaceRecycleView;

/**
 * @athor 魏鹏
 * @dw 直播间基类 本类主要封装了直播间豪华礼物动画 和一些公共逻辑
 */
public class ShowLiveActivityBase extends ToolBarBaseActivity {
    @InjectView(R.id.rl_live_root)
    protected RelativeLayout mRoot;


    //连送礼物动画显示区
    @InjectView(R.id.ll_show_gift_animator)
    protected LinearLayout mShowGiftAnimator;

    //观众数量
    @InjectView(R.id.tv_live_num)
    protected TextView mLiveNum;

    @InjectView(R.id.tv_yingpiao_num)
    protected TextView mYpNum;

    //聊天listview
    @InjectView(R.id.lv_live_room)
    protected ListView mLvChatList;

    //点击chat按钮
    @InjectView(R.id.iv_live_chat)
    protected ImageView mLiveChat;

    //左上角主播head
    @InjectView(R.id.iv_live_emcee_head)
    protected AvatarView mEmceeHead;

    //底部menu
    @InjectView(R.id.ll_bottom_menu)
    protected RelativeLayout mButtonMenu;

    @InjectView(R.id.fl_bottom_menu)
    protected FrameLayout mButtonMenuFrame;

    @InjectView(R.id.ll_click_lit)
    protected LinearLayout mClickHit;

    //chatInput
    @InjectView(R.id.ll_live_chat_edit)
    protected LinearLayout mLiveChatEdit;

    @InjectView(R.id.ll_yp_labe)
    protected LinearLayout mLiveLade;

    @InjectView(R.id.et_live_chat_input)
    protected EditText mChatInput;

    @InjectView(R.id.tv_live_number)
    protected TextView mTvLiveNumber;

    //观众列别listview
    @InjectView(R.id.hl_room_user_list)
    protected RecyclerView mRvUserList;

    @InjectView(R.id.iv_live_new_message)
    protected ImageView mIvNewPrivateChat;

    @InjectView(R.id.tglbtn_danmu_setting)
    protected Button mBtnDanMu;

    //弹幕控制 HHH
    protected DanmuControl mDanmuControl;
    @InjectView(R.id.danmakuView)
    protected IDanmakuView mDanmakuView;

    protected Gson mGson = new Gson();

    //当前正在显示的两个动画
    protected Map<Integer,View> mGiftShowNow = new HashMap<>();

    //礼物消息队列
    protected Map<Integer,SendGiftBean> mGiftShowQueue = new HashMap();

    //礼物队列
    protected List<SendGiftBean> mLuxuryGiftShowQueue = new ArrayList<>();

    //动画是否播放完毕
    protected boolean giftAnimationPlayEnd = true;

    protected int mShowGiftFirstUid = 0;

    protected int mShowGiftSecondUid = 0;

    //socket服务器连接状态
    protected boolean mConnectionState = false;

    //聊天adapter
    protected ChatListAdapter mChatListAdapter;

    //用户列表adapter
    protected UserListAdapter mUserListAdapter;

    //聊天list
    protected List<ChatBean> mChats = new ArrayList<>();

    //用户列表list
    protected List<UserBean> mUserList = new ArrayList<>();

    protected List<GridView> mGiftViews = new ArrayList<>();

    //socket
    public ChatServer mChatServer;

    protected UserBean mUser;

    protected int ACE_TEX_TO_USER = 0;

    protected Handler mHandler;

    //屏幕宽度
    protected int mScreenWidth;

    //屏幕高度
    protected int mScreenHeight;

    protected Random mRandom = new Random();

    //房间号码
    protected int mRoomNum;

    protected boolean mDanMuIsOpen = false;

    protected BroadcastReceiver broadCastReceiver;

    //礼物
    protected View mGiftView;


    @Override
    public void initData() {
        //屏幕常量
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mHandler = new Handler();
        mChatListAdapter = new ChatListAdapter(this);
        mLvChatList.setAdapter(mChatListAdapter);
        registerPrivateBroadcast();
    }

    //注册私信广播监听
    protected void registerPrivateBroadcast(){

        IntentFilter cmdFilter = new IntentFilter("com.ruziniu.phonelive");
        if(broadCastReceiver == null){
            broadCastReceiver = new BroadcastReceiver(){
                @Override
                public void onReceive(Context context, Intent intent) {
                    // TODO Auto-generated method stub
                    mIvNewPrivateChat.setVisibility(View.VISIBLE);
                }
            };
        }
        registerReceiver(broadCastReceiver,cmdFilter);
    }

    /**
     * @dw 显示私信页面
     * */
    protected void showPrivateChat() {
        //解除私信广播监听
        try {
            unregisterReceiver(broadCastReceiver);
        }catch (Exception e){

        }
        //隐藏新消息标记
        mIvNewPrivateChat.setVisibility(View.GONE);
        PrivateChatCorePagerDialogFragment privateChatFragment = new PrivateChatCorePagerDialogFragment();

        privateChatFragment.show(getSupportFragmentManager(),"PrivateChatCorePagerDialogFragment");
        privateChatFragment.setDialogInterface(new DialogInterface() {
            @Override
            public void cancelDialog(View v, Dialog d) {
                //弹窗关闭继续监听新消息
                registerPrivateBroadcast();

                //获取私信未读数量

                if(PhoneLivePrivateChat.getUnreadMsgsCount() > 0){
                    mIvNewPrivateChat.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void determineDialog(View v, Dialog d) {

            }
        });

    }

    @Override
    public void initView() {
        final LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(OrientationHelper.HORIZONTAL);
        mRvUserList.setLayoutManager(manager);
        //BBB设置每个item间距
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.space_5);
        mRvUserList.addItemDecoration(new SpaceRecycleView(spacingInPixels));
        mRvUserList.setAdapter(mUserListAdapter = new UserListAdapter(getLayoutInflater()));
        mScreenWidth = (int) TDevice.getScreenWidth();
        mScreenHeight = (int) TDevice.getScreenHeight();


        //聊天listview点击事件注册
        mLvChatList.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    hideEditText();
                }
                return false;
            }
        });
        //用户列表滑动到最后监听
        mRvUserList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                //滑动停止状态
                if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    int lastVisibleItem = manager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = manager.getItemCount();
                    if(lastVisibleItem == (totalItemCount - 1)){
                        synchronized (ShowLiveActivityBase.this){
                            loadMoreUserList();
                        }
                    }
                }
            }
        });
        //用户列表点击事件
        mUserListAdapter.setOnItemClickListener(new UserListAdapter.OnRecyclerViewItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //展示用户详细信息弹窗
                showUserInfoDialog(mUser,mUserList.get(position),mRoomNum);

            }
        });

        //弹幕控制器和视图关联 HHH
        mDanmuControl = new DanmuControl(this);
        mDanmuControl.setDanmakuView(mDanmakuView);
        //mDanmuControl.hide();

        //弹幕点击事件
        mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {
            @Override
            public void onDanmakuClick(BaseDanmaku latest) {

            }

            @Override
            public void onDanmakuClick(IDanmakus danmakus) {

            }
        });


    }
    //加载更多用户列表
    protected void loadMoreUserList(){
        PhoneLiveApi.loadMoreUserList(mUserList.size(),mRoomNum,new StringCallback(){

            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {
                String res = ApiUtils.checkIsSuccess(response);
                if(null != res){
                    try {
                        JSONObject jsonObj = new JSONObject(res);
                        JSONArray uListArray = jsonObj.getJSONArray("list");
                        //09.10 wp
                        if(uListArray.length() > 0){
                            mUserList.clear();
                        }
                        for(int i = 0; i < uListArray.length(); i++){
                            UserBean u = mGson.fromJson(uListArray.getString(i),UserBean.class);
                            mUserList.add(u);
                        }
                        ChatServer.LIVE_USER_NUMS = StringUtils.toInt(jsonObj.getString("nums"),0);
                        mLiveNum.setText(ChatServer.LIVE_USER_NUMS + "");
                        //按照等级重新排序
                        sortUserList();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    //发送聊天
    protected void sendChat() {
        String sendMsg = mChatInput.getText().toString();
        sendMsg = sendMsg.trim();
        if(mConnectionState && !sendMsg.equals("")){
            mChatInput.setText("");
            mChatServer.doSendMsg(sendMsg, mUser,ACE_TEX_TO_USER);
            //hideEditText();
        }
    }

    @Override
    protected boolean hasActionBar() {
        return false;
    }

    //进入显示礼物队列信息初始化
    protected View initShowEvenSentSendGift(SendGiftBean mSendGiftInfo){
        View view = getLayoutInflater().inflate(R.layout.item_show_gift_animator, null);
        if(mShowGiftFirstUid == 0){
            mShowGiftFirstUid = mSendGiftInfo.getUid();
        }else{
            mShowGiftSecondUid = mSendGiftInfo.getUid();
        }
        mGiftShowNow.put(mSendGiftInfo.getUid(), view);
        return view;
    }
    //定时检测当前显示礼物是否超时过期
    protected boolean timingDelGiftAnimation(int index){

        int id = index == 1 ? mShowGiftFirstUid:mShowGiftSecondUid;
        SendGiftBean mSendGiftInfo = mGiftShowQueue.get(id);
        if(mSendGiftInfo != null){
            long time = System.currentTimeMillis() - mSendGiftInfo.getSendTime();
            if((time > 4000) && (mShowGiftAnimator != null)){
                //超时 从礼物队列和显示队列中移除
                mShowGiftAnimator.removeView(mGiftShowNow.get(id));

                mGiftShowQueue.remove(id);

                mGiftShowNow.remove(id);
                if(index == 1){
                    mShowGiftFirstUid = 0;
                }else{
                    mShowGiftSecondUid = 0;
                }
                //从礼物队列中获取一条新的礼物信息进行显示
                if(mGiftShowQueue.size() != 0){

                    Iterator iterator = mGiftShowQueue.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry entry = (Map.Entry) iterator.next();
                        SendGiftBean sendGift = (SendGiftBean) entry.getValue();

                        if(mShowGiftFirstUid != sendGift.getUid() && mShowGiftSecondUid != sendGift.getUid()){//判断队列中的第一个礼物是否已经正在显示
                            showEvenSentGiftAnimation(initShowEvenSentSendGift(sendGift),sendGift,1);
                            break;
                        }
                    }
                }
                return false;
            }else{
                return true;
            }
        }
        return true;
    }


    /**
    * @dw 点亮
    * @param index 点亮的图片位置下标
    * */
    protected int showLit(final int index) {

        final ImageView heart = new ImageView(ShowLiveActivityBase.this);
        //点亮的背景图片

        heart.setBackgroundResource(ChatServer.heartImg[index]);
        //尺寸参数
        heart.setLayoutParams(new RelativeLayout.LayoutParams((int) TDevice.dpToPixel(30)
                ,(int) TDevice.dpToPixel(30)));
        //x位置
        heart.setX(mScreenWidth - mScreenWidth/3);
        //y位置
        heart.setY(mScreenHeight-200);
        mRoot.addView(heart);
        //点亮xy 平移动画
        ObjectAnimator translationX = ObjectAnimator.ofFloat(heart,"translationX",mRandom.nextInt(500)+(mScreenWidth-200) - (mScreenWidth/3));
        ObjectAnimator translationY = ObjectAnimator.ofFloat(heart,"translationY",mRandom.nextInt(mScreenHeight/2) + 200);
        //渐变动画
        ObjectAnimator alpha = ObjectAnimator.ofFloat(heart,"alpha",0f);
        //xy放大动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(heart,"scaleX",0.8f,1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(heart,"scaleY",0.8f,1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(translationX,translationY,alpha,scaleX,scaleY);
        set.setDuration(5000);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(null != mRoot ){
                    mRoot.removeView(heart);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.start();
        return index;

    }
    protected void switchPlayAnimation(SendGiftBean mSendGiftBean){
        switch (mSendGiftBean.getGiftid()){
            case 22: //烟花礼物
                showFireworksAnimation(mSendGiftBean);
                break;
            case 21: //游轮礼物
                showCruisesAnimation(mSendGiftBean);
                break;
            case 9: //红色小轿车
                showRedCarAnimation(mSendGiftBean);
                break;
            case 19: //飞机礼物
                showPlainAnimation(mSendGiftBean);
                break;
            default:
                //普通连送礼物
                showOrdinaryGiftInit(mSendGiftBean);
                break;
        }
    }
    /**
    * @dw 豪华礼物飞机动画
    * @param mSendGiftBean 礼物信息
    * */
    protected void showPlainAnimation(final SendGiftBean mSendGiftBean){
        if(!giftAnimationPlayEnd){
            return;
        }
        //飞机动画初始化
        giftAnimationPlayEnd = false;
        //撒花的颜色
        final int[] colorArr = new int[]{R.color.red,R.color.yellow,R.color.blue,R.color.green,R.color.orange,R.color.pink};

        mGiftView = getLayoutInflater().inflate(R.layout.view_live_gift_animation_plain,null);
        //用户头像
        AvatarView uHead = (AvatarView) mGiftView.findViewById(R.id.iv_animation_head);
        uHead.setAvatarUrl(mSendGiftBean.getAvatar());
        mRoot.addView(mGiftView);
        final RelativeLayout mRootAnimation = (RelativeLayout) mGiftView.findViewById(R.id.rl_animation_flower);


        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {

                ObjectAnimator plainAnimator = ObjectAnimator.ofFloat(mGiftView,"translationX",mScreenWidth,0);
                plainAnimator.setDuration(3000);
                plainAnimator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                       subscriber.onNext("");
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                plainAnimator.start();
            }
        });

        observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Random random = new Random();
                int num = random.nextInt(50) + 10;
                int width = mRootAnimation.getWidth();
                int height = mRootAnimation.getHeight();
                //获取花朵

                for(int i  = 0; i<num; i ++){
                    int color = random.nextInt(5);
                    int x  = random.nextInt(50);
                    final int tx = random.nextInt(width);
                    final int ty = random.nextInt(height);
                    TextView flower = new TextView(ShowLiveActivityBase.this);
                    flower.setX(x);
                    flower.setText("❀");
                    flower.setTextColor(getResources().getColor(colorArr[color]));
                    flower.setTextSize(50);
                    //每个花朵的动画
                    mRootAnimation.addView(flower);
                    flower.animate().alpha(0f).translationX(tx).translationY(ty).setDuration(5000).start();

                }
                if(mHandler == null) return;
                //飞机移动到中间后延迟一秒钟,开始继续前行-x
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        ObjectAnimator plainAnimator = ObjectAnimator.ofFloat(mGiftView,"translationX",-mGiftView.getWidth());
                        plainAnimator.setDuration(2000);
                        plainAnimator.addListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                if(null != mGiftView){
                                    if(null != mRoot){
                                        mRoot.removeView(mGiftView);
                                    }
                                    mLuxuryGiftShowQueue.remove(0);
                                    giftAnimationPlayEnd = true;
                                    if(mLuxuryGiftShowQueue.size() > 0 && mHandler != null){
                                        switchPlayAnimation(mLuxuryGiftShowQueue.get(0));
                                    }
                                }
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
                        plainAnimator.start();
                    }
                },4000);
            }
        });

    }
    /**
    * @dw 红色小轿车动画
    * @author 魏鹏
    * @param sendGiftBean 赠送的礼物信息
    * */
    protected void showRedCarAnimation(SendGiftBean sendGiftBean){
        if(!giftAnimationPlayEnd){
            return;
        }

        giftAnimationPlayEnd = false;
        //获取汽车动画布局
        mGiftView = getLayoutInflater().inflate(R.layout.view_live_gift_animation_car_general,null);
        AvatarView uHead = (AvatarView) mGiftView.findViewById(R.id.iv_animation_red_head);

        uHead.setAvatarUrl(sendGiftBean.getAvatar());
        //获取到汽车image控件
        final ImageView redCar = (ImageView) mGiftView.findViewById(R.id.iv_animation_red_car);
        //添加到当前页面
        mRoot.addView(mGiftView);

        final int vw = redCar.getLayoutParams().width;
        //动画第二次
        final Runnable carRunnable = new Runnable() {
            @Override
            public void run() {
                //小汽车切换帧动画开始继续移动向-x
                redCar.setImageResource(R.drawable.car_red1);
                mGiftView.animate().translationX(~vw)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                //小汽车从底部重新回来切换帧动画
                                redCar.setImageResource(R.drawable.car_10001);
                                ObjectAnimator oX = ObjectAnimator.ofFloat(mGiftView,"translationX",mScreenWidth,mScreenWidth/2-vw/2);
                                ObjectAnimator oY = ObjectAnimator.ofFloat(mGiftView,"translationY",mScreenHeight/2,mScreenHeight >> 2);

                                //重新初始化帧动画参数
                                AnimatorSet animatorSet = new AnimatorSet();
                                animatorSet.playTogether(oX,oY);
                                animatorSet.setDuration(2000);
                                animatorSet.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        //小汽车加速帧动画切换
                                        redCar.setImageResource(R.drawable.backcar1);
                                        mGiftView.animate().translationX(-vw).translationY(0)
                                                .setDuration(1000)
                                                .withEndAction(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //从汽车队列中移除,开始下一个汽车动画
                                                        if(mGiftView == null || mRoot == null)return;
                                                        mRoot.removeView(mGiftView);
                                                        mLuxuryGiftShowQueue.remove(0);
                                                        giftAnimationPlayEnd = true;
                                                        if(mLuxuryGiftShowQueue.size() > 0 && mHandler != null){
                                                            switchPlayAnimation(mLuxuryGiftShowQueue.get(0));
                                                        }
                                                    }
                                                })
                                                .start();

                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                                animatorSet.start();

                            }
                        })
                        .setDuration(1000).start();
            }
        };

        //汽车动画init
        ObjectAnimator ox = ObjectAnimator.ofFloat(mGiftView,"translationX",mScreenWidth + vw,mScreenWidth/2-vw/2);
        ox.setDuration(1500);
        ObjectAnimator oy = ObjectAnimator.ofFloat(mGiftView,"translationY",mScreenHeight >> 2);
        //设置背景帧动画
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ox,oy);
        animatorSet.setDuration(1500);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                //小汽车停在中间
                redCar.setImageResource(R.drawable.car_red6);
                if(mHandler == null) return;
                mHandler.postDelayed(carRunnable,500);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animatorSet.start();

    }
    /**
    * @dw 邮轮
    * @author 魏鹏
    * @param mSendGiftBean 赠送的礼物信息
    * */
    protected void showCruisesAnimation(SendGiftBean mSendGiftBean){
        if(!giftAnimationPlayEnd){
            return;
        }
        giftAnimationPlayEnd = false;
        mGiftView = getLayoutInflater().inflate(R.layout.view_live_gift_animation_cruises,null);

        //游轮上的用户头像
        AvatarView mUhead = (AvatarView) mGiftView.findViewById(R.id.live_cruises_uhead);
        ((TextView)mGiftView.findViewById(R.id.tv_live_cruises_uname)).setText(mSendGiftBean.getNicename());
        mUhead.setAvatarUrl(mSendGiftBean.getAvatar());

        mRoot.addView(mGiftView);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGiftView.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mGiftView.setLayoutParams(params);
        final RelativeLayout cruises = (RelativeLayout) mGiftView.findViewById(R.id.rl_live_cruises);

        //游轮开始平移动画
        cruises.animate().translationX(mScreenWidth/2 + mScreenWidth/3).translationY(120f).withEndAction(new Runnable() {
            @Override
            public void run() {
                if(mHandler == null) return;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //游轮移动到中间后重新开始移动
                        cruises.animate().translationX(mScreenWidth*2).translationY(200f).setDuration(3000)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        //结束后从队列移除开始下一个邮轮动画
                                        if(mRoot == null)return;
                                        mRoot.removeView(mGiftView);
                                        mLuxuryGiftShowQueue.remove(0);
                                        giftAnimationPlayEnd = true;
                                        if(mLuxuryGiftShowQueue.size() > 0 && mHandler != null){
                                            switchPlayAnimation(mLuxuryGiftShowQueue.get(0));
                                        }
                                    }
                                }).start();
                    }
                },2000);

            }
        }).setDuration(3000).start();

        //邮轮海水动画
        ImageView seaOne = (ImageView) mGiftView.findViewById(R.id.iv_live_water_one);
        ImageView seaTwo = (ImageView) mGiftView.findViewById(R.id.iv_live_water_one2);
        ObjectAnimator obj = ObjectAnimator.ofFloat(seaOne,"translationX",-50,50);
        obj.setDuration(1000);
        obj.setRepeatCount(-1);
        obj.setRepeatMode(ObjectAnimator.REVERSE);
        obj.start();
        ObjectAnimator obj2 = ObjectAnimator.ofFloat(seaTwo,"translationX",50,-50);
        obj2.setDuration(1000);
        obj2.setRepeatCount(-1);
        obj2.setRepeatMode(ObjectAnimator.REVERSE);
        obj2.start();
    }
    /**
     * @dw 烟花
     * @author 魏鹏
     * */
    protected void showFireworksAnimation(SendGiftBean mSendGiftBean){
        if(!giftAnimationPlayEnd){
            return;
        }
        giftAnimationPlayEnd = false;
        //初始化烟花动画
        final ImageView animation = new ImageView(this);
        RelativeLayout.LayoutParams pa = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pa.addRule(RelativeLayout.CENTER_IN_PARENT);
        animation.setLayoutParams(pa);
        animation.setImageResource(R.drawable.gift_fireworks_heart_animation);
        final AnimationDrawable an = (AnimationDrawable) animation.getDrawable();
        mRoot.addView(animation);
        ThreadManager.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                int duration = 0;
                for(int i = 0; i< an.getNumberOfFrames(); i++){
                    duration += an.getDuration(i);
                }
                an.start();
                if(mHandler == null) return;
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //删除当前礼物,开始队列下一个
                        if(mRoot == null)return;
                        mRoot.removeView(animation);
                        mLuxuryGiftShowQueue.remove(0);
                        giftAnimationPlayEnd = true;
                        if(mLuxuryGiftShowQueue.size() > 0 && mHandler != null){
                            switchPlayAnimation(mLuxuryGiftShowQueue.get(0));
                        }

                    }
                },duration);
            }
        });

    }
    /**
     * @dw 连送
     * @author 魏鹏
     * @param mShowGiftLayout 礼物显示View
     * @param gitInfo 赠送的礼物信息
     * @param num 赠送礼物的数量(无用)
     * */
    protected void showEvenSentGiftAnimation(final View mShowGiftLayout,final SendGiftBean gitInfo,int num) {
        final AvatarView mGiftIcon = (AvatarView) mShowGiftLayout.findViewById(R.id.av_gift_icon);
        final StrokeTextView mGiftNum = (StrokeTextView) mShowGiftLayout.findViewById(R.id.tv_show_gift_num);
        ((AvatarView) mShowGiftLayout.findViewById(R.id.av_gift_uhead)).setAvatarUrl(gitInfo.getAvatar());
        ((TextView) mShowGiftLayout.findViewById(R.id.tv_gift_uname)).setText(gitInfo.getNicename());
        ((TextView) mShowGiftLayout.findViewById(R.id.tv_gift_gname)).setText(gitInfo.getGiftname());
        mGiftIcon.setAvatarUrl(gitInfo.getGifticon());

        if(mShowGiftAnimator != null){
            mShowGiftAnimator.addView(mShowGiftLayout);//添加到动画区域显示效果
        }
        //动画开始平移
        ObjectAnimator oa1 = ObjectAnimator.ofFloat(mShowGiftLayout,"translationX",-340f,0f);
        oa1.setDuration(300);
        oa1.start();
        oa1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                showGiftNumAnimation(mGiftNum, gitInfo.getUid());
                //礼物图片平移动画
                ObjectAnimator giftIconAnimator = ObjectAnimator.ofFloat(mGiftIcon, "translationX", -40f, (mShowGiftLayout.getRight() - mGiftIcon.getWidth()*2));
                giftIconAnimator.setDuration(500);
                giftIconAnimator.start();
                //获取当前礼物是正在显示的哪一个
                final int index = mShowGiftFirstUid == gitInfo.getUid() ? 1 : 2;
                if (mHandler != null) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (timingDelGiftAnimation(index)) {
                                if (mHandler != null) {
                                    mHandler.postDelayed(this, 1000);
                                }
                            } else {
                                mHandler.removeCallbacks(this);
                            }

                        }
                    }, 1000);
                }

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    /**
    * @dw 礼物数量增加动画
    * @param tv 显示数量的TextView
    * @param uid 送礼物用户id,需要根据id取出队列中的礼物信息进行赠送时间重置
    * */
    protected void showGiftNumAnimation(TextView tv,int uid){
        tv.setText("X" + mGiftShowQueue.get(uid).getGiftcount());
        PropertyValuesHolder p1 = PropertyValuesHolder.ofFloat("scaleX",1f,0.2f,1f);
        PropertyValuesHolder p2 = PropertyValuesHolder.ofFloat("scaleY",1f,0.2f,1f);
        ObjectAnimator.ofPropertyValuesHolder(tv,p1,p2).setDuration(200).start();
        mGiftShowQueue.get(uid).setSendTime(System.currentTimeMillis());//重置发送时间
    }
    /**
    * @dw 连送礼物初始化
    * @param mSendGiftInfo 赠送礼物信息
    * */
    protected void showOrdinaryGiftInit(final SendGiftBean mSendGiftInfo){
        //礼物动画View
        View mShowGiftLayout = mGiftShowNow.get(mSendGiftInfo.getUid());
        //设置当前礼物赠送时间
        mSendGiftInfo.setSendTime(System.currentTimeMillis());
        boolean isShow = false;//是否刚刚加入正在显示队列
        boolean isFirst = false;//是否是第一次赠送礼物
        //是否是第一次送礼物,为空表示礼物队列中没有查询到该用户的送礼纪录
        if(mGiftShowQueue.get(mSendGiftInfo.getUid()) == null){
            mGiftShowQueue.put(mSendGiftInfo.getUid(),mSendGiftInfo);//加入礼物消息队列
            //将是否第一次送礼设为true
            isFirst = true;
        }
        //是否是新的礼物类型,对比两次礼物的id是否一致
        boolean isNewGift = !(mSendGiftInfo.getGiftid() == mGiftShowQueue.get(mSendGiftInfo.getUid()).getGiftid());
        //当前的正在显示礼物队列不够两条(最多两条),并且当前送礼物用户不在list中
        if((mGiftShowNow.size() < 2) && (mShowGiftLayout == null)){
            //初始化显示礼物布局和信息
            mShowGiftLayout = initShowEvenSentSendGift(mSendGiftInfo);
            isShow = true;
        }
        /*
        * mShowGiftLayout不等于null表示在正在显示的礼物队列中查询到了该用户送礼物纪录
        * 将是否正在显示isShow设置为true
        * */
        if(mShowGiftLayout != null){
            isShow = true;
        }
        /*
        * 如果是新礼物(表示礼物队列中存在送礼物纪录)
        * 存在就将最新礼物的icon和数量重置,并且覆盖older信息
        * */
        if(isNewGift){
            ((AvatarView)mShowGiftLayout.findViewById(R.id.av_gift_icon)).setAvatarUrl(mSendGiftInfo.getGifticon());
            ((StrokeTextView)mShowGiftLayout.findViewById(R.id.tv_show_gift_num)).setText("X1");
            ((TextView)mShowGiftLayout.findViewById(R.id.tv_gift_gname)).setText(mSendGiftInfo.getGiftname());
            //新礼物覆盖之前older礼物信息
            mGiftShowQueue.put(mSendGiftInfo.getUid(), mSendGiftInfo);
        }
        /*
        * 判断是否是连送礼物并且不是第一次赠送并且不是新礼物
        * 不是第一次赠送并且不是新礼物才需要添加数量(否则数量和礼物信息需要重置),
        * */
        if(mSendGiftInfo.getEvensend().equals("y")&&(!isFirst)&&(!isNewGift)){//判断当前礼物是否属于连送礼物
            //是连送礼物,将消息队列中的礼物赠送数量加1
            mGiftShowQueue.get(mSendGiftInfo.getUid()).setGiftcount(mGiftShowQueue.get(mSendGiftInfo.getUid()).getGiftcount() + 1);
        }
        //需要显示在屏幕上并且是第一次送礼物需要进行动画初始化
        if(isShow && isFirst){
            showEvenSentGiftAnimation(mShowGiftLayout,mSendGiftInfo,1);
        }else if(isShow && (!isNewGift)){//存在显示队列并且不是新礼物进行数量加一动画
            showGiftNumAnimation((StrokeTextView) mShowGiftLayout.findViewById(R.id.tv_show_gift_num), mSendGiftInfo.getUid());
        }
    }
    /**
    * @dw 赠送礼物进行初始化操作
    * 判断当前礼物是属于豪华礼物还是连送礼物,并且对映票进行累加
    * @param mSendGiftInfo 赠送礼物信息
    * */
    protected void showGiftInit(SendGiftBean mSendGiftInfo){
        //票数更新
        if(null!=mYpNum && !mYpNum.getText().toString().equals("") && null!=mSendGiftInfo){
            mYpNum.setText(String.valueOf(Integer.parseInt(mYpNum.getText().toString()) + (int)(mSendGiftInfo.getTotalcoin())));
        }else if(null!=mYpNum && mYpNum.getText().toString().equals("")){
            mYpNum.setText(String.valueOf((int)(mSendGiftInfo.getTotalcoin())));
        }
        //判断是要播放哪个豪华礼物
        int gId = mSendGiftInfo.getGiftid();
        if(gId == 19 || gId == 21 || gId == 22 || gId == 9 || gId == 19  ){
            mLuxuryGiftShowQueue.add(mSendGiftInfo);
        }
        switchPlayAnimation(mSendGiftInfo);
    }

    public void chatListItemClick(ChatBean chat){

    }

    //显示输入框
    protected void showEditText() {
        mChatInput.setFocusable(true);
        mChatInput.setFocusableInTouchMode(true);
        mChatInput.requestFocus();
        InputMethodUtils.toggleSoftKeyboardState(this);
        mLiveChatEdit.setVisibility(View.VISIBLE);
        mButtonMenu.setVisibility(View.GONE);
    }
    //隐藏输入框
    protected void hideEditText(){
        if(mLiveChatEdit.getVisibility() != View.GONE && InputMethodUtils.isShowSoft(this)){
            InputMethodUtils.closeSoftKeyboard(this);
            mButtonMenu.setVisibility(View.VISIBLE);
            mLiveChatEdit.setVisibility(View.GONE);
            ACE_TEX_TO_USER = 0;
        }
    }
    //添加一条聊天
    protected void addChatMessage(ChatBean c){
        if(mChats.size()>30)mChats.remove(0);
        mChats.add(c);
        mChatListAdapter.setChats(mChats);
        mChatListAdapter.notifyDataSetChanged();
        //HHH 2016-09-08 Null指针退出
        if(mLvChatList!=null)
        {
            mLvChatList.setSelection(mChats.size() - 1);
        }

    }


    //添加弹幕 HHH

    protected void addDanmu(final  ChatBean c)  {

        final SoftReference<ChatBean>  refChatBean= new SoftReference<ChatBean>(c);
        final String danmuStr =new StringBuilder() .append(" ") .append(c.getUser_nicename()).append(":") .append("\n               ") .append(c.getContent()).toString();


        Glide.with(this).load(c.getAvatar()).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Danmu danmu = new Danmu(0, c.getId(), "Comment", resource, danmuStr,refChatBean);
                mDanmuControl.addDanmu(danmu,0);

            }
            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                SoftReference<ChatBean>  refChatBean= new SoftReference<ChatBean>(c);
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_nouser);
                Danmu danmu = new Danmu(0, c.getId(), "Comment", bitmap,  danmuStr,refChatBean);
                mDanmuControl.addDanmu(danmu,0);
            }
        }); //方法中设置asBitmap可以设置回调类型

    }

    //用户信息弹窗
    protected void showUserInfoDialog(UserBean mUserInfo,UserBean toUserInfo,int roomNum){
        UserInfoDialogFragment u = new UserInfoDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable("MYUSERINFO",mUserInfo);
        b.putSerializable("TOUSERINFO",toUserInfo);
        b.putInt("ROOMNUM",roomNum);
        u.setArguments(b);
        u.show(getSupportFragmentManager(),"UserInfoDialogFragment");
    }

    public void dialogReply(UserBean toUser){

    }
    //僵尸粉丝
    protected void addZombieFans(String zombies){
        String fans = ApiUtils.checkIsSuccess(zombies);
        if(null != fans){
            try {
                //设置在线用户数量
                JSONObject jsonInfoObj = new JSONObject(fans);
                JSONArray fansJsonArray = jsonInfoObj.getJSONArray("list");

                if(!(mUserList.size() >= 20) && fansJsonArray.length() > 0){
                    for(int i = 0; i < fansJsonArray.length() ; i++){
                        UserBean u = mGson.fromJson(fansJsonArray.getString(i),UserBean.class);
                        mUserList.add(u);
                    }
                    sortUserList();
                }
                //在线人数统计
                if(fansJsonArray.length() > 0){
                    ChatServer.LIVE_USER_NUMS = StringUtils.toInt(jsonInfoObj.getString("nums"),0);
                    mLiveNum.setText( ChatServer.LIVE_USER_NUMS + "");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    //用户列表排序
    protected void sortUserList(){

        for(int i = 0; i < mUserList.size() - 1; i++){
            for(int j = 0 ; j < mUserList.size() - 1 -i; j++){

                //判断等级大小进行排序
                if(mUserList.get(j).getLevel() < mUserList.get(j + 1).getLevel()){
                    UserBean temp = mUserList.get(j);
                    mUserList.set(j,mUserList.get(j+1));
                    mUserList.set(j + 1,temp);
                }
            }
        }
        mUserListAdapter.setUserList(mUserList);
    }
    //当用户状态改变
    protected void onUserStatusChange(UserBean user,boolean state){
        //设置在线人数
        mLiveNum.setText(String.valueOf(ChatServer.LIVE_USER_NUMS)==null?"0":String.valueOf(ChatServer.LIVE_USER_NUMS));

        for(int i = 0; i< mUserList.size(); i++){
            if(user.getId() == mUserList.get(i).getId()){
                mUserList.remove(i);
                break;
            }
        }
        if(state && !mUserList.contains(user)){//用户上线
            //判断该用户是否存在列表中
            mUserList.add(user);
            TLog.log("加入不存在" +user.getId());
            TLog.log("加入" +user.getId());
        }else{

            TLog.log("离开" +user.getId());
        }
        //列表重新排序
        sortUserList();
    }
    //连接结果
    public void onConnectRes(boolean res){
        if(res){
            mConnectionState = true;
            //开启定时发送心跳包
            mChatServer.heartbeat(mHandler);
            AppContext.showToastAppMsg(this,"连接成功");

            //请求僵尸粉丝
            mChatServer.getZombieFans();
        }else {
            AppContext.showToastAppMsg(this,"连接失败");
        }
    }
    //直播结束弹窗遮罩
    protected void showLiveEndDialog(int uid,int liveEndYpNum){
        if(uid != mRoomNum){
            LiveEndFragmentDialog liveEndFragmentDialog = new LiveEndFragmentDialog();
            Bundle bundle = new Bundle();
            bundle.putInt("roomnum",mRoomNum);
            liveEndFragmentDialog.setArguments(bundle);
            liveEndFragmentDialog.show(getSupportFragmentManager(),"liveEndFragmentDialog");

        }else{
            LiveEmceeEndFragmentDialog dialog = new LiveEmceeEndFragmentDialog();
            Bundle bundle = new Bundle();
            bundle.putString("ypNum",ChatServer.LIVE_USER_NUMS + "人观看");
            bundle.putString("liveNum","共收获:" + liveEndYpNum + "牛丸");
            dialog.setArguments(bundle);
            dialog.show(getSupportFragmentManager(),"LiveEmceeEndFragmentDialog");
        }

    }

    protected BitmapDrawable getResourcesImage(int resourcesId){
        BitmapFactory.Options opt = new BitmapFactory.Options();

        opt.inPreferredConfig = Bitmap.Config.RGB_565;

        opt.inPurgeable = true;

        opt.inInputShareable = true;

        InputStream is = getResources().openRawResource(

                resourcesId);

        Bitmap bm = BitmapFactory.decodeStream(is, null, opt);

        BitmapDrawable bd = new BitmapDrawable(getResources(), bm);

        return bd;
    }

    //弹幕状态控制 HHH
    @Override
    protected void onPause() {
        super.onPause();
        mDanmuControl.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDanmuControl.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDanmuControl.destroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //解除广播
        try {
            unregisterReceiver(broadCastReceiver);
        }catch (Exception e){

        }
    }

    @Override
    public void onClick(View view) {

    }
    //发送弹幕 HHH
    protected void sendBarrage()
    {
        if(mChatInput.getText().toString().trim().length()==0 ||(!mConnectionState))
        {
            return;
        }

        StringCallback callback = new StringCallback() {
            @Override
            public void onError(Call call, Exception e) {
                AppContext.showToastAppMsg(ShowLiveActivityBase.this,getString(R.string.senderror));
            }
            @Override
            public void onResponse(String response) {
                //TLog.log(response);
                sendBarrageOnResponse(response);
            }
        };
        PhoneLiveApi.sendBarrage(mUser,mChatInput.getText().toString(),mUser.getId(),callback);

    }

    protected void sendBarrageOnResponse(String response) {
    }
}
