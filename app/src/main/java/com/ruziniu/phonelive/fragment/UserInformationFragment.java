package com.ruziniu.phonelive.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.ruziniu.phonelive.AppConfig;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.AppManager;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.BaseFragment;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.interf.ListenMessage;
import com.ruziniu.phonelive.ui.DisplayActivity;
import com.ruziniu.phonelive.ui.other.PhoneLivePrivateChat;
import com.ruziniu.phonelive.utils.LoginUtils;
import com.ruziniu.phonelive.utils.StringUtils;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.AvatarView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;

/**
 * 登录用户中心页面
 */
public class UserInformationFragment extends BaseFragment implements ListenMessage {

    //头像
    @InjectView(R.id.iv_avatar)
    AvatarView mIvAvatar;
    //昵称
    @InjectView(R.id.tv_name)
    TextView mTvName;
    //
    @InjectView(R.id.iv_gender)
    ImageView mIvGender;

    //签名
    @InjectView(R.id.tv_signature)
    TextView mTvSignature;
    //修改信息
    @InjectView(R.id.iv_editInfo)
    ImageView mIvEditInfo;

    @InjectView(R.id.ll_user_container)
    View mUserContainer;
    //退出登陆
    @InjectView(R.id.rl_user_unlogin)
    View mUserUnLogin;


    @InjectView(R.id.ll_loginout)
    LinearLayout mLoginOut;

    //直播记录
    @InjectView(R.id.tv_info_u_live_num)
    TextView mLiveNum;

    //关注
    @InjectView(R.id.tv_info_u_follow_num)
    TextView mFollowNum;

    //粉丝
    @InjectView(R.id.tv_info_u_fans_num)
    TextView mFansNum;

    //发送
    @InjectView(R.id.tv_send)
    TextView mSendNum;

    //id
    @InjectView(R.id.tv_id)
    TextView mUId;

    //私信
    @InjectView(R.id.iv_info_private_core)
    ImageView mPrivateCore;


    @InjectView(R.id.iv_hot_new_message)
    ImageView mIvNewMessage;

    private boolean mIsWatingLogin;

    private UserBean mInfo;

    private  EMMessageListener mMsgListener;


    private void steupUser() {
        if (mIsWatingLogin) {
            mUserContainer.setVisibility(View.GONE);
            mUserUnLogin.setVisibility(View.VISIBLE);
        } else {
            mUserContainer.setVisibility(View.VISIBLE);
            mUserUnLogin.setVisibility(View.GONE);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        unListen();

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
            @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_information,
                container, false);
        ButterKnife.inject(this, view);
        initView(view);
        initData();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requestData(true);

    }

    @Override
    public void onStart() {

        super.onStart();
        mInfo = AppContext.getInstance().getLoginUser();
        fillUI();
        listenMessage();
    }

    @Override
    public void initView(View view) {

        view.findViewById(R.id.ll_live).setOnClickListener(this);
        view.findViewById(R.id.ll_following).setOnClickListener(this);
        view.findViewById(R.id.ll_fans).setOnClickListener(this);
        view.findViewById(R.id.ll_profit).setOnClickListener(this);
        view.findViewById(R.id.ll_setting).setOnClickListener(this);
        view.findViewById(R.id.ll_level).setOnClickListener(this);
        view.findViewById(R.id.ll_diamonds).setOnClickListener(this);

        view.findViewById(R.id.ll_authenticate).setOnClickListener(this);
        view.findViewById(R.id.ll_upload).setOnClickListener(this);
        mUserUnLogin.setOnClickListener(this);
        mLoginOut.setOnClickListener(this);
        mIvEditInfo.setOnClickListener(this);
        mPrivateCore.setOnClickListener(this);

    }

    private void fillUI() {
        if (mInfo == null)
            return;

        mIvAvatar.setAvatarUrl(mInfo.getAvatar());
        //昵称
        mTvName.setText(mInfo.getUser_nicename());
        //性别
        mIvGender.setImageResource(
                StringUtils.toInt(mInfo.getSex()) == 1 ? R.drawable.global_male : R.drawable.global_female);
        //签名
        if (mInfo.getSignature()!=null){
            mTvSignature.setText(mInfo.getSignature().equals("") ? getString(R.string.defaultsign) : mInfo.getSignature());
        }
        //mTvSignature.setText(mInfo.getSignature().equals("") ? getString(R.string.defaultsign) : mInfo.getSignature());
        mUId.setText("ID:" + mInfo.getId());


    }

