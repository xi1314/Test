package com.ruziniu.phonelive.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.tandong.bottomview.view.BottomView;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ShowLiveActivityBase;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.fragment.ManageListDialogFragment;
import com.ruziniu.phonelive.ui.StartLiveActivity;
import com.ruziniu.phonelive.ui.VideoPlayerActivity;
import com.ruziniu.phonelive.ui.other.ChatServer;
import com.ruziniu.phonelive.utils.TLog;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

/**
 * 直播间管理弹窗
 */
public class BottomMenuView extends FrameLayout implements View.OnClickListener {

    private LayoutInflater inflater;
    private TextView mSetManage,mReport,mShutUp,mCancel,mManageList;

    private UserBean mUser;
    private UserBean mToUser;
    private int mRoomNum = 0;
    private Activity activity;
    private ChatServer mChatServer;
    private BottomView mBottomView;

    public BottomMenuView(Context context) {
        super(context);
        inflater = LayoutInflater.from(context);
        initView();
    }

    public BottomMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }
    /**
    * @dw 设置操作选项
    * @param user 当前用户
    * @param touser 操作用户
    * @param roomnum 房间号码
    * */
    public void setOptionData(UserBean user, UserBean touser, int roomnum, Activity activity, ChatServer chat, BottomView bottomView){
        this.mUser = user;
        this.mToUser = touser;
        this.mRoomNum = roomnum;
        this.activity = activity;
        this.mBottomView = bottomView;
        this.mChatServer = chat;
        if(mToUser.getuType() == 40){
            mSetManage.setText("删除管理");
        }
    }
    /**
    *@dw 是否是主播
    *@param  isEmcee true是false管理
    * */
    public void setIsEmcee(boolean isEmcee){
        if(!isEmcee){
            mSetManage.setVisibility(View.GONE);
            mManageList.setVisibility(View.GONE);
            mReport.setVisibility(View.GONE);
        }else {
            mReport.setVisibility(View.GONE);
        }
    }


    private void initView() {
        inflater.inflate(R.layout.view_manage_menu,this);
        mSetManage = (TextView) findViewById(R.id.tv_manage_set_manage);
        mShutUp = (TextView) findViewById(R.id.tv_manage_shutup);
        mCancel = (TextView) findViewById(R.id.tv_manage_cancel);
        mManageList = (TextView) findViewById(R.id.tv_manage_manage_list);
        mReport = (TextView) findViewById(R.id.tv_manage_set_report);
        mSetManage.setOnClickListener(this);
        mShutUp.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mManageList.setOnClickListener(this);
        mReport.setOnClickListener(this);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_manage_set_manage:
                setManage();
                break;
            case R.id.tv_manage_shutup:
                shutUp();
                break;
            case R.id.tv_manage_cancel:
                if(mBottomView != null)
                    mBottomView.dismissBottomView();
                break;
            case R.id.tv_manage_manage_list:
                //UIHelper.shoManageListActivity(activity);
                ManageListDialogFragment fragment = new ManageListDialogFragment();
                fragment.setStyle(ManageListDialogFragment.STYLE_NO_TITLE,0);
                fragment.show(((ShowLiveActivityBase)activity).getSupportFragmentManager(),"ManageListDialogFragment");
                break;
            case R.id.tv_manage_set_report:
                break;
        }
    }
    //禁言
    private void shutUp() {
        //TLog.log("up set shutUp");
        if(mRoomNum == 0 || mUser == null || mToUser == null || activity == null || mChatServer == null)return;
        //TLog.log("bottom set shutUp uid:" + mToUser.getId());
        PhoneLiveApi.setShutUp(
                mRoomNum,mToUser.getId(),
                mUser.getId(),
                mUser.getToken(),
                new StringCallback(){

                    @Override
                    public void onError(Call call, Exception e) {
                        AppContext.showToastAppMsg(activity,"操作失败");
                    }

                    @Override
                    public void onResponse(String response) {
                        String res = ApiUtils.checkIsSuccess(response);
                        if(null == res) return;
                        mChatServer.doSetShutUp(mUser,mToUser);
                        mBottomView.dismissBottomView();
                    }
                });
    }
    //设置管理员
    private void setManage() {
        //TLog.log("up set manage");
        if(mRoomNum == 0 || mUser == null || mToUser == null || activity == null || mChatServer == null)return;
        //TLog.log("bottom set manage uid:" + mToUser.getId());
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                PhoneLiveApi.setManage(
                        mRoomNum,
                        mToUser.getId(),
                        mUser.getToken(),
                        new StringCallback(){

                            @Override
                            public void onError(Call call, Exception e) {
                                AppContext.showToastAppMsg(activity,"操作失败");
                            }

                            @Override
                            public void onResponse(String response) {
                                String res = ApiUtils.checkIsSuccess(response);
                                if(null == res) return;
                                subscriber.onNext("");
                                mBottomView.dismissBottomView();

                            }
                        });
            }
        });
        observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                PhoneLiveApi.isManage(mRoomNum, mToUser.getId(), new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {

                    }

                    @Override
                    public void onResponse(String response) {
                        String res = ApiUtils.checkIsSuccess(response);
                        if(res == null) return;
                        mToUser.setuType(Integer.parseInt(res));

                        if(mToUser.getuType() == 40){
                            mChatServer.doSetOrRemoveManage(mUser,mToUser,mToUser.getUser_nicename() + "被设为管理员");
                        }else{
                            mChatServer.doSetOrRemoveManage(mUser,mToUser,mToUser.getUser_nicename() + "被删除管理员");
                        }
                    }
                });
            }
        });
    }

}
