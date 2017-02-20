package com.ruziniu.phonelive.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.ruziniu.phonelive.AppContext;
import com.ruziniu.phonelive.R;
import com.ruziniu.phonelive.api.remote.ApiUtils;
import com.ruziniu.phonelive.api.remote.PhoneLiveApi;
import com.ruziniu.phonelive.base.ToolBarBaseActivity;
import com.ruziniu.phonelive.bean.UserBean;
import com.ruziniu.phonelive.em.ChangInfo;
import com.ruziniu.phonelive.utils.UIHelper;
import com.ruziniu.phonelive.widget.AvatarView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import butterknife.InjectView;
import okhttp3.Call;

/**
 * 用户信息详情页面
 */
public class UserInfoDetailActivity extends ToolBarBaseActivity {

    private UserBean mUser;
    @InjectView(R.id.rl_userHead)
    RelativeLayout mRlUserHead;
    @InjectView(R.id.rl_userNick)
    RelativeLayout mRlUserNick;
    @InjectView(R.id.rl_userSign)
    RelativeLayout mRlUserSign;
    @InjectView(R.id.rl_userSex)
    RelativeLayout mRlUserSex;
    @InjectView(R.id.tv_userNick)
    TextView mUserNick;
    @InjectView(R.id.tv_userSign)
    TextView mUserSign;
    @InjectView(R.id.av_userHead)
    AvatarView mUserHead;
    @InjectView(R.id.iv_info_sex)
    ImageView mUserSex;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_myinfo_detail;
    }

    @Override
    public void initView() {
        mRlUserNick.setOnClickListener(this);
        mRlUserSign.setOnClickListener(this);
        mRlUserHead.setOnClickListener(this);
        mRlUserSex.setOnClickListener(this);
    }

    @Override
    public void initData() {
        setActionBarTitle(R.string.editInfo);
        sendRequiredData();
    }

    private void sendRequiredData() {
        PhoneLiveApi.getMyUserInfo(AppContext.getInstance().getLoginUid(), AppContext.getInstance().getToken(), callback);
    }

    @Override
    public void onClick(View v) {
        if(mUser != null){
            switch (v.getId()){
                case R.id.rl_userNick:
                    UIHelper.showEditInfoActivity(
                            this,"修改昵称",
                            getString(R.string.editnickpromp),
                            mUser.getUser_nicename(),
                            ChangInfo.CHANG_NICK);
                    break;
                case R.id.rl_userSign:
                    UIHelper.showEditInfoActivity(
                            this,"修改签名",
                            getString(R.string.editsignpromp),
                            mUser.getSignature(),
                            ChangInfo.CHANG_SIGN);
                    break;
                case R.id.rl_userHead:
                    UIHelper.showSelectAvatar(this,mUser.getAvatar());
                    break;
                case R.id.rl_userSex:
                    UIHelper.showChangeSex(this,mUser.getSex());
                    break;
            }
        }

    }

    @Override
    protected void onRestart() {
        mUser = AppContext.getInstance().getLoginUser();
        fillUI();
        super.onRestart();
    }

    @Override
    protected boolean hasBackButton() {
        return true;
    }
    private final StringCallback callback = new StringCallback() {
        @Override
        public void onError(Call call, Exception e) {

        }

        @Override
        public void onResponse(String s) {

            if(ApiUtils.checkIsSuccess(s)!= null){
                mUser = new Gson().fromJson(ApiUtils.checkIsSuccess(s),UserBean.class);
                fillUI();
            }else{

            }
        }
    };

    @Override
    protected void onStart() {
        if(mUser != null){
            fillUI();
        }

        super.onStart();
    }


    private void fillUI() {
        mUserNick.setText(mUser.getUser_nicename());
        mUserSign.setText(mUser.getSignature());
        mUserHead.setAvatarUrl(mUser.getAvatar());
        mUserSex.setImageResource(mUser.getSex() == 1?R.drawable.global_male:R.drawable.global_female);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkHttpUtils.getInstance().cancelTag("getMyUserInfo");
    }

    public void onResume() {
        super.onResume();
    }
    public void onPause() {
        super.onPause();
    }


}