    protected void requestData(boolean refresh) {
        if (AppContext.getInstance().isLogin()) {
            mIsWatingLogin = false;
            sendRequestData();
        } else {
            mIsWatingLogin = true;
        }
        steupUser();
    }

    private void sendRequestData() {
        int uid = AppContext.getInstance().getLoginUid();
        String token = AppContext.getInstance().getToken();
        PhoneLiveApi.getMyUserInfo(uid,token,stringCallback);
    }

    private String getCacheKey() {
        return "my_information" + AppContext.getInstance().getLoginUid();
    }

    private StringCallback stringCallback = new StringCallback() {
       @Override
       public void onError(Call call, Exception e) {

       }

       @Override
       public void onResponse(String s) {
           String res = ApiUtils.checkIsSuccess(s);
           if(res == null){
               UIHelper.showLoginSelectActivity(getActivity());
               getActivity().finish();
               return;
           }
           mInfo = new Gson().fromJson(res,UserBean.class);
           AppContext.getInstance().updateUserInfo(mInfo);
           mLiveNum.setText(""+mInfo.getLiverecordnum());
           mFollowNum.setText(""+mInfo.getAttentionnum());
           mFansNum.setText(""+mInfo.getFansnum());
           mSendNum.setText("送出:  " + mInfo.getConsumption());

       }
    };


    @Override
    public void onClick(View v) {

        final int id = v.getId();
        switch (id) {

            case R.id.ll_upload://上传
                Intent intent = new Intent(getActivity(),DisplayActivity.class);
                startActivity(intent);
                break;
            case R.id.ll_authenticate://申请认证
                UIHelper.showWebView(getActivity(), AppConfig.MAIN_URL2 + "/index.php?g=User&m=Rz&a=auth&uid=" + AppContext.getInstance().getLoginUid(),"申请认证");
                break;
            case R.id.iv_info_private_core:
                mIvNewMessage.setVisibility(View.GONE);
                UIHelper.showPrivateChatSimple(getActivity(),mInfo.getId());
                break;

            case R.id.iv_avatar:
                break;
            case R.id.ll_live:
                UIHelper.showLiveRecordActivity(getActivity(),mInfo.getId());
                break;
            case R.id.ll_following:
                UIHelper.showAttentionActivity(getActivity(), mInfo.getId());
                break;
            case R.id.ll_fans:
                UIHelper.showFansActivity(getActivity(),mInfo.getId());
                    break;
            case R.id.ll_setting:
                UIHelper.showSetting(getActivity());
                break;
            case R.id.ll_diamonds://我的钻石
                Bundle dBundle = new Bundle();
                dBundle.putString("diamonds", mInfo.getCoin());
                UIHelper.showMyDiamonds(getActivity(), dBundle);
                break;
            case R.id.ll_level://我的等级
                UIHelper.showLevel(getActivity(), AppContext.getInstance().getLoginUid());
                break;
            case R.id.rl_user_center:
                break;
            case R.id.rl_user_unlogin:
                AppManager.getAppManager().finishAllActivity();
                UIHelper.showLoginSelectActivity(getActivity());
                getActivity().finish();

                break;
            case R.id.ll_loginout:
                LoginUtils.outLogin(getActivity());
                getActivity().finish();
                break;
            case R.id.iv_editInfo://编辑资料
                UIHelper.showMyInfoDetailActivity(getActivity());
                break;
            case R.id.ll_profit://收益
                Bundle pBundle = new Bundle();
                pBundle.putString("votes",mInfo.getVotes());
                UIHelper.showProfitActivity(getActivity(),pBundle);
                break;
            default:
                break;
        }
    }


    @Override
    public void initData() {
       //获取私信未读数量
        if(PhoneLivePrivateChat.getUnreadMsgsCount() > 0){
            mIvNewMessage.setVisibility(View.VISIBLE);
        }

    }


    public void listenMessage(){

        mMsgListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                //收到消息
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mIvNewMessage.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                //收到透传消息
            }

            @Override
            public void onMessageReadAckReceived(List<EMMessage> messages) {
                //收到已读回执
            }

            @Override
            public void onMessageDeliveryAckReceived(List<EMMessage> message) {
                //收到已送达回执
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                //消息状态变动
            }
        };
        EMClient.getInstance().chatManager().addMessageListener(mMsgListener);

    }
    public void unListen(){
        EMClient.getInstance().chatManager().removeMessageListener(mMsgListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        sendRequestData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag("getMyUserInfo");
    }
}
